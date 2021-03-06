package com.example.project.photoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import com.lyft.android.scissors.CropView;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
Superclass for all GLeditor activities.
 This activity provides all necessary methods for setup of the open gl canvas
and utility methods needed for editing operations such as Undo.
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
    protected LinearLayout hueContainer;
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
    // indexes for use with handling the brush tools effects specially within undo/redo
    protected int brushindex = 0;
    protected int brushRedoIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fm = new FileManager(this);
    }


    @Override
    protected void onStop(){
        super.onStop();
    }

    /**
    * Load the textures using the previous image. This method is for use with adjustable
    * effects so that the same image is edited upon each change of value instead of applying
    * different multipliers sequentially.
     */
    public void loadPreviewTexture() {
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, images.getPreviousImage(), 0);
        GLToolbox.initTexParams();
    }

    /**
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

    /**
    This method creates an effect, initialises it to the desired effect and then applies it to the input texture.
    The output is then given in the output texture in mTextures
    @param inputTexture - The texture to which the effect should be applied.
    @param outputTexture - The texture that results from applying to the input texture.
     */
    public void applyEffect(int inputTexture, int outputTexture) {
        Effect effect;

        // If we aren't undoing we can init the effect using the current slider value and also add the parameter
        // to the stack of parameters.
        if(!undo && !redo) {
                effect = effectHandler.initEffect(mEffectContext, mCurrentEffect, EditUtils.calculateSliderValue(slider.getProgress()));
            // Only add to the stack if its an adjustable effect.
            if(EditUtils.isAdjustableEffect(mCurrentEffect)) {
                // Check that there are the same amount of effects as parameters
                if (history.getEffects().size() > history.getParam().size()) {
                    history.pushParam(EditUtils.calculateSliderValue(slider.getProgress()));
                    // If they aren't equal then we have changed the slider multiple times in the same effect.
                    // In this case remove the most recent value and replace it with the current value.
                } else {
                    history.popParam();
                    history.pushParam(EditUtils.calculateSliderValue(slider.getProgress()));
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
            if (EditUtils.isAdjustableEffect(mCurrentEffect)) {
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
            else if (EditUtils.isFilter(mCurrentEffect)) {
                loadTextures();
                filterInitialiser.applyFilter(mTextures, 0, 1, mCurrentEffect, mEffectContext, mImageWidth, mImageHeight);
            }
            //else if the effect is not 'none'
            else if (mCurrentEffect != R.id.none) {
                if (mCurrentEffect == R.id.crop) {
                    if (!undo) {
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
                }
                else {
                    loadTextures();
                    applyEffect(0, 1);
                    effectApplied = true;
                }
            }
        }
        else {
            if(mCurrentEffect == R.id.hue){
                applyHue();
                loadHuePreview();
            }
            else {
                loadTextures();
                applyEffect(0, 1);
            }
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

    /**
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

    /**
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

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    }

    /**
    Undo algorithm for non destructive editing. Takes the stored stacks of effects and parameters
     removes the most recent one and sequentially reapplies them to get the image minus the most
     recent effect applied.
     Known issue with this method with a potential race condition. Hacky fix by waiting at the end of
     the loop for the results to properly show up.
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
        // set the effect to the effect in the redo stacks
        mCurrentEffect = history.getRedoEffects().get(redoIndex);
        effectParameter = history.getRedoParams().get(redoIndex);
        //push the redone effect back to the history as the most recently applied
        history.pushRedo(mCurrentEffect, effectParameter);
        // Special case for handling brush effects
        // Effectively don't re render just change the image to a screenshot taken when we leave brush tools
        if (mCurrentEffect == 111){
            Uri brushimage = history.getImage("BrushIm" + brushRedoIndex);
            brushRedoIndex++;
            Bitmap b = null;
            try {
                b = MediaStore.Images.Media.getBitmap(context.getContentResolver(), brushimage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            images.setImage(b);
            mCurrentEffect = R.id.none;
        }

        // Call a render
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

    /**
     * Apply the hue colour filter onto the ImageView used for hue.
     */
    protected void applyHue() {
        hueView.getDrawable().setColorFilter(effectHandler.adjustHue(hueSlider.getProgress()));
        if (history.getEffects().size() > history.getParam().size()) {
            history.pushParam(EditUtils.calculateSliderValue(hueSlider.getProgress()));
            // If they aren't equal then we have changed the slider multiple times in the same effect.
            // In this case remove the most recent value and replace it with the current value.
        } else {
            history.popParam();
            history.pushParam(EditUtils.calculateSliderValue(hueSlider.getProgress()));
        }
    }

    /**
     * For use with the crop tool. Sets the texture
     * to the cropped image.
     */
    public void applyCrop() {
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, images.getImage(), 0);
        GLToolbox.initTexParams();
    }

    /**
     * Retrieves the edited Bitmap from the ImageView
     * used to change the hue of the image.
     * @param hueImgView ImageView used to update hue of Bitmap
     * @return the updated Bitmap
     */
    protected Bitmap getHuePreviewBitmap(final View hueImgView) {
        int width = hueImgView.getWidth();
        int height = hueImgView.getHeight();
        Bitmap huePreview = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(huePreview);
        hueImgView.layout(hueImgView.getLeft(), hueImgView.getTop(), hueImgView.getRight(), hueImgView.getBottom());
        hueImgView.draw(c);
        return huePreview;
    }

    /**
     * This method sets the texture to the image in the
     * hue imageView, with the edited hue.
     */
    protected void loadHuePreview() {
        Bitmap huePreview = getHuePreviewBitmap(hueView);
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[1]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, huePreview, 0);
        GLToolbox.initTexParams();
    }

    /**
     * Shows the options menu and initialises its on clicks
     * @param v
     */
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
                        OpenDialog.openDialog(context);
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

    /**
     * Generate all the layers for the layer editor
     */
    public void prepLayers() {
        EditUtils.clearPrivateStorage(context, "layers");
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

    /**
     * prepare all relevant variables for undoing an effect
     */
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

    /**
     * Generate an image by successively applying a list of effects
     */
    public void genLayers(){
        // update the hue view to the most recent image
        hueView.setImageBitmap(images.getOriginalImage());
        brushindex = 0;
        for (int i = 0; i <= history.getEffects().size() - 1; i++) {
            // Set the current effect and corresponding parameter to their values at the current index.
            mCurrentEffect = history.getEffects().get(i);
            effectParameter = history.getParam().get(i);
            if (mCurrentEffect == 111){
                Uri brushimage = history.getImage("BrushIm" + brushindex);
                brushRedoIndex = brushindex;
                brushindex++;
                Bitmap b = null;
                try {
                    b = MediaStore.Images.Media.getBitmap(context.getContentResolver(), brushimage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                images.setImage(b);
                mCurrentEffect = R.id.none;
            }
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

    @Override
    /**
     * Special handling for pressing the back button to ensure the relevant information
     * is handed back to the previous activity
     */
    public void onBackPressed() {
        EditUtils.clearPrivateStorage(context, "back");
        Intent data = new Intent();
        save(images.getImage(), context, 17, "back");
        android.os.SystemClock.sleep(200);
        File[] f = fm.getFileList(getFilesDir().toString() + "/back");
        Uri i = Uri.fromFile(f[0]);
        data.putExtra("Image1", i);
        data.putExtra("History", history);
        // add data to Intent
        setResult(Activity.RESULT_OK, data);
        images.recycle();
        super.onBackPressed();
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