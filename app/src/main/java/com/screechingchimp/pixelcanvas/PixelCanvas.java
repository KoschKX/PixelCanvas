package com.screechingchimp.pixelcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.graphics.Paint;
import android.graphics.Rect;

import java.util.Random;
import android.util.Log;

import android.view.MotionEvent;
import android.os.AsyncTask;

public class PixelCanvas extends SurfaceView implements Runnable {

    Thread thread = null;

    private SurfaceHolder surfaceHolder;
    private Bitmap bmpIcon;

    private Canvas canvas;

    volatile boolean running = false;

    volatile boolean touched = false;
    volatile int curr_x, curr_y, prev_x, prev_y;

    private Bitmap buffer = null;

    private Context ctx;
    private MainActivity mActivity;
    private AsyncTask asyncTask = null;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Random random;

    long loopStartTime;
    long loopEndTime;
    int loopTime;
    int loopDelay;


    private static final String TAG = "PixelCanvas";

    public PixelCanvas(Context context) {
        super(context);
        this.ctx = context;
        this.mActivity = (MainActivity) context;
        init();
    }

    public PixelCanvas(Context context,
                       AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PixelCanvas(Context context,
                       AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        bmpIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.buddha);

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                canvas = holder.lockCanvas(null);
                drawSomething(canvas);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder,
                                       int format, int width, int height) {
                // TODO Auto-generated method stub

                //drawPixel(curr_x, curr_y, 10, 0xFFFF0000);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }

        });
    }

    public void onResumeMySurfaceView() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void onPauseMySurfaceView() {
        boolean retry = true;
        running = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void drawPixel(int x, int y, int size, int color) throws InterruptedException {

        for (int i = 0; i < 3; i++) {
            Canvas canvas = surfaceHolder.lockCanvas();

            Paint paint = new Paint();
            Rect r = new Rect(x, y, x + size, y + size);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            canvas.drawRect(r, paint);

            // border
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            canvas.drawRect(r, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }

    }

    /*
    public void startTask() {
        asyncTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (touched) {
                    while(touched) {
                        try {
                            Thread.sleep(10);
                            mActivity.runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    try {

                                        //drawLine(prev_x, prev_y, curr_x, curr_y, 10, 0xFFFF0000);
                                        drawPixel(prev_x, prev_y, 10, 0xFFFF0000);

                                    } catch (InterruptedException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }

                                }
                            });

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

        }.execute();

    }
    */

    public void drawLine(int x1, int y1, int x2, int y2, int col){

        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;

        dx = dx;
        dy = dy;

        int err = (dx > dy ? dx : -dy) / 2;
        while (true) {


            //drawPixel(x1 * thk, y1 * thk, thk, col);
            drawPix(x1, y1, col);

            if (x1 == x2 && y1 == y2) break;
            int e2 = err;
            if (e2 > -dx) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dy) {
                err += dx;
                y1 += sy;
            }
        }
        // }
    }

    public void drawPix(int x, int y,int color) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

       //for (int i = 0; i < 3; i++) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawPoint(x, y, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
       // }
    }

    public void drawPixLine(int x1, int y1, int x2, int y2, int color) {

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

       loopEndTime = System.currentTimeMillis();
       loopTime = (int) (loopEndTime - loopStartTime);

        //for (int i = 0; i < 3; i++) {

        if(surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawLine(x1, y1, x2, y2, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
        }


        /*

        if (loopTime < loopDelay) {
            if (surfaceHolder.getSurface().isValid()) {
                try {
                    Thread.sleep(loopDelay - loopTime);
                } catch (InterruptedException e) {
                    Log.v(TAG, "Thread.sleep", e);

                }
            }
        }
        */


        //}
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while(running){
            if(surfaceHolder.getSurface().isValid()){


            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        int action = event.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:
                touched = true;

                this.running=false;

                prev_x = (int)event.getX();
                prev_y = (int)event.getY();
                //drawPixel(prev_x, prev_y, 10, 0xFFFF0000);

                break;
            case MotionEvent.ACTION_MOVE:
                touched = true;

                curr_x= (int)event.getX();
                curr_y = (int) event.getY();

                Log.d(TAG, "x1: " + prev_x + " y1: " + prev_y + " x2: " + curr_x + " y2: " + curr_y);
               //drawPix(curr_x,curr_y,0xFFFF0000);
                drawPixLine(curr_x, curr_y, prev_x, prev_y, 0xFFFF0000);

                prev_x = (int)event.getX();
                prev_y = (int)event.getY();


                if(curr_x > prev_x||curr_y> prev_y) {
                    return false;
                }

                break;
            case MotionEvent.ACTION_UP:
                touched = false;

                prev_x=curr_x;
                prev_y=curr_y;

                this.running=true;

                break;
            case MotionEvent.ACTION_CANCEL:
                touched = false;
                break;
            case MotionEvent.ACTION_OUTSIDE:
                touched = false;
                break;
            default:
        }
        return true; //processed
    }

    protected void drawSomething(Canvas canvas) {
    canvas.drawColor(Color.BLACK);
	    canvas.drawBitmap(bmpIcon, getWidth() / 2, getHeight() / 2, null);
 }
}
