package com.example.project.photoapplication;

import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

public class Layers extends AppCompatActivity {

    private EditHistory history;
    File[] mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layers);
        populateImages();
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(this, mResources);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);

    }

    private void populateImages(){
        FileManager fm = new FileManager(this);
        mResources = fm.getFileList(getFilesDir().toString());

    }










}
