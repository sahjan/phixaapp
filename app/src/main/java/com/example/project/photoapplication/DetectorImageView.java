package com.example.project.photoapplication;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Sahjan on 24/08/2017.
 */

class DetectorImageView extends ImageView {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mBitmapWidth = 200;
    private int mBitmapHeight = 200;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean redEye = false;
    private int mDisplayStyle = 0;
    private int [] mPX = null;
    private int [] mPY = null;
    private float scaledEyeCircleSize;
    //float viewWidth;
    //float viewHeight;
    //float scale;
    //Rect rect;

    public DetectorImageView(Context c) {
        super(c);
        //this.setScaleType(ImageView.ScaleType.FIT_XY);
        init();
    }

    public DetectorImageView(Context c, AttributeSet attrs) {
        super(c, attrs);
        //this.setScaleType(ImageView.ScaleType.FIT_XY);
        init();
    }

    private void init() {
        mBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);

        //viewWidth = mCanvas.getWidth();
        //viewHeight = mCanvas.getHeight();
        //scale = Math.min(viewWidth/mBitmapWidth, viewHeight/mBitmapHeight);
        //rect = new Rect(0,0,(int) (mBitmapWidth * scale), (int) (mBitmapHeight * scale));

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(0x80ff0000);
        mPaint.setStrokeWidth(3);
    }

    public void setRedEye() {
        redEye = true;
    }

    public void setScaledEyeCircleSize(float eyesDistance) {
        scaledEyeCircleSize = eyesDistance / 4;
    }

    private void initRedEyeFilter(int i) {
        /*mPaint.setStyle(Paint.Style.FILL);
        //mPaint.setColor(0xff000000);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

            canvas.drawCircle(mPX[i], mPY[i], scaledEyeCircleSize, mPaint);

        mPaint.setColorFilter(new PorterDuffColorFilter(0x7f990040,
                PorterDuff.Mode.SRC_OVER)); */

        for(int x = (mPX[i] - Math.round(scaledEyeCircleSize)); x < (mPX[i] + Math.round(scaledEyeCircleSize)); x++) {
            for(int y = (mPY[i] - Math.round(scaledEyeCircleSize)); y < (mPY[i] + Math.round(scaledEyeCircleSize)); y++) {

                //int c = mBitmap.getColorSpace();
                //int  red = (c & 0x00ff0000) >> 16;
                //int  green = (c & 0x0000ff00) >> 8;
                //int  blue = c & 0x000000ff;
                int pixel = mBitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                float redIntensity = ((float)red / ((green + blue) / 2));
                if (redIntensity > 2.2f) { //2.2f
                    //Color newColor = new Color(90, green, blue);
                    //mBitmap.setRGB(x, y, newColor.getRGB());
                    mBitmap.setPixel(x, y, Color.rgb(45, green, blue)); //90
                }
            }
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            mBitmapWidth = bm.getWidth();
            mBitmapHeight = bm.getHeight();
            mBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.RGB_565);
            mCanvas = new Canvas();
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawBitmap(bm, 0, 0, null);
            //mCanvas.drawBitmap(bm, null, rect, null);
        }
        super.setImageBitmap(bm);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mBitmapWidth = (mBitmap != null) ? mBitmap.getWidth() : 0;
        mBitmapHeight = (mBitmap != null) ? mBitmap.getHeight() : 0;
        if (mBitmapWidth == w && mBitmapHeight == h) {
            return;
        }

        if (mBitmapWidth < w) mBitmapWidth = w;
        if (mBitmapHeight < h) mBitmapHeight = h;
    }

    // set up detected face features for display
    public void setDisplayPoints(int [] xx, int [] yy, int total, int style) {
        mDisplayStyle = style;
        mPX = null;
        mPY = null;

        if (xx != null && yy != null && total > 0) {
            mPX = new int[total];
            mPY = new int[total];

            for (int i = 0; i < total; i++) {
                mPX[i] = xx[i];
                mPY[i] = yy[i];
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, null);
            //mCanvas.drawBitmap(mBitmap, null, rect, null);

            if (mPX != null && mPY != null) {
                for (int i = 0; i < mPX.length; i++) {
                    if (redEye) {
                        initRedEyeFilter(i);
                    }
                    else if (mDisplayStyle == 1) {
                        canvas.drawCircle(mPX[i], mPY[i], scaledEyeCircleSize, mPaint);
                    } else {
                        canvas.drawRect(mPX[i] - 20,  mPY[i] - 20, mPX[i] + 20,  mPY[i] + 20, mPaint);
                    }
                }
            }
        }
    }
}