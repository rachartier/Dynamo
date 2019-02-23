package iut.ipi.runnergame.Activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import iut.ipi.runnergame.Hud.Input.BaseArrowClickable;
import iut.ipi.runnergame.Sensor.Accelerometer;
import iut.ipi.runnergame.Util.Point.AbstractPoint;
import iut.ipi.runnergame.Util.Point.PointCell;

import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

import iut.ipi.runnergame.Animation.SpriteSheetAnimation.BaseSpriteSheetAnimation;
import iut.ipi.runnergame.Entity.Plateform.PlateformManager;
import iut.ipi.runnergame.Entity.Plateform.PlateformType;
import iut.ipi.runnergame.Entity.Plateform.TypesPlateform.SimplePlateform;
import iut.ipi.runnergame.Entity.Player.Player;
import iut.ipi.runnergame.Hud.Cross;
import iut.ipi.runnergame.Hud.Input.BaseCrossClickable;
import iut.ipi.runnergame.Physics.PhysicsManager;
import iut.ipi.runnergame.R;
import iut.ipi.runnergame.Spritesheet.Spritesheet;
import iut.ipi.runnergame.Util.Point.PointScaled;
import iut.ipi.runnergame.Util.WindowDefinitions;

public class GameActivity extends SurfaceView implements Runnable {
    private ConstraintLayout constraintLayout;

    private SurfaceHolder   holder = null;
    private Canvas          canvas = null;

    private Player player;
    private Cross cross;

    private AbstractPoint pointClicked = new PointScaled();
    private PlateformManager plateformManager;

    private Accelerometer accelerometer;

    // volatile as it'll get modified in different thread
    private volatile boolean gamePlaying = true;

    public GameActivity(Context context) {
        super(context);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        accelerometer = new Accelerometer(context);

        plateformManager = new PlateformManager(context);

        cross = new BaseCrossClickable(context, R.drawable.sprite_cross_1, BaseCrossClickable.DEFAULT_SCALE, new PointScaled(1000.f, WindowDefinitions.heightPixels - 1000));

        try {
            player = new Player(new PointCell(5, 0), new BaseSpriteSheetAnimation(context, R.drawable.sprite_player_1, Player.DEFAULT_SCALE, 4, Player.DEFAULT_FRAME_DURATION, 3, 4));

            for(int i = 0; i < 10; i += 2)
                plateformManager.add(PlateformType.SIMPLE, new PointCell(20-i*2, i), 20);

            player.getAnimationManager().setDurationFrame(Player.ANIMATION_RUNNING_LEFT, 100);
            player.getAnimationManager().setDurationFrame(Player.ANIMATION_RUNNING_RIGHT, 100);
        }
        catch(IOException e) {

        }

        holder = getHolder();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getRootView().performClick();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    pointClicked.x = event.getX() / WindowDefinitions.ratioWidth;
                    pointClicked.y = event.getY() / WindowDefinitions.ratioHeight;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    pointClicked.x = -1;
                    pointClicked.y = -1;
                }

                Log.d("point", pointClicked.toString());

                return true;
            }
        });
    }

    @Override
    public void run() {
        while(gamePlaying) {
            update();
            draw();
        }
    }

    private long last = System.currentTimeMillis();
    public void update() {

        long now = System.currentTimeMillis();
        long res = now - last;

        cross.updateArrowPressed(pointClicked);

        if(cross.getArrowTop().getIsClicked()) {
            player.jump(Player.IMPULSE_JUMP);
        }
        else if(cross.getArrowLeft().getIsClicked()) {
            player.moveLeft(Player.IMPULSE_MOVEMENT);
            player.getAnimationManager().start(Player.ANIMATION_RUNNING_LEFT);
        }
        else if(cross.getArrowRight().getIsClicked()) {
            player.moveRight(Player.IMPULSE_MOVEMENT);
            player.getAnimationManager().start(Player.ANIMATION_RUNNING_RIGHT);
        }
        else {
            player.getAnimationManager().start(Player.ANIMATION_IDLE);
        }

        plateformManager.translate(player.getPosition().x - Player.DEFAULT_X_POS, 0);

        PhysicsManager.updatePlayerPosition(player, plateformManager.getPlateforms(),(float)res/1000.0f);

        last = now;
    }

    public void draw() {
        if(holder.getSurface().isValid()) {
            canvas = holder.lockCanvas();
            if(canvas == null) return;

            canvas.scale(WindowDefinitions.ratioWidth, WindowDefinitions.ratioWidth);

            Paint p = new Paint();
            Paint p2 = new Paint();

            p.setColor(Color.GREEN);
            p2.setColor(Color.RED);

            Paint paint = new Paint();

            paint.setDither(false);
            paint.setAntiAlias(false);
            paint.setFilterBitmap(false);

            canvas.drawColor(Color.DKGRAY);

            plateformManager.drawPlateformOnCanvas(canvas);

            canvas.drawBitmap(player.getSprite(), Player.DEFAULT_X_POS, player.getPosition().y, new Paint());

            cross.drawRectOnCanvas(canvas, p, p2);
            cross.drawOnCanvas(canvas);

            holder.unlockCanvasAndPost(canvas);
        }
    }
}