package com.example.project.photoapplication;

import java.util.ArrayList;

/**
 * Created by Ed on 05/08/2017.
 */

public class EditStack<T> implements Cloneable {

    private ArrayList<T> items;


    public EditStack(){
        items = new ArrayList<>();
    }
    

    public void push(T item){
        items.add(item);
    }

    public void pop()
    {
        if (items.size() > 0){
            items.remove(items.size()-1);
        }
    }


    public void removeIndex(int index){
        items.remove(index);
    }

    public void addAtIndex(int index, T item){
        items.add(index, item);
    }


    public int size(){
        return items.size();
    }

    public boolean empty(){
        return items.isEmpty();
    }


    public void clear(){
        items.clear();
    }

    public T get(int index){
        return items.get(index);
    }

    @Override
    public EditStack<T> clone() {
        EditStack<T> clone = null;
        try {
             clone = (EditStack) super.clone();
            clone.items = new ArrayList(items);
        } catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return clone;
    }
}
