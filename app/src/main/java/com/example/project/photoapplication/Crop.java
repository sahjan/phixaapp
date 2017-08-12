package com.example.project.photoapplication;

import android.content.Intent;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import com.steelkiwi.cropiwa.CropIwaView;

/**
 * Created by Sahjan on 11/08/2017.
 */

public class Crop extends BaseEditor implements GLSurfaceView.Renderer {

    private CropIwaView cropView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.crop_activity);

        isChangedActivity = true;

        //Initialise the renderer and tell it to only render when Explicit
        //requested with the RENDERMODE_WHEN_DIRTY option
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Initialise all the activity fields to relevant values
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("Images");

        //set the crop tool
        cropView = (CropIwaView) findViewById(R.id.crop_view);
        cropView.setImageUri(uri);

        mCurrentEffect = R.id.none;
        context = this;
        history = new EditHistory();
        images = new Image(uri, context);

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

    }

    public void setSliderProgress(){
        Crop.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        Crop.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Crop.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
