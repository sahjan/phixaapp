package com.example.project.photoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;

public class Layers extends AppCompatActivity {

    File[] mResources;
    EditHistory history;
    ViewPager mViewPager;
    CustomPagerAdapter mCustomPagerAdapter;
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layers);
        populateImages();
        Intent intent = getIntent();
        final EditHistory history = intent.getParcelableExtra("History");
        Log.e("History", Integer.toString(history.getEffects().size()));
        context = this;

        mCustomPagerAdapter = new CustomPagerAdapter(this, mResources, history);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);

        ImageButton back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back = new Intent(context, MainPage.class);
                back.putExtra("History", history);
                Uri i = Uri.fromFile(mResources[mResources.length-1]);
                back.putExtra("Image", i);
                startActivity(back);
            }
        });

    }

    private void populateImages(){
        FileManager fm = new FileManager(this);
        mResources = fm.getFileList(getFilesDir().toString());

    }

    @Override
    public void onBackPressed(){
        //do nothing
    }


    public File[] removeItem(int index){
        File[] newFiles = new File[mResources.length-1];
        for(int i = 0; i<mResources.length; i++ ){
            if(i == index){
                //Do nothing
            }
            else {
                newFiles[i] = mResources[i];
            }
        }
        return newFiles;
    }

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
            Log.e("Index", Integer.toString(index));
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
            Log.e("Index", Integer.toString(index));
            intent.putExtra("History", history);
            Log.e("History", Integer.toString(history.getEffects().size()));
            startActivity(intent);

        }

    }










}
