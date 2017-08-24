package com.example.project.photoapplication;

import android.media.effect.Effect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Ed on 13/07/2017.
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

    public void initRedo(){
        redoEffects = (EditStack<Integer>) history.clone();
        redoParams = (EditStack<Float>) historyValues.clone();
    }

    public void clearRedo(){
        redoEffects.clear();
        redoParams.clear();
    }

    public void pushRedo(Integer effectID, Float param){
        history.push(effectID);
        historyValues.push(param);

    }

    public EditStack<Integer> getRedoEffects(){
        return redoEffects;
    }

    public EditStack<Float> getRedoParams(){
        return redoParams;
    }

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

    public boolean checkEmpty(){
        if (history.empty() || historyValues.empty()){
            return true;
        }
        else {
            return false;
        }
    }

    public void putImage(String name, Uri image){
        images.put(name, image);
    }

    public Uri getImage(String key){
        return images.get(key);
    }

    public HashMap<String, Uri> getImages(){
        return images;
    }

    public EditStack<Integer> getEffects(){ return history; }

    public EditStack<Float> getParam(){ return historyValues;}

    public void removeLayer(int index){
        history.removeIndex(index);
        historyValues.removeIndex(index);
    }

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



