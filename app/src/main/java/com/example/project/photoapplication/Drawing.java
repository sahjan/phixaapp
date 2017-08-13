package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import java.io.IOException;

public class Drawing extends AppCompatActivity implements ColourPickerDialog.OnColorChangedListener {

    private Bitmap image;
    private Uri path;
    private DrawableView view;
    private Paint mPaint;
    private ColourPickerDialog c;
    private SeekBar seek;
    private BlurMaskFilter mBlur;
    private float x,y;
    private Button colour;
    private Boolean blur = false;

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

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);


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

        colour = (Button) findViewById(R.id.but2);
        colour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c.show();
                c.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));


            }
        });

        Button size = (Button) findViewById(R.id.but1);
        size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seek.setVisibility(View.VISIBLE);
            }
        });


        seek = (SeekBar) findViewById(R.id.adjustSlider);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeSize(seek.getProgress());
            }
        });


        Button type = (Button) findViewById(R.id.but3);
        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setblur();
                blur = true;
            }
        });

        View.OnTouchListener image_Listener = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(blur) {
                        getSize();
                        float screenX = event.getX();
                        float screenY = event.getY();
                        float viewX = screenX;
                        float viewY = screenY;
                        int[] viewCoords = new int[2];
                        view.getLocationOnScreen(viewCoords);
                        x = viewX - viewCoords[0];
                        y = viewY - (viewCoords[1] - 380);
                        Log.e("Coords", Float.toString(x) + " " + Float.toString(y));
                        getBlurColour();
                        return true;
                    }
                }
                return false;
            }
        };

        view.setOnTouchListener(image_Listener);


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

    public void changeSize(float width){
        view.setStrokeWidth(width);
    }


    public void setblur(){
        view.setBlur(mBlur);
        Log.e("Blur", "blurring");
    }

    public void getBlurColour() {
        int pix = image.getPixel((int) x, (int) y);
        int red = Color.red(pix);
        int green = Color.green(pix);
        int blue = Color.blue(pix);
        int pixColour = Color.rgb(red, green, blue);
        view.setColour(pixColour);
    }

    public void getSize(){
        int w = view.getWidth();
        int h = view.getMeasuredHeight();
        Log.e("dimensions", h + " " + w);
        int scaledHeight = w * image.getHeight()/image.getWidth();
        int scaledWidth = w;

        image = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false);
        image = image.copy(Bitmap.Config.ARGB_8888, true);
    }



}
