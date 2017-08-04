package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by Ed on 03/08/2017.
 * Stores all images used by the editors and handles operations to update them.
 */

public class Image {

    private Bitmap image;
    private Bitmap previousImage;
    private final Bitmap originalImage;


    public Image(Uri path, Context context){
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(context.getContentResolver(), path);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        originalImage = b;
        initImages();



    }

    public void initImages(){
        image = originalImage;
        previousImage = originalImage;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getPreviousImage() {
        return previousImage;
    }

    public void setPreviousImage() {
        previousImage = image;
    }

    public void recycle(){
        image.recycle();
        previousImage.recycle();
        originalImage.recycle();
    }
}
