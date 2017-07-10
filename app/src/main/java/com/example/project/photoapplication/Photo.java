package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Photo class contains a bitmap to be used for editing operations.
 * upon creation it takes an image passed to it in the form of a file path
 * and then decodes this file into a bitmap which it then stores.
 */

public class Photo {

    private Bitmap bitmap;
    private String imageName;
    private Uri filePath;


    // constructor
    public Photo(Uri path, Context context) throws IOException {
        filePath = path;
        bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), path);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Uri getURI(){return filePath;}
}
