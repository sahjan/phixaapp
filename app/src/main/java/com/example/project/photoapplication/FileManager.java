package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ed on 11/07/2017.
 */

public class FileManager {


    private Context context;


    public FileManager(Context context) {
        this.context = context;


    }

    public void saveBitmap(Bitmap bitmap){

        File dir = getAlbumStorageDir();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date now = new Date();
        String filename = formatter.format(now) +".PNG";
        File file = new File(dir, filename);
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    }


    public File getAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Saved_Images");
        if (!file.mkdirs()) {
            Log.e("LOG_TAG", "Directory not created");
        }
        return file;
    }
}


