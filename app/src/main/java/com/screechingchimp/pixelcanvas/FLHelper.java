package com.screechingchimp.pixelcanvas;

import android.app.AlertDialog;
import android.content.Context;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import android.content.DialogInterface;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.os.Environment;

import java.io.InputStream;
import java.util.ArrayList;

import android.view.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

import android.graphics.Movie;

import androidx.core.app.ActivityCompat;

public class FLHelper extends View{

	private Context ctx;
	private static MainActivity mActivity;

	private static final String SAVE_DIR = "PixelCanvas";
	private static final String CACHE_DIR = "cache";
  	public static final String UNDO_FOLDER ="undo";
  	public static final String FRAME_FOLDER ="frame";
	public static File DATA_ROOT;

	//version of database

	private static final String TAG = "PixelCanvas";

	public FLHelper(Context context) {
		super(context);
		this.ctx = context;
        mActivity=(MainActivity) context;
				DATA_ROOT = mActivity.getFilesDir();
	}

	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;
		try {
			File file = new File(DATA_ROOT, path);
			if (!file.exists()) {
				if (!file.mkdirs()) {
					Log.e("TravellerLog :: ", "Problem creating Image folder");
					ret = false;
				}
			}
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
		return ret;
	}

	public void listFiles(){
		String file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/";
		//Log.d("Files", "Path: " + file_path);
		File f = new File(file_path);
		File file[] = f.listFiles();
		//Log.d("Files", "Size: "+ file.length);
		for (int i=0; i < file.length; i++)
		{
			Log.d("Files", "FileName:" + file[i].getName());
		}
	}

	public void importGif(String filename){
		ArrayList<Bitmap> frmGif = new ArrayList<Bitmap>();
		ArrayList<Integer> frmSpd = new ArrayList<Integer>();
		frmSpd=mActivity.fileHelper.decodeGifSpeed(filename);
		if(frmGif!=null) {
			int ww = frmGif.get(0).getWidth();
			int hh = frmGif.get(0).getHeight();
			mActivity.createImage(ww, hh, frmGif.get(0));
			int cnt = frmGif.size();
			mActivity.frameSpeedArray.clear();
			for (int i = 0; i < cnt; i++) {
				mActivity.framesList.addItem(frmGif.get(i));
				mActivity.frameSpeedArray.add(frmSpd.get(i));
			}
			if (mActivity.isAnimating) {
				mActivity.playPreview();
			}
		}
	}

	public void encodeGif( ArrayList<Bitmap> bitmapArray,ArrayList<Integer> speedArray,String filename){
		int cnt=bitmapArray.size();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + SAVE_DIR + "/";
		createDirIfNotExists(path);

		File dst=new File(path+filename+".gif");
		try {
			OutputStream out = new FileOutputStream(dst, false);

			GifEncoder gifEncoder = new GifEncoder();
			gifEncoder.setTransparent(0x00000000);
            //gifEncoder.setQuality(10);
			gifEncoder.start(bos);
			for (int i = 0; i < cnt; i++) {
                gifEncoder.setDelay(speedArray.get(i));
                gifEncoder.addFrame(bitmapArray.get(i));
			}
			gifEncoder.finish();

			out.write(bos.toByteArray());
			out.flush();
			out.close();

		}catch(Throwable e){
			Log.d(TAG,e.toString());
		}
	}

	public ArrayList<Integer> decodeGifSpeed(String filename){
		ArrayList<Integer> intArray = new ArrayList<Integer>();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + SAVE_DIR + "/";
		//String path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/";
		createDirIfNotExists(path);

		//Log.d(TAG,"test");

		File src=new File(path+filename);
		try {
			InputStream in = new FileInputStream(src);
			GifDecoder gifDecoder = new GifDecoder();
			gifDecoder.read(in);

			int cnt = gifDecoder.getFrameCount();
			//Log.d(TAG, "frame count: " + cnt);
			for (int i = 0; i < cnt; i++) {
				//gifDecoder.advance();
				int delay = gifDecoder.getDelay(i);
				intArray.add(delay);
			}

		}catch(Throwable e){
			Log.d(TAG, "Failed: " + e.toString());
		}

		return  intArray;
	}

	public ArrayList<Bitmap> decodeGif(String filename){
		ArrayList<Bitmap> bitmapArray = new ArrayList<Bitmap>();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + SAVE_DIR + "/";
		//String path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/";
		createDirIfNotExists(path);
        if(filename.indexOf(".gif")>-1) {
            File src = new File(path + filename);
            try {
                InputStream in = new FileInputStream(src);
                GifDecoder gifDecoder = new GifDecoder();
                gifDecoder.read(in);
                gifDecoder.bgColor = 0x00000000;
                int cnt = gifDecoder.getFrameCount();
                for (int i = 0; i < cnt; i++) {
                    //gifDecoder.advance();
                    Bitmap bitmap = Bitmap.createBitmap(gifDecoder.getFrame(i).copy(Bitmap.Config.ARGB_8888, true));
                    int delay = gifDecoder.getDelay(i);
                    bitmapArray.add(bitmap);
                }
            } catch (Throwable e) {
                Log.d(TAG, "Failed: " + e.toString());
            }
        }else{
            bitmapArray=null;
            AlertDialog alert;
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(ctx.getString(R.string.not_gif))
                    .setCancelable(false)
                    .setNegativeButton(ctx.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alert = builder.create();
            alert.show();
        }

		return bitmapArray;
	}

	public static void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst,false);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		//out.flush();
		out.close();
		in.close();
	}

	public static void deleteFile(File file){
		try {
			if (file.exists()){
				file.delete();
			}
		} catch(Throwable e) {
			Log.d(TAG,"Failed to clear DB");
		}
	}


	private static boolean checkFileExist(File file) {
		return file.exists();
	}

	public static void clearCache(String folder){
		try {
            String file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR;
            if(folder!=null||folder.trim().equals("")){
                file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR + "/" + folder;
            }
			File dir = new File(file_path);
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i = 0; i < children.length; i++) {
					new File(dir, children[i]).delete();
				}
			}
		}catch(Throwable e){
			Log.d(TAG,e.toString());
		}
	}

	public static void clearUndoCache(){
		try {
			String file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR + "/"+UNDO_FOLDER;
			File dir = new File(file_path);
			if (dir.isDirectory()) {
				String[] children = dir.list();
				for (int i = 0; i < children.length; i++) {
					new File(dir, children[i]).delete();
				}
			}
		}catch(Throwable e){
			Log.d(TAG,e.toString());
		}
	}

    public static void removeCachedBitmap(String folder,String filename){
        //try {
        String file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR;
        if(folder!=null||folder.trim().equals("")){
            file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR + "/" + folder;
        }

        File file = new File(file_path,  filename);
        if(checkFileExist(file)) {
            file.delete();
        }

    }

	public static Bitmap loadCachedBitmap(String folder,String filename){
		//try {
        String file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR;
        if(folder!=null||folder.trim().equals("")){
            file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR + "/" + folder;
        }

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = mActivity.getConfig();
        options.inMutable=true;
		Bitmap bitmap = BitmapFactory.decodeFile(file_path+"/"+filename, options);

		//}catch(Throwable e){
			//Log.d(TAG,e.toString());
		//}
		return bitmap;
	}

	public static String saveCachedBitmap(Bitmap bmp,String folder,String filename){


		String returnPath="";

        String file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR;
        if(folder!=null||folder.trim().equals("")){
            file_path = DATA_ROOT.getAbsolutePath() + "/" +  SAVE_DIR + "/" + CACHE_DIR + "/" + folder;
        }

		createDirIfNotExists(file_path);

		try {
			File dir = new File(file_path);
			if (!dir.exists())
				dir.mkdirs();
			File file = new File(dir,  filename);
			FileOutputStream fOut = new FileOutputStream(file);

			bmp.compress(Bitmap.CompressFormat.PNG, 85, fOut);
			fOut.flush();
			fOut.close();
		}catch(Throwable e){
			Log.d(TAG,e.toString());
		}

		return filename;
	}

}
