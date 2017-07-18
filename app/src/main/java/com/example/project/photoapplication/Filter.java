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

    private EffectContext eContext;

    public Filter(EffectContext effectContext) {
        eContext = effectContext;
    }

    /**
     * Creates 'Old Film' filter. Uses 5 effects.
     */
    public ArrayList oldFilmFilter() {
        ArrayList filterComponents = new ArrayList<Effect>();

        Effect contrast = initContrast(eContext, 1.5f);
        Effect saturation = initSaturate(eContext, 0.3f);
        Effect grain = initGrain(eContext, 0.7f);
        Effect brightness = initBrightness(eContext, 1.2f);
        Effect grayscale = initGrayscale(eContext);

        //put initialised effects in an array list
        filterComponents.add(contrast);
        filterComponents.add(saturation);
        filterComponents.add(grain);
        filterComponents.add(brightness);
        filterComponents.add(grayscale);

        // then loop thru this array list in the main
        //activity, applying each one in turn to the
        //texture, before rendering.
        return filterComponents;
    }

}
