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
        /*
         * Initialise the renderer and tell it to only render when Explicit
         * requested with the RENDERMODE_WHEN_DIRTY option
         */
        Intent intent = getIntent();
        setUri((Uri) intent.getParcelableExtra("Image"));

        setmEffectView((GLSurfaceView) findViewById(R.id.effectsview));
        getmEffectView().setEGLContextClientVersion(2);
        getmEffectView().setRenderer(this);
        getmEffectView().setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        setCurrentEffect(R.id.none);
        setContext(this);
        setHistory(new Stack<Integer>());
        try {
            setImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), getUri()));
            setOriginalImage(getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isEffectApplied()) {
            setPreviousImage(getImage());
        }

        setEffectHandler(new Effects());
        setSlider((SeekBar) findViewById(R.id.adjustSlider));
        getSlider().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
                //queueEvent ensures this occurs in the Renderer thread.
                getmEffectView().queueEvent(new Runnable() {
                    public void run() {
                        applyEffect(0, 1);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getmTextures()[1]);
                        getmEffectView().requestRender();
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        // BUTTONS...


        // Transform0 Button, when clicked, moves to Transform0 Activity
        findViewById(R.id.but1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Transform0.class);
                intent.putExtra("Image", getUri());
                startActivity(intent);
                finish();
            }
        });

        // Adjust Button, when clicked, moves to Adjust Activity
        findViewById(R.id.but2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Adjust1.class);
                intent.putExtra("Image", getUri());
                startActivity(intent);
                finish();
            }
        });

        // Brush Button, when clicked, moves to Brush Activity
        findViewById(R.id.but3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Brush2.class);
                intent.putExtra("Image", getUri());
                startActivity(intent);
                finish();
            }
        });

        // Overlay Button, when clicked, moves to Overlay Activity
        findViewById(R.id.but4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Overlay3.class);
                intent.putExtra("Image", getUri());
                startActivity(intent);
                finish();
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

    // Shows Drop Down Menu upon clicking '...' button in top right of page
    public void showOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.more_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.save:
                        save(getImage(), getContext());


                        break;

//                        case R.id.undo:
//                            undo();
//                            break;

                    case R.id.open:
                        open();

                }
                return true;
            }
        });
        popup.show();
    }

    public void save(Bitmap bitmap, Context context) {
        SaveThread saver = new SaveThread(context, bitmap);
        saver.execute();
        MainPage.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainPage.this, "File Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}


