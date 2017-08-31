package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Stores all images used by the editors and handles operations to update them.
 */
public class Image {

    private Bitmap image;
    private Bitmap previousImage;
    private final Bitmap originalImage;


    /**
     * Constructor for creation of an entirely fresh Image object
     * @param path
     * @param context
     */
    public Image(Uri path, Context context) {
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(context.getContentResolver(), path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        originalImage = b;
        initImages();
    }

    /**
     * Constructor for creating an Image object when effects have already been applied
     * and we need to regain the original image
     * @param path
     * @param context
     * @param history
     */
    public Image(Uri path, Context context, EditHistory history){
        Bitmap b = null;
        Bitmap o = null;
        try {
            b = MediaStore.Images.Media.getBitmap(context.getContentResolver(), path);
            o = MediaStore.Images.Media.getBitmap(context.getContentResolver(), history.getImages().get("OriginalImage"));
        } catch (IOException e) {
            e.printStackTrace();

        }
        originalImage = o;
        image = b;
        previousImage = b;
    }


    /**
     * Initialise all images so that they are set to the base image
     */
    public void initImages() {
        image = originalImage;
        previousImage = originalImage;
    }

    /**
     * Recycle all the images to regain memory
     */
    public void recycle() {
        image.recycle();
        previousImage.recycle();
        originalImage.recycle();
    }


    // Getters and setters

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

    public Bitmap getOriginalImage(){
        return originalImage;
    }



}
