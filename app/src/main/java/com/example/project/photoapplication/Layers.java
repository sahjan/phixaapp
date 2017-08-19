package com.example.project.photoapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

public class Layers extends AppCompatActivity {

    File[] mResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layers);
        populateImages();
        Intent intent = getIntent();
        EditHistory history = intent.getParcelableExtra("History");
        CustomPagerAdapter mCustomPagerAdapter = new CustomPagerAdapter(this, mResources, history);


        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);

    }

    private void populateImages(){
        FileManager fm = new FileManager(this);
        mResources = fm.getFileList(getFilesDir().toString());

    }










}
