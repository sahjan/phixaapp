package com.example.project.photoapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

/**
 * The home page for all editing operations
 */
public class MainPage extends BaseEditor implements GLSurfaceView.Renderer {

    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

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
        if (!mInitialized){
            EditUtils.clearPrivateStorage(context, "brush");
        }

        // If we have a history extra we are starting the activity from a layer and need to
        // create the image with the original image stored in the edit history
        if(intent.hasExtra("History")){
            history = intent.getParcelableExtra("History");
            images = new Image(uri, context, history);
            Log.e("Old", "xx");
        }
        else {
            history = new EditHistory(uri);
            images = new Image(uri, context);
            Log.e("New", "xx");
        }

        ImageButton layers = (ImageButton) findViewById(R.id.layers);
        layers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(history.checkEmpty()){
                    showToast("No Effects Applied!");
                }
                else {
                    prepLayers();
                    Intent layers = new Intent(MainPage.this, Layers.class);
                    history.putImage("finalImage", uri);
                    layers.putExtra("History", history);
                    startActivity(layers);
                    images.recycle();
                }
            }
        });



        final AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
        builder.setTitle("Exit editor")
                .setMessage("All your changes will be lost. Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        images.recycle();
                        Intent loader = new Intent(context, Loader.class);
                        startActivity(loader);
                        images.recycle();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

        effectHandler = new Effects();

        // BUTTONS

        //Face button, moves to face activity
        findViewById(R.id.faceImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, FaceActivity.class);
                intent.putExtra("Image", uri);
                intent.putExtra("History", history);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Transform Button, when clicked, moves to transform activity
        findViewById(R.id.transformImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, TransformActivity.class);
                intent.putExtra("Image", uri);
                intent.putExtra("History", history);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Adjust Button, when clicked, moves to adjust Activity
        findViewById(R.id.adjustImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, AdjustActivity.class);
                intent.putExtra("Image", uri);
                intent.putExtra("History", history);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Brush Button, when clicked, moves to drawing Activity
        findViewById(R.id.brushImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Drawing.class);
                intent.putExtra("Image", uri);
                intent.putExtra("History", history);
                intent.putExtra("BrushIndex", brushindex);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Filter button, when clicked, moves to Filter Activity
        findViewById(R.id.overlayImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, FilterActivity.class);
                intent.putExtra("Image", uri);
                intent.putExtra("History", history);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        //keeps app from crashing when pressing back button too soon
        //after applying an effect (on my tablet)
        if (images.getImage() == null) {
            images = new Image(uri, context);
        }
        //keeps app from crashing when reopening after exiting or locking the device.
        else if (images.getImage().isRecycled()) {
            images = new Image(uri, context);
        }
        mEffectView.onResume();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        dialog.show();
    }

    public void setSliderProgress(){
        MainPage.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        MainPage.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainPage.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Intent intent = data;
                uri = intent.getParcelableExtra("Image1");
                history = intent.getParcelableExtra("History");
                images = new Image(uri, context, history);
                if(intent.hasExtra("brushIndex")){
                    brushindex = intent.getIntExtra("brushIndex", 0);
                }
                mEffectView.requestRender();
            }
        }
    }

}


