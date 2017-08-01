package com.example.project.photoapplication;

import android.media.effect.Effect;

import java.util.Stack;

/**
 * Created by Ed on 13/07/2017.
 */

public class EditHistory {

    private Stack<Integer> history;
    private Stack<Float> historyValues;
    private Stack<Integer> redoEffects;
    private Stack<Float> redoParams;

    public EditHistory(){
        history = new Stack<>();
        historyValues = new Stack<>();
        redoEffects = new Stack<>();
        redoParams = new Stack<>();

    }

    public void initRedo(){
        redoEffects = (Stack<Integer>) history.clone();
        redoParams = (Stack<Float>) historyValues.clone();
    }

    public void clearRedo(){
        redoEffects.clear();
        redoParams.clear();
    }

    public void pushRedo(Integer effectID, Float param){
        history.push(effectID);
        historyValues.push(param);

    }

    public Stack<Integer> getRedoEffects(){
        return redoEffects;
    }

    public Stack<Float> getRedoParams(){
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

    public Stack<Integer> getEffects(){ return history; }

    public Stack<Float> getParam(){ return historyValues;}
}
