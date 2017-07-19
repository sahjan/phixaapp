package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Ed on 11/07/2017.
 */

    public class FileManager {


        private Context context;
        private String mostRecentPath;


        public FileManager(Context context) {
            this.context = context;


        }

        public void saveBitmap(Bitmap bitmap) {

            File dir = getAlbumStorageDir();
            String filename = createName() + ".jpg";
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
            scanSystem(file);

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

//    public File saveCameraFile() {
//        File storageDir = getAlbumStorageDir();
//        String name = createName();
//        File image = new File(storageDir, name);
//
//        mostRecentPath = image.getAbsolutePath();
//        return image;
//
//    }

        public File createImageFile() throws IOException {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mostRecentPath = image.getAbsolutePath();
            return image;
        }

        public String createName() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String filename = formatter.format(now); //+".JPEG";
            return filename;
        }

        public String getMostRecentPath() {
            return mostRecentPath;
        }

        public void scanSystem(File file) {
            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

        }

        public File[] getFileList(){
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Saved_Images";
            File directory = new File(path);
            directory.mkdirs();
            File[] files = directory.listFiles();
            return files;
        }





    }


