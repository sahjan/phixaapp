package com.example.project.photoapplication;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;

/**
 * The editor activity that contains all adjustment style effects
 */
public class AdjustActivity extends BaseEditor implements GLSurfaceView.Renderer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_adjust1);

        hueViewHandler = new Handler();
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

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

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
                        sliderValue = EditUtils.calculateSliderValue(slider.getProgress());
                    }
                });
            }
            });

        //hue image view
        hueView = (ImageView) findViewById(R.id.hueView);
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

        setButtons();

        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });
    }

    /**
     * Sets effect depending on the button pressed
     * @param chosenEffect the effect chosen, as int (menu ID)
     */
    private void setChosenEffect(int chosenEffect) {
        resetRedo();
        if (!EditUtils.isAdjustableEffect(chosenEffect)) {
            history.pushParam(0.0f);
        }
        setCurrentEffect(chosenEffect);
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
            hueContainer.setVisibility(View.VISIBLE);
            isHueSliderVisible = true;
        }
        else if (EditUtils.isAdjustableEffect(mCurrentEffect)) {
            if (isHueSliderVisible) {
                hueContainer.setVisibility(View.GONE);
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
            hueContainer.setVisibility(View.GONE);
            isHueSliderVisible = false;
        }
    }

    /**
     * This method sets onClickListeners for each button.
     */
    private void setButtons() {
        findViewById(R.id.autofixImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.autofix);
            }
        });

        findViewById(R.id.brightnessImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.brightness);
            }
        });

        findViewById(R.id.contrastImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.contrast);
            }
        });

        findViewById(R.id.hueImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.hue);
            }
        });

        findViewById(R.id.saturationImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.saturate);
            }
        });

        findViewById(R.id.highlightsImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.highlights);
            }
        });

        findViewById(R.id.shadowsImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.shadows);
            }
        });

        findViewById(R.id.filllightImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.filllight);
            }
        });

        findViewById(R.id.fisheyeImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.fisheye);
            }
        });

        findViewById(R.id.duotoneImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.duotone);
            }
        });

        findViewById(R.id.grainImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.grain);
            }
        });

        findViewById(R.id.temperatureImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.temperature);
            }
        });

        findViewById(R.id.tintImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.tint);
            }
        });

        findViewById(R.id.topBut).setVisibility(View.INVISIBLE);
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
        mCurrentEffect = R.id.none;
        super.onResume();
    }

    public void setSliderProgress(){
        AdjustActivity.this.mEffectView.post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        AdjustActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(AdjustActivity.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
