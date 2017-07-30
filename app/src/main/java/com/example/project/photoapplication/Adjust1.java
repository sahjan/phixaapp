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
        history = new Stack<Integer>();
        historyValues = new Stack<Float>();
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

        findViewById(R.id.but11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPopupAdjust(view);

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
                    historyValues.push(0.0f);
                }
                setCurrentEffect(menuItem.getItemId());
                // Set the previous image to the current image to ensure that multiple changes to the slider
                // without changing effects only ever edit the last image from before selecting this effect.
                previousImage = image;

                if(!undo){
                    history.push(mCurrentEffect);
                }
                mEffectView.requestRender();

                //show slider only when an adjustable effect chosen.
                if (isAdjustableEffect(mCurrentEffect)) {
                    slider.setVisibility(View.VISIBLE);
                    isSliderVisible = true;
                    setSliderProgress();
                }
                //else hide slider
                else if (!isAdjustableEffect(mCurrentEffect) && isSliderVisible)
                {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
                }

                return true;
            }
        });
        popup.show();
    }

    public void save(Bitmap bitmap, Context context){
        SaveThread saver = new SaveThread(context, bitmap);
        saver.execute();
        showToast("File Saved!");

    }

    public void setSliderProgress(){
        Adjust1.this.mEffectView.post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        Adjust1.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Adjust1.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
