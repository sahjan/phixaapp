package com.example.project.photoapplication;

import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * Editor activity that performs all Filter effects
 */
public class FilterActivity extends BaseEditor implements GLSurfaceView.Renderer{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filters);

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

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

        effectHandler = new Effects();

        //set onclick listeners for the buttons
        setFilterButtons();

        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });

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

        findViewById(R.id.topBut).setVisibility(View.INVISIBLE);
    }

    /**
     * set mCurrent effect depending on the button pressed
     * @param chosenEffect the effect chosen
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
        mCurrentEffect = R.id.none;
        super.onResume();
    }

    /**
     * Set onClickListeners for the buttons.
     */
    private void setFilterButtons() {
        findViewById(R.id.alienButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.alien);
            }
        });

        findViewById(R.id.crossprocessButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.crossprocess);
            }
        });

        findViewById(R.id.documentaryButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.documentary);
            }
        });

        findViewById(R.id.grayscaleButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.grayscale);
            }
        });

        findViewById(R.id.intenseColoursButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.intenseColours);
            }
        });

        findViewById(R.id.lomoishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.lomoish);
            }
        });

        findViewById(R.id.negativeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.negative);
            }
        });

        findViewById(R.id.oldFilmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.oldFilm);
            }
        });

        findViewById(R.id.posterizeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.posterize);
            }
        });

        findViewById(R.id.sepiaButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.sepia);
            }
        });

        findViewById(R.id.vignetteButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChosenEffect(R.id.vignette);
            }
        });
    }

    public void setSliderProgress(){
        FilterActivity.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        FilterActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(FilterActivity.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
