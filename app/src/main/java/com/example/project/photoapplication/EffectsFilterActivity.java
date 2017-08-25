package com.example.project.photoapplication;

import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;


/*
The main editor activity. Contains all current editor functionality including transform/adjust effects.
Will be broken apart into seperate activities in the future.
 */

public class EffectsFilterActivity extends BaseEditor implements GLSurfaceView.Renderer {


    private int index;
    private int[] effect;
    private float[] params;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layouteffectseditor);

         //Initialise the renderer and tell it to only render when Explicit
         //requested with the RENDERMODE_WHEN_DIRTY option
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Initialise all the activity fields to relevant values
        final Intent intent = getIntent();
        mCurrentEffect = R.id.none;
        context = this;
        history = intent.getParcelableExtra("History");
        // Add a null effect to the history stack so that we can increment the index
        // so that the next layer gets added to the rigth of the layer you clicked at and doesnt give us a null P E
        history.pushEffect(R.id.none);
        history.pushParam(0.0f);
        uri = intent.getParcelableExtra("Image");
        index = intent.getIntExtra("Index", 0);
        index ++;
        effect = new int[1];
        params = new float[1];

        images = new Image(uri, this);

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
                        sliderValue = EditUtils.calculateSliderValue(slider.getProgress());
                        params[0] = sliderValue;
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
        hueView.setImageBitmap(images.getPreviousImage());

        // Set the onclick listeners for all the buttons in the activity.
        // All show the popups with relevant functionalities.


        findViewById(R.id.transformImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupTransform(view);

            }
        });

        findViewById(R.id.adjustImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupAdjust(view);

            }
        });

        findViewById(R.id.overlayImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupOverlay(view);

            }
        });
//        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showOptions(view);
//            }
//        });
        findViewById(R.id.Accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Effect", Integer.toString(effect[0]));
                history.addLayer(index, effect[0], params[0]);
                history.removeLayer(history.getEffects().size()-1);
                prepLayers();
                Intent i = new Intent(context, Layers.class);
                i.putExtra("History", history);
                startActivity(i);
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
                    if (!EditUtils.isAdjustableEffect(menuItem.getItemId())) {
                        params[0] = 0.0f;
                    }
                }
                setCurrentEffect(menuItem.getItemId());
                if(!undo){
                    // Push the selected effect to the history stack
                    effect[0] = mCurrentEffect;
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
                if (!EditUtils.isAdjustableEffect(menuItem.getItemId())) {
                    params[0] = 0.0f;
                }
                setCurrentEffect(menuItem.getItemId());
                // Set the previous image to the current image to ensure that multiple changes to the slider
                // without changing effects only ever edit the last image from before selecting this effect.
                images.setPreviousImage();

                if(!undo){
                    effect[0] = mCurrentEffect;
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
                else if (EditUtils.isAdjustableEffect(mCurrentEffect)) {
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
                else if (!EditUtils.isAdjustableEffect(mCurrentEffect))
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
     * Overlay menu
     * @param v
     */
    public void showPopupOverlay(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.overlay);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (!EditUtils.isAdjustableEffect(menuItem.getItemId())) {
                    params[0] = 0.0f;
                }

                setCurrentEffect(menuItem.getItemId());
                if(!undo){
                    effect[0] = mCurrentEffect;
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

