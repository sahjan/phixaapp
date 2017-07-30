package com.example.project.photoapplication;

import android.media.effect.Effect;

import java.util.Stack;

/**
 * Created by Ed on 13/07/2017.
 */

public class EditHistory {

    private Stack<Integer> history;
    private Stack<Float> historyValues;

    public EditHistory(){
        history = new Stack<>();
        historyValues = new Stack<>();

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
        if (history.empty() && historyValues.empty()){
            return true;
        }
        else {
            return false;
        }
    }

    public Stack<Integer> getEffects(){ return history; }

    public Stack<Float> getParam(){ return historyValues;}
}
