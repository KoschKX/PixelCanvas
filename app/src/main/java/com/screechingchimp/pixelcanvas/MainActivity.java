package com.screechingchimp.pixelcanvas;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.content.Context;

import android.os.Handler;
import android.preference.PreferenceManager;

//import android.support.v7.app.AppCompatActivity;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.opengl.GLSurfaceView;

import android.util.Log;
import android.widget.FrameLayout;

import android.view.ViewTreeObserver;

import android.graphics.Color;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

import java.lang.Thread.UncaughtExceptionHandler;
import java.io.StringWriter;
import java.io.PrintWriter;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ImageButton;

import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PixelCanvas";

    final Context context = this;

    private Space toolBarSpace;

    private GLSurfaceView glView;
    public FrameLayout canvasFrame;
    public FrameLayout canvasHolder;
    public FrameLayout paletteFrame;
    public FrameLayout ditherFrame;
    public FrameLayout brushFrame;

    private DrawView drawCanvas;
    DrawView mDrawingView;

    public FrameLayout colorSwatchAFrame;
    public ImageButton colorSwatchA;
    public FrameLayout ditherSwatchFrame;
    public ImageButton ditherSwatch;
    public FrameLayout sizeSwatchFrame;
    public ImageButton sizeSwatch;

    private LinearLayout swatchHolder;

    public CustomListLinear framesList;
    private HorizontalScrollView frameScrollView;
    private ScrollView toolScrollView;

    private LinearLayout secondToolBox;
    private LinearLayout secondBrush;
    private LinearLayout secondLine;
    private LinearLayout secondSquare;
    private LinearLayout secondCircle;

    private Space frameBoxSpaceA;
    private Space frameBoxSpaceB;
    private Space frameBoxSpaceC;

    private LinearLayout toolHolder;

    private PaletteView palette;
    private DitherView dither;
    private TextView brushSizeDisp;
    private BrushView brush;
    private ImageView preview;

    private Button testButton;
    private ImageButton playButton;

    private ImageButton brushButton;
    private ImageButton pencilButton;
    private ImageButton lineButton;
    private ImageButton eraserButton;
    private ImageButton squareButton;
    private ImageButton circleButton;
    private ImageButton fillButton;

    private ImageButton dropperButton;
    private ImageButton magnifyButton;
    private ImageButton moveButton;
    private ImageButton selectButton;

    private ImageButton quickDropper;

    private ImageButton filledCircleButton;
    private ImageButton filledSquareButton;

    private Handler animHandler = new Handler();
    private Timer timer;
    long starttime = 0;
    private int previewFrame=0;
    private int defaultFrameSpeed=100;
    private int currFrameSpeed=100;
    public ArrayList<Integer> frameSpeedArray = new ArrayList<Integer>();
    public boolean isAnimating;

    public int currIndex=0;
    public boolean hasUndos;
    public boolean hasRedos;
    public boolean paused=true;
    private boolean firstRun=true;

    public ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();

    public String currentFileName="New Sprite";
    public String currentFilePath="";

    public MNHelper mnhelper;
    public FLHelper fileHelper;

    private Dialog settingsWindow;

    private Dialog newImageWindow;
    private Dialog gifImportWindow;
    private Dialog aboutWindow;

    public static Menu toolMenu;
    public boolean toolLongPress=false;

    public void askPermissions(){
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        int PERMISSION_REQUEST_READ_FOLDERS = 1;
        ActivityCompat.requestPermissions(this,PERMISSIONS,PERMISSION_REQUEST_READ_FOLDERS);
    }
    public boolean checkPermissions(){
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        }else{
            return false;
        }
    }

    public ArrayList grabSettings() {
        ArrayList arr = new ArrayList<String>();
        arr.add(0, String.valueOf(mDrawingView.pauseOnDraw));
        return arr;
    }

    public void settingsChange(String type){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        if (type.equals("pauseondraw")){
            editor.putBoolean("pauseondraw", mDrawingView.pauseOnDraw);
        }
        if (type.equals("all")) {
            editor.putBoolean("pauseondraw", mDrawingView.pauseOnDraw);
        }
        if(!firstRun) {
        }
        editor.commit();
    }

    public void injectSetting(String type,String val){
        if (type.equals("pauseondraw")){
            mDrawingView.pauseOnDraw=Boolean.parseBoolean(val);
        }
    }

    public void getSettings() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        mDrawingView.pauseOnDraw = settings.getBoolean("pauseondraw", mDrawingView.pauseOnDraw);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter stackTrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stackTrace));
                StringBuilder errorReport = new StringBuilder();
                errorReport.append(stackTrace.toString());
                Log.d(TAG, e.toString() + "\n" + errorReport.toString());
                System.exit(10);
            }
        });

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolBarSpace = (Space) findViewById(R.id.toolBarSpace);

        canvasHolder = (FrameLayout) findViewById(R.id.canvasHolder);
        canvasFrame=(FrameLayout)findViewById(R.id.canvasFrame);
        paletteFrame = (FrameLayout) findViewById(R.id.paletteFrame);
        brushFrame = (FrameLayout) findViewById(R.id.brushFrame);
        ditherFrame = (FrameLayout) findViewById(R.id.ditherFrame);
        brushSizeDisp = (TextView) findViewById(R.id.brushSizeDisp);
        //pixelCanvas = (PixelCanvas) findViewById(R.id.pixelCanvas);
        toolHolder = (LinearLayout) findViewById(R.id.toolHolder);
        secondBrush = (LinearLayout) findViewById(R.id.secondBrush);
        secondLine = (LinearLayout) findViewById(R.id.secondLine);
        secondSquare = (LinearLayout) findViewById(R.id.secondSquare);
        secondCircle = (LinearLayout) findViewById(R.id.secondCircle);
        secondToolBox = (LinearLayout) findViewById(R.id.secondToolBox);
        preview = (ImageView) findViewById(R.id.preview);
        frameBoxSpaceA = (Space) findViewById(R.id.frameBoxSpaceA);
        frameBoxSpaceB = (Space) findViewById(R.id.frameBoxSpaceB);
        frameBoxSpaceC = (Space) findViewById(R.id.frameBoxSpaceC);
        palette = (PaletteView) findViewById(R.id.palette);
        dither = (DitherView) findViewById(R.id.dither);
        brush = (BrushView) findViewById(R.id.brush);
        colorSwatchAFrame = (FrameLayout) findViewById(R.id.colorSwatchAFrame);
        colorSwatchA = (ImageButton) findViewById(R.id.colorSwatchA);
        ditherSwatchFrame = (FrameLayout) findViewById(R.id.ditherSwatchFrame);
        ditherSwatch = (ImageButton) findViewById(R.id.ditherSwatch);
        sizeSwatchFrame = (FrameLayout) findViewById(R.id.sizeSwatchFrame);
        sizeSwatch = (ImageButton) findViewById(R.id.sizeSwatch);
        swatchHolder = (LinearLayout) findViewById(R.id.swatchHolder);

        mnhelper = new MNHelper(getApplicationContext());

        mDrawingView=new DrawView(MainActivity.this, this,null);
        final FrameLayout mDrawingPad=(FrameLayout)findViewById(R.id.canvasHolder);
        mDrawingPad.addView(mDrawingView);

        testButton = (Button) findViewById(R.id.testButton);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addBlankFrame();
            }
        });

        playButton = (ImageButton) findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isAnimating) {
                    stopPreview();
                }else{
                    playPreview();
                }
            }
        });

        brushButton = (ImageButton) findViewById(R.id.brushButton);
        brushButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("brush", false);
            }
        });
        brushButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSecondTools("brush");
                return false;
            }
        });

        pencilButton = (ImageButton) findViewById(R.id.pencilButton);
        pencilButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("pencil", false);
            }
        });
        lineButton = (ImageButton) findViewById(R.id.lineButton);
        lineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("line",false);
            }
        });
        squareButton = (ImageButton) findViewById(R.id.squareButton);
        squareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("rectangle",false);
            }
        });
        squareButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSecondTools("square");
                return false;
            }
        });

        filledSquareButton = (ImageButton) findViewById(R.id.filledSquareButton);
        filledSquareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("rectangle-filled", false);
            }
        });

        selectButton = (ImageButton) findViewById(R.id.selectButton);
        selectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("select", false);
            }
        });

        circleButton = (ImageButton) findViewById(R.id.circleButton);
        circleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("ellipse",false);
            }
        });
        circleButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showSecondTools("circle");
                return false;
            }
        });

        filledCircleButton = (ImageButton) findViewById(R.id.filledCircleButton);
        filledCircleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("ellipse-filled", false);
            }
        });

        fillButton = (ImageButton) findViewById(R.id.fillButton);
        fillButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("fill",false);
            }
        });

        dropperButton = (ImageButton) findViewById(R.id.dropperButton);
        dropperButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("dropper",false);
            }
        });

        eraserButton = (ImageButton) findViewById(R.id.eraserButton);
        eraserButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("eraser",false);
            }
        });
        magnifyButton = (ImageButton) findViewById(R.id.magnifyButton);
        magnifyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("zoom",false);
            }
        });
        moveButton = (ImageButton) findViewById(R.id.moveButton);
        moveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTool("move",false);
            }
        });

        framesList = (CustomListLinear) findViewById(R.id.framesList);

        toolScrollView = (ScrollView) findViewById(R.id.toolScrollerView);
        toolScrollView.setVerticalScrollBarEnabled(false);
        toolScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        frameScrollView = (HorizontalScrollView) findViewById(R.id.frameScrollView);
        frameScrollView.setHorizontalScrollBarEnabled(false);
        frameScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

        quickDropper = (ImageButton) findViewById(R.id.quickDropper);
        quickDropper.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                            setTool("dropper",true);
                        break;
                    case MotionEvent.ACTION_UP:
                            setTool(mDrawingView.toggleTool,false);
                        break;
                }
                return true;
            }
        });

        canvasFrame.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                int touch_count = event.getPointerCount();
                Rect tmpRect = new Rect();
                canvasHolder.getHitRect(tmpRect);
                if (touch_count > 1) {
                    if (!tmpRect.contains((int) event.getX(0), (int) event.getY(0))) {
                        mDrawingView.onTouch(canvasHolder, event, false);
                        mDrawingView.frameTouched = true;
                    } else {
                        mDrawingView.frameTouched = false;
                        mDrawingView.onTouch(canvasHolder, event, false);
                    }
                } else {
                    mDrawingView.frameTouched = false;
                }
                return true;
            }
        });

        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(firstRun) {
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    toolbar.setTitleTextColor(getResources().getColor(R.color.titlebarTextColor));
                    setToolSizes();
                    palette.initialize(mDrawingView, colorSwatchA, mDrawingView.brushColor);
                    dither.initialize(mDrawingView, ditherSwatch, mDrawingView.brushDither);
                    brush.initialize(mDrawingView, sizeSwatch, mDrawingView.brushSize);
                    mDrawingView.setScale((canvasFrame.getHeight() - mDrawingView.getHeight()) / 150);
                    fileHelper=new FLHelper(context);
                    newImageWindow = new Dialog(context);
                    mnhelper.makeNewImageWindow(MainActivity.this, newImageWindow, false);
                    gifImportWindow = new Dialog(context);
                    mnhelper.makeGifImportWindow(MainActivity.this, gifImportWindow, false);
                    aboutWindow = new Dialog(context);
                    mnhelper.makeAboutWindow(MainActivity.this, aboutWindow, false);
                    settingsWindow = new Dialog(context);
                    mnhelper.makeSettingsWindow(MainActivity.this, settingsWindow, false);
                    frameSpeedArray.add(defaultFrameSpeed);
                    getSettings();
                    firstRun=false;
                }
            }
        });

        colorSwatchA.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (paletteFrame.getVisibility() == View.GONE) {
                    brush.toggleVisibility(false);
                    dither.toggleVisibility(false);
                    palette.toggleVisibility(true);
                } else {
                    //Log.d(TAG, "color swatch invisible");
                    palette.toggleVisibility(false);
                }
            }
        });

        ditherSwatch.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "brush swatch clicked");
                if(ditherFrame.getVisibility()==View.GONE) {
                    palette.toggleVisibility(false);
                    dither.toggleVisibility(true);
                    brush.toggleVisibility(false);
                }else{
                    dither.toggleVisibility(false);
                }
            }
        });

        sizeSwatch.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(brushFrame.getVisibility()==View.GONE) {
                    palette.toggleVisibility(false);
                    dither.toggleVisibility(false);
                    brush.toggleVisibility(true);
                }else{
                    brush.toggleVisibility(false);
                }
            }
        });

        sizeSwatchFrame.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                //Log.d(TAG, "brush swatch clicked");
                if(brushFrame.getVisibility()==View.GONE) {
                    palette.toggleVisibility(false);
                    dither.toggleVisibility(false);
                    brush.toggleVisibility(true);
                }else{
                    brush.toggleVisibility(false);
                }
            }
        });
    }

    public void refreshGUI(){
    }

    public void showSecondTools(String toolType){
        toolLongPress=true;
        secondBrush.setVisibility(View.INVISIBLE);
        secondLine.setVisibility(View.INVISIBLE);
        secondCircle.setVisibility(View.INVISIBLE);
        secondSquare.setVisibility(View.INVISIBLE);
        if(toolType=="brush"){
            secondBrush.setVisibility(View.VISIBLE);
        }else if(toolType=="line"){
            secondLine.setVisibility(View.VISIBLE);
        }else if(toolType=="circle"){
            secondCircle.setVisibility(View.VISIBLE);
        }else if(toolType=="square"){
            secondSquare.setVisibility(View.VISIBLE);
        }
        swatchHolder.setVisibility(View.INVISIBLE);
        secondToolBox.setVisibility(View.VISIBLE);
    }

    public void hideSecondTools(){
        secondBrush.setVisibility(View.INVISIBLE);
        secondLine.setVisibility(View.INVISIBLE);
        secondCircle.setVisibility(View.INVISIBLE);
        secondSquare.setVisibility(View.INVISIBLE);
        secondToolBox.setVisibility(View.GONE);
        swatchHolder.setVisibility(View.VISIBLE);
    }

    public void hideFrames(){
        if (paletteFrame.getVisibility() == View.VISIBLE) {
            palette.toggleVisibility(false);
        }
        if(brushFrame.getVisibility()== View.VISIBLE){
            brush.toggleVisibility(false);
        }
    }

    public void setDither(int index){
        dither.setDither(index);
    }

    public void setTool(String type, boolean isToggle){
        Log.d(TAG, "Tool Changed: " + type);
        mDrawingView.setTool(type, isToggle);
        if(!toolLongPress) {
            hideSecondTools();
        }
        toolLongPress=false;
    }

    public void setToolSizes(){
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics ();
        display.getMetrics(outMetrics);
        float density  = getResources().getDisplayMetrics().density;
        float dpHeight = outMetrics.heightPixels / density;
        float dpWidth  = outMetrics.widthPixels / density;
        float height = outMetrics.heightPixels / density;
        float width  = outMetrics.widthPixels / density;
        int sz=0;
        float div=0;
        div=25f/density;
        if(dpHeight>dpWidth ){
            sz=(int)(dpHeight/div);
        }else{
            sz=(int)(dpWidth/div);
        }
    }

    public Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), mDrawingView.config);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public void addDuplicateFrame(){
        Bitmap nbmp = Bitmap.createBitmap(mDrawingView.mBitmap);
        framesList.addItem(nbmp);
        frameSpeedArray.add(defaultFrameSpeed);
        loadBitmap(nbmp);
        setFrameIndex(framesList.getCount() - 1);
        mDrawingView.clearUndo();
    }

    public void removeFrame(int index){
        framesList.removeItem(index);
        int newIndex=mDrawingView.currIndex-1;
        if(newIndex<0){
            newIndex=0;
        }
        previewFrame=0;
        setFrameIndex(newIndex);
        mDrawingView.clearUndo();
        mDrawingView.clearSelection();
        loadBitmap(framesList.getBitmap(newIndex));
    }

    public void addBlankFrame(){
        Bitmap nbmp = Bitmap.createBitmap(mDrawingView.mBitmap.getWidth(), mDrawingView.mBitmap.getHeight(), mDrawingView.mBitmap.getConfig()); // this creates a MUTABLE bitmap
        framesList.addItem(nbmp);
        frameSpeedArray.add(defaultFrameSpeed);
        loadBitmap(nbmp);
        setFrameIndex(framesList.getCount() - 1);
        mDrawingView.clearUndo();
        mDrawingView.clearSelection();
    }

    public void makeThumb(int index, Bitmap bmp) {
       if(framesList.getCount()<1){
           framesList.addItem(bmp);
       }else {
           framesList.setItem(index, bmp);
       }
    }

    public void updateThumb() {
        framesList.setItem(mDrawingView.currIndex, mDrawingView.mBitmap);
    }

    public void clearUndo(){
        mDrawingView.clearUndo();
    }
    public void setFrameIndex(int index){
        mDrawingView.currIndex=index;
    }
    public void loadBitmap(Bitmap bmp) {
        mDrawingView.load(bmp);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void stopPreview(){
        isAnimating=false;
        previewFrame=0;
        animHandler.removeCallbacks(animRunnable);
        updatePreview();
        playButton.setImageResource(R.drawable.ic_action_playback_play_b);
    }

    public void playPreview(){
        paused=false;
        playButton.setImageResource(R.drawable.ic_action_playback_stop_b);
        previewFrame=0;
        if(frameSpeedArray.size()>0){
            currFrameSpeed=frameSpeedArray.get(0);
        }else{
            currFrameSpeed=defaultFrameSpeed;
        }
        isAnimating=true;
        animHandler.removeCallbacks(animRunnable);
        animHandler.postDelayed(animRunnable, currFrameSpeed);
    }

    public int getFrameSpeed(int index){
        return frameSpeedArray.get(index);
    }
    public void setFrameSpeed(int index,int speed){
        frameSpeedArray.set(index, speed);
    }
    public void showFrameOptions(int index){
        mnhelper.makeFrameOptionsWindow(index, MainActivity.this, newImageWindow, true);
    }

    private Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            nextFrame();
        }
    };

    public void nextFrame(){
        int cnt = framesList.getCount();
        if (cnt > 0) {
            if (previewFrame > cnt - 1) {
                previewFrame = 0;
            }
            animHandler.removeCallbacks(animRunnable);
            if(cnt>0) {
                if(!paused) {
                    preview.setImageBitmap(framesList.getBitmap(previewFrame).copy(mDrawingView.config, false));
                    previewFrame++;
                    if (frameSpeedArray.size() > previewFrame) {
                        currFrameSpeed = frameSpeedArray.get(previewFrame);
                    } else if (frameSpeedArray.size() > 0) {
                        currFrameSpeed = frameSpeedArray.get(0);
                    } else {
                        currFrameSpeed = defaultFrameSpeed;
                    }
                }
                if(!mDrawingView.pauseOnDraw){
                    mDrawingView.buffRefresh=true;
                }
                animHandler.postDelayed(animRunnable, currFrameSpeed);
            }else{
                preview.setImageBitmap(framesList.getBitmap(previewFrame));
            }
        }
    }

    public void updatePreview(){
        preview.setImageBitmap(mDrawingView.mBitmap.copy(mDrawingView.config, false));
    }

    public Bitmap.Config getConfig(){
        Bitmap.Config config=mDrawingView.config;
        return config;
    }

    public void refreshMenu(String mode){
        if(mode.equals("hideredo")){
            hasRedos=false;
        }else if(mode.equals("showredo")){
            hasRedos=true;
        }else if(mode.equals("hideundo")){
            hasUndos=false;
        }else if(mode.equals("showundo")) {
            hasUndos = true;
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item;
        item = menu.findItem(R.id.action_settings);
        setMenuIcon(item, R.string.settings, R.drawable.ic_action_settings_b);
        item = menu.findItem(R.id.action_new_image);
        setMenuIcon(item, R.string.new_image, R.drawable.ic_action_tablet_b);
        item = menu.findItem(R.id.action_export_gif);
        setMenuIcon(item, R.string.export_gif, R.drawable.ic_action_export_b);
        item = menu.findItem(R.id.action_import_gif);
        setMenuIcon(item, R.string.import_gif, R.drawable.ic_action_import_b);
        item = menu.findItem(R.id.action_about);
        setMenuIcon(item, R.string.about, R.drawable.ic_action_bulb_b);
        if(hasUndos){
            menu.findItem(R.id.action_undo).setVisible(true);
            menu.findItem(R.id.action_noundos).setVisible(false);
        }else{
            menu.findItem(R.id.action_undo).setVisible(false);
            menu.findItem(R.id.action_noundos).setVisible(true);
        }
        if(hasRedos) {
            menu.findItem(R.id.action_redo).setVisible(true);
            menu.findItem(R.id.action_noredos).setVisible(false);
        }else{
            menu.findItem(R.id.action_redo).setVisible(false);
            menu.findItem(R.id.action_noredos).setVisible(true);
        }
        toolMenu=menu;
        return true;
    }

    public void setMenuIcon(MenuItem item,int string,int drawableRes){
        String s = "* " + getString(string);
        SpannableStringBuilder builder = new SpannableStringBuilder(s);
        Drawable myIcon =getResources().getDrawable(drawableRes);
        myIcon.setBounds(0, 0, myIcon.getIntrinsicWidth(), myIcon.getIntrinsicHeight());
        SuperscriptSpanAdjuster m = new SuperscriptSpanAdjuster(1.25);
        builder.setSpan(m, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ImageSpan(myIcon, ImageSpan.ALIGN_BASELINE), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        item.setTitle(builder);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about){
            mnhelper.makeAboutWindow(MainActivity.this, aboutWindow, true);
        } else if (id == R.id.action_new_image){
            mnhelper.makeNewImageWindow(MainActivity.this, newImageWindow, true);
        } else if (id == R.id.action_settings) {
            mnhelper.makeSettingsWindow(MainActivity.this, settingsWindow, true);
            return true;
        }else if (id == R.id.action_undo) {
            mDrawingView.undo();
            return true;
        }else if (id == R.id.action_redo) {
            mDrawingView.redo();
            return true;
        }else if (id == R.id.action_export_gif) {
            int cnt=framesList.getCount();
            ArrayList<Bitmap> toGif = new ArrayList<Bitmap>();
            for(int i=0;i<cnt;i++) {
                toGif.add(framesList.getBitmap(i));
            }
            ArrayList<Integer> toGifSpd = new ArrayList<Integer>();
            for(int i=0;i<cnt;i++) {
                toGifSpd.add(frameSpeedArray.get(i));
            }
            mnhelper.makeGifExportWindow(MainActivity.this, newImageWindow, true);
            return true;
        }else if (id == R.id.action_import_gif) {
            mnhelper.makeGifImportWindow(MainActivity.this, newImageWindow, true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createImage(int width, int height, Bitmap loadBitmap){
        canvasHolder.removeAllViews();
        canvasHolder.getLayoutParams().width=width;
        canvasHolder.getLayoutParams().height=height;
        int tmpSz=mDrawingView.brushSize;
        int tmpCol=mDrawingView.brushColor;
        int tmpDth=mDrawingView.brushDither;
        String tmpTool=mDrawingView.tool;
        if(loadBitmap==null){
            Bitmap.Config conf = mDrawingView.config; // see other conf types
            loadBitmap = Bitmap.createBitmap(width, height, conf); // this creates a MUTABLE bitmap
        }
        mDrawingView = new DrawView(MainActivity.this, this, loadBitmap);
        FrameLayout mDrawingPad = (FrameLayout) findViewById(R.id.canvasHolder);
        mDrawingPad.addView(mDrawingView);
        mDrawingView.setScale((canvasFrame.getHeight() - height) / 60);
        brush.initialize(mDrawingView, sizeSwatch, tmpSz);
        dither.initialize(mDrawingView, ditherSwatch, tmpDth);
        palette.initialize(mDrawingView, colorSwatchA, tmpCol);
        mDrawingView.setSize(tmpSz);
        mDrawingView.setColor(tmpCol);
        mDrawingView.setTool(tmpTool,false);
        preview.setImageBitmap(loadBitmap);
        framesList.clear();
    }

    public void exportGif(String filename){
        int cnt=framesList.getCount();
        ArrayList<Bitmap> toGif = new ArrayList<Bitmap>();
        for(int i=0;i<cnt;i++) {
            toGif.add(framesList.getBitmap(i));
        }
        ArrayList<Integer> toGifSpd = new ArrayList<Integer>();
        for(int i=0;i<cnt;i++) {
            toGifSpd.add(frameSpeedArray.get(i));
        }
        fileHelper.encodeGif(toGif,toGifSpd,filename);
    }
    public void importGif(String filename){
        ArrayList<Bitmap> frmGif = new ArrayList<Bitmap>();
        ArrayList<Integer> frmSpd = new ArrayList<Integer>();
        frmGif=fileHelper.decodeGif(filename);
        frmSpd=fileHelper.decodeGifSpeed(filename);
        if(frmGif!=null) {
            int ww = frmGif.get(0).getWidth();
            int hh = frmGif.get(0).getHeight();
            createImage(ww, hh, frmGif.get(0));
            int cnt = frmGif.size();
            frameSpeedArray.clear();
            for (int i = 0; i < cnt; i++) {
                framesList.addItem(frmGif.get(i));
                frameSpeedArray.add(frmSpd.get(i));
            }
            if (isAnimating) {
                playPreview();
            }
        }
    }


}
