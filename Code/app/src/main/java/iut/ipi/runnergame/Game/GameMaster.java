package iut.ipi.runnergame.Game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import iut.ipi.runnergame.Activity.GameActivity;
import iut.ipi.runnergame.Engine.Graphics.Animation.SpriteSheetAnimation.BaseSpriteSheetAnimation;
import iut.ipi.runnergame.Engine.Graphics.Hud.AbstractCross;
import iut.ipi.runnergame.Engine.Graphics.Hud.Hint.AbstractHint;
import iut.ipi.runnergame.Engine.Graphics.Hud.Hint.ShakeHint;
import iut.ipi.runnergame.Engine.Graphics.Hud.Input.BaseCrossClickable;
import iut.ipi.runnergame.Engine.Graphics.Point.AbstractPoint;
import iut.ipi.runnergame.Engine.Graphics.Point.PointRelative;
import iut.ipi.runnergame.Engine.Physics.PhysicsManager;
import iut.ipi.runnergame.Engine.Sfx.Sound.AbstractPlayer;
import iut.ipi.runnergame.Engine.Sfx.Sound.SoundEffectPlayer;
import iut.ipi.runnergame.Engine.WindowDefinitions;
import iut.ipi.runnergame.Engine.WindowUtil;
import iut.ipi.runnergame.Entity.Player.BasePlayer;
import iut.ipi.runnergame.Entity.Player.Player;
import iut.ipi.runnergame.Entity.Shadow.Shadow;
import iut.ipi.runnergame.Game.Level.LevelCreator;
import iut.ipi.runnergame.Game.Level.Loader.LevelLoaderText;
import iut.ipi.runnergame.R;

public class GameMaster extends Thread {
    private final long FPS = 60L;
    private final long FRAME_PERIOD = 1000L / FPS;

    private final float SHAKE_HINT_OFFSET = WindowUtil.convertPixelsToDp(50);

    private final AbstractPoint defaultPointCross = new PointRelative(10, 50);
    private final AbstractPoint defaultPointCrossAB = new PointRelative(90, 50);
    private final AbstractPoint defaultPointPlayer = new PointRelative(50, 0);

    private List<AbstractPoint> pointFingerPressed = new ArrayList<>();

    private SurfaceHolder holder = null;

    private Player player;
    private AbstractCross cross;
    private AbstractCross crossAB;

    private LevelCreator levelCreator;

    private AbstractPlayer sfxPlayer;

    private AbstractHint shakeHint;

    private boolean isRunning = true;
    private boolean paused = false;
    private Object pauseKey = new Object();

    public GameMaster(Context context, SurfaceHolder surfaceHolder) {
        AbstractPoint.clearPoolPoints();
        updatePoolPoints();

        cross = new BaseCrossClickable(context, R.drawable.sprite_cross_1, BaseCrossClickable.DEFAULT_SCALE, 4, defaultPointCross);
        crossAB = new BaseCrossClickable(context, R.drawable.sprite_cross_ab, BaseCrossClickable.DEFAULT_SCALE, 1, defaultPointCrossAB);

        sfxPlayer = new SoundEffectPlayer(context);
        sfxPlayer.add("footsteps", R.raw.sfx_jump);

        try {
            player = new BasePlayer(context, defaultPointPlayer, Player.DEFAULT_SCALE);

            levelCreator = new LevelCreator(context, player, new LevelLoaderText(context, player, R.raw.level));

            shakeHint = new ShakeHint(context, 0.5f, 4, 200, new PointRelative(45, 20));
            shakeHint.show();

            player.getAnimationManager().setDurationFrame(Player.ANIMATION_RUNNING_LEFT, 100);
            player.getAnimationManager().setDurationFrame(Player.ANIMATION_RUNNING_RIGHT, 100);
        }
        catch(IOException ignored) {

        }

        surfaceHolder.setFixedSize((int)WindowDefinitions.WIDTH, (int)WindowDefinitions.HEIGHT);
        holder = surfaceHolder;
    }

    public void updatePoolPoints() {
        AbstractPoint.resizePointsInPool();
    }

    public void pauseUpdate() {
        paused = true;
    }

    public void resumeUpdate() {
        synchronized (pauseKey) {
            paused = false;
            pauseKey.notifyAll();
        }
    }

    public void stopUpdate() {
        isRunning = false;
    }

    private long time = System.currentTimeMillis();
    public void waitUntilNeededUpdate(long start) {
        long now = System.currentTimeMillis();

        if(now - time >= 1000L) // 1 seconde
            time = now;

        long sleepTime = (FRAME_PERIOD/2) - (now - start); // il faut endormir le thread si il ne fait rien

        if(sleepTime > 0L) {
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                isRunning = false;
            }
        }
    }

    private boolean update = true;
    @Override
    public void run() {
        while(isRunning) {
            synchronized (pauseKey) {
                if(paused) {
                    try {
                        pauseKey.wait();
                    } catch (InterruptedException e) {
                        break;
                    }

                    // en attendant, isRunning peut avoir changé
                    if(!isRunning) {
                        break;
                    }
                }

                long start = System.currentTimeMillis();

                update(1.0f/FPS);
                draw();

                waitUntilNeededUpdate(start);
            }
        }
    }

    public void kill() {
        isRunning = false;
        sfxPlayer.release();
        AbstractPoint.clearPoolPoints();
    }

    public void update(float dt) {
        boolean idle = true;

        cross.updateArrowPressed(pointFingerPressed);
        crossAB.updateArrowPressed(pointFingerPressed);

        if(cross.getArrowTop().getIsClicked() || crossAB.getArrowLeft().getIsClicked()) {
            idle = false;

            if (player.isOnGround()) {
                sfxPlayer.playUntilFinished("footsteps");
                player.setHasAnotherJump(true);
            }

            player.jump(Player.IMPULSE_JUMP);

            if (player.getImpulse().x >= 0) {
                player.getAnimationManager().start(Player.ANIMATION_JUMP_RIGHT);
            } else {
                player.getAnimationManager().start(Player.ANIMATION_JUMP_LEFT);
            }
        }
        if(cross.getArrowLeft().getIsClicked()) {
            player.moveLeft(Player.IMPULSE_MOVEMENT);

            if(player.isOnGround())
                player.getAnimationManager().start(Player.ANIMATION_RUNNING_LEFT);
            else
                player.getAnimationManager().start(Player.ANIMATION_JUMP_LEFT);
            idle = false;
        }
        if(cross.getArrowRight().getIsClicked()) {
            player.moveRight(Player.IMPULSE_MOVEMENT);

            if(player.isOnGround())
                player.getAnimationManager().start(Player.ANIMATION_RUNNING_RIGHT);
            else
                player.getAnimationManager().start(Player.ANIMATION_JUMP_RIGHT);

            idle = false;
        }
        if(idle){
            if(player.isOnGround())
                player.getAnimationManager().start(Player.ANIMATION_IDLE);
        }

        levelCreator.updateLevel(dt);

        if(levelCreator.getLevel().getShadowManager().getShadowRadius() < levelCreator.getLevel().getShadowManager().getShadowMinRadius() + SHAKE_HINT_OFFSET) {
            shakeHint.show();
        } else {
            shakeHint.hide();
        }

        PhysicsManager.updatePlayerPosition(player, levelCreator.getLevel().getPlateforms(),dt);

        if(player.isDead()) {
            int distance = (int)(player.getPosition().x - Player.DEFAULT_POS.x);
            GameActivity.launchLoseActivity(new GameOverDataBundle(GameActivity.strTimer, distance, levelCreator.getLevel().getLength(), player.getScore()));
        }
    }

    private Paint paint = new Paint();
    public void draw() {
        if(holder.getSurface().isValid()) {
            Canvas canvas = holder.lockCanvas();
            if(canvas == null) return;

            canvas.drawColor(Color.BLACK);
            levelCreator.getLevel().drawOnCanvas(canvas);

            canvas.drawBitmap(player.getSprite(), Player.DEFAULT_POS.x, player.getPosition().y, paint);

            // oblige de les afficher 2x, car le calcul de l ombre empeche de mettre des elements soit au dessus soit en dessous
            // les mettres au dessus et en dessous corrige le probleme
            shakeHint.drawOnCanvas(canvas);
            cross.drawOnCanvas(canvas);
            crossAB.drawOnCanvas(canvas);

            levelCreator.getLevel().drawShadowOnCanvas(canvas);

            cross.drawOnCanvas(canvas);
            crossAB.drawOnCanvas(canvas);

            shakeHint.drawOnCanvas(canvas);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    public void setPointsFingerPressed(AbstractPoint[] points) {
        pointFingerPressed.clear();

        if(points == null) return;

        pointFingerPressed.addAll(Arrays.asList(points));
    }
}
