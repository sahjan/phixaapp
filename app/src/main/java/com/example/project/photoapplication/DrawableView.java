package com.example.project.photoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Ed on 09/08/2017.
 */

public class DrawableView extends View {
    public int width;
    public int height;
    private int scaledHeight;
    private int scaledWidth;
    private Matrix m;
    private Bitmap mbitmap;
    private Canvas mcanvas;
    private Path path;
    private Paint paint;
    private float mX, mY;
    private Context context;
    private static final float Tolerance = 5;
    private ArrayList<Path> paths;
    private HashMap<Path, Integer> colours;
    private HashMap<Path, Float> brushSize;
    private HashMap<Path, Boolean> blurring;
    private int colour = Color.BLACK;
    private float brushWidth = 4f;
    private boolean blur = false;
    private BlurMaskFilter blurFilter = new BlurMaskFilter(20, BlurMaskFilter.Blur.NORMAL);
    private boolean selecting = false;
    private HashMap<Path, BlurMaskFilter> pathAndFilters;
    private ArrayList<Path> redoPaths = new ArrayList<>();
    private boolean redoInit;






    public DrawableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        path = new Path();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(4f);
        paths = new ArrayList<>();
        colours = new HashMap<>();
        brushSize = new HashMap<>();
        blurring = new HashMap<>();
        pathAndFilters = new HashMap<>();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mcanvas = new Canvas(mbitmap);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mbitmap, 0, 0, paint);
        // draws all previous paths
        for (Path p : paths) {
            paint.setColor(colours.get(p));
            paint.setStrokeWidth(brushSize.get(p));
            if (blurring.get(p)) {
                paint.setXfermode(null);
                paint.setAlpha(0xFF);
                paint.setMaskFilter(pathAndFilters.get(p));
            } else {
                paint.setMaskFilter(null);
            }
            canvas.drawPath(p, paint);
        }

            // draws the current path
            paint.setColor(colour);
            if (blur) {
                paint.setXfermode(null);
                paint.setAlpha(0xFF);
                paint.setMaskFilter(blurFilter);
            } else {
                paint.setMaskFilter(null);
            }
            canvas.drawPath(path, paint);

        super.onDraw(canvas);
    }



    private void startTouch (float x , float y){
        path.moveTo(x,y);
        mX = x;
        mY = y ;
    }
    public void moveTouche (float x,float y ) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if(dx >= Tolerance || dy >= Tolerance){
            path.quadTo(mX,mY,(x+mX)/2,(y+mY)/2);
            mX = x ;
            mY = y;

        }
    }
    private void upTouch(){
        path.lineTo(mX,mY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(!selecting) {
                    startTouch(x, y);
                    invalidate();
                }
                break ;
            case MotionEvent.ACTION_UP:
                if(!selecting) {
                    upTouch();
                    paths.add(path);
                    colours.put(path, colour);
                    brushSize.put(path, brushWidth);
                    blurring.put(path, blur);
                    pathAndFilters.put(path, blurFilter);
                    path = new Path();
                    invalidate();
                }
                break ;
            case MotionEvent.ACTION_MOVE:
                if(!selecting) {
                    moveTouche(x, y);
                    invalidate();
                }
                break ;

        }
        return true ;
    }

    public void setColour(int colour){
        this.colour = colour;
    }

    public void setStrokeWidth(float width){
        paint.setStrokeWidth(width);
        brushWidth = width;
    }




    public void setBlur(){
        if(blur){
            blur = false;
        }
        else {
            blur = true;
        }
    }

    public void setSelecting(){
        if(selecting){
            selecting = false;
        }
        else {
            selecting = true;
        }
    }


    public void createBlurFilter(float blurSize){
        blurFilter = new BlurMaskFilter(blurSize, BlurMaskFilter.Blur.NORMAL);

    }

    public void undo(){
        if(!paths.isEmpty()) {
            if(!redoInit) {
                initRedo();
            }
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void redo(){
        if(!(redoPaths.size() == paths.size()) && !redoPaths.isEmpty()) {
            paths.add(redoPaths.get(paths.size()));
            invalidate();
        }
    }

    public void initRedo(){
        redoInit = true;
        redoPaths = new ArrayList<>(paths);

    }






}