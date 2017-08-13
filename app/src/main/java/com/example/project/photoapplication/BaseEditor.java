package com.example.project.photoapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lyft.android.scissors.CropView;

import java.io.File;
import java.nio.IntBuffer;

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
    // Whether the hue slider is visible
    protected boolean isHueSliderVisible = false;
    // The URI of the loaded image
    protected Uri uri;
    // The current value the slider is set to.
    protected float sliderValue;
    // The effect parameter to use in the undo method
    protected float effectParameter;
    //class for filters.
    protected Filter filterInitialiser = new Filter();
    // All available effects are initialised from here
    protected Effects effectHandler;
    // The slider
    protected SeekBar slider;
    //Hue slider
    protected SeekBar hueSlider;
    //hue imageview
    protected ImageView hueView;
    //handler to update hue from UI thread
    protected android.os.Handler hueViewHandler;
    protected boolean isChangedActivity = false;
    //for cropping
    protected CropView cropView;
    protected LinearLayout cropButtons;
    //handler to access crop tool from renderer thread
    protected android.os.Handler cropViewHandler;
    // The current activity context
    protected Context context;
    // The edit history
    protected EditHistory history;
    protected Image images;
    protected boolean redo = false;
    protected boolean redoInit = false;
    protected int redoIndex;
    protected boolean layers = false;

    protected FileManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = new FileManager(this);
    }


    @Override
    protected void onStop(){
        super.onStop();
        images.recycle();
    }

    /*
    Load the textures using the previous image. This method is for use with adjustable
    effects so that the same image is edited upon each change of value instead of applying
    different multipliers sequentially.
     */
    public void loadPreviewTexture() {
//        mImageWidth = images.getPreviousImage().getWidth();
//        mImageHeight = images.getPreviousImage().getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, images.getPreviousImage(), 0);
        GLToolbox.initTexParams();
    }

    /*
    Load the image and bind it to an OpenGL texture.
     */
    public void loadTextures() {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0);
        mImageWidth = images.getImage().getWidth();
        mImageHeight = images.getImage().getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        // Bind to texture - tells OpenGL that subsequent
        // OpenGL calls should affect this texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        //load the bitmap into the bound texture
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, images.getImage(), 0);

        // Set texture parameters
        GLToolbox.initTexParams();
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

    /**
    Render a texture to the screen.
     */
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

    /**
    The actions taken when the canvas is drawn to.
     */
    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            mInitialized = true;
        }
        loadTextures();

            //if adjustable effect
        if(!redo) {
            if (isAdjustableEffect(mCurrentEffect)) {
                if (mCurrentEffect == R.id.hue) {
                    applyHue();
                    loadHuePreview();
                }
                else {
                    loadPreviewTexture();
                    applyEffect(0, 1);
                }
            }
            //else if filter
            else if (isFilter(mCurrentEffect)) {
                loadTextures();
                filterInitialiser.applyFilter(mTextures, 0, 1, mCurrentEffect, mEffectContext, mImageWidth, mImageHeight);
            }
            //else if the effect is not 'none'
            else if (mCurrentEffect != R.id.none) {
                if (mCurrentEffect == R.id.crop) {
                    //show crop tool. Access from UI thread
                    cropViewHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cropView.setVisibility(View.VISIBLE);
                            cropButtons.setVisibility(View.VISIBLE);
                            Toast.makeText(context, "Zoom in, and pan to choose your desired crop region.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    loadTextures();
                    applyEffect(0, 1);
                    effectApplied = true;
                }
            }
        }
        else {
            loadTextures();
            applyEffect(0,1);
        }
        renderResult();
        // Set the image to whatever has been rendered to the screen
        images.setImage(takeScreenshot(gl));

        /*
        update the hue imageView if in adjust/transform/brush/overlay activity, so
        that it contains the latest image everytime an effect is applied.
        isChangedActivity is set to true when switched to another activity.
*/
        if (isChangedActivity && mCurrentEffect != R.id.hue) {
            //update the hueView on the UI thread
            hueViewHandler.post(new Runnable() {
                @Override
                public void run() {
                    hueView.setImageBitmap(images.getImage());
                }
            });
        }

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
    public void save( Bitmap bitmap,  Context context, int index, String type) {
        FileManager fm = new FileManager(context);
        fm.startSave(context, bitmap, index, type);
        if(type.equals("normal")) {
            showToast("File Saved");
        }
    }


    /*
    Take a screenshot of the canvas
     */
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
        prepUndo();
        // Return the images to their original state so that effects will be applied to an unaltered image.
        images.initImages();
        // If the stack is empty then there are no effects to render so just render the original image.
        if (!history.getEffects().empty()) {
            // Iterate across the stack for the amount of objects within it.
            genLayers();
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
                images.setPreviousImage();
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

    protected void applyHue() {
        hueView.getDrawable().setColorFilter(effectHandler.adjustHue(hueSlider.getProgress()));
    }

    public void applyCrop() {
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[1]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, images.getImage(), 0);
        GLToolbox.initTexParams();
    }

    protected Bitmap getHuePreviewBitmap(final View hueImgView) {
        int width = hueImgView.getWidth();
        int height = hueImgView.getHeight();
        Bitmap huePreview = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(huePreview);
        hueImgView.layout(hueImgView.getLeft(), hueImgView.getTop(), hueImgView.getRight(), hueImgView.getBottom());
        hueImgView.draw(c);
        return huePreview;
    }

    //hue bitmap needs to be moved to Images class
    protected void loadHuePreview() {
        Bitmap huePreview = getHuePreviewBitmap(hueView);
        //Bitmap huePreview = ((BitmapDrawable) hueView.getDrawable()).getBitmap();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[1]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, huePreview, 0);
        GLToolbox.initTexParams();
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
            chosenEffect == R.id.hue ||
            chosenEffect == R.id.saturate ||
            chosenEffect == R.id.temperature ||
            chosenEffect == R.id.shadows ||
            chosenEffect == R.id.highlights ||
            chosenEffect == R.id.vignette) {
            return true;
        }
        return false;
    }

    /**
     Check whether the effect passed to the method is a filter.
     @param chosenEffect - the id of the effect to be checked.
     */
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



    public void showOptions(View v){
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.more_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.save:
                        save(images.getImage(), context, 1, "normal");
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
                        createOpenAlert();
                        break;

                    case R.id.Redo:
                        if(history.getRedoEffects().empty() || history.getRedoEffects().size() == history.getEffects().size()){
                            showToast("Nothing to redo!");
                        }
                        else {
                            redo();
                        }
                        break;
                    case R.id.layer:
                        prepLayers();
                        Intent intent = new Intent(context, Layers.class);
                        startActivity(intent);
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

    public void createOpenAlert(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        builder.setTitle("Open")
                .setMessage("Opening a new image will discard all unsaved progress \n Do you want to continue?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        open();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }


    public void prepLayers() {
        File[] files = fm.getFileList(getFilesDir().toString());
        for (File file: files){
            file.delete();
        }
        // Set undo to true so effects applied in the re-render process aren't added to the stack
        undo = true;
        layers = true;
        // Return the images to their original state so that effects will be applied to an unaltered image.
        images.initImages();
        // If the stack is empty then there are no effects to render so just render the original image.
        if (!history.getEffects().empty()) {
            // Iterate across the stack for the amount of objects within it.
            genLayers();
        }
        mCurrentEffect = R.id.none;
        // Done undoing so return undo to false
        undo = false;
        layers = false;
    }

    public void prepUndo(){
        if (!history.checkEmpty()) {
            if (!redoInit) {
                history.initRedo();
                redoInit = true;
            }
            history.popEffect();
            history.popParam();
            redoIndex = history.getEffects().size();
        }
    }

    public void genLayers(){
        for (int i = 0; i <= history.getEffects().size() - 1; i++) {
            // Set the current effect and corresponding parameter to their values at the current index.
            mCurrentEffect = history.getEffects().get(i);
            effectParameter = history.getParam().get(i);
            // Call a render
            mEffectView.requestRender();
            // Set the previous image to the most recent image.
            images.setPreviousImage();
            // temporary fix for race condition
            android.os.SystemClock.sleep(600);
            if (layers){
                save(images.getImage(), context, i, "layer");
            }

        }
    }

    // Getters and setters:

    public void setCurrentEffect(int menuID) {
        mCurrentEffect = menuID;
    }

    public GLSurfaceView getmEffectView() {
        return mEffectView;
    }


    public int getmCurrentEffect() {
        return mCurrentEffect;
    }


    public Uri getUri() {
        return uri;
    }

    public boolean isEffectApplied() {
        return effectApplied;
    }

    public boolean isSliderVisible() {
        return isSliderVisible;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
