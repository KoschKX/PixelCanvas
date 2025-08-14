package com.screechingchimp.pixelcanvas;

/**
 * Created by Kosch on 12/2/2015.
 */
import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;

import android.util.Log;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MNHelper
{

    private Context ctx;

    public MNHelper(Context context)
    {
        this.ctx = context;
    }

    private int skinIndex=0;
    private int parser=0;
    private int mode=0;

    private static final String TAG = "PixelCanvas";

    MainActivity mActivity;
    Dialog dialog;

    private boolean pauseOnDraw;

    public void makeAboutWindow(MainActivity mainActivity,Dialog mainDialog,boolean show){

        mActivity=mainActivity;
        dialog=mainDialog;

        dialog.setContentView(R.layout.about_main);
        dialog.setTitle(R.string.about);

        if(show){
            dialog.show();
        }

    }

    public void makeEmailWindow(MainActivity mainActivity, Intent intent, String title, String content){

        mActivity=mainActivity;

        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        intent.putExtra(Intent.EXTRA_TITLE, ctx.getString(R.string.send_email));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        mActivity.startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean endsWithExtension(final String fileName, final String extension) {
        return fileName != null && fileName.toLowerCase().endsWith(extension.toLowerCase());
    }

    public static boolean createDirIfNotExists(String path) {
  		boolean ret = true;
  		try {
  			File file = new File(path);
  			if (!file.exists()) {
  				if (!file.mkdirs()) {
  					ret = false;
  				}
  			}
  		} catch (Exception e) {}
  		return ret;
  	}


    private CustomListAdapter importGifAdt;
    private int importGifIndex=0;
    private int importGifTextColor=-1;
    public void makeGifImportWindow(final MainActivity mainActivity,Dialog mainDialog,boolean show){
        if(show&&!mainActivity.checkPermissions()) {
            mainActivity.askPermissions();
            dialog.dismiss();
            return;
        }
        dialog=mainDialog;
        ArrayList<String>arrayFiles = new ArrayList<String>();
        dialog.setContentView(R.layout.importgif_main);
        dialog.setTitle(R.string.import_gif);
        String SAVE_DIR = "PixelCanvas";
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + SAVE_DIR + "/";
        //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SAVE_DIR + "/";
        createDirIfNotExists(path);
        ListView list=(ListView)dialog.findViewById(R.id.fileList);
        importGifAdt = new CustomListAdapter(mainActivity,arrayFiles);
        list.setAdapter(importGifAdt);
        File f = new File(path);
        File file[] = f.listFiles();
        if(file!=null) {
            for (int i = 0; i < file.length; i++) {
                if (!file[i].isDirectory()) {
                    mainActivity.currentFileName=file[i].getName();
                    importGifAdt.add(file[i].getName());
                }
            }
        }
        importGifAdt.notifyDataSetChanged();
        if(show) {
            dialog.show();
            if (list.getCount() > 0) {
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        importGifIndex = position;
                        parent.setSelection(position);
                        importGifAdt.markListItem(position);
                    }
                });
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                builder.setMessage("No GIFs found.")
                        .setCancelable(false)
                        .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                dialog.dismiss();
            }
            Button impBtn = (Button) dialog.findViewById(R.id.importButton);
            impBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ListView list = (ListView) dialog.findViewById(R.id.fileList);
                    int index = importGifIndex;
                    if (index > -1) {
                        String filename = importGifAdt.getItem(index);
                        mActivity.importGif(filename);
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    public void makeGifExportWindow(final MainActivity mainActivity,Dialog mainDialog,boolean show) {
        if(show&&!mainActivity.checkPermissions()) {
            mainActivity.askPermissions();
            dialog.dismiss();
            return;
        }
        dialog = mainDialog;
        dialog.setContentView(R.layout.exportgif_main);
        dialog.setTitle("Export Gif");
        if (show) {
            dialog.show();
            Button ciBtn = (Button) dialog.findViewById(R.id.createImageButton);
            ciBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText name = (EditText) dialog.findViewById(R.id.nameSwatch);
                    if (name.getText().toString().trim().equals("")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                        builder.setMessage(ctx.getString(R.string.new_image_name_required))
                                .setCancelable(false)
                                .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        String file_name = name.getText().toString().trim();
                        mainActivity.exportGif(file_name);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
                        builder.setMessage("GIF exported.")
                                .setCancelable(false)
                                .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                        dialog.dismiss();
                    }
                }
            });
        }
    }

    public void makeFrameOptionsWindow(final int index, final MainActivity mainActivity,Dialog mainDialog,boolean show){
        mActivity=mainActivity;
        dialog=mainDialog;
        dialog.setContentView(R.layout.frameoptions_main);
        dialog.setTitle(R.string.frame_options);
        EditText speed=(EditText)dialog.findViewById(R.id.speedSwatch);
        speed.setText(String.valueOf(mActivity.getFrameSpeed(index)));
        if(show){
            dialog.show();
        }
        Button aplBtn=(Button)dialog.findViewById(R.id.applyButton);
        aplBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText speed=(EditText)dialog.findViewById(R.id.speedSwatch);
                int spd=Integer.valueOf(speed.getText().toString());
                mainActivity.setFrameSpeed(index,spd);
                dialog.dismiss();
            }
        });
        Button remBtn=(Button)dialog.findViewById(R.id.removeButton);
        remBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setMessage(ctx.getString(R.string.remove_frame))
                        .setCancelable(false)
                        .setPositiveButton(ctx.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mainActivity.removeFrame(index);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(ctx.getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                dialog.dismiss();
            }
        });

    }

    public void makeSettingsWindow(MainActivity mainActivity,Dialog mainDialog,boolean show) {
        mActivity = mainActivity;
        dialog = mainDialog;
        dialog.setContentView(R.layout.settings_main);
        dialog.setTitle(R.string.settings);
        ArrayList mSettings=mainActivity.grabSettings();
        pauseOnDraw=Boolean.valueOf(mSettings.get(0).toString());
        CheckBox swp=(CheckBox)dialog.findViewById(R.id.pauseOnDrawSwatch1);
        swp.setChecked(false);
        if(show){
            dialog.show();
        }
        swp.setChecked(pauseOnDraw);
        swp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox swp = (CheckBox) dialog.findViewById(R.id.pauseOnDrawSwatch1);
                pauseOnDraw = swp.isChecked();
                mActivity.injectSetting("pauseondraw", String.valueOf(pauseOnDraw));
                mActivity.settingsChange("pauseondraw");
            }
        });
    }

    public void makeNewImageWindow(final MainActivity mainActivity,Dialog mainDialog,boolean show){
        mActivity=mainActivity;
        dialog=mainDialog;
        dialog.setContentView(R.layout.newimage_main);
        dialog.setTitle(R.string.new_image);
        if(show){
            dialog.show();
        }
        Button ciBtn=(Button)dialog.findViewById(R.id.createImageButton);
        ciBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText name=(EditText)dialog.findViewById(R.id.nameSwatch);
                EditText sizA=(EditText)dialog.findViewById(R.id.widthSwatch);
                EditText sizB=(EditText)dialog.findViewById(R.id.heightSwatch);
                if(name.getText().toString().trim().equals("")){
                    mActivity=mainActivity;
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(ctx.getString(R.string.new_image_name_required))
                            .setCancelable(false)
                            .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    mActivity.currentFileName=name.getText().toString().trim();
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }else if(sizA.getText().toString().trim().equals("")||sizB.getText().toString().trim().equals("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(ctx.getString(R.string.new_image_dimensions_required))
                            .setCancelable(false)
                            .setPositiveButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }else {
                    mainActivity.createImage(Integer.valueOf(sizA.getText().toString()), Integer.valueOf(sizB.getText().toString()), null);
                    dialog.dismiss();
                }
            }
        });
    }
}
