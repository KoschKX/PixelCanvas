package com.screechingchimp.pixelcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.TextView;

public class BrushView extends ImageView {

    private static final String TAG = "PixelCanvas";

    private Bitmap brushBitmap;
    DrawView mDrawingView;
    public ImageButton sizeSwatch;

    private static final int NONE = 0;
    private int mode = NONE;

    private MainActivity mActivity;

    private TextView disp;
    private FrameLayout frame;

        public BrushView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            mActivity=(MainActivity)ctx;
        }

        public void initialize(DrawView dv, ImageButton sw, int defaultSize){
            sizeSwatch = sw;
            mDrawingView = dv;

            disp=(TextView) mActivity.findViewById(R.id.brushSizeDisp);
            frame=(FrameLayout) mActivity.findViewById(R.id.brushFrame);

            sizeSwatch.setScaleX(defaultSize);
            sizeSwatch.setScaleY(defaultSize);

            brushBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brushspectrum);

        }

        public void toggleVisibility(boolean show){
            if(show) {
                frame.setVisibility(VISIBLE);
                //brushBitmap = getBitmapFromView(this);
            }else{
                frame.setVisibility(View.GONE);
            }
        }

        private void getSize(int x){

            x=(getWidth()-x)/10;
            if(x<1){
                x=1;
            }
            if(x>50){
                x=50;
            }

            //Log.d(TAG,"size: "+x);
            disp.setText(String.valueOf(x));

            sizeSwatch.setScaleX(x);
            sizeSwatch.setScaleY(x);

            sizeSwatch.invalidate();

            mDrawingView.setSize(x);
            mDrawingView.setTool(mDrawingView.tool,false);
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
                            if(x>0&&(x<frame.getWidth())) {
                                getSize(x);
                            }
                            //invalidate();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(x>0&&(x<frame.getWidth())) {
                                getSize(x);
                            }
                            //invalidate();
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
