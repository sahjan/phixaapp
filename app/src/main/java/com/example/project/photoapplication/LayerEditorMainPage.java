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
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import static android.R.attr.button;


/*
The main editor activity. Contains all current editor functionality including transform/adjust effects.
Will be broken apart into seperate activities in the future.
 */

public class LayerEditorMainPage extends BaseEditor implements GLSurfaceView.Renderer {


    private int index;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layereditor);

        //Initialise the renderer and tell it to only render when Explicit
        //requested with the RENDERMODE_WHEN_DIRTY option
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Initialise all the activity fields to relevant values
        Intent intent = getIntent();
        mCurrentEffect = R.id.none;
        context = this;
        history = intent.getParcelableExtra("History");
        uri = intent.getParcelableExtra("Image");
        index = intent.getIntExtra("Index", 0);
        Log.e("Layer Editor Main Page Index=", Integer.toString(index));

        images = new Image(uri, this, history);

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

        effectHandler = new Effects();


        Button a = (Button) findViewById(R.id.Delete);
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.removeLayer(index);
                prepLayers();
                Log.e("History", Integer.toString(history.getEffects().size()));
                Intent i = new Intent(context, Layers.class);
                i.putExtra("History", history);
                startActivity(i);
            }
        });

        Button b = (Button) findViewById(R.id.Add);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, EffectsFilterActivity.class);
                i.putExtra("History", history);
                i.putExtra("Index", index);
                i.putExtra("Image", uri);
                startActivity(i);

            }
        });



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
                    }
                });
            }
        });

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
        LayerEditorMainPage.this.mEffectView.post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        LayerEditorMainPage.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LayerEditorMainPage.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

