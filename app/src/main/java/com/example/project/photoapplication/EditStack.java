package com.example.project.photoapplication;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * A special data structure for use in the EditHistory
 * In effect the EditStack is a stack, in that, generally items are added and removed from the top of the stack
 * However to allow for the special operations the EditHistory requires from a data structure,
 * the edit stack allows a user to remove and add at an arbitrary location within the stack
 */

public class EditStack<T> implements Cloneable, Parcelable {

    private ArrayList<T> items;
    public static final Parcelable.Creator<EditStack> CREATOR
            = new Parcelable.Creator<EditStack>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public EditStack createFromParcel(Parcel in) {
            return new EditStack(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public EditStack[] newArray(int size) {
            return new EditStack[size];
        }
    };


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

    /**
     * The clone method allows us to conduct a full copy of the EditStack
     * First conducts a shallow copy and then conducts a full deep copy
     * @return
     */
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

    @Override
    public void writeToParcel(Parcel out, int flags){
        out.writeList(items);
    }

    @Override
    public int describeContents(){
        return 0;
    }

    private EditStack(Parcel in){
        items =  in.readArrayList(ArrayList.class.getClassLoader());
    }
}
