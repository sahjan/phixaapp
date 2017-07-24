package com.example.project.photoapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Stack;

public class Adjust1 extends BaseEditor implements GLSurfaceView.Renderer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_adjust1);
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
                        applyEffect(0,1);
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

        findViewById(R.id.but11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPopupAdjust(view);

            }
        });

    }

    /**
     * Adjust menu
//     * @param v
     */
    public void showPopupAdjust(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.adjust);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setCurrentEffect(menuItem.getItemId());
                setPreviousImage(getImage());

                if(isUndo() == false){
                    getHistory().push(getmCurrentEffect());
                }
                getmEffectView().requestRender();

                //show slider only when an adjustable effect chosen.
                if (isAdjustableEffect(getmCurrentEffect())) {
                    getSlider().setVisibility(View.VISIBLE);
                    setSliderVisible(true);
                    getmEffectView().post(new Runnable() {
                        @Override
                        public void run() {
                            getSlider().setProgress(50);
                        }
                    });

                }
                //else hide slider
                else if (!isAdjustableEffect(getmCurrentEffect()) && isSliderVisible())
                {
                    getSlider().setVisibility(View.GONE);
                    setSliderVisible(false);
                }

                return true;
            }
        });
        popup.show();
    }



    public void save(Bitmap bitmap, Context context){
        SaveThread saver = new SaveThread(context, bitmap);
        saver.execute();
        Adjust1.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Adjust1.this, "File Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
