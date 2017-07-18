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
            case R.id.bw:
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
                effect.setParameter("contrast", 1.4f);
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
                effect.setParameter("strength", .8f);
                break;
            case R.id.fisheye:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_FISHEYE);
                effect.setParameter("scale", .5f);
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
                effect.setParameter("angle", 180);
                break;
            case R.id.saturate:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_SATURATE);
                effect.setParameter("scale", .5f);
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
                effect.setParameter("scale", .9f);
                break;
            case R.id.tint:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_TINT);
                effect.setParameter("tint", Color.MAGENTA);
                break;
            case R.id.vignette:
                effect = effectFactory.createEffect(
                        EffectFactory.EFFECT_VIGNETTE);
                effect.setParameter("scale", .5f);
                break;
            default:
                break;
        }
        return effect;
    }



}
