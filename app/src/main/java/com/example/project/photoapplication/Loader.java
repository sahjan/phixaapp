package com.example.project.photoapplication;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;


public class Loader extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private Photo photo ;
    private Button launch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);
        Button load = (Button) findViewById(R.id.load);
        Button launch = (Button) findViewById(R.id.launch);


        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();


            }
        });
        launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(photo != null) {
                    Intent intent = new Intent(getApplicationContext(), EffectsFilterActivity.class);
                    intent.putExtra("Image", photo.getURI());
                    startActivity(intent);
                }


            }
        });
    }

    private void openGallery() {
        Intent gallery =
                // open an intent to select some data
                new Intent(Intent.ACTION_PICK,
                        // populate the activity with images from the media store
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        // start the activity to get a picture back
        startActivityForResult(gallery, PICK_IMAGE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();

            try {
                photo = new Photo(imageUri, this);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }




}
