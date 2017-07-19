package com.example.project.photoapplication;

import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;

/**
 * Created by Sahjan on 12/07/2017.
 *
 * This class is responsible for initialising the chosen
 * effect and passing it back to the main activity to
 * apply and render.
 */

public class Effects {

    private Effect effect;

    public Effects() {
        effect = null;
    }

    /**
     * @param effectContext the EffectContext being used.
     * @param chosenEffect the chosen effect
     * @return the initialised effect
     */
    public Effect initEffect(EffectContext effectContext, int chosenEffect, float sliderProgress) {

        EffectFactory effectFactory = effectContext.getFactory();
        if (effect != null) {
            effect.release();
        }
        /**
         * Initialize the correct effect based on the selected menu/action item
         */
        switch (chosenEffect) {
            case R.id.none:
                break;
            case R.id.autofix:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_AUTOFIX);
                effect.setParameter("scale", 0.5f);
                break;
            case R.id.bw: //this effect is not black & white, it only enhances the shadows and highlights
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_BLACKWHITE);
                effect.setParameter("black", .1f);
                effect.setParameter("white", .7f);
                break;
            case R.id.brightness:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_BRIGHTNESS);
                effect.setParameter("brightness", sliderProgress); //2.0f
                break;
            case R.id.contrast:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CONTRAST);
                effect.setParameter("contrast", 1.5f);
                break;
            case R.id.crossprocess:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_CROSSPROCESS);
                break;
            case R.id.documentary:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DOCUMENTARY);
                break;
            case R.id.duotone:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_DUOTONE);
                effect.setParameter("first_color", Color.YELLOW);
                effect.setParameter("second_color", Color.DKGRAY);
                break;
            case R.id.filllight:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FILLLIGHT);
                effect.setParameter("strength", sliderProgress);
                break;
            case R.id.fisheye:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FISHEYE);
                effect.setParameter("scale", sliderProgress);
                break;
            case R.id.flipvert:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                effect.setParameter("vertical", true);
                break;
            case R.id.fliphor:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FLIP);
                effect.setParameter("horizontal", true);
                break;
            case R.id.grain:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAIN);
                effect.setParameter("strength", 1.0f);
                break;
            case R.id.grayscale:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_GRAYSCALE);
                break;
            case R.id.lomoish:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_LOMOISH);
                break;
            case R.id.negative:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_NEGATIVE);
                break;
            case R.id.posterize:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_POSTERIZE);
                break;
            case R.id.rotate:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_ROTATE);
                effect.setParameter("angle", 90);
                break;
            case R.id.saturate:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SATURATE);
                effect.setParameter("scale", sliderProgress);
                break;
            case R.id.sepia:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SEPIA);
                break;
            case R.id.sharpen:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SHARPEN);
                break;
            case R.id.temperature:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TEMPERATURE);
                effect.setParameter("scale", sliderProgress);
                break;
            case R.id.tint:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TINT);
                effect.setParameter("tint", Color.MAGENTA);
                break;
            case R.id.vignette:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_VIGNETTE);
                effect.setParameter("scale", sliderProgress);
                break;
            default:
                break;
        }
        return effect;
    }


    /* The following methods initialise individual effects depending on a
     * specified parameter. Used to create filters in the Filters class. */

    /**
     * Initialise autofix Effect.
     * @param effectContext the EffectContext being used
     * @param parameter specified intensity of the effect.
     * @return the initialised effect.
     */
    public Effect initAutofix(EffectContext effectContext, float parameter) {
        Effect autofix;
        EffectFactory effectFactory = effectContext.getFactory();

        autofix = effectFactory.createEffect(
                EffectFactory.EFFECT_AUTOFIX);
        autofix.setParameter("scale", 0.5f);
        return autofix;
    }

    public Effect initSH(EffectContext effectContext, float shadowsParam, float highlightsParam) {
        Effect sH;
        EffectFactory effectFactory = effectContext.getFactory();

        sH = effectFactory.createEffect(
                EffectFactory.EFFECT_BLACKWHITE);
        sH.setParameter("black", shadowsParam);
        sH.setParameter("white", highlightsParam);
        return sH;
    }

    public Effect initBrightness(EffectContext effectContext, float parameter) {
        Effect brightness;
        EffectFactory effectFactory = effectContext.getFactory();

        brightness = effectFactory.createEffect(
                EffectFactory.EFFECT_BRIGHTNESS);
        brightness.setParameter("brightness", parameter);
        return brightness;
    }

    public Effect initContrast(EffectContext effectContext, float parameter) {
        Effect contrast;
        EffectFactory effectFactory = effectContext.getFactory();

        contrast = effectFactory.createEffect(
                EffectFactory.EFFECT_CONTRAST);
        contrast.setParameter("contrast", parameter);
        return contrast;
    }

    public Effect initFillLight(EffectContext effectContext, float parameter) {
        Effect fillLight;
        EffectFactory effectFactory = effectContext.getFactory();

        fillLight = effectFactory.createEffect(
                EffectFactory.EFFECT_FILLLIGHT);
        fillLight.setParameter("strength", parameter);
        return fillLight;
    }

    public Effect initGrain(EffectContext effectContext, float parameter) {
        Effect grain;
        EffectFactory effectFactory = effectContext.getFactory();

        grain = effectFactory.createEffect(
                EffectFactory.EFFECT_GRAIN);
        grain.setParameter("strength", parameter);
        return grain;
    }

    public Effect initGrayscale(EffectContext effectContext) {
        Effect grayscale;
        EffectFactory effectFactory = effectContext.getFactory();

        grayscale = effectFactory.createEffect(
                EffectFactory.EFFECT_GRAYSCALE);
        return grayscale;
    }

    public Effect initSaturate(EffectContext effectContext, float parameter) {
        Effect saturate;
        EffectFactory effectFactory = effectContext.getFactory();

        saturate = effectFactory.createEffect(
                EffectFactory.EFFECT_SATURATE);
        saturate.setParameter("scale", parameter);
        return saturate;
    }

    public Effect initTemperature(EffectContext effectContext, float parameter) {
        Effect temperature;
        EffectFactory effectFactory = effectContext.getFactory();

        temperature = effectFactory.createEffect(
                EffectFactory.EFFECT_TEMPERATURE);
        temperature.setParameter("scale", parameter);
        return temperature;
    }

    public Effect initVignette(EffectContext effectContext, float parameter) {
        Effect vignette;
        EffectFactory effectFactory = effectContext.getFactory();

        vignette = effectFactory.createEffect(
                EffectFactory.EFFECT_VIGNETTE);
        vignette.setParameter("scale", parameter);
        return vignette;
    }

}
