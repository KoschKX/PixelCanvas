package com.screechingchimp.pixelcanvas;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.widget.ImageButton;


public class DitherView extends ImageView {

    private static final String TAG = "PixelCanvas";

    private Bitmap ditherBitmap;
    DrawView mDrawingView;
    public ImageButton ditherSwatch;

    private static final int NONE = 0;
    private int mode = NONE;

    private MainActivity mActivity;
    private Context ctx;

    private FrameLayout frame;

        public DitherView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            mActivity=(MainActivity)ctx;
            this.ctx = ctx;
        }

        public void initialize(DrawView dv, ImageButton sw, int defaultColor){
            ditherSwatch = sw;
            mDrawingView = dv;

            frame=(FrameLayout) mActivity.findViewById(R.id.ditherFrame);

            ditherBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ditherspectrum);
            //ditherSwatch.setBackgroundColor(defaultColor);
        }

        public void toggleVisibility(boolean show){
            if(show) {
                frame.setVisibility(VISIBLE);
            }else{
                frame.setVisibility(View.GONE);
            }
        }

        public void setDither(int index){
            getPattern(index, true);
        }

        private void getPattern(int x, boolean direct){



            if(!direct){
               //x=(getWidth()-x)/32;

                Resources r = getResources();
                float ww = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());

                //Log.d(TAG, "x: " + x);

                if(x<=ww*0){
                    x=0;
                }else if(x<=ww*1){
                    x=1;
                }else if(x<=ww*2){
                    x=2;
                }else if(x<=ww*3){
                    x=3;
                }else if(x<=ww*4){
                    x=4;
                }else if(x<=ww*5){
                    x=5;
                }else if(x<=ww*6){
                    x=6;
                }else if(x<=ww*7){
                    x=7;
                }else if(x<=ww*8){
                    x=0;
                }

            }

            Bitmap fillBMP = null;

                if(x<0){
                    x=0;
                }
                if(x>7) {
                    x=7;
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;

                if(x==1){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_7,options);
                }else if(x==2){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_6,options);
                }else if(x==3){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_5,options);
                }else if(x==4){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_4,options);
                }else if(x==5){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_3,options);
                }else if(x==6){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_2,options);
                }else if(x==7){
                    fillBMP = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.dither_1,options);
                }



            BitmapShader cpattern = null;

            if(x>0) {

                Paint patternPaint;
                patternPaint = new Paint();
                patternPaint.setColor(mDrawingView.brushColor);

                patternPaint.setAntiAlias(false);
                patternPaint.setStyle(Paint.Style.FILL);

                BitmapShader pattern = new BitmapShader(fillBMP, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                patternPaint.setShader(pattern);

                Bitmap sbmpPattern = Bitmap.createBitmap(ditherSwatch.getWidth(), ditherSwatch.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(sbmpPattern).drawPaint(patternPaint);
                BitmapDrawable swPattern = new BitmapDrawable(getResources(), sbmpPattern);

                ditherSwatch.setImageDrawable(swPattern);

                patternPaint.setColorFilter(new PorterDuffColorFilter(mDrawingView.brushColor, PorterDuff.Mode.SRC_IN));

                sbmpPattern = Bitmap.createBitmap(fillBMP.getWidth(), fillBMP.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(sbmpPattern).drawPaint(patternPaint);

                cpattern = new BitmapShader(sbmpPattern, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

            }else{
                Bitmap sbmpPattern = Bitmap.createBitmap(ditherSwatch.getWidth(), ditherSwatch.getHeight(), Bitmap.Config.ARGB_8888);
                new Canvas(sbmpPattern).drawColor(0xFF000000);
                BitmapDrawable swPattern = new BitmapDrawable(getResources(), sbmpPattern);
                ditherSwatch.setImageDrawable(swPattern);
            }


            //cpattern=createCheckerBoard(1, mDrawingView.brushColor);

            mDrawingView.setPattern(cpattern,x);
                //mDrawingView.setColor(col);
            mDrawingView.setTool(mDrawingView.tool,false);

        }



    private BitmapShader createCheckerBoard(int pixelSize, int col)
        {
            Bitmap bitmap = Bitmap.createBitmap(pixelSize * 2, pixelSize * 2, Bitmap.Config.ARGB_8888);

            Paint fill = new Paint();
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(col);

            Canvas canvas = new Canvas(bitmap);
            Rect rect = new Rect(0, 0, pixelSize, pixelSize);
            canvas.drawRect(rect, fill);

            rect.offset(pixelSize, pixelSize);
            canvas.drawRect(rect, fill);

            Paint paint = new Paint();

            BitmapShader shader=new BitmapShader(bitmap, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT);
            paint.setShader(shader);

            return shader;
        }

        public static Bitmap getBitmapFromView(View view) {
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(returnedBitmap);
            Drawable bgDrawable =view.getBackground();
            if (bgDrawable!=null)
                bgDrawable.draw(canvas);
            else
                canvas.drawColor(Color.WHITE);
            view.draw(canvas);
            return returnedBitmap;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int)event.getX();
            int y = (int)event.getY();

            int touch_count=event.getPointerCount();

            if(touch_count<2) {
                if(mode == NONE) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            getPattern(x,false);
                            invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            getPattern(x,false);
                            invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            toggleVisibility(false);
                            break;
                    }
                }
            }

            return true;
        }

}
