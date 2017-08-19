package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.IOException;

public class LayerEditorMainPage extends AppCompatActivity {

    private Uri path;
    private Bitmap image;
    private EditHistory history;
    private int index;
    private ImageView view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer_editor_main_page);
        Intent intent = getIntent();
        path = intent.getParcelableExtra("Image");
//        history = intent.getParcelableExtra("History");
//        index = intent.getIntExtra("Index", 0);
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        image = b;
        view = (ImageView) findViewById(R.id.layerImageView);
        view.setImageBitmap(image);
    }
}
