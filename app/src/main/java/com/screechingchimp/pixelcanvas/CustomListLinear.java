package com.screechingchimp.pixelcanvas;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Hashtable;

import android.graphics.Color;
import android.graphics.Typeface;

import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;

import android.graphics.Bitmap;

import android.util.Log;


public class CustomListLinear extends LinearLayout{

    private static final String TAG = "PixelCanvas";

    private Activity context;
    private MainActivity mActivity;
    private ArrayList<Bitmap> frames;
    Hashtable<String, Bitmap> frameHash = new Hashtable<String, Bitmap>();

    FLHelper frameFileHelper;

    int currPos=0;

    public CustomListLinear(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = (Activity)context;
        this.mActivity = (MainActivity)context;
        this.frames = new ArrayList<Bitmap>();

        this.setOrientation(HORIZONTAL);
        this.setGravity(Gravity.BOTTOM);

        frameFileHelper.clearCache(frameFileHelper.FRAME_FOLDER);
    }

    public void clear(){
        this.removeAllViews();

        frameFileHelper.clearCache(frameFileHelper.FRAME_FOLDER);
        frameHash.clear();
        frames.clear();

        currPos=0;
    }

    public Bitmap getBitmap(int index){
        Bitmap bmp=frames.get(index);
        return bmp;
    }

    public int getCount(){
        return frames.size();
    }

    public void reloadFrames() {
        int cnt=getCount();
        for(int f=0;f<cnt;f++){
            loadFrame(f,false);
        }
    }

    public void setItem(int index,Bitmap bmp) {

        frames.set(index, bmp);
        String fileName=FLHelper.saveCachedBitmap(bmp,frameFileHelper.FRAME_FOLDER,String.valueOf(index));
        loadFrame(index,false);

        LayoutInflater inflater = context.getLayoutInflater();
        View view=this.getChildAt(index);

        ImageView img = (ImageView) view.findViewById(R.id.img);

        Drawable d = new BitmapDrawable(context.getResources(), bmp);
        img.setImageDrawable(d);
    }

    public void removeItem(int index) {
        this.removeView(this.getChildAt(index));
        frames.remove(index);
        FLHelper.removeCachedBitmap(frameFileHelper.FRAME_FOLDER, String.valueOf(index));
    }

    public void addItem(Bitmap bmp) {

        String fileName=FLHelper.saveCachedBitmap(bmp,frameFileHelper.FRAME_FOLDER,String.valueOf(frames.size()));
        frames.add(bmp);

        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.list_single, null);

        ImageView img = (ImageView) view.findViewById(R.id.img);
        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Drawable d = new BitmapDrawable(context.getResources(), bmp);
        img.setImageDrawable(d);

        Point bmpPoint=getImageRatio(bmp.getWidth(), bmp.getHeight(), this.getMeasuredHeight(),this.getMeasuredHeight());
        //Log.d(TAG,"pointX: "+bmpPoint.x+", pointY: "+bmpPoint.y);

        img.getLayoutParams().width=this.getMeasuredHeight();
        img.getLayoutParams().height=this.getMeasuredHeight();

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int index = ((ViewGroup) v.getParent()).indexOfChild(v);

                if(index!=mActivity.currIndex) {
                    loadFrame(index, true);
                }

                if(!mActivity.isAnimating) {
                    mActivity.updatePreview();
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int index = ((ViewGroup) v.getParent()).indexOfChild(v);
                mActivity.showFrameOptions(index);
                return false;
            }
        });

        /*
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT, 1.0f);

        view.setLayoutParams(param);
        */

        this.addView(view, currPos);

        currPos++;
        //notifyDataSetChanged();
    }

    public Point getImageRatio(int bwidth, int bheight, int swidth, int sheight){
        int new_width=0;
        int new_height=0;
        if (bwidth / swidth <  bheight / sheight) {
            new_width = swidth;
            new_height = (int) Math.floor((double) bheight
                    * (double) swidth / (double) bwidth);
        } else {
            new_height = sheight;
            new_width = (int) Math.floor((double) bwidth
                    * (double) sheight / (double) bheight);
        }
        return new Point(new_width,new_height);
    }

    public void loadFrame(int index, boolean display){
        //Bitmap bmp=frameFileHelper.loadCachedBitmap(frameFileHelper.FRAME_FOLDER, String.valueOf(index));
        Bitmap bmp=frames.get(index).copy(mActivity.getConfig(),true);

        frames.set(index,bmp);

        mActivity.currIndex=index;

        //mActivity.loadBitmap(frames.get(index));
        if(display) {
            mActivity.loadBitmap(bmp);
            mActivity.setFrameIndex(index);
            mActivity.clearUndo();
        }
    }

}
