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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Stack;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/*
Superclass for all editor activities. This activity provides all necessary methods for setup of the open gl canvas
and utility methods needed for editing operations such as Undo. The save method is abstract as part of the method
requires a class name that so far we have been unable to pass as a class name successfully.
subclasses must implement their own oncreate methods to complete set up of the canvas and display an image.
 */
public abstract class BaseEditor extends AppCompatActivity implements GLSurfaceView.Renderer {


    // Fields relating to the open GL operations
    protected GLSurfaceView mEffectView;
    protected EffectContext mEffectContext;
    protected TextureRenderer mTexRenderer = new TextureRenderer();
    protected int[] mTextures = new int[2];
    protected int mImageWidth;
    protected int mImageHeight;
    // The currently selected effect
    protected int mCurrentEffect;
    // Whether the GLview has been initialised
    protected boolean mInitialized = false;
    // Whether the activity is currently undoing an effect
    protected boolean undo = false;
    // Whether the effect has been succesfully applied
    protected boolean effectApplied = false;
    // Whether the adjustment slider is visible
    protected boolean isSliderVisible = false;
    // The URI of the loaded image
    protected Uri uri;
    // The most recently rendered image, may have an effect applied to it.
    protected Bitmap image;
    // Will always contain the original image loaded
    protected Bitmap originalImage;
    // The previous image to be rendered
    protected Bitmap previousImage;
    // The current value the slider is set to.
    protected float sliderValue;
    // The effect parameter to use in the undo method
    protected float effectParameter;
    private Filter filterInitialiser = new Filter();
    // All available effects are initialised from here
    protected Effects effectHandler;
    // The slider
    protected SeekBar slider;
    // The current activity context
    protected Context context;
    // The edit history
    protected EditHistory history;

    protected boolean redo = false;
    protected boolean redoInit = false;
    protected int redoIndex;

    protected static final String TAG = "RedoTag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
    Load the textures using the previous image. This method is for use with adjustable effects so that the same image is edited
    upon each change of value instead of applying different multipliers sequentially.
     */
    public void loadPreviewTexture() {
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, previousImage, 0);
        GLToolbox.initTexParams();
    }

    /*
    Load the image and bind it to an OpenGL texture.
     */
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

    public void updateTexture(Bitmap image){
        // Bind to texture - tells OpenGL that subsequent
        // OpenGL calls should affect this texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        //load the bitmap into the bound texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, image, 0);

    }

    /*
    This method creates an effect, initialises it to the desired effect and then applies it to the input texture.
    The output is then given in the output texture in mTextures
    @param InputTexture - The texture to which the effect should be applied.
    @param outputTexture - The texture that results from applying to the input texture.
     */
    public void applyEffect(int inputTexture, int outputTexture) {
        Effect effect;

        // If we aren't undoing we can init the effect using the current slider value and also add the parameter
        // to the stack of parameters.
        if(!undo && !redo) {
            effect = effectHandler.initEffect(mEffectContext, mCurrentEffect, calculateSliderValue(slider.getProgress()));
            // Only add to the stack if its an adjustable effect.
            if(isAdjustableEffect(mCurrentEffect)) {
                // Check that there are the same amount of effects as parameters
                if (history.getEffects().size() > history.getParam().size()) {
                    history.pushParam(calculateSliderValue(slider.getProgress()));
                    // If they aren't equal then we have changed the slider multiple times in the same effect.
                    // In this case remove the most recent value and replace it with the current value.
                } else {
                    history.popParam();
                    history.pushParam(calculateSliderValue(slider.getProgress()));
                }
            }
        }
        // If we are undoing initialise the effect with the parameter set in the undo method.
        else {
            effect = effectHandler.initEffect(mEffectContext, mCurrentEffect, effectParameter);
        }
        effect.apply(mTextures[inputTexture], mImageWidth, mImageHeight, mTextures[outputTexture]);


    }

    /*
    Render a texture to the screen.
     */
    public void renderResult() {
        Log.e(TAG, Integer.toString(mCurrentEffect));
        if (mCurrentEffect != R.id.none) {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[1]);
        }
        else {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    /*
    The actions taken when the canvas is drawn to.
     */
    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures();
            mInitialized = true;
        }

            //if adjustable effect
        if(!redo) {
            if (isAdjustableEffect(mCurrentEffect)) {
                loadPreviewTexture();
                applyEffect(0, 1);
            }
            //else if filter
            else if (isFilter(mCurrentEffect)) {
                loadTextures();
                filterInitialiser.applyFilter(mTextures, 0, 1, mCurrentEffect, mEffectContext, mImageWidth, mImageHeight);
            }
            //else if the effect is not 'none'
            else if (mCurrentEffect != R.id.none) {
                loadTextures();
                applyEffect(0, 1);
                effectApplied = true;
            }
        }
        else {
            loadTextures();
            applyEffect(0,1);
        }

        renderResult();
        // Set the image to whatever has been rendered to the screen
        image = takeScreenshot(gl);

        // These two effects produce results inconsistent with the rest of the effects when using redo
        // Because of this we have to make sure redo is set to false at the end of the rendering cycle
        // when these two effects are in use.
        if(mCurrentEffect == R.id.brightness || mCurrentEffect == R.id.contrast){
            redo = false;

        }

    }

    /*
    The save method, every subclass must implement a way of saving files.
    @param bitmap - The image to save as a file
    @param context - the context of the activity calling the method
     */
    public abstract void save( Bitmap bitmap,  Context context);


    /*
    Take a screenshot of the canvas
     */
    public Bitmap takeScreenshot(GL10 mGL) {
        final int mWidth = mEffectView.getWidth();
        final int mHeight = mEffectView.getHeight();
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        IntBuffer ibt = IntBuffer.allocate(mWidth * mHeight);
        // Believe the suspected race condition within the undo method is caused by glreadpixels.
        // Method is an 'incoherent' method in the OpenGL memory model and so does not guarantee that
        // changes made to a variable will be visible by successor accesses
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

    /*
    Undo algorithm for non destructive editing. Takes the stored stacks of effects and parameters
     removes the most recent one and sequentially reapplies them to get the image minus the most
     recent effect applied.
     Known issue with this method with a potential race condition. When run in debugger works fine
     but when run normally has unreliable results and appears to not update the image to use in rendering.
     Believe I have pinpointed this to the takescreenshot methods (specifically the glreadpixels part)
     results not being visible. Temporary fix in place by waiting for a fraction of a second at the end
     of each loop.
     */
    public void undo() {
        // Set undo to true so effects applied in the re-render process aren't added to the stack
        undo = true;
        // If both the effect and parameters stack have items within them pop an effect and its corresponding parameter.
        if(!history.checkEmpty()) {
            if(!redoInit){
                history.initRedo();
                redoInit = true;
            }
            history.popEffect();
            history.popParam();
            redoIndex = history.getEffects().size();
        }
        // Return the images to their original state so that effects will be applied to an unaltered image.
        image = originalImage;
        previousImage = originalImage;
        // If the stack is empty then there are no effects to render so just render the original image.
        if (!history.getEffects().empty()) {
            // Iterate across the stack for the amount of objects within it.
            for (int i = 0; i <= history.getEffects().size() - 1; i++) {
                // Set the current effect and corresponding parameter to their values at the current index.
                mCurrentEffect = history.getEffects().get(i);
                effectParameter = history.getParam().get(i);
                // Call a render
                mEffectView.requestRender();
                // Set the previous image to the most recent image.
                previousImage = image;
                // temporary fix for race condition
                android.os.SystemClock.sleep(600);

            }
        } else {
            mCurrentEffect = R.id.none;
            mEffectView.requestRender();
        }
        mCurrentEffect = R.id.none;
        // Done undoing so return undo to false
        undo = false;
    }

    public void redo(){

                redo = true;
                mCurrentEffect = history.getRedoEffects().get(redoIndex);
                effectParameter = history.getRedoParams().get(redoIndex);
                history.pushRedo(mCurrentEffect, effectParameter);
                mEffectView.requestRender();
                previousImage = image;
                redoIndex++;

                // For this method not to cause potential crashes or for it to actually work when used with the below two effects
                // We have to set redo to false only in the ondraw frame method.
                // However if you do that with other effects the redo doesn't work
                // so for other effects redo has to be set to false here
                if(mCurrentEffect == R.id.brightness || mCurrentEffect == R.id.contrast){
                    //do nothing
                }
                else {
                    redo = false;
                }
            }




    /**
    Check whether the effect passed to the method is an adjustable effect.
    @param chosenEffect - the id of the effect to be checked.
     */
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

    // Start the loader activity to load a new photo.
    public void open(){
        Intent intent = new Intent(this, Loader.class);
        startActivity(intent);
    }

    // Convert the slider values of 0-100 to numbers that equate with the correct values for the effect parameters
    public float calculateSliderValue(int sliderValue){
        float effectValue = (float) sliderValue/50;
        return effectValue;
    }

    // An AsyncTask to conduct saving on a seperate thread to ensure the UI does not lock up while the save is in progress.
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

    public void showOptions(View v){
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.more_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.save:
                        save(getImage(), getContext());
                        break;

                    case R.id.undo:
                        // tell the user when there are no effects to undo.
                        if(history.checkEmpty()) {
                            showToast("Nothing to Undo!");

                        }
                        undo();

                        slider.setVisibility(View.INVISIBLE);
                        setSliderProgress();
                        break;

                    case R.id.open:
                        open();
                        break;

                    case R.id.Redo:
                        if(history.getRedoEffects().empty() || history.getRedoEffects().size() == history.getEffects().size()){
                            showToast("Nothing to redo!");
                        }
                        else {
                            redo();
                        }
                        break;

                }
                return true;
            }
        });
        popup.show();
    }

    public void resetRedo(){
        history.clearRedo();
        redoInit = false;
    }

    public abstract void showToast(String toastSting);

    public abstract void setSliderProgress();

    // Getters and setters:

    public float getSliderValue() {
        return sliderValue;
    }

    public void setSliderValue(float sliderValue) {
        this.sliderValue = sliderValue;
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

//    public Stack<Integer> getHistory() {
//        return history;
//    }
//
//    public void setHistory(Stack<Integer> history) {
//        this.history = history;
//    }

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

//    public Stack<Float> getHistoryValues() {
//        return historyValues;
//    }
//
//    public void setHistoryValues(Stack<Float> historyValues) {
//        this.historyValues = historyValues;
//    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

}
