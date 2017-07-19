package com.example.project.photoapplication;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.Environment.getExternalStorageState;

public class EffectsFilterActivity extends Activity implements GLSurfaceView.Renderer {

    private GLSurfaceView mEffectView;
    private int[] mTextures = new int[2];
    private EffectContext mEffectContext;
    private TextureRenderer mTexRenderer = new TextureRenderer();
    private int mImageWidth;
    private int mImageHeight;
    private boolean mInitialized = false;
    private int mCurrentEffect;
    private boolean undo = false;
    private Uri uri;
    private Bitmap image;
    private Bitmap originalImage;
    private Stack<Integer> history;
    private int previousEffect = 0;

    private boolean effectApplied = false;
    private Bitmap previousImage;

    private Filter filterInitialiser;
    private Effects effectHandler;
    private SeekBar slider;
    private boolean isSliderVisible = false;

    public void setCurrentEffect(int menuID) {
        mCurrentEffect = menuID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        /**
         * Initialise the renderer and tell it to only render when Explicit
         * requested with the RENDERMODE_WHEN_DIRTY option
         */
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("Image");

        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mCurrentEffect = R.id.none;
        history = new Stack<>();
        try {
            image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            originalImage = image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!effectApplied) {
            previousImage = image;
        }

        filterInitialiser = new Filter();

        effectHandler = new Effects();
        slider = findViewById(R.id.slider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
                //queueEvent ensures this occurs in the Renderer thread.
                mEffectView.queueEvent(new Runnable() {
                    public void run() {
                        applyEffect(0,1);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[1]);
                        mEffectView.requestRender();
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                previousEffect = mCurrentEffect;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


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
     * @param v
     */
    public void showPopupTransform(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.transform);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setCurrentEffect(menuItem.getItemId());
                if(undo == false){
                    history.push(mCurrentEffect);
                }
                mEffectView.requestRender();

                //hide the slider upon choosing an option from here
                if (isSliderVisible) {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
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
                setCurrentEffect(menuItem.getItemId());
                if(undo == false){
                    history.push(mCurrentEffect);
                }
                mEffectView.requestRender();

                //show slider only when an adjustable effect chosen.
                if (isAdjustableEffect(mCurrentEffect) && !isSliderVisible) {
                    slider.setVisibility(View.VISIBLE);
                    isSliderVisible = true;
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
                setCurrentEffect(menuItem.getItemId());
                if(!undo){
                    history.push(mCurrentEffect);
                }
                mEffectView.requestRender();

                //hide the slider upon choosing an option from here
                if (isSliderVisible) {
                    slider.setVisibility(View.GONE);
                    isSliderVisible = false;
                }

                return true;
            }
        });
        popup.show();
    }

    public void showOptions(View v){
            PopupMenu popup = new PopupMenu(this, v);
            popup.inflate(R.menu.more_options);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()){
                        case R.id.save:
                            save(image);
                            save(image);

                            //disable the button to prevent multiple accidental saves which can crash the app
                            findViewById(R.id.moreOpt).setEnabled(false);

                            Timer buttonTimer = new Timer();
                            buttonTimer.schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {

                                        @Override
                                        public void run() {
                                            findViewById(R.id.moreOpt).setEnabled(true);
                                        }
                                    });
                                }
                            }, 5000);
                            break;

//                        case R.id.undo:
//                            undo();
//                            break;

                        case R.id.open:
                            open();

                    }
                    return true;
                }
    });
    popup.show();
    }
    
    private void loadPreviewTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, previousImage, 0);
        GLToolbox.initTexParams();
    }

    private void loadTextures() {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0);

        mImageWidth = image.getWidth();
        mImageHeight = image.getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

        // Bind to texture - tells OpenGL that subsequent
        // OpenGL calls should affect this texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        //load the bitmap into the bound texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

        // Set texture parameters
        GLToolbox.initTexParams();
    }

    private void applyEffect(int inputTexture, int outputTexture) {
        //side note: for onDrawFrame method, inputTexture = 0, outputTexture = 1
        Effect effect = effectHandler.initEffect(mEffectContext, mCurrentEffect, calculateSliderValue(slider.getProgress()));
        effect.apply(mTextures[inputTexture], mImageWidth, mImageHeight, mTextures[outputTexture]);
    }

    private void renderResult() {
        if (mCurrentEffect != R.id.none) {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[1]);
        }
        /* else if (mCurrentEffect == R.id.oldFilm) {
            //render the filter applied
            mTexRenderer.renderTexture(mTextures[1]);
        } */
        else {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    //Renderer override
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
        }
            loadTextures();
            mInitialized = true;

        //do this if the effect chosen is an adjustable one
        if (isAdjustableEffect(mCurrentEffect)) {

            loadPreviewTexture();
            applyEffect(0, 1);

            if(previousEffect != mCurrentEffect && previousEffect != 0){
                previousImage = takeScreenshot(gl);
            }
        }
        //else if the effect is non-adjustable, and not 'none'
        else if (mCurrentEffect != R.id.none) {
            applyEffect(0, 1);
            effectApplied = true;
        }

        //filter. Unfinished
        /* else if (mCurrentEffect == R.id.oldFilm) {
            //if filter chosen, apply the filter.
            ArrayList<Effect> oldFilm = filterInitialiser.getOldFilmFilter(mEffectContext);
            for (Effect component : oldFilm) {
                component.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
            }
            effectApplied = true;
        } */

            renderResult();

        if (effectApplied) {
            previousImage = takeScreenshot(gl);
        }
        effectApplied = false;

        image = takeScreenshot(gl);
    }

    public void save(Bitmap bitmap){
        FileManager fm = new FileManager(this);
        fm.saveBitmap(bitmap);
        Context context = getApplicationContext();
        CharSequence text = "File Saved!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

    public Bitmap takeScreenshot(GL10 mGL) {
        final int mWidth = mEffectView.getWidth();
        final int mHeight = mEffectView.getHeight();
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
        mGL.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                ibt.put((mHeight - i - 1) * mWidth + j, ib.get(i * mWidth + j));
            }
        }

        Bitmap mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        mBitmap.copyPixelsFromBuffer(ibt);
        return mBitmap;
    }

    //Renderer override
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    //Renderer override
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    public void undo() {
        undo = true;
        if(!history.empty()) {
            history.pop();
        }
        image = originalImage;
        if (!history.empty()) {
            for (int i = 0; i <= history.size() - 1; i++) {
                mCurrentEffect = history.get(i);
                mEffectView.requestRender();
            }
        } else {
            mCurrentEffect = R.id.none;
            mEffectView.requestRender();
        }
        undo = false;
    }

    private boolean isAdjustableEffect(int chosenEffect) {
        if (chosenEffect == R.id.brightness ||
            chosenEffect == R.id.contrast ||
            chosenEffect == R.id.filllight ||
            chosenEffect == R.id.fisheye ||
            chosenEffect == R.id.grain ||
            chosenEffect == R.id.saturate ||
            chosenEffect == R.id.temperature ||
            chosenEffect == R.id.vignette) {
            return true;
        }
        else {
            return false;
        }
    }

    private void open(){
        Intent intent = new Intent(this, Loader.class);
        startActivity(intent);
    }
    private float calculateSliderValue(int sliderValue){
        float effectValue = (float) sliderValue/50;
        return effectValue;
    }


}

