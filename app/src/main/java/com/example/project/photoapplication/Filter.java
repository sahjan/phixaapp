package com.example.project.photoapplication;

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

}
