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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.Stack;


/*
The main editor activity. Contains all current editor functionality including transform/adjust effects.
Will be broken apart into seperate activities in the future.
 */

public class EffectsFilterActivity extends BaseEditor implements GLSurfaceView.Renderer {

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
//
//        try {
//            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//            originalImage = image;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        images = new Image(uri, context);

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

        effectHandler = new Effects();

        // Assign the slider to its XML counterpart and set its listener
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

        //assign the hue slider and set its listener. Does nothing yet.
        hueSlider = (SeekBar) findViewById(R.id.hueSlider);
        hueSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //hue image view
        hueView = (ImageView) findViewById(R.id.hueView);
        hueView.setImageBitmap(images.getImage());

        // Set the onclick listeners for all the buttons in the activity.
        // All show the popups with relevant functionalities.
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
     * @param v - The view that the popup is created on.
     */
    public void showPopupTransform(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.transform);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                history.clearRedo();
                redoInit = false;
                if(!undo) {
                    // If its not an adjustable effect just push a no value float to the stack so that
                    // the parameters line up with the effect in the history
                    if (!isAdjustableEffect(menuItem.getItemId())) {
                        history.pushParam(0.0f);
                    }
                }
                setCurrentEffect(menuItem.getItemId());
                if(!undo){
                    // Push the selected effect to the history stack
                    history.pushEffect(mCurrentEffect);
                }
                // render the requested effect.
                mEffectView.requestRender();

                //hide the slider upon choosing an option from here as it is not required.
                if (isSliderVisible) {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
                }
                if (isHueSliderVisible) {
                    hueSlider.setVisibility(View.GONE);
                    isHueSliderVisible = false;
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
                history.clearRedo();
                redoInit = false;
                if(history.getRedoEffects().empty()){
                    Log.e("Tag", "empty");
                }
                if (!isAdjustableEffect(menuItem.getItemId())) {
                    history.pushParam(0.0f);
                }
                setCurrentEffect(menuItem.getItemId());
                // Set the previous image to the current image to ensure that multiple changes to the slider
                // without changing effects only ever edit the last image from before selecting this effect.
                images.setPreviousImage();

                if(!undo){
                    history.pushEffect(mCurrentEffect);
                }
                mEffectView.requestRender();

                //show slider only when an adjustable effect chosen.
                if (mCurrentEffect == R.id.hue) {
                    if(isSliderVisible) {
                        slider.setVisibility(View.GONE);
                        isSliderVisible = false;
                    }
                    hueSlider.setVisibility(View.VISIBLE);
                    isHueSliderVisible = true;
                }
                else if (isAdjustableEffect(mCurrentEffect)) {
                    if (isHueSliderVisible) {
                        hueSlider.setVisibility(View.GONE);
                        isHueSliderVisible = false;
                    }
                    slider.setVisibility(View.VISIBLE);
                    setSliderProgress();
                    //only need to do this once if 2 adjustable effects chosen consecutively
                    if (!isSliderVisible) {
                        isSliderVisible = true;
                    }
                }
                //else hide slider
                else if (!isAdjustableEffect(mCurrentEffect))
                {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
                    hueSlider.setVisibility(View.GONE);
                    isHueSliderVisible = false;
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
                    history.pushParam(0.0f);
                }

                setCurrentEffect(menuItem.getItemId());
                if(!undo){
                    history.pushEffect(getmCurrentEffect());
                }
                mEffectView.requestRender();

                //hide the slider upon choosing an option from here
                if (isSliderVisible) {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
                }
                if (isHueSliderVisible) {
                    hueSlider.setVisibility(View.GONE);
                    isHueSliderVisible = false;
                }

                return true;
            }
        });
        popup.show();
    }

    /*
    Implements the abstract save method of the superclass. Saves the file passed to it in a background thread
    then tells the user once this has been executed.
    @param bitmap - The image to save to a file.
    @param context - The context of the current activity.
     */
    public void save( Bitmap bitmap,  Context context){
        // create a new AsyncTask to save the image in the background to prevent UI lockup and executes it.
        SaveThread saver = new SaveThread(context, bitmap);
        saver.execute();
        // Tell the user that the file is saved.
        showToast("File Saved!");
    }

    /*
    Set the slider back to the mid point.
     */
    public void setSliderProgress(){
        EffectsFilterActivity.this.mEffectView.post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        EffectsFilterActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(EffectsFilterActivity.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

