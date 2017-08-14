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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.PopupMenu;
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
    private boolean blur = false;
    private boolean emboss = false;
    private Button type;
    private boolean colourDropper = false;
    private int currentColour = Color.BLACK;
    private Button accept;
    private Button currentColourBut;

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
        view.setDrawingCacheEnabled(true);


        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_drawing);
        layout.post(new Runnable() {
            @Override
            public void run() {
                setView();
            }
        });


        colour = (Button) findViewById(R.id.but2);
        colour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupColour(view);



            }
        });

        Button size = (Button) findViewById(R.id.but1);
        size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seek.setVisibility(View.VISIBLE);
            }
        });

        type = (Button) findViewById(R.id.but3);
        type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setblur();
                if(blur) {
                    blur = false;
                }
                else {
                    blur = true;
                }
            }
        });

        accept = (Button) findViewById(R.id.but4);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setViewColour();
            }
        });

        accept = (Button) findViewById(R.id.but4);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setViewColour();
                setSelecting();
                accept.setVisibility(View.INVISIBLE);

            }
        });

        currentColourBut = (Button) findViewById(R.id.currentColour);
        currentColourBut.setBackgroundColor(currentColour);



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




        View.OnTouchListener image_Listener = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(colourDropper) {
                        getSize();
                        float screenX = event.getX();
                        float screenY = event.getY();
                        float viewX = screenX;
                        float viewY = screenY;
                        int[] viewCoords = new int[2];
                        view.getLocationOnScreen(viewCoords);
                        x = viewX - viewCoords[0];
                        y = viewY - (viewCoords[1] - 380);
                        if (y > image.getHeight()) {
                            y = image.getHeight() - 1;
                        }
                        if (y < 0){
                            y = 0;
                        }
                        Log.e("Coords", Float.toString(x) + " " + Float.toString(y));
                        getBlurColour();
                        return false;
                    }
                }
                return false;
            }
        };

        view.setOnTouchListener(image_Listener);

    }


    public void showPopupColour(View v) {
        final PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.colour);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.ColourPicker:
                        colourDropper = true;
                        accept.setVisibility(View.VISIBLE);
                        view.setSelecting();
                        break;
                    case R.id.ColourWheel:
                        colourDropper = false;
                        c.show();
                        c.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }
                return true;
            }

        });
        popup.show();
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
//        if(blur){
//            view.createBlurFilter(seek.getProgress());
//        }
//        else {
            view.setStrokeWidth(width);
//        }
    }


    public void setblur(){
        view.setBlur();
        Log.e("Blur", "blurring");
    }


    public void getBlurColour() {
        Log.e("coords", Integer.toString((int) y));
        int pix = image.getPixel((int) x, (int) y);

        int red = Color.red(pix);
        int green = Color.green(pix);
        int blue = Color.blue(pix);
        int pixColour = Color.rgb(red, green, blue);
        currentColourBut.setBackgroundColor(pixColour);
        currentColour = pixColour;
    }

    public Bitmap getSize(){
        int w = view.getWidth();
        int h = view.getMeasuredHeight();
        Log.e("dimensions", h + " " + w);
        int scaledHeight = w * image.getHeight()/image.getWidth();
        int scaledWidth = w;

        image = Bitmap.createScaledBitmap(image, scaledWidth, scaledHeight, false);
        image = image.copy(Bitmap.Config.ARGB_8888, true);
        return image;
    }

    public void setViewColour(){
        view.setColour(currentColour);
    }

    public void setSelecting(){
        view.setSelecting();
        if (colourDropper){
            colourDropper = false;
        }
        else {
            colourDropper = true;
        }
    }



}
