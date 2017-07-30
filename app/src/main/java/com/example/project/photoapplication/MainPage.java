package com.example.project.photoapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Stack;

public class MainPage extends BaseEditor implements GLSurfaceView.Renderer {


// CREATE CANVAS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);


        //Initialise the renderer and tell it to only render when Explicit
        //requested with the RENDERMODE_WHEN_DIRTY option
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Initialise all the activity fields to relevant values
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("Image");
        mCurrentEffect = R.id.none;
        context = this;
        history = new EditHistory();

        try {
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), getUri());
            originalImage = image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isEffectApplied()) {
            previousImage = image;
        }

        //filterInitialiser = new Filter();
        effectHandler = new Effects();

        // Assign the slider to its XML counterpart and set its relevant listeners
        slider = (SeekBar) findViewById(R.id.adjustSlider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //queueEvent ensures this occurs in the Renderer thread.
                // When we stop tracking the touch on the slider apply the effect with its parameter and request a render.
                mEffectView.queueEvent(new Runnable() {
                    public void run() {
                        applyEffect(0,1);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[1]);
                        mEffectView.requestRender();
                        sliderValue = calculateSliderValue(slider.getProgress());
                    }
                });
            }
        });


        // BUTTONS...


        // Transform0 Button, when clicked, moves to Transform0 Activity
        findViewById(R.id.but1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Transform0.class);
                intent.putExtra("Image", uri);
                startActivity(intent);
                finish();
            }
        });

        // Adjust Button, when clicked, moves to Adjust Activity
        findViewById(R.id.but2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Adjust1.class);
                intent.putExtra("Image", uri);
                startActivity(intent);
                finish();
            }
        });

        // Brush Button, when clicked, moves to Brush Activity
        findViewById(R.id.but3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Brush2.class);
                intent.putExtra("Image", uri);
                startActivity(intent);
                finish();
            }
        });

        // Overlay Button, when clicked, moves to Overlay Activity
        findViewById(R.id.but4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Overlay3.class);
                intent.putExtra("Image", uri);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });

    }

//        // More FX Button, when clicked, shows More FX Options PopUp
//        findViewById(R.id.but5).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                showPopup*OTHERFX*(view);
//
//            }
//        });
//
//    }


    public void save(Bitmap bitmap, Context context) {
        SaveThread saver = new SaveThread(context, bitmap);
        saver.execute();
        showToast("File Saved");
    }


    public void setSliderProgress(){
        MainPage.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        MainPage.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainPage.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}


