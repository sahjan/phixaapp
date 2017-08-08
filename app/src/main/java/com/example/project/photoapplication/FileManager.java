package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The FileManager handles the saving of all images that the application creates.
 */

    public class FileManager {


        private Context context;
        private String mostRecentPath;


        public FileManager(Context context) {
            this.context = context;


        }

        /*
        Saves a bitmap to a file and then send it in an outputstream.
        Scan the system so that the image is available to the gallery
         */
        public void saveBitmap(Bitmap bitmap) {
            // Create a file with the correct file path for the directory.
            File dir = getAlbumStorageDir();
            // Get a unique filename
            String filename = createName() + ".jpg";
            // Create a new file with the correct parent directory and filename
            File file = new File(dir, filename);
            FileOutputStream out;
            try {
                // Attach our file to an outputstream and compress it.
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                // Send the file to storage and close the stream.
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Scan the system to ensure the image is available to everyone.
            scanSystem(file);

        }

        /*
        Generates a file with the correct directory to save to. If the directory does not exist then create it.
         */
        public File getAlbumStorageDir() {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Saved_Images");
            // If it doesn't exist, make it
            if (!file.mkdirs()) {
                Log.e("LOG_TAG", "Directory not created");
            }
            return file;
        }

        /*
        Method used for loading an image via the camera
        @return File - The image file saved from the camera.
         */
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

        /*
        Create a unique name for a file based on the time.
        @return filename - the string that will form the unique filename for a file.
         */
        public String createName() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String filename = formatter.format(now); //+".JPEG";
            return filename;
        }

        public String getMostRecentPath() {
            return mostRecentPath;
        }

        /*
        Scan the system so the image is available.
        @param file - the file to scan for
         */
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

        /*
        Get a list of files in the directory represented by the string path
        @return File[] - return an array of all files in the folder.
         */
        public File[] getFileList(String path){

            File directory = new File(path);
            directory.mkdirs();
            // Create an array of all the files in the directory
            File[] files = directory.listFiles();
            return files;
        }




        public void saveLayer(int index, Bitmap image){
            String filename = "Layer" + index;
            File file = new File(context.getFilesDir(), filename);
            FileOutputStream out = null;
            try {
            out = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG, 50, out);
                out.flush();
                out.close();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    public void startSave(Context context, Bitmap bitmap, int index, String type){
        SaveThread saver = new SaveThread(context, bitmap, index, type);
        saver.execute();
    }


    //An AsyncTask to conduct saving on a seperate thread to ensure the UI does not lock up while the save is in progress.
    protected class SaveThread extends android.os.AsyncTask<String, Void, Boolean> {

        Context context;
        Bitmap image;
        int layerIndex;
        String type;

        public SaveThread(Context context, Bitmap image, int layerIndex, String type){
            this.context = context;
            this.image = image;
            this.layerIndex = layerIndex;
            this.type = type;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            FileManager fm = new FileManager(context);
            switch (type) {
                case "normal":
                    fm.saveBitmap(image);
                    break;

                case "layer":
                    fm.saveLayer(layerIndex, image);
                    break;
            }
            return true;
        }
    }






}


