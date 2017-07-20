package com.example.project.photoapplication;

import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;

import java.util.ArrayList;

/**
 * Created by Sahjan on 18/07/2017.
 *
 * This class is responsible for creating the
 * filters and passing it back to the main activity
 * to apply and render.
 *
 */

public class Filter extends Effects {

    /**
     * Creates 'Old Film' filter. Uses 5 effects.
     * @return the arraylist of the effects that make this filter.
     */
    public ArrayList<Effect> getOldFilmFilter(EffectContext effectContext) {
        ArrayList filterComponents = new ArrayList<Effect>();

        Effect contrast = initContrast(effectContext, 1.5f);
        Effect brightness = initBrightness(effectContext, 1.2f);
        Effect grain = initGrain(effectContext, 0.7f);
        Effect vignette = initVignette(effectContext, 0.5f);
        Effect grayscale = initGrayscale(effectContext);

        //put initialised effects in an array list
        filterComponents.add(contrast);
        filterComponents.add(brightness);
        filterComponents.add(grain);
        filterComponents.add(vignette);
        filterComponents.add(grayscale);

        return filterComponents;
    }

    /**
     * Creates 'Intense Colours' filter. Uses 3 effects.
     * @return the arraylist of the effects that make this filter.
     */
    public ArrayList<Effect> getIntenseColoursFilter(EffectContext effectContext) {
        ArrayList filterComponents = new ArrayList<Effect>();

        Effect contrast = initContrast(effectContext, 1.5f);
        Effect saturation = initSaturate(effectContext, 0.3f);
        Effect brightness = initBrightness(effectContext, 1.2f);

        //put initialised effects in an array list
        filterComponents.add(contrast);
        filterComponents.add(saturation);
        filterComponents.add(brightness);

        return filterComponents;
    }

    /* Filters
        else if (mCurrentEffect == R.id.oldFilm) {
        //if filter chosen, apply the filter.
        Effect grain = effectHandler.initGrain(mEffectContext, 1.6f);
        grain.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
        Effect vignette = effectHandler.initVignette(mEffectContext, 1.2f);
        vignette.apply(mTextures[1], mImageWidth, mImageHeight, mTextures[0]);
        Effect grayscale = effectHandler.initGrayscale(mEffectContext);
        grayscale.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
        effectApplied = true;
    }
        else if (mCurrentEffect == R.id.intenseColours) {
        Effect contrast = effectHandler.initContrast(mEffectContext, 1.5f);
        contrast.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
        Effect saturation = effectHandler.initSaturate(mEffectContext, 0.3f);
        saturation.apply(mTextures[1], mImageWidth, mImageHeight, mTextures[0]);
        Effect brightness = effectHandler.initBrightness(mEffectContext, 1.2f);
        brightness.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
        effectApplied = true;
    }
        else if (mCurrentEffect == R.id.alien) {
        Effect tint = effectHandler.initTint(mEffectContext, Color.GREEN);
        tint.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
        Effect fisheye = effectHandler.initFisheye(mEffectContext, 1.2f);
        fisheye.apply(mTextures[1], mImageWidth, mImageHeight, mTextures[0]);
        Effect contrast = effectHandler.initAutofix(mEffectContext, 1.2f);
        contrast.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
        effectApplied = true;
    }
      end filters
     */

}
