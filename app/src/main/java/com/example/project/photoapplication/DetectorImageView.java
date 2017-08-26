package com.example.project.photoapplication;

import android.graphics.Color;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

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
        if(redEye) {
            redEye = false;
        }
        else {
            redEye = true;
        }
    }

    public void setScaledEyeCircleSize(float eyesDistance) {
        scaledEyeCircleSize = eyesDistance / 4;
    }

    private void applyRedEyeFix(int i) {
        for(int x = (mPX[i] - Math.round(scaledEyeCircleSize)); x < (mPX[i] + Math.round(scaledEyeCircleSize)); x++) {
            for(int y = (mPY[i] - Math.round(scaledEyeCircleSize)); y < (mPY[i] + Math.round(scaledEyeCircleSize)); y++) {

                int pixel = mBitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                float redIntensity = ((float)red / ((green + blue) / 2));
                if (redIntensity > 2.2f) {
                    mBitmap.setPixel(x, y, Color.rgb(45, green, blue));
                }
            }
        }
        redEye = false;
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
                        applyRedEyeFix(i);
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