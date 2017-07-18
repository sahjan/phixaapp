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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

import static android.os.Environment.getExternalStorageState;

public class EffectsFilterActivity extends Activity implements GLSurfaceView.Renderer {

    private GLSurfaceView mEffectView;
    private int[] mTextures = new int[4];
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

    private boolean effectApplied = false;
    private Bitmap previousImage;

    private Effects effectHandler;
    private SeekBar slider;
    private boolean isSliderVisible = false;

    public void setCurrentEffect(int effect) {
        mCurrentEffect = effect;
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

        effectHandler = new Effects();
        slider = findViewById(R.id.slider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
                //queueEvent ensures this occurs in the Renderer thread.
                mEffectView.queueEvent(new Runnable() {
                    public void run() {
                        applyEffect(2,3);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[3]);
                        mEffectView.requestRender();
                        //restore the original image so the effect
                        //is not additively applied
                        loadPreviewTexture();
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //Button effect = new Button(this);
       // effect.setText("effect");
       // this.addContentView(effect,
               // new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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


//        LinearLayout test = new LinearLayout(this);
//        Button save = new Button(this);
//        save.setText("Save");
//        test.addView(save);
//        test.setGravity(Gravity.BOTTOM | Gravity.CENTER_VERTICAL);
//        this.addContentView(test,
//                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
//
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                save(image);
//
//
//            }
//        });
//
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

                /* //show the slider when an effect is chosen. Only
                //do this once.
                //if(mCurrentEffect != R.id.none && !isSliderVisible) { */

                //show slider only when brightness chosen.
                if (mCurrentEffect == R.id.brightness) {
                    slider.setVisibility(View.VISIBLE);
                    isSliderVisible = true;
                }

                /* TEMPORARY */
                //hide slider when another effect chosen
                if (mCurrentEffect != R.id.brightness) {
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

    /**
     * this method is for loading the preview texture as
     * well as updating it back to the original image
     * when the effect parameter is changed and needs
     * to be applied.
     * Prevents slider from additively applying the effect.
     */
    private void loadPreviewTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[2]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, previousImage, 0);
        GLToolbox.initTexParams();
        // don't know the parameters for this
        // method but it is recommended when updating a texture
        //GLES20.glTexSubImage2D();
    }


    private void loadTextures() {
        // Generate textures
        GLES20.glGenTextures(4, mTextures, 0);

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
        Effect effect = effectHandler.initEffect(mEffectContext, mCurrentEffect, slider.getProgress());
        effect.apply(mTextures[inputTexture], mImageWidth, mImageHeight, mTextures[outputTexture]);
    }

    private void renderResult() {
        if (mCurrentEffect != R.id.none && mCurrentEffect != R.id.brightness) {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[1]);
        }
        //render the result of brightness applied
        //on the current image
        else if (mCurrentEffect == R.id.brightness) {
            mTexRenderer.renderTexture(mTextures[3]);
        }
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

        if (mCurrentEffect != R.id.none && mCurrentEffect != R.id.brightness) {
            //if an effect is chosen apply it to the texture
            applyEffect(0, 1);
            effectApplied = true;
        }
        else if (mCurrentEffect == R.id.brightness) {
            //apply the effect on the current image.
            loadPreviewTexture();
            applyEffect(2, 3);
        }
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


}

