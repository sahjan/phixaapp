package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.io.IOException;

public class Drawing extends AppCompatActivity implements ColourPickerDialog.OnColorChangedListener {

    private Bitmap image;
    private Uri path;
    private DrawableView view;
    private Paint mPaint;
    private ColourPickerDialog c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        c = new ColourPickerDialog(this, this, mPaint.getColor());

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

        Button butt = (Button) findViewById(R.id.but1);
        butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.show();
                c.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


            }
        });


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

    public void colorChanged(int color) {
        mPaint.setColor(color);
        view.setColour(color);
        Log.e("Colour", mPaint.toString());
    }

}
