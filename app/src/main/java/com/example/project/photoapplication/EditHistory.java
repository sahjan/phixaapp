package com.example.project.photoapplication;

import android.media.effect.Effect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Stores all necessary data regarding the history of editing an image
 */

public class EditHistory implements Parcelable {

    private EditStack<Integer> history;
    private EditStack<Float> historyValues;
    private EditStack<Integer> redoEffects;
    private EditStack<Float> redoParams;
    private HashMap<String, Uri> images;

    public EditHistory(Uri originalImage){
        history = new EditStack<>();
        historyValues = new EditStack<>();
        redoEffects = new EditStack<>();
        redoParams = new EditStack<>();
        images = new HashMap<>();
        images.put("OriginalImage", originalImage);

    }

    /**
     *  Clone the effects and params at the current time
     *  to give you everything you can redo once you have undone something from that stack
     */
    public void initRedo(){
        redoEffects = (EditStack<Integer>) history.clone();
        redoParams = (EditStack<Float>) historyValues.clone();
    }

    /**
     * Clear the redo stack of all objects
     */
    public void clearRedo(){
        redoEffects.clear();
        redoParams.clear();
    }

    /**
     * Used for once you have redone an effect,
     * push it back to the history stack so that you can undo it again
     * @param effectID - The effect to push
     * @param param - The parameter of the effect
     */
    public void pushRedo(Integer effectID, Float param){
        history.push(effectID);
        historyValues.push(param);

    }

    // Getters
    public EditStack<Integer> getRedoEffects(){
        return redoEffects;
    }

    public EditStack<Float> getRedoParams(){
        return redoParams;
    }

    public HashMap<String, Uri> getImages(){
        return images;
    }

    public EditStack<Integer> getEffects(){ return history; }

    public EditStack<Float> getParam(){ return historyValues;}

    public Uri getImage(String key){
        return images.get(key);
    }

    // Add and remove methods

    public void pushEffect(Integer effectID){
        history.push(effectID);
    }

    public void pushParam(Float param){
        historyValues.push(param);
    }

    public void popEffect(){
        history.pop();
    }

    public void popParam(){
        historyValues.pop();
    }

    public void putImage(String name, Uri image){
        images.put(name, image);
    }

    /**
     * Checks whether either of the effects or values stacks are empty
     * @return
     */
    public boolean checkEmpty(){
        if (history.empty() || historyValues.empty()){
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Remove an effect and its associated parameter from the stacks
     * @param index
     */
    public void removeLayer(int index){
        history.removeIndex(index);
        historyValues.removeIndex(index);
    }

    /**
     * Add an effect and its associated parameter to the stacks
     * @param index
     * @param effect
     * @param param
     */
    public void addLayer(int index, int effect, float param){
        history.addAtIndex(index, effect);
        historyValues.addAtIndex(index, param);
    }


    // Methods for parcelable implementation


    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeParcelable(history, flags);
        out.writeParcelable(historyValues, flags);
        out.writeParcelable(redoEffects, flags);
        out.writeParcelable(redoParams, flags);
        out.writeMap(images);
    }

    private EditHistory(Parcel in){
        history = in.readParcelable(EditStack.class.getClassLoader());
        historyValues = in.readParcelable(EditStack.class.getClassLoader());
        redoEffects = in.readParcelable(EditStack.class.getClassLoader());
        redoParams = in.readParcelable(EditStack.class.getClassLoader());
        images = in.readHashMap(HashMap.class.getClassLoader());
    }

    @Override
    public int describeContents(){
        return 0;
    }

    public static final Parcelable.Creator<EditHistory> CREATOR
            = new Parcelable.Creator<EditHistory>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public EditHistory createFromParcel(Parcel in) {
            return new EditHistory(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public EditHistory[] newArray(int size) {
            return new EditHistory[size];
        }
    };
}



