package com.example.project.photoapplication;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;


public class Loader extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    static final int REQUEST_TAKE_PHOTO = 1;
    private FileManager fm = new FileManager(this);
    private Uri cameraPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loader);
        ImageButton load = (ImageButton) findViewById(R.id.openButton);


        load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();


            }
        });

        ImageButton camera = (ImageButton) findViewById(R.id.camButton);

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();


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

//    private void openCamera(){
//        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        // Ensure that there's a camera activity to handle the intent
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            // Create the File for the photo
//            File photoFile = null;
//
//
//                photoFile = fm.saveCameraFile();
//
//            if(photoFile != null) {
//                Uri photoURI = Uri.fromFile(photoFile);
//                cameraPath = photoURI;
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
//                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
//            }
//
//        }
//
//    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = fm.createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                cameraPath = photoURI;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            Intent intent = new Intent(getApplicationContext(), EffectsFilterActivity.class);
            intent.putExtra("Image", imageUri);
            startActivity(intent);

        }
        else if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            Intent intent = new Intent(getApplicationContext(), EffectsFilterActivity.class);
            intent.putExtra("Image", cameraPath);
            startActivity(intent);
        }



    }




}
