package com.example.project.photoapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.effect.EffectContext;
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

public class EffectsFilterActivity extends BaseEditor implements GLSurfaceView.Renderer {




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        /**
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
        setHistoryValues(new Stack<Float>());
        try {
            setImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), getUri()));
            setOriginalImage(getImage());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!isEffectApplied()) {
            setPreviousImage(getImage());
        }

        //filterInitialiser = new Filter();

        setEffectHandler(new Effects());
        setSlider((SeekBar) findViewById(R.id.adjustSlider));
        getSlider().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //queueEvent ensures this occurs in the Renderer thread.
                getmEffectView().queueEvent(new Runnable() {
                    public void run() {
                        applyEffect(0,1);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getmTextures()[1]);
                        getmEffectView().requestRender();
                        setSliderValue(calculateSliderValue(getSlider().getProgress()));
                    }
                });
            }
        });


        findViewById(R.id.but1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupTransform(view);

            }
        });

        findViewById(R.id.but2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupAdjust(view);

            }
        });

        findViewById(R.id.but3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupBrush(view);

            }
        });
        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });
        
    }


    /**
     * Transform menu
     * @param v
     */
    public void showPopupTransform(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.transform);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(!isUndo()) {
                    if (!isAdjustableEffect(menuItem.getItemId())) {
                        getHistoryValues().push(0.0f);
                    }
                }
                setCurrentEffect(menuItem.getItemId());
                if(isUndo() == false){

                    getHistory().push(getmCurrentEffect());
                }
                getmEffectView().requestRender();

                //hide the slider upon choosing an option from here
                if (isSliderVisible()) {
                    getSlider().setVisibility(View.GONE);
                    setSliderVisible(false);
                }

                return true;
            }
        });
        popup.show();
    }

    /**
     * Adjust menu
     * @param v
     */
    public void showPopupAdjust(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.adjust);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!isAdjustableEffect(menuItem.getItemId())) {
                    getHistoryValues().push(0.0f);
                }
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
                    setSliderProgress();
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

    /**
     * Brush menu
     * @param v
     */
    public void showPopupBrush(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.brush);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if (!isAdjustableEffect(menuItem.getItemId())) {
                    getHistoryValues().push(0.0f);
                }

                setCurrentEffect(menuItem.getItemId());
                if(!isUndo()){
                    getHistory().push(getmCurrentEffect());
                }
                getmEffectView().requestRender();

                //hide the slider upon choosing an option from here
                if (isSliderVisible()) {
                    getSlider().setVisibility(View.GONE);
                    setSliderVisible(false);
                }

                return true;
            }
        });
        popup.show();
    }

    public void showOptions(View v){
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.more_options);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.save:
                            save(getImage(), getContext());
                            break;

                        case R.id.undo:
                            undo();
                            getSlider().setVisibility(View.INVISIBLE);
                            setSliderProgress();
                            break;

                        case R.id.open:
                            open();

                    }
                    return true;
                }
    });
    popup.show();
    }

    public void save( Bitmap bitmap,  Context context){
        SaveThread saver = new SaveThread(context, bitmap);
        saver.execute();
        EffectsFilterActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(EffectsFilterActivity.this, "File Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setSliderProgress(){
        EffectsFilterActivity.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                getSlider().setProgress(50);
            }
        });
    }

}

