package com.example.project.photoapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import java.nio.IntBuffer;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class BaseEditor extends AppCompatActivity implements GLSurfaceView.Renderer {

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

    private boolean effectApplied = false;
    private Bitmap previousImage;

    //private Filter filterInitialiser;
    private Effects effectHandler;
    private SeekBar slider;
    private boolean isSliderVisible = false;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setCurrentEffect(int menuID) {
        mCurrentEffect = menuID;
    }

    public GLSurfaceView getmEffectView() {
        return mEffectView;
    }

    public void setmEffectView(GLSurfaceView mEffectView) {
        this.mEffectView = mEffectView;
    }

    public int[] getmTextures() {
        return mTextures;
    }

    public void setmTextures(int[] mTextures) {
        this.mTextures = mTextures;
    }

    public EffectContext getmEffectContext() {
        return mEffectContext;
    }

    public void setmEffectContext(EffectContext mEffectContext) {
        this.mEffectContext = mEffectContext;
    }

    public TextureRenderer getmTexRenderer() {
        return mTexRenderer;
    }

    public void setmTexRenderer(TextureRenderer mTexRenderer) {
        this.mTexRenderer = mTexRenderer;
    }

    public int getmImageWidth() {
        return mImageWidth;
    }

    public void setmImageWidth(int mImageWidth) {
        this.mImageWidth = mImageWidth;
    }

    public int getmImageHeight() {
        return mImageHeight;
    }

    public void setmImageHeight(int mImageHeight) {
        this.mImageHeight = mImageHeight;
    }

    public boolean ismInitialized() {
        return mInitialized;
    }

    public void setmInitialized(boolean mInitialized) {
        this.mInitialized = mInitialized;
    }

    public int getmCurrentEffect() {
        return mCurrentEffect;
    }

    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getOriginalImage() {
        return originalImage;
    }

    public void setOriginalImage(Bitmap originalImage) {
        this.originalImage = originalImage;
    }

    public Stack<Integer> getHistory() {
        return history;
    }

    public void setHistory(Stack<Integer> history) {
        this.history = history;
    }

    public boolean isEffectApplied() {
        return effectApplied;
    }

    public void setEffectApplied(boolean effectApplied) {
        this.effectApplied = effectApplied;
    }

    public Bitmap getPreviousImage() {
        return previousImage;
    }

    public void setPreviousImage(Bitmap previousImage) {
        this.previousImage = previousImage;
    }

    public Effects getEffectHandler() {
        return effectHandler;
    }

    public void setEffectHandler(Effects effectHandler) {
        this.effectHandler = effectHandler;
    }

    public SeekBar getSlider() {
        return slider;
    }

    public void setSlider(SeekBar slider) {
        this.slider = slider;
    }

    public boolean isSliderVisible() {
        return isSliderVisible;
    }

    public void setSliderVisible(boolean sliderVisible) {
        isSliderVisible = sliderVisible;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


    public void loadPreviewTexture() {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, previousImage, 0);
        GLToolbox.initTexParams();
    }

    public void loadTextures() {
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

    public void applyEffect(int inputTexture, int outputTexture) {
        //side note: for onDrawFrame method, inputTexture = 0, outputTexture = 1
        Effect effect = effectHandler.initEffect(mEffectContext, mCurrentEffect, calculateSliderValue(slider.getProgress()));
        effect.apply(mTextures[inputTexture], mImageWidth, mImageHeight, mTextures[outputTexture]);
    }

    public void renderResult() {
        if (mCurrentEffect != R.id.none) {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[1]);
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

        //if adjustable effect
        if (isAdjustableEffect(mCurrentEffect)) {
            loadPreviewTexture();
            applyEffect(0, 1);
        }

        //else if filter
        if (isFilter(mCurrentEffect)) {
            //nothing yet
        }

        //else if the effect is not 'none'
        else if (mCurrentEffect != R.id.none) {
            applyEffect(0, 1);
            effectApplied = true;
        }

        renderResult();

//        if (effectApplied) {
//            previousImage = takeScreenshot(gl);
//        }
//        effectApplied = false;

        image = takeScreenshot(gl);
    }

    public abstract void save( Bitmap bitmap,  Context context);


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

    public boolean isAdjustableEffect(int chosenEffect) {
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

    public boolean isFilter (int chosenEffect) {
        if (chosenEffect == R.id.alien ||
            chosenEffect == R.id.intenseColours ||
            chosenEffect == R.id.oldFilm) {
            return true;
        }
        return false;
    }

    public void open(){
        Intent intent = new Intent(this, Loader.class);
        startActivity(intent);
    }
    public float calculateSliderValue(int sliderValue){
        float effectValue = (float) sliderValue/50;
        return effectValue;
    }

    protected class SaveThread extends AsyncTask<String, Void, Boolean> {

        Context context;
        Bitmap image;

        public SaveThread(Context context, Bitmap image){
            this.context = context;
            this.image = image;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            FileManager fm = new FileManager(context);
            fm.saveBitmap(image);
            return null;
        }
    }

}
