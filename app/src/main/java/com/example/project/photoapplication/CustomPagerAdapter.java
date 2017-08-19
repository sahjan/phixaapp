package com.example.project.photoapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

/**
 * Created by Ed on 07/08/2017.
 */

public class CustomPagerAdapter extends PagerAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    File[] mResources;
    File currentFile;
    EditHistory history;
    int index;


    public CustomPagerAdapter(Context context, File[] pics, EditHistory history){
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResources = pics;
        this.history = history;

    }

    @Override
    public int getCount(){
        return mResources.length;
    }


    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        index = position;
        imageView.setImageBitmap(getImage(mResources[position]));
        currentFile = mResources[position];
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEdit();
            }
        });

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    public Bitmap getImage(File file){
        Bitmap b = BitmapFactory.decodeFile(file.getAbsolutePath());
        return b;
    }


    public void startEdit(){
        Intent intent = new Intent(mContext, LayerEditorMainPage.class);
        intent.putExtra("Image", Uri.fromFile(currentFile));
        intent.putExtra("Index", index);
        intent.putExtra("History", history);
        mContext.startActivity(intent);

    }


}
