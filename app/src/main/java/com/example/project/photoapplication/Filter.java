package com.example.project.photoapplication;

import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;

import java.util.ArrayList;

/**
 * Created by Sahjan on 18/07/2017.
 *
 * This class is responsible for creating the
 * filters and allowing the Base Editor to apply them.
 *
 */

public class Filter extends Effects {

    /**
     * Creates 'Old Film' filter. Uses 3 effects.
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
     * Creates 'Alien' filter. Uses 3 effects.
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

    /**
     * Apply the filter.
     * @param textures the array of textures from BaseEditor
     * @param inputTexture texture 0
     * @param outputTexture texture 1. A filter must always end with
     *                      the output texture because this is the
     *                      texture that is rendered.
     * @param chosenFilter the chosen filter.
     * @param effectContext Effect Context being used.
     * @param imgWidth width of img.
     * @param imgHeight height of img.
     */
    public void applyFilter(int[] textures, int inputTexture, int outputTexture, int chosenFilter, EffectContext effectContext, int imgWidth, int imgHeight) {
        if (chosenFilter == R.id.alien) {
            ArrayList<Effect> alien = getAlienFilter(effectContext);
            Effect tint = alien.get(0);
            tint.apply(textures[inputTexture], imgWidth, imgHeight, textures[outputTexture]);
            Effect fisheye = alien.get(1);
            fisheye.apply(textures[outputTexture], imgWidth, imgHeight, textures[inputTexture]);
            Effect contrast = alien.get(2);
            contrast.apply(textures[inputTexture], imgWidth, imgHeight, textures[outputTexture]);
        }
        else if (chosenFilter == R.id.intenseColours) {
            ArrayList<Effect> intenseColours = getIntenseColoursFilter(effectContext);
            Effect contrast = intenseColours.get(0);
            contrast.apply(textures[inputTexture], imgWidth, imgHeight, textures[outputTexture]);
            Effect saturation = intenseColours.get(1);
            saturation.apply(textures[outputTexture], imgWidth, imgHeight, textures[inputTexture]);
            Effect brightness = intenseColours.get(2);
            brightness.apply(textures[inputTexture], imgWidth, imgHeight, textures[outputTexture]);
        }
        else if (chosenFilter == R.id.oldFilm) {
            ArrayList<Effect> oldFilm = getOldFilmFilter(effectContext);
            Effect grain = oldFilm.get(0);
            grain.apply(textures[inputTexture], imgWidth, imgHeight, textures[outputTexture]);
            Effect vignette = oldFilm.get(1);
            vignette.apply(textures[outputTexture], imgWidth, imgHeight, textures[inputTexture]);
            Effect grayscale = oldFilm.get(2);
            grayscale.apply(textures[inputTexture], imgWidth, imgHeight, textures[outputTexture]);
        }
    }
}
