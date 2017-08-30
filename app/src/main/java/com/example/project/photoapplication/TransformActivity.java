package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lyft.android.scissors.CropView;

import java.io.File;

public class TransformActivity extends BaseEditor implements GLSurfaceView.Renderer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transform0);

        cropViewHandler = new Handler();

        hueViewHandler = new Handler();
        //hue image view
        hueView = (ImageView) findViewById(R.id.hueView);
        isChangedActivity = true;

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
        history = intent.getParcelableExtra("History");
        images = new Image(uri, context, history);

        //set the crop tool
        cropView = (CropView) findViewById(R.id.cropView);
        //confirm and cancel buttons
        cropButtons = (LinearLayout) findViewById(R.id.cropButtons);
        ImageButton confirmButton = (ImageButton) findViewById(R.id.confirmCrop);
        ImageButton cancelButton = (ImageButton) findViewById(R.id.cancelCrop);
        //button listeners
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                images.setImage(cropView.getImageBitmap());
                cropView.setVisibility(View.GONE);
                cropButtons.setVisibility(View.GONE);
                mCurrentEffect = R.id.none;
                mEffectView.queueEvent(new Runnable() {
                    public void run() {
                        mEffectView.requestRender();
                    }
                });
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap croppedBitmap = cropView.crop();
                images.setImage(croppedBitmap);
                cropView.setVisibility(View.GONE);
                cropButtons.setVisibility(View.GONE);
                mCurrentEffect = R.id.none;
                //REQUEST THE RENDER HERE
                mEffectView.queueEvent(new Runnable() {
                    public void run() {
                        applyCrop();
                        mEffectView.requestRender();
                    }
                });
            }
        });

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

        effectHandler = new Effects();

        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });

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
                    }
                });
            }
        });

        //set onclick listeners for the buttons
        findViewById(R.id.cropImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropView.setImageBitmap(images.getImage());
                setChosenEffect(R.id.crop);
            }
        });

        findViewById(R.id.horizflipImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.fliphor);
            }
        });

        findViewById(R.id.vertflipImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.flipvert);
            }
        });

        findViewById(R.id.rotateImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.rotate);
            }
        });

        //hue image view
        hueView.setImageBitmap(images.getImage());
        //hue container
        hueContainer = (LinearLayout) findViewById(R.id.hueSliderContainer);
        //assign the hue slider and set its listener.

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
                //request a render of the hue change
                mEffectView.queueEvent(new Runnable() {
                    public void run() {
                        applyHue();
                        loadHuePreview();
                        mEffectView.requestRender();
                    }
                });
            }
        });

        findViewById(R.id.topBut).setVisibility(View.INVISIBLE);
    }



    /**
     * sets the chosen effect depending on the button clicked
     * @param chosenEffect the chosen effect
     */
    public void setChosenEffect(int chosenEffect) {
        resetRedo();
        if(!undo) {
            // If its not an adjustable effect just push a no value float to the stack so that
            // the parameters line up with the effect in the history
            if (!EditUtils.isAdjustableEffect(chosenEffect)) {
                history.pushParam(0.0f);
            }
        }
        setCurrentEffect(chosenEffect);
        if(!undo){
            // Push the selected effect to the history stack
            history.pushEffect(mCurrentEffect);
        }
        // render the requested effect.
        mEffectView.requestRender();

        //hide the slider upon choosing an option from here as it is not required.
        if (isSliderVisible()) {
            slider.setVisibility(View.GONE);
            isSliderVisible = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (images.getImage().isRecycled()) {
            Log.e("Recycled", "XX");
            images = new Image(uri, context);
        }
        mEffectView.onResume();
        mCurrentEffect = R.id.none;
        super.onResume();
    }

    public void setSliderProgress(){
        TransformActivity.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        TransformActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(TransformActivity.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
