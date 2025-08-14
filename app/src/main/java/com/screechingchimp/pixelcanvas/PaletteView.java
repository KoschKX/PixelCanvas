package com.screechingchimp.pixelcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.widget.ImageButton;


public class PaletteView extends ImageView {

    private static final String TAG = "PixelCanvas";

    private Bitmap paletteBitmap;
    DrawView mDrawingView;
    public ImageButton colorSwatchA;

    private static final int NONE = 0;
    private int mode = NONE;

    private MainActivity mActivity;

    private FrameLayout frame;

        public PaletteView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            mActivity=(MainActivity)ctx;
        }

        public void initialize(DrawView dv, ImageButton sw, int defaultColor){
            colorSwatchA = sw;
            mDrawingView = dv;

            frame=(FrameLayout) mActivity.findViewById(R.id.paletteFrame);

            paletteBitmap =BitmapFactory.decodeResource(getResources(), R.drawable.colorspectrum);

            colorSwatchA.setBackgroundColor(defaultColor);
        }

        public void toggleVisibility(boolean show){
            if(show) {
                frame.setVisibility(VISIBLE);
                //paletteBitmap = getBitmapFromView(this);

            }else{
                frame.setVisibility(View.GONE);
            }
        }

        private void getColor(Bitmap bmp, int x, int y){
            if (x > 0 && y > 0 && x < bmp.getWidth() && y < bmp.getHeight()) {
                int pixel = bmp.getPixel(x, y);

                //Log.d(TAG, "new color: " + pixel);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int col = Color.argb(255, r, g, b);

                colorSwatchA.setBackgroundColor(col);
                mDrawingView.setColor(col);
                mDrawingView.setTool(mDrawingView.tool,false);
            }
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
                            getColor(paletteBitmap, x, y);
                            invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            getColor(paletteBitmap, x, y);
                            invalidate();
                            break;
                        case MotionEvent.ACTION_UP:
                            //getColor(paletteBitmap, x, y);
                            //invalidate();
                            toggleVisibility(false);
                            break;
                    }
                }
            }

            return true;
        }

}
