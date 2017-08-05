package com.example.project.photoapplication;

import android.media.effect.Effect;

import java.util.Stack;

/**
 * Created by Ed on 13/07/2017.
 */

public class EditHistory {

    private EditStack<Integer> history;
    private EditStack<Float> historyValues;
    private EditStack<Integer> redoEffects;
    private EditStack<Float> redoParams;

    public EditHistory(){
        history = new EditStack<>();
        historyValues = new EditStack<>();
        redoEffects = new EditStack<>();
        redoParams = new EditStack<>();

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

    public EditStack<Integer> getEffects(){ return history; }

    public EditStack<Float> getParam(){ return historyValues;}
}
