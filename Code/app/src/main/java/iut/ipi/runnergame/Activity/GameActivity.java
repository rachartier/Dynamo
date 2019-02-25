package iut.ipi.runnergame.Activity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import iut.ipi.runnergame.Game.GameManager;
import iut.ipi.runnergame.R;
import iut.ipi.runnergame.Util.Point.AbstractPoint;
import iut.ipi.runnergame.Util.Point.Point;

public class GameActivity extends AppCompatActivity {
    private GameManager gameManager;

    private SurfaceView surfaceView;
    private TextView textViewTimer;

    private AbstractPoint[] fingerPoints = new Point[10]; // comme les 10 doigts de la main

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_game);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getSupportActionBar().hide();

        textViewTimer = findViewById(R.id.textview_timer);
        surfaceView = findViewById(R.id.surface_view);

        gameManager = new GameManager(getApplicationContext(), surfaceView.getHolder());
        gameManager.start();

        for(int i = 0; i < fingerPoints.length; ++i) {
            fingerPoints[i] = new Point();
        }

        surfaceView.setOnTouchListener(new  View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                surfaceView.getRootView().performClick();

                int maskedAction = event.getActionMasked();
                int pointerId = event.getPointerId(event.getActionIndex());

                switch (maskedAction) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN: {
                        MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();

                        event.getPointerCoords(pointerId, pointerCoords);

                        fingerPoints[pointerId].x = pointerCoords.x;
                        fingerPoints[pointerId].y = pointerCoords.y;

                        gameManager.setPointsFingerPressed(fingerPoints);
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP: {
                        fingerPoints[pointerId].x = -1;
                        fingerPoints[pointerId].y = -1;

                        gameManager.setPointsFingerPressed(fingerPoints);
                        break;
                    }
                }
                return true;
            }
        });

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                gameManager.updatePosition();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });


        Timer timer = new Timer();

        final long timerStarted = System.currentTimeMillis();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long millis = System.currentTimeMillis();

                        long millisec = ((millis - timerStarted)) % 1000;
                        long seconds = ((millis - timerStarted) / 1000) % 60;

                        String strSeconds = ((seconds < 10) ? "0" : "") + String.valueOf(seconds);
                        String strMillisec = ((millisec < 100) ? "0" : "") + String.valueOf(millisec);

                        textViewTimer.setText(strSeconds + "." + strMillisec);
                    }
                });
            }
        }, 0, 10);
    }
}