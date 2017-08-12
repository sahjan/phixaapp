package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.IOException;

public class Drawing extends AppCompatActivity {

    private Bitmap image;
    private Uri path;
    private DrawableView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        Intent intent = getIntent();
        path = intent.getParcelableExtra("Image");
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
        }
        catch(IOException e){
            e.printStackTrace();
        }

        image = b.copy(Bitmap.Config.ARGB_8888, true);
         view = (DrawableView) findViewById(R.id.canvas);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_drawing);
        layout.post(new Runnable() {
            @Override
            public void run() {
                setView();
            }
        });

        view.setDrawingCacheEnabled(true);

    }


    public void save(){
        Bitmap b = view.getDrawingCache();
        FileManager fm = new FileManager(this);
        fm.saveBitmap(b);
    }

    public void setView(){
        int w = view.getWidth();
        int h = view.getMeasuredHeight();
        Log.e("dimensions", h + " " + w);
        int scaledHeight = w * image.getHeight()/image.getWidth();
        int scaledWidth = w;
        Log.e("dimensions", scaledHeight + " " + scaledWidth);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(scaledWidth, scaledHeight);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        view.setLayoutParams(lp);
        view.setBackground(new BitmapDrawable(getResources(), image));
    }
}
