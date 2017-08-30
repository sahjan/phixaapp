package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This activity gets all the images in the apps public storage directory and loads the 6 most recent
 * to image views that can be clicked on to load that image
 */
public class RecentImages extends AppCompatActivity {
    private FileManager fm = new FileManager(this);
    private File[] files;
    ArrayList<Uri> uris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_images);

        ImageButton recent1 = (ImageButton) findViewById(R.id.recent1);
        ImageButton recent2 = (ImageButton) findViewById(R.id.recent2);
        ImageButton recent3 = (ImageButton) findViewById(R.id.recent3);
        ImageButton recent4 = (ImageButton) findViewById(R.id.recent4);
        ImageButton recent5 = (ImageButton) findViewById(R.id.recent5);
        ImageButton recent6 = (ImageButton) findViewById(R.id.recent6);

        files = fm.getFileList(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/Saved_Images");
        uris = convertToUri();
        
        try {
            if (checkSize(0)) {
                recent1.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(0)));
                recent1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMainPage(uris.get(0));
                    }
                });
            }
            if (checkSize(1)) {
                recent2.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(1)));
                recent2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMainPage(uris.get(1));
                    }
                });
            }
            if (checkSize(2)) {
                recent3.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(2)));
                recent3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMainPage(uris.get(2));
                    }
                });
            }
            if (checkSize(3)) {
                recent4.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(3)));
                recent4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMainPage(uris.get(3));
                    }
                });
            }
            if (checkSize(4)) {
                recent5.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(4)));
                recent5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMainPage(uris.get(4));
                    }
                });
            }
            if (checkSize(5)) {
                recent6.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(5)));
                recent6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startMainPage(uris.get(5));
                    }
                });
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Converts files in the files arraylist to their proper URI's
     * @return
     */
    public ArrayList<Uri> convertToUri(){
        ArrayList<Uri> f = new ArrayList<>();
        // iterate through all files in the array and convert the file to an official URI
        if(files != null){
            for (int i = 0; i < files.length; i++){
                Uri uri = Uri.parse(files[i].toURI().toString());
                f.add(uri);
            }
        }
        // Reverse the list so that the most recent images are first in the list
        Collections.reverse(f);
        return f;
    }


    /**
     * Start the main page activity and send the selected image to it
     * @param image
     */
    public void startMainPage(Uri image){
        Intent intent = new Intent(getApplicationContext(), MainPage.class);
        intent.putExtra("Image", image);
        startActivity(intent);
        finish();
    }

    /**
     * Checks if the uris array is bigger than the index passed to it
     * @param index
     * @return
     */
    public Boolean checkSize(int index){
        if(uris.size() > index) {
            return true;
        }
        else{
            return false;
        }
    }


}
