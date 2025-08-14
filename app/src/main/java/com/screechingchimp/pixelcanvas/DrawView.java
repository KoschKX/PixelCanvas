package com.screechingchimp.pixelcanvas;

import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.os.Handler;
import android.view.MotionEvent;

import android.view.View;

import android.graphics.Path;
import android.util.Log;

import android.widget.ImageView;

import android.graphics.Matrix;
import android.graphics.PointF;

import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;

import android.widget.FrameLayout;

import android.graphics.BitmapShader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;

import android.graphics.Point;
import java.util.Queue;
import java.util.LinkedList;

import android.graphics.Region;
import java.util.Hashtable;

import android.graphics.BitmapFactory;
import android.graphics.DashPathEffect;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuffColorFilter;

class DrawView extends ImageView {

    private boolean accuracy=false;
    private boolean drawRects=false;
    public boolean pauseOnDraw=true;

    Paint       mPaint;
    //MaskFilter  mEmboss;
    //MaskFilter  mBlur;
    Bitmap  mBitmap;
    Canvas  mCanvas;
    Path    mPath;
    Paint   mBitmapPaint;

    Paint   bPaint;
    Bitmap  bBitmap;
    Canvas  bCanvas;
    Path    bPath;
    Paint   bBitmapPaint;

    Paint   dPaint;

    Bitmap  sBitmap;
    Paint   sPaint;
    Canvas  sCanvas;

    int selPhase=0;

    Paint checkerPaint;
    boolean checkerBG=true;
    private boolean hideCheckerBG=false;

    ImageView img;

    Canvas canvas;
    MainActivity mActivity;

    public int brushColor=0xFF000000;
    public int brushDither=0;
    public BitmapShader brushPattern=null;
    public int brushSize=1;

    public android.graphics.Bitmap.Config config=Bitmap.Config.ARGB_8888;

    public int currIndex;

    private float moveX=0;
    private float moveY=0;

    public boolean pencil=true;

    private volatile boolean touched = false;
    public  volatile boolean frameTouched = false;
    private volatile float curr_x, curr_y, prev_x, prev_y, buff_x, buff_y;
    private volatile float scale = 1f;

    private float minScale=1f;
    private float maxScale=20f;

    private static final String TAG = "PixelCanvas";

           /* multitouch vars */

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    // we can be in one of these 3 states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    // remember some things for zooming
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private PointF end = new PointF();
    private float oldDist = 1f;
    private float newDist = 1f;
    private float d = 0f;
    private float newRot = 0f;
    private float[] lastEvent = null;

    private boolean select=false;
    private boolean selectMove=false;
    public Rect selRect;

    private boolean fullBuff=false;
    private boolean buff=false;
    public boolean buffRefresh=false;
    public Rect buffRectInner;
    public Rect buffRect;
    public Rect imgRect;

    private boolean isTransColor=false;
    private int transColor=Color.argb(255, 255, 0, 255);
    public int bgColor=Color.argb(255, 255, 255, 255);

    private Handler handler;

    public String tool="brush";
    public String toggleTool="brush";

    private ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<String> undoTest = new ArrayList<String>();

    private ArrayList<String> undo = new ArrayList<String>();
    Hashtable<String, Bitmap> undoHash = new Hashtable<String, Bitmap>();

    private ArrayList<PointF> mPoints = new ArrayList<PointF>();

    int undoHashNum=0;
    int currUndo=0;
    int undoLimit=9;

    FLHelper hashFileHelper;

    private boolean firstEdit=true;

    public FrameLayout frame;
    final public View holder;

    public boolean locked=false;
    public boolean makingBitmap=false;

    private int bCnt=0;

    private Runnable delayUnLock = new Runnable() {
        @Override
        public void run() {
            locked = false;
            Log.d(TAG,"unlocked");
        }
    };

    public DrawView(MainActivity mainActivity,Context context,Bitmap loadBMP) {
        super(context);
        // TODO Auto-generated constructor stub

        mPaint = new Paint();
        bPaint = new Paint();

        setTool("brush", false);

        dPaint = new Paint();
        dPaint.setAntiAlias(false);
        dPaint.setDither(false);
        dPaint.setFilterBitmap(false);
        dPaint.setColor(0x22FF0000);
        dPaint.setStyle(Paint.Style.STROKE);
        dPaint.setStrokeJoin(Paint.Join.MITER);
        dPaint.setStrokeCap(Paint.Cap.SQUARE);
        dPaint.setStrokeWidth(1);
        dPaint.setFilterBitmap(true);

        sPaint = new Paint();
        sPaint.setAntiAlias(false);
        sPaint.setDither(false);
        sPaint.setFilterBitmap(false);
        sPaint.setStyle(Paint.Style.STROKE);
        sPaint.setStrokeJoin(Paint.Join.ROUND);
        sPaint.setStrokeCap(Paint.Cap.ROUND);
        sPaint.setStrokeWidth(1);
        sPaint.setFilterBitmap(true);
        sPaint.setPathEffect(new DashPathEffect(new float[]{2, 2}, selPhase));
        sPaint.setColor(Color.BLACK);

        mPath = new Path();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(false);
        mBitmapPaint.setDither(false);
        mBitmapPaint.setFilterBitmap(false);
        mBitmapPaint.setColor(Color.RED);
        bPath = new Path();
        bBitmapPaint = new Paint();
        bBitmapPaint.setAntiAlias(false);
        bBitmapPaint.setDither(false);
        bBitmapPaint.setFilterBitmap(false);
        bBitmapPaint.setColor(Color.RED);

        //this.setScaleType(ScaleType.MATRIX);
        this.setDrawingCacheQuality(DRAWING_CACHE_QUALITY_HIGH);
        //this.setImageMatrix(matrix);
        img=this;

        mActivity = mainActivity;

        holder=(FrameLayout)  mActivity.findViewById(R.id.canvasHolder);
        frame=(FrameLayout)  mActivity.findViewById(R.id.canvasFrame);
        handler = new Handler();

        if(checkerBG) {
            checkerPaint = createCheckerBoard(4, 0xFFeeeeee);
        }else {
            holder.setBackgroundColor(0xeeeeee);
        }

        hashFileHelper=new FLHelper(context);
        hashFileHelper.clearCache(hashFileHelper.UNDO_FOLDER);

        if(loadBMP!=null){

            //Log.d(TAG,"Test");

            setBuffRect(0, 0, loadBMP.getWidth(), loadBMP.getHeight(), 0);
            mBitmap = loadBMP;
            mCanvas = new Canvas(mBitmap);
            load(loadBMP);

            String fileName=FLHelper.saveCachedBitmap(mBitmap, hashFileHelper.UNDO_FOLDER, "0");
            undo.add(0, fileName);

            mActivity.updatePreview();
        }else{
            undo.add(0, "clear");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setBuffRect(0, 0, w, h, 0);
        if(mBitmap==null) {
            mBitmap = Bitmap.createBitmap(w, h,config);
        }
        bBitmap = Bitmap.createBitmap(w, h, config);
        sBitmap = Bitmap.createBitmap(w, h, config);
        bCanvas = new Canvas(bBitmap);
        sCanvas = new Canvas(sBitmap);
        mCanvas = new Canvas(mBitmap);

        //mCanvas.setMatrix(matrix);
        //bCanvas.setMatrix(matrix);
    }


    @Override
    public void draw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.draw(canvas);

        //mActivity.refreshGUI();

        if(checkerBG&&!hideCheckerBG) {
            canvas.drawPaint(checkerPaint);
        }

        if(buffRefresh||isHardwareAccelerated()){
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawBitmap(bBitmap, 0, 0, bBitmapPaint);
        }else {
            if (buff) {
                //Log.d(TAG, "select: " + select);
                if (fullBuff) {
                    canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                } else {
                    canvas.drawBitmap(mBitmap, buffRect, buffRect, mBitmapPaint);
                }
                canvas.drawBitmap(bBitmap, buffRect, buffRect, bBitmapPaint);
            } else {
                canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                if (tool.equals("path")) {
                    canvas.drawPath(mPath, mPaint);
                }
            }
        }

        if(select && !makingBitmap) {
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            if(selectMove&&sBitmap!=null){
                canvas.drawBitmap(sBitmap, moveX, moveY, null);
            }else {
                drawSelRect(selRect.left, selRect.top, selRect.right, selRect.bottom, true);
                canvas.drawBitmap(sBitmap, selRect, selRect, bBitmapPaint);
            }
        }

        if(buffRefresh){
            buffRefresh=false;
        }

        drawRects=false;
        if(drawRects) {
            canvas.drawRect(buffRectInner, dPaint);
        }

    }

    public void setScale(float s){
        View v=holder;
        scale=s;

        v.setScaleX(scale);
        v.setScaleY(scale);

        if (v.getScaleX() < minScale) {
            v.setScaleX(minScale);
        }else if (v.getScaleX() > maxScale) {
            v.setScaleX(maxScale);
        }

        if (v.getScaleY() < minScale) {
            v.setScaleY(minScale);
        }else if (v.getScaleY() > maxScale) {
            v.setScaleY(maxScale);
        }
    }

    public void redo(){

        hideCheckerBG=true;

        if(undo.size()>0) {
            currUndo--;
            if(currUndo<0){
                currUndo=0;
            }

            //Log.d(TAG,""+ undoTest);
            //Log.d(TAG, "CurrUndo: " + currUndo);
            if(hashFileHelper!=null&&hashFileHelper.UNDO_FOLDER!=null&&undo!=null) {
                Bitmap undoFile = hashFileHelper.loadCachedBitmap(hashFileHelper.UNDO_FOLDER, undo.get(currUndo));
                if(undoFile!=null) {
                    load(undoFile.copy(config, true));
                }
            }

            mActivity.refreshMenu("showundo");
            mActivity.updateThumb();
        }

        if(currUndo==0){
            mActivity.refreshMenu("hideredo");
        }

        hideCheckerBG=false;

        //Log.d(TAG, "CurrUndo: " + currUndo);
    }

    public void undo(){

        hideCheckerBG=true;

        if (undo.size() > 0) {
            currUndo++;

            if (currUndo > undoLimit) {
                currUndo = undoLimit;
            }
            if (currUndo > undo.size() - 1) {
                currUndo = undo.size() - 1;
            }

            if(undo.get(currUndo).equals("clear")){
                mBitmap.eraseColor(Color.TRANSPARENT);
                mCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
                invalidate();
            }else {
                if(hashFileHelper!=null&&undo!=null) {
                    Bitmap undoFile = hashFileHelper.loadCachedBitmap(hashFileHelper.UNDO_FOLDER, undo.get(currUndo));
                    if(undoFile!=null) {
                        load(undoFile.copy(config,true));
                    }
                }
            }
            mActivity.updateThumb();
            mActivity.refreshMenu("showredo");
        }

        if(currUndo==undo.size()-1){
            mActivity.refreshMenu("hideundo");
        }

        hideCheckerBG=false;
    }

    public void setBuffRect(float x1,float y1, float x2, float y2, int p){

        float tmpX=0;
        float tmpY=0;
        if(x1<0){
            x1=-p;
        }
        if(y1<0){
            y1=-p;
        }
        if(x2<0){
            x2=-p;
        }
        if(y2<0){
            y2=-p;
        }

        if(x1>x2){
            tmpX=x1;
            x1=x2-p;
            x2=tmpX+p;
        }
        if(y1>y2){
            tmpY=y1;
            y1=y2-p;
            y2=tmpY+p;
        }

        buffRect=new Rect((int)(x1-p),(int)(y1-p),(int)(x2+p),(int)(y2+p));
        if(accuracy&&(mPaint.getStrokeWidth()==1)){
            //buffRectInner = new Rect(x2 + (p ), y2 + (p), x1 - (p - 2), y1 - (p));
            //buffRectInner = new Rect(x1 - p + 1, y1 - p + 1, x2 + p - 1, y2 + p - 1);
            //buffRectInner = new Rect((int)(x1 - p + 1),(int)(y1 - p + 1),(int)(x2 + p),(int)( y2 + p));
            buffRectInner = buffRect;
        }else {
            buffRectInner = new Rect((int)(x1 - p + 1),(int)( y1 - p + 1),(int)(x2 + p - 1),(int)(y2 + p - 1));
           //buffRectInner = buffRect;
        }
        clearBuffer(false);
    }

    public void setSelRect(float x1,float y1, float x2,float y2, int p){

        float tmpX=0;
        float tmpY=0;
        if(x1<0){
            x1=0;
        }
        if(y1<0){
            y1=0;
        }
        if(x2<0){
            x2=0;
        }
        if(y2<0){
            y2=0;
        }

        if(x1>x2){
            tmpX=x1;
            x1=x2-p;
            x2=tmpX+p;
        }
        if(y1>y2){
            tmpY=y1;
            y1=y2-p;
            y2=tmpY+p;
        }

        selRect=new Rect((int)x1,(int)y1,(int)(x2+p),(int)(y2+p));
    }

    public void setBuffPath(Path path){
        Region clip = new Region(0, 0, mCanvas.getWidth(), mCanvas.getHeight());
        Region region = new Region();

        region.setPath(path, clip);
        Rect mRect=region.getBounds();

        float rX1=mRect.left;
        float rY1=mRect.top;
        float rX2=mRect.right;
        float rY2=mRect.bottom;

        int bsz=(int)mPaint.getStrokeWidth();
        setBuffRect(rX1-bsz-1, rY1-bsz-1,rX2+bsz+1, rY2+bsz+1, 1);
    }

    public void clearBuffer(boolean all){
        if(all){
            bBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), config);
            bCanvas = new Canvas(bBitmap);
            this.invalidate();
        }else {
            if (buff) {
                if (tool.equals("brush") || (tool.equals("pencil"))) {
                    this.invalidate(buffRectInner);
                } else if (tool.equals("select")) {
                    sBitmap = Bitmap.createBitmap(sBitmap.getWidth(), sBitmap.getHeight(), config);
                    sCanvas = new Canvas(sBitmap);
                    if (selRect != null) {
                        this.invalidate(selRect);
                    }
                } else {
                    bBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), config);
                    bCanvas = new Canvas(bBitmap);
                    this.invalidate();
                }
            } else {
                this.invalidate(buffRect);
            }
        }
    }

    public void clearSelection(){
        select=false;
        selectMove=false;
        if(sCanvas!=null) {
            sBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), config);
        }
        selRect=new Rect(0,0,0,0);
    }

    public void load(Bitmap bmp){
        setBuffRect(0, 0, bmp.getWidth(), bmp.getHeight(), 0);
        mBitmap = bmp;
        mCanvas = new Canvas(bmp);
        this.invalidate();
    }

    public void setSize(int size){
        brushSize=size;

        mPaint.setStrokeWidth(brushSize);
        bPaint.setStrokeWidth(brushSize);
    }

    public void setColor(int color){

        if(color==transColor){
            color=Color.argb(0, 255, 0, 255);
            isTransColor=true;

            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            bPaint.setColor(bgColor);
        }else{
            isTransColor=false;

            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            bPaint.setColor(color);
        }

        bPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));

        brushColor=color;
        mPaint.setColor(brushColor);
        mActivity.setDither(brushDither);

    }

    public void setPattern(BitmapShader pattern, int index){

        brushDither=index;
        brushPattern=pattern;

        /*
        Matrix localmatrix = new Matrix();
        localmatrix.setTranslate(0,0); // a translation for example
        localmatrix.setScale(1f,1f);
        brushPattern.setLocalMatrix(localmatrix);
        */

        if(pattern!=null) {
            mPaint.setShader(pattern);
            bPaint.setShader(pattern);
        }else{
            mPaint.setShader(null);
            bPaint.setShader(null);
        }
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        //float ss=holder.getScaleX();
        //mActivity.refreshGUI();

        if(pauseOnDraw) {
            if(!mActivity.paused) {
                mActivity.paused = true;
            }
        }

        if(firstEdit) {
            //undo.add(0, getBitmap());
            firstEdit=false;
        }

        bCnt=0;

        if(tool.equals("brush")) {

            buffRefresh=true;
            buff = true;
            fullBuff=false;

            //prev_x = -1;
            //prev_y = -1;

            prev_x = (int) x;
            prev_y = (int) y;

            //if(!accuracy) {
            bPath.reset();
            mPath.reset();

            bPath.moveTo(x, y);
            mPath.moveTo(x, y);
            //}

            drawPix(x,y);

        }else if(tool.equals("pencil")||tool.equals("eraser")) {

            buffRefresh = true;
            if (tool.equals("eraser") || isTransColor) {
                buff = false;
                fullBuff = false;
            } else {
                buff = true;
                fullBuff = false;
            }

            //prev_x = -1;
            //prev_y = -1;

            prev_x = (int) x;
            prev_y = (int) y;

            //if(!accuracy) {
            bPath.reset();
            mPath.reset();

            //if(accuracy) {
            mPoints.clear();
            mPoints.add(new PointF(prev_x, prev_y));
            //}

            bPath.moveTo(x, y);
            mPath.moveTo(x, y);
            //}

            drawPix(x,y);

        }else if(tool.equals("line")) {

            prev_x = x;
            prev_y = y;

            buff=true;
            fullBuff=true;

            // If transparent
            if(brushColor==16711935) {
                checkerPaint.setStrokeCap(bPaint.getStrokeCap());
                checkerPaint.setStyle(bPaint.getStyle());
                checkerPaint.setStrokeWidth(bPaint.getStrokeWidth());
            }

            drawPix(prev_x, prev_y);

        }else if(tool.equals("rectangle")||tool.equals("rectangle-filled")) {

            prev_x = x;
            prev_y = y;

            buff = true;
            fullBuff=true;

            // If transparent
            if(brushColor==16711935) {
                checkerPaint.setStrokeCap(bPaint.getStrokeCap());
                checkerPaint.setStyle(bPaint.getStyle());
                checkerPaint.setStrokeWidth(bPaint.getStrokeWidth());
            }

            //drawPix(prev_x, prev_y);

        }else if(tool.equals("ellipse")||tool.equals("ellipse-filled")) {

            prev_x = (int) x;
            prev_y = (int) y;

            buff=true;
            fullBuff=true;

            // If transparent
            if(brushColor==16711935) {
                checkerPaint.setStrokeCap(bPaint.getStrokeCap());
                checkerPaint.setStyle(bPaint.getStyle());
                checkerPaint.setStrokeWidth(bPaint.getStrokeWidth());
            }

            //drawPix(prev_x, prev_y);

        }else if(tool.equals("dropper")) {

            curr_x = (int) x;
            curr_y = (int) y;

            getPixColor(curr_x, curr_y);

        }else if(tool.equals("zoom")) {

            curr_x = x;
            curr_y = y;

            prev_x = x;
            prev_y = y;

            zoom(prev_x, prev_y, curr_x, curr_y, true);


        }else if(tool.equals("select")) {

            prev_x = (int) x;
            prev_y = (int) y;

            buff = true;
            if (selRect!=null){

                if(selRect.contains((int)prev_x, (int)prev_y)) {

                    //Log.d(TAG, "select move");

                    drawSelMove(prev_x, prev_y, true);

                    tool = "select-move";

                }else{

                    clearSelection();

                }
            }
            //drawSel(prev_x, prev_y);

        }else if(tool.equals("move")) {

            curr_x = x;
            curr_y = y;

            move(curr_x,curr_y,true);

        }else if (tool.equals("fill")){

            curr_x = (int) x;
            curr_y = (int) y;

            final Point pt = new Point();
            pt.x= (int)curr_x; //x co-ordinate where the user touches on the screen
            pt.y= (int)curr_y; //y co-ordinate where the user touches on the screen

            floodFill(mBitmap, pt);

        }else if(tool.equals("path")){

            bPath.moveTo(x, y);
            mPath.moveTo(x, y);
            mX = x;
            mY = y;

        }

        if (pauseOnDraw) {
            if(mActivity.paused) {
                mActivity.updatePreview();
            }
        }

    }


    public void drawPixels(Bitmap bmp,ArrayList<PointF> array,int col) {
        int s = array.size();
        int[] intArray = new int[s];
        for (int i = 0; i < s; i++) {
            float x2=array.get(i).x;
            float y2=array.get(i).y;
            float x1=x2;
            float y1=y2;
            if(i>0) {
                x1 = array.get(i-1).x;
                y1 = array.get(i-1).y;
            }

            buff=true;
            fullBuff=false;

            /*
            int dx = x2;
            int dy = y2;

            if(array.get(0).x==dx){
                dx=dx+1;
            }

            if(array.get(0).y==dy){
                dy=dy+1;
            }
            */

            //setBuffRect(x2,y2,x1,y1,0);

            //bBitmap.setPixel(, array.get(i).y,col);
            drawLine(bmp, x1, y1, x2, y2, col);

            setBuffRect(array.get(0).x, array.get(0).y, x1, y1, 1);

        }

    }

    public void drawLine(Bitmap bmp,float x1,float y1,float x2,float y2,int col){

        float dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        float dy = Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;

        dx=dx;
        dy=dy;

        //int[] currPxls=new int[mBitmap.getWidth()*mBitmap.getHeight()];

        float err = (dx > dy ? dx : -dy) / 2;
        while (true) {

            if(x1>-1&&x1<mBitmap.getWidth()&&(x2>-1&&x2<mBitmap.getWidth())&&y1>-1&&y1<mBitmap.getHeight()&&(y2>-1&&y2<mBitmap.getHeight())) {

                bBitmap.setPixel((int)x1, (int)y1, col);

                if (tool.equals("eraser")) {
                    mBitmap.setPixel((int) x1, (int)y1, 0x00000000);
                    mCanvas.drawBitmap(bBitmap, 0, 0, mPaint);
                } else {
                    //mBitmap.setPixel(x1, y1, col);
                    //mCanvas.drawBitmap(mBitmap, 0, 0, mPaint);
                    bBitmap.setPixel((int)x1, (int)y1, col);
                    bCanvas.drawBitmap(bBitmap, 0, 0, bPaint);
                }

                //setBuffRect(x1,y1,x2,y2,0);

            }

            //mBitmap.setPixel(x1,y1,col);

            if (x1 == x2 && y1 == y2) break;
            float e2 = err;
            if (e2 > -dx) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dy) {
                err += dx;
                y1 += sy;
            }
        }
    }

    private void touch_move(float x, float y) {

        if(tool.equals("brush")||tool.equals("pencil")||tool.equals("eraser")) {

            boolean quad=false;

            if(accuracy&&mPaint.getStrokeWidth()==1) {

                curr_x = x;
                curr_y = y;

                //drawPixLine(prev_x, prev_y, curr_x, curr_y);

                mPoints.add(new PointF(curr_x, curr_y));
                drawPixels(bBitmap, mPoints, mPaint.getColor());

                prev_x = curr_x;
                prev_y = curr_y;

            }else {

                curr_x = x;
                curr_y = y;

                float dx = Math.abs(curr_x - prev_x);
                float dy = Math.abs(curr_y - prev_y);

                if (bCnt == 0) {
                    //
                    bPath.reset();
                    if(mPoints.size()>0) {
                        prev_x = mPoints.get(0).x;
                        prev_y = mPoints.get(0).y;
                    }
                    bPath.moveTo(prev_x, prev_y);
                }

                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {

                    mPath.quadTo(prev_x, prev_y, (curr_x+prev_x)/2, (curr_y+prev_y)/2);

                    if(tool.equals("eraser")||isTransColor){
                        mCanvas.drawPath(mPath, mPaint);
                    }else {
                        bCanvas.drawPath(mPath, bPaint);
                    }

                    bPath.quadTo(prev_x, prev_y, (curr_x + prev_x) / 2, (curr_y + prev_y) / 2);

                    if(isHardwareAccelerated()){
                        setBuffPath(mPath);
                    }else{
                      if(bCnt==0) {  // was causing flickering
                           setBuffPath(mPath);
                      }else{
                           setBuffPath(bPath);
                      }
                    }

                    buff_x=(curr_x + buff_x) / 2;
                    buff_y=(curr_y + buff_y) / 2;

                    //Log.d(TAG,"quadTo");

                }else{

                    mPath.lineTo(curr_x, curr_y);

                    if(tool.equals("eraser")||isTransColor) {
                        mCanvas.drawPath(mPath, mPaint);
                    }else{
                        bCanvas.drawPath(mPath, bPaint);
                    }
                    //Log.d(TAG, "lineTo");

                    bPath.lineTo(curr_x, curr_y);

                    if(isHardwareAccelerated()){
                      setBuffPath(mPath);
                    }else{
                      if(bCnt==0) {
                          setBuffPath(mPath);
                      }else{
                          setBuffPath(bPath);
                      }
                    }

                    buff_x=curr_x;
                    buff_y=curr_y;

                }
            }

            if(bCnt>4) {
                //if(accuracy) {
                mPoints.clear();
                mPoints.add(new PointF(curr_x, curr_y));
                //}
                bCnt=0;
            }else{
                bCnt++;
            }

            prev_x = curr_x;
            prev_y = curr_y;

        }else if(tool.equals("line")) {

            curr_x = (int) x;
            curr_y = (int) y;

            drawPixLine(prev_x, prev_y, curr_x, curr_y);

        }else if(tool.equals("rectangle")||tool.equals("rectangle-filled")) {

            curr_x = (int) x;
            curr_y = (int) y;

            drawPixRect(prev_x, prev_y, curr_x, curr_y);

        }else if(tool.equals("ellipse")||tool.equals("ellipse-filled")) {

            curr_x = (int) x;
            curr_y = (int) y;

            drawPixElps(prev_x, prev_y, curr_x, curr_y);

        }else if(tool.equals("dropper")) {

            curr_x = (int) x;
            curr_y = (int) y;

            getPixColor(curr_x, curr_y);

        }else if(tool.equals("zoom")) {

            curr_x = (int) x;
            curr_y = (int) y;

            zoom(prev_x, prev_y, curr_x, curr_y, false);

            prev_x = (int) x;
            prev_x = (int) y;

        }else if(tool.equals("select")) {

            curr_x = (int) x;
            curr_y = (int) y;

            drawSelRect(prev_x, prev_y, curr_x, curr_y,false);

        }else if(tool.equals("select-move")) {

            curr_x = (int) x;
            curr_y = (int) y;

            drawSelMove(curr_x, curr_y, false);

        }else if(tool.equals("move")) {

            curr_x = (int) x;
            curr_y = (int) y;

            move(curr_x, curr_y, false);

        }else if(tool.equals("path")) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);

            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;

                mCanvas.drawPath(mPath, mPaint);
            }
        }

    }
    private void touch_up() {

        if (pauseOnDraw) {
            if (mActivity.paused) {
                mActivity.paused = false;
            }
        }

        //this.invalidate();

        if(tool.equals("brush")||tool.equals("pencil")||tool.equals("eraser")) {

            buff = false;
            mCanvas.drawBitmap(bBitmap, 0, 0, bBitmapPaint);

        }else if(tool.equals("line")) {

            buff=false;

            // If transparent
            if(brushColor==16711935) {
                drawPixLine(prev_x, prev_y, curr_x, curr_y);

                checkerPaint.setStrokeCap(Paint.Cap.SQUARE);
                checkerPaint.setStyle(Paint.Style.FILL);
                checkerPaint.setStrokeWidth(1);
            }else {
                mCanvas.drawBitmap(bBitmap, 0, 0, bBitmapPaint);
            }

        }else if(tool.equals("rectangle")||tool.equals("rectangle-filled")) {

            buff=false;
            // If transparent
            if(brushColor==16711935) {
                drawPixRect(prev_x,prev_y,curr_x,curr_y);

                checkerPaint.setStrokeCap(Paint.Cap.SQUARE);
                checkerPaint.setStyle(Paint.Style.FILL);
                checkerPaint.setStrokeWidth(1);

            }else{
                mCanvas.drawBitmap(bBitmap, 0, 0, bBitmapPaint);
            }

        }else if(tool.equals("ellipse")||tool.equals("ellipse-filled")) {

            buff = false;
            // If transparent
            if(brushColor==16711935) {
                drawPixElps(prev_x, prev_y, curr_x, curr_y);

                checkerPaint.setStrokeCap(Paint.Cap.SQUARE);
                checkerPaint.setStyle(Paint.Style.FILL);
                checkerPaint.setStrokeWidth(1);
            }else {
                mCanvas.drawBitmap(bBitmap, 0, 0, bBitmapPaint);
            }

        }else if(tool.equals("select")) {

            buff= false;
            drawSelRect(curr_x, curr_y, prev_x, prev_y,false);

        }else if(tool.equals("select-move")) {

            drawSelDrop(curr_x,curr_y);

            buff=false;

            clearSelection();

            tool="select";

        }else if(tool.equals("path")) {

            mPath.lineTo(mX, mY);
            mCanvas.drawPath(mPath, mPaint);
            mPath.reset();
            mPath= new Path();
        }

        bBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), config);
        bCanvas = new Canvas(bBitmap);

        saveBitmap();

        for (int u = 0; u < currUndo; u++) {
            if(undo.size() > 0){
                undo.remove(0);
            }
            //undoTest.remove(0);
        }
        currUndo=0;

        if(undoHash.get(String.valueOf(undo.size()-1))!=null) {
            undoHash.get(String.valueOf(undo.size() - 1)).recycle();
        }

        String fileName=FLHelper.saveCachedBitmap(getBitmap(),hashFileHelper.UNDO_FOLDER,String.valueOf(undo.size()));
        undo.add(0, fileName);

        undoHashNum++;

        mActivity.refreshMenu("hideredo");
        mActivity.refreshMenu("showundo");


        bPath.reset();
        mPath.reset();

        if(pauseOnDraw||mActivity.paused) {
            mActivity.updatePreview();
        }
    }

    public void move(float currX, float currY, boolean isStart){

        if(isStart){
            start.set(currX, currY);
        }else {
            View v = holder;

            end.set(currX, currY);

            float diffX=end.x-start.x;
            float diffY=end.y-start.y;

            //Log.d(TAG, "diffX: " + diffX + ", diffY: " + diffY+", scale: "+scale);

            v.setX(v.getX() + diffX);
            v.setY(v.getY() + diffY);

            start.set(end.x, end.y);

        }
    }


    public void zoom(float prevX, float prevY, float currX, float currY, boolean isStart){

        float diffX=prevX-currX;
        float diffY=prevY-currY;

        float newDist=(float)Math.sqrt(currX *currX + currY*currY);

        if(isStart){
            oldDist=(float)Math.sqrt(currX *currX + currY*currY);
        }else {
            scale = (newDist / oldDist);

            View v = holder;

            //Log.d(TAG, "zoom: " + scale);
            v.setScaleX(v.getScaleX() * scale);
            v.setScaleY(v.getScaleY() * scale);

            if (v.getScaleX() < minScale) {
                v.setScaleX(minScale);
            }else if (v.getScaleX() > maxScale) {
                v.setScaleX(maxScale);
            }

            if (v.getScaleY() < minScale) {
                v.setScaleY(minScale);
            }else if (v.getScaleY() > maxScale) {
                v.setScaleY(maxScale);
            }

        }
    }

    public void clearUndo(){
        hashFileHelper.clearCache(hashFileHelper.UNDO_FOLDER);
        undo.clear();

        mActivity.refreshMenu("hideundo");
        mActivity.refreshMenu("hideredo");
    }

    public void saveBitmap() {
        //mActivity.bitmapArray.add(currIndex,mBitmap);
        makingBitmap=true;

        mActivity.makeThumb(currIndex, mBitmap);

        makingBitmap = false;
    }

    public Bitmap getBitmap() {

        makingBitmap=true;

        hideCheckerBG=true;
        mCanvas.drawColor(Color.TRANSPARENT);

        this.setDrawingCacheEnabled(true);
        this.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(this.getDrawingCache());
        this.setDrawingCacheEnabled(false);

        hideCheckerBG=false;

        makingBitmap=false;

        return bmp;
    }

    public void setTool(String type, boolean isToggle){

        if(!type.equals("select")){
            //clearSelection();
        }

        if (type.equals("pencil")) {

            mPaint.setAntiAlias(false);
            mPaint.setDither(false);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.MITER);
            mPaint.setStrokeCap(Paint.Cap.SQUARE);
            mPaint.setPathEffect(null);
            mPaint.setStrokeWidth(1);
            mPaint.setFilterBitmap(false);

            bPaint.setAntiAlias(false);
            bPaint.setDither(false);
            bPaint.setStyle(Paint.Style.STROKE);
            bPaint.setStrokeJoin(Paint.Join.MITER);
            bPaint.setPathEffect(null);
            bPaint.setStrokeCap(Paint.Cap.SQUARE);
            bPaint.setStrokeWidth(1);
            bPaint.setFilterBitmap(false);

        }else if(type.equals("brush")){

            mPaint.setAntiAlias(false);
            mPaint.setDither(false);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);

            if(brushSize<2){
                mPaint.setStrokeJoin(Paint.Join.MITER);
                mPaint.setStrokeCap(Paint.Cap.SQUARE);

                mPaint.setStrokeWidth(1);
                mPaint.setPathEffect(null);
            }else {
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStrokeCap(Paint.Cap.ROUND);

                mPaint.setStrokeWidth(brushSize);
                mPaint.setPathEffect(new CornerPathEffect(brushSize));
            }

            mPaint.setFilterBitmap(false);

            bPaint.setAntiAlias(false);
            bPaint.setDither(false);
            bPaint.setStyle(Paint.Style.STROKE);

            if(brushSize<2){
                bPaint.setStrokeJoin(Paint.Join.MITER);
                bPaint.setStrokeCap(Paint.Cap.SQUARE);

                bPaint.setStrokeWidth(1);
                bPaint.setPathEffect(null);
            }else {
                bPaint.setStrokeJoin(Paint.Join.ROUND);
                bPaint.setStrokeCap(Paint.Cap.ROUND);

                bPaint.setStrokeWidth(brushSize);
                bPaint.setPathEffect(new CornerPathEffect(brushSize));
            }

            bPaint.setStrokeWidth(brushSize);
            bPaint.setFilterBitmap(false);

        } else if(type.equals("rectangle") || type.equals("rectangle-filled") || type.equals("line") || type.equals("ellipse") || type.equals("ellipse-filled") || type.equals("eraser")) {

            mPaint.setAntiAlias(false);
            mPaint.setDither(false);

            if(type.equals("rectangle-filled") || type.equals("ellipse-filled")){
                mPaint.setStyle(Paint.Style.FILL);
            }else{
                mPaint.setStyle(Paint.Style.STROKE);
            }

            if (type.equals("rectangle") || type.equals("rectangle-filled")) {
                mPaint.setStrokeJoin(Paint.Join.MITER);
                mPaint.setStrokeCap(Paint.Cap.SQUARE);
            }else{
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStrokeCap(Paint.Cap.ROUND);
            }

            mPaint.setPathEffect(null);
            mPaint.setStrokeWidth(brushSize);
            mPaint.setFilterBitmap(false);

            bPaint.setAntiAlias(false);
            bPaint.setDither(false);

            if(type.equals("rectangle-filled") || type.equals("ellipse-filled")){
                bPaint.setStyle(Paint.Style.FILL);
            }else{
                bPaint.setStyle(Paint.Style.STROKE);
            }

            bPaint.setPathEffect(null);

            if (type.equals("rectangle") || type.equals("rectangle-filled")) {
                bPaint.setStrokeJoin(Paint.Join.MITER);
                bPaint.setStrokeCap(Paint.Cap.SQUARE);
            }else{
                bPaint.setStrokeJoin(Paint.Join.ROUND);
                bPaint.setStrokeCap(Paint.Cap.ROUND);
            }

            bPaint.setStrokeWidth(brushSize);
            bPaint.setFilterBitmap(false);


        } else if(type.equals("eraser")) {

            mPaint.setAntiAlias(false);
            mPaint.setDither(false);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.MITER);
            mPaint.setStrokeCap(Paint.Cap.SQUARE);

            if(brushSize<2){
                mPaint.setStrokeJoin(Paint.Join.MITER);
                mPaint.setStrokeCap(Paint.Cap.SQUARE);

                mPaint.setStrokeWidth(1);
                mPaint.setPathEffect(null);
            }else {
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStrokeCap(Paint.Cap.ROUND);

                mPaint.setStrokeWidth(brushSize);
                mPaint.setPathEffect(new CornerPathEffect(brushSize));
            }

            mPaint.setFilterBitmap(false);

            bPaint.setAntiAlias(false);
            bPaint.setDither(false);

            if(brushSize<2){
                bPaint.setStrokeJoin(Paint.Join.MITER);
                bPaint.setStrokeCap(Paint.Cap.SQUARE);

                bPaint.setStrokeWidth(1);
                bPaint.setPathEffect(null);
            }else {
                bPaint.setStrokeJoin(Paint.Join.ROUND);
                bPaint.setStrokeCap(Paint.Cap.ROUND);

                bPaint.setStrokeWidth(brushSize);
                bPaint.setPathEffect(new CornerPathEffect(brushSize));
            }

            bPaint.setStrokeWidth(brushSize);
            bPaint.setFilterBitmap(false);

        }

        if (type.equals("eraser") || isTransColor){
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }else{
            mPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
        }

        bPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));

        if(isToggle) {
            toggleTool = tool;
            tool = type;
        }

        tool = type;

        //Log.d(TAG,"Tool Changed: "+tool);
    }


    private Bitmap clearBitmapRect(Bitmap bmp, Rect rect)
    {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        Canvas c = new Canvas(bmOverlay);
        c.drawBitmap(bmp, 0, 0, null);
        c.drawRect(rect, p);

        return bmOverlay;
    }

    private Bitmap pasteIntoBitmap(Bitmap sourcebmp, Bitmap pastebmp,int x, int y)
    {
        Bitmap bmOverlay = Bitmap.createBitmap(sourcebmp.getWidth(), sourcebmp.getHeight(), sourcebmp.getConfig());

        Paint p = new Paint();
        p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        Canvas c = new Canvas(bmOverlay);
        c.drawBitmap(sourcebmp, 0, 0, null);
        c.drawBitmap(pastebmp, x, y, null);

        return bmOverlay;
    }


    /*
    public void drawSel(int x, int y) {
        setBuffRect(x, y, x, y,1);
        bCanvas.drawPoint(x, y, sPaint);
    }
    */


    public void drawSelDrop(float x1, float y1){

        selectMove=false;

        mBitmap=pasteIntoBitmap(mBitmap, sBitmap, (int) moveX,(int) moveY);
        mCanvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        sBitmap = Bitmap.createBitmap(1, 1, mBitmap.getConfig());

        int w =mBitmap.getWidth();
        int h= mBitmap.getHeight();
        setBuffRect(0, 0, w, h,0);
        if(mBitmap == null) {
            mBitmap = Bitmap.createBitmap(w, h, config);
        }
        mCanvas = new Canvas(mBitmap);

        bBitmap = Bitmap.createBitmap(w, h, config);
        bCanvas = new Canvas(bBitmap);

        //clearBuffer(true);

        //mCanvas.setMatrix(matrix);

        //mActivity.updateThumb();
    }

    public void drawSelMove(float x1, float y1,boolean grab){

        if(grab==true){

            sBitmap=Bitmap.createBitmap(mBitmap, selRect.left,selRect.top,selRect.right-selRect.left, selRect.bottom-selRect.top);

            x1=x1-sBitmap.getWidth()/2;
            y1=y1-sBitmap.getHeight()/2;

            moveX=x1;
            moveY=y1;

            mBitmap=clearBitmapRect(mBitmap,selRect);

        }else{
            //Log.d(TAG,"sBitmap: "+sBitmap.getWidth());
            if(sBitmap!=null) {

                x1=x1-sBitmap.getWidth()/2;
                y1=y1-sBitmap.getHeight()/2;

                bCanvas.drawBitmap(sBitmap, x1, y1, null);
                setBuffRect(x1, y1, x1 + sBitmap.getWidth(), y1 + sBitmap.getHeight(), 0);

            }

            moveX=x1;
            moveY=y1;

        }

        selectMove = true;

        //Log.d(TAG, "x:, "+moveX+", y: "+moveY);

    }

    public void drawSelRect(float x1, float y1, float x2, float y2, boolean drawOnly) {
        if(!selectMove) {
            if (drawOnly) {

                selPhase++;
                if (selPhase > 2) {
                    selPhase = 0;
                }

                setBuffRect(x1, y1, x2+1, y2+1, 0);

                sPaint.setPathEffect(null);
                sPaint.setColor(Color.BLACK);
                sCanvas.drawRect(x1, y1, x2-1, y2-1, sPaint);

                sPaint.setPathEffect(new DashPathEffect(new float[]{2, 2}, selPhase));
                sPaint.setColor(Color.WHITE);
                sCanvas.drawRect(x1, y1, x2-1, y2-1, sPaint);

            } else {
                int ww = this.getWidth() - 1;
                int hh = this.getHeight() - 1;

                if (x1 < 0) {
                    x1 = 0;
                } else if (x1 > ww) {
                    x1 = ww;
                }
                if (x2 < 0) {
                    x2 = 0;
                } else if (x2 > ww) {
                    x2 = ww;
                }
                if (y1 < 0) {
                    y1 = 0;
                } else if (y1 > hh) {
                    y1 = hh;
                }
                if (y2 < 0) {
                    y2 = 0;
                } else if (y2 > hh) {
                    y2 = hh;
                }

                float diffX = Math.abs(x1 - x2);
                float diffY = Math.abs(y1 - y2);

                if (diffX > 1 && diffY > 1) {

                    setBuffRect(x1, y1, x2+1, y2+1, 0);
                    setSelRect(x1, y1, x2, y2, 0);

                    select = true;
                } else {
                    select = false;
                }
            }
        }
    }

    public void drawPix(float x, float y) {
        int ss=(int)mPaint.getStrokeWidth();
        setBuffRect(x-ss,y-ss,x+ss,y+ss,ss);
        //for (int i = 0; i < 3; i++) {
        if(buff) {
            bCanvas.drawPoint(x, y, bPaint);
        } else {
            mCanvas.drawPoint(x, y, mPaint);
        }
        // }
    }

    public void drawPixLine(float x1, float y1, float x2, float y2) {
        int ss=(int)mPaint.getStrokeWidth();
        setBuffRect(x1, y1, x2,y2,ss);

        if (buff) {
            // If transparent
            if(brushColor==16711935) {
                bCanvas.drawLine(x1, y1, x2, y2, checkerPaint);
            }else{
                bCanvas.drawLine(x1, y1, x2, y2, bPaint);
            }
        } else {
            mCanvas.drawLine(x1, y1, x2, y2, mPaint);
        }
    }

    public void drawPixRect(float x1, float y1, float x2, float y2) {
        int ss=(int)mPaint.getStrokeWidth();
        setBuffRect(x1, y1, x2, y2, ss);

        if (buff) {
            // If transparent
            if(brushColor==16711935) {
                bCanvas.drawRect(x1, y1, x2, y2, checkerPaint);
            }else {
                bCanvas.drawRect(x1, y1, x2, y2, bPaint);
            }
        } else {
            mCanvas.drawRect(x1, y1, x2, y2, mPaint);
        }
    }

    public void drawPixElps(float x1, float y1, float x2, float y2) {
        RectF or = new RectF(x1, y1, x2, y2);
        int ss=(int)mPaint.getStrokeWidth();;
        setBuffRect(x1, y1, x2, y2, ss);

        if(buff) {
            // If transparent
            if(brushColor==16711935) {
                bCanvas.drawOval(or, checkerPaint);
            }else{
                bCanvas.drawOval(or, bPaint);
            }
        }else{
            mCanvas.drawOval(or, mPaint);
        }
    }

    public void getPixColor(float x, float y){
        int pixel = mBitmap.getPixel((int) x, (int) y);

        int a = Color.alpha(pixel);
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);

        int col = Color.argb(255, r, g, b);

        Log.d(TAG, "Dropper Color: " + a);

        if(a==0){
            col=transColor;
        }

        mActivity.colorSwatchA.setBackgroundColor(col);
        mActivity.mDrawingView.setColor(col);;
    }

    public void floodFill(Bitmap  image, Point node) {

        int pixel = mBitmap.getPixel(node.x, node.y);

        int a = Color.alpha(pixel);
        int r = Color.red(pixel);
        int g = Color.green(pixel);
        int b = Color.blue(pixel);

        int targetColor = Color.argb(a, r, g, b);
        int replacementColor=mPaint.getColor();

        //Log.d(TAG,"targetColor: "+targetColor+", replacementColor: "+replacementColor);

        if(targetColor==0&&replacementColor==16711935) {
            //Avoid Crash
        }else{
            int width = image.getWidth();
            int height = image.getHeight();
            int target = targetColor;
            int replacement = replacementColor;

            if (target != replacement) {
                Queue<Point> queue = new LinkedList<Point>();
                do {
                    int x = node.x;
                    int y = node.y;
                    while (x > 0 && image.getPixel(x - 1, y) == target) {
                        x--;
                    }
                    boolean spanUp = false;
                    boolean spanDown = false;
                    while (x < width && image.getPixel(x, y) == target) {
                        image.setPixel(x, y, replacement);
                        if (!spanUp && y > 0 && image.getPixel(x, y - 1) == target) {
                            queue.add(new Point(x, y - 1));
                            spanUp = true;
                        } else if (spanUp && y > 0
                                && image.getPixel(x, y - 1) != target) {
                            spanUp = false;
                        }
                        if (!spanDown && y < height - 1
                                && image.getPixel(x, y + 1) == target) {
                            queue.add(new Point(x, y + 1));
                            spanDown = true;
                        } else if (spanDown && y < height - 1
                                && image.getPixel(x, y + 1) != target) {
                            spanDown = false;
                        }
                        x++;
                    }
                } while ((node = queue.poll()) != null);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int touch_count = event.getPointerCount();

        float x = event.getX();
        float y = event.getY();

        if(touch_count<2) {
            if(tool.equals("move")||tool.equals("zoom")) {
                x = event.getRawX();
                y = event.getRawY();
            }

            Point point = new Point((int)x,(int)y);
            if(mode == NONE) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        if(!locked) {
                            touch_start(x, y);
                        }
                        mActivity.hideFrames();

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!locked) {
                            touch_move(x, y);
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        if(!locked) {
                            touch_up();
                        }else{
                            clearBuffer(true);

                            //this.invalidate();
                            handler.postDelayed(delayUnLock, 100);
                        }
                        break;
                }
            }

        }else{

            if(!frameTouched){
                Rect tmpRect = new Rect();
                this.getHitRect(tmpRect);

                if (tmpRect.contains((int) event.getX(0), (int) event.getY(0))) {
                    onTouch(holder, event, true);
                    //Log.d(TAG, "inside touch");
                }

                locked = true;

            }else{
                return false;
            }

        }
        return true;
    }

    /* multitouch */
    public boolean onTouch(View v, MotionEvent event, boolean raw) {

        //int touch_count= event.getPointerCount();

        bPath.reset();
        mPath.reset();

        if (pauseOnDraw) {
            if(mActivity.paused) {
                mActivity.paused = false;
            }
        } else {
            buffRefresh = true;
        }

        if(!locked) {
            Log.d(TAG, "locked");
            locked = true;
            clearBuffer(true);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                //savedMatrix.set(matrix);

                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:

                mActivity.hideFrames();

                buff=false;

                start.set(event.getRawX(), event.getRawY());

                if(!raw){
                    oldDist = spacing(event)/holder.getScaleX();
                }else{
                    oldDist = spacing(event);
                }

                if (oldDist*scale > 10f) {
                    //savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                //d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:

                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;

                break;
            case MotionEvent.ACTION_MOVE:

                if(!raw){
                    newDist = spacing(event) / holder.getScaleX();
                }else{
                    newDist = spacing(event);
                }

                float diffD=Math.abs(oldDist - newDist);

                // Log.d(TAG,"diffD: "+diffD);

                if(diffD<10){
                    mode = DRAG;
                }else{
                    mode= ZOOM;
                }

                if (mode == ZOOM || mode == DRAG) {
                    //Log.d(TAG,"mode: zoom");

                    if (newDist > 10f) {
                        //matrix.set(savedMatrix);
                        scale = (newDist / oldDist);

                        v.setScaleX(v.getScaleX() * scale);
                        v.setScaleY(v.getScaleY() * scale);

                        if (v.getScaleX() < minScale) {
                            v.setScaleX(minScale);
                            v.setScaleY(minScale);
                        }

                        if (v.getScaleY() > maxScale) {
                            v.setScaleX(maxScale);
                            v.setScaleY(maxScale);
                        }
                        //matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                        /*
                        if (lastEvent != null && event.getPointerCount() == 3) {
                            //newRot = rotation(event);
                            float r = newRot - d;
                            float[] values = new float[9];
                            matrix.getValues(values);
                            float tx = values[2];
                            float ty = values[5];
                            float sx = values[0];
                            float xc = (view.getWidth() / 2) * sx;
                            float yc = (view.getHeight() / 2) * sx;
                            //matrix.postRotate(r, tx + xc, ty + yc);
                        }
                        */
                }

                end.set(event.getRawX(), event.getRawY());
                if (mode == DRAG) {
                    //Log.d(TAG, "mode: drag");
                    //matrix.set(savedMatrix);

                    float diffX=end.x-start.x;
                    float diffY=end.y-start.y;

                    //Log.d(TAG, "diffX: " + diffX + ", diffY: " + diffY+", scale: "+scale);

                    v.setX(v.getX() + diffX);
                    v.setY(v.getY() + diffY);

                    //matrix.postTranslate(end.x, end.y);
                }
                start.set(end.x, end.y);

                break;
        }
        //view.setImageMatrix(matrix);

        return true;
    }

        /* multitouch */

    /**
     * Determine the space between the first two fingers
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    private Paint createCheckerBoard(int pixelSize, int col)
    {
        Bitmap bitmap = Bitmap.createBitmap(pixelSize * 2, pixelSize * 2, config);

        Paint fill = new Paint();
        fill.setStyle(Paint.Style.FILL);
        fill.setColor(col);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(bgColor);

        Rect rect = new Rect(0, 0, pixelSize, pixelSize);
        canvas.drawRect(rect, fill);

        rect.offset(pixelSize, pixelSize);
        canvas.drawRect(rect, fill);

        Paint paint = new Paint();
        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.REPEAT, BitmapShader.TileMode.REPEAT));

        return paint;
    }


    /* Filters */

    public static Bitmap decreaseColorDepth(Bitmap src, int bitOffset) {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                R = ((R + (bitOffset / 2)) - ((R + (bitOffset / 2)) % bitOffset) - 1);
                if(R < 0) { R = 0; }
                G = ((G + (bitOffset / 2)) - ((G + (bitOffset / 2)) % bitOffset) - 1);
                if(G < 0) { G = 0; }
                B = ((B + (bitOffset / 2)) - ((B + (bitOffset / 2)) % bitOffset) - 1);
                if(B < 0) { B = 0; }

                // set pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }



    /* Old Stuff
    public void drawLine(int x1,int y1,int x2,int y2,int col){

        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;

        dx=dx;
        dy=dy;

        int err = (dx > dy ? dx : -dy) / 2;
        while (true) {
            // setPixel(x0,y0);

            int tmpcol=col;

            //currPxls[(y1-1)*image.getWidth()+x1]=tmpcol;

            Log.v(TAG, "tmpcol: "+tmpcol);

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
    }
    */

    /*
    public void renderLine(int x1,int y1,int x2,int y2,int col,int thk){

        //if(thk==1){

        //  GLRenderer.inst.renderLine(x1, y1, x2, y2, col, thk);

        //}else {

        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;

        dx=dx*thk;
        dy = dy * thk;

        int err = (dx > dy ? dx : -dy) / 2;
        while (true) {

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
        }
    }
    */

}
