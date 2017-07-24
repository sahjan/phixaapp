package com.example.project.photoapplication;

import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;

import java.util.ArrayList;

/**
 * Created by Sahjan on 18/07/2017.
 *
 * This class is responsible for creating the
 * filters and passing them back to the main activity
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

        Effect grain = initGrain(effectContext, 0.7f);
        Effect vignette = initVignette(effectContext, 1.6f);
        Effect grayscale = initGrayscale(effectContext);

        //put initialised effects in an array list
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

    /**
     * Creates 'Alien' filter. Uses 2 effects.
     * @return the arraylist of the effects that make this filter.
     */
    public ArrayList<Effect> getAlienFilter(EffectContext effectContext) {
        ArrayList filterComponents = new ArrayList<Effect>();

        Effect tint = initTint(effectContext, Color.GREEN);
        Effect fisheye = initFisheye(effectContext, 1.2f);
        Effect contrast = initContrast(effectContext, 1.2f);

        //put initialised effects in an array list
        filterComponents.add(tint);
        filterComponents.add(fisheye);
        filterComponents.add(contrast);

        return filterComponents;
    }

}
