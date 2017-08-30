package com.example.project.photoapplication;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.io.File;

/**
 * Created by Ed on 17/08/2017.
 * Contains static methods for data processing that can be called from anywhere and are used my multiple activities/classes
 */

public class EditUtils {


    /**
     * Check whether the effect passed to the method is a filter.
     *
     * @param chosenEffect - the id of the effect to be checked.
     */
    public static boolean isFilter(int chosenEffect) {
        if (chosenEffect == R.id.alien ||
                chosenEffect == R.id.intenseColours ||
                chosenEffect == R.id.oldFilm) {
            return true;
        }
        return false;
    }


    /**
     * Check whether the effect passed to the method is an adjustable effect.
     *
     * @param chosenEffect - the id of the effect to be checked.
     */
    public static boolean isAdjustableEffect(int chosenEffect) {
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
     * Convert the slider values of 0-100 to numbers that equate with the correct values for the effect parameters
      */
    public static float calculateSliderValue(int sliderValue) {
        float effectValue = (float) sliderValue / 50;
        return effectValue;
    }

    /**
     * Clear the Apps private storage folders of all files stored there
     * @param context
     * @param type - The folder to clear of files
     */

    public static void clearPrivateStorage(Context context, String type){
        FileManager fm = new FileManager(context);
        File[] files;
        if(type.equals("layers")) {
             files = fm.getFileList(context.getFilesDir().toString() + "/layers");
        }
        else if (type.equals("back")) {
             files = fm.getFileList(context.getFilesDir().toString() + "/back");
        }
        else {
            files = fm.getFileList(context.getFilesDir().toString() + "/brush");
        }
        for (File file: files){
            file.delete();
        }
    }
}
