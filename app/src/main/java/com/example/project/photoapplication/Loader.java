package com.example.project.photoapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

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
        isStoragePermissionGranted();


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
                openCamera();

            }
        });


        ImageButton recent = (ImageButton) findViewById(R.id.recentButton);

        recent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRecent();
            }
        });



    }


    /**
     * Open the devices public picture gallery to select an image to use with the editor activities
     */
    private void openGallery() {
        Intent gallery =
                // open an intent to select some data
                new Intent(Intent.ACTION_PICK,
                        // populate the activity with images from the media store
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        // start the activity to get a picture back
        startActivityForResult(gallery, PICK_IMAGE);

    }


    /**
     * Start the camera to take a picture for use by the editor activities
     */
    private void openCamera() {
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
                // Output the result of the camera to the file at this URI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Open the recent images gallery
     */
    private void openRecent(){
        Intent intent = new Intent(this, RecentImages.class);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            Uri imageUri = data.getData();
            Intent intent = new Intent(getApplicationContext(), MainPage.class);
            intent.putExtra("Image", imageUri);
            startActivity(intent);

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_TAKE_PHOTO) {
            Intent intent = new Intent(getApplicationContext(), MainPage.class);
            intent.putExtra("Image", cameraPath);
            startActivity(intent);
        }
    }

    /**
     * Check permissions -
     * Get permissions that are required if they are not in place already
      */

        public  boolean isStoragePermissionGranted() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Log.v("TAG","Permission is granted");
                    return true;
                } else {

                    Log.v("TAG","Permission is revoked");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    return false;
                }
            }
            else { //permission is automatically granted on sdk<23 upon installation
                Log.v("TAG","Permission is granted");
                return true;
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.v("TAG","Permission: "+permissions[0]+ "was "+grantResults[0]);
                //resume tasks needing this permission
            }
        }



    }


