package com.example.project.photoapplication;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class RecentImages extends AppCompatActivity {
    private FileManager fm = new FileManager(this);
    private File[] files;

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

        files = fm.getFileList();
        ArrayList<Uri> uris = convertToUri();
        if(uris.size() > 0) {
            try {
                recent1.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(0)));
                recent2.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(1)));
                recent3.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(2)));
                recent4.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(3)));
                recent5.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(4)));
                recent6.setImageBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), uris.get(5)));
                Double d = 0.2;
                recent1.setScaleX(d.floatValue());
                recent1.setScaleY(d.floatValue());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
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




}
