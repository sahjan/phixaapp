package com.example.project.photoapplication;

import android.media.effect.Effect;

import java.util.Stack;

/**
 * Created by Ed on 13/07/2017.
 */

public class EditHistory {

    private Stack<Effect> history;

    public EditHistory(){
        history = new Stack<>();

    }

    public void push(Effect effect){
        history.push(effect);
    }

    public void pop(){
        history.pop();
    }
}
