package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import javax.microedition.khronos.opengles.GL;

public class Transform0 extends BaseEditor implements GLSurfaceView.Renderer{

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
        history = new EditHistory();
        images = new Image(uri, context);

        //set the crop tool
        cropView = (CropView) findViewById(R.id.cropView);
        cropView.setImageBitmap(images.getImage());

        //confirm and cancel buttons
        cropButtons = (LinearLayout) findViewById(R.id.cropButtons);
        ImageButton confirmButton = (ImageButton) findViewById(R.id.confirmCrop);
        ImageButton cancelButton = (ImageButton) findViewById(R.id.cancelCrop);

        //button listeners
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropView.setVisibility(View.GONE);
                cropButtons.setVisibility(View.GONE);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap croppedBitmap = cropView.crop();
                images.setImage(croppedBitmap);
                cropView.setVisibility(View.GONE);
                cropButtons.setVisibility(View.GONE);
                //confirmedCrop = true;
            }
        });

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
                        sliderValue = calculateSliderValue(slider.getProgress());
                    }
                });
            }
        });

        findViewById(R.id.but01).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupTransform(view);

            }
        });

        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mEffectView.onPause();
        //mEffectView.setPreserveEGLContextOnPause(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEffectView.onResume();
    }

    /**
     * Transform0 menu
     * @param v
     */
    public void showPopupTransform(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.transform);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                resetRedo();
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
                if (isSliderVisible()) {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
                }

                return true;
            }
        });
        popup.show();
    }

    public void setSliderProgress(){
        Transform0.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        Transform0.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Transform0.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
