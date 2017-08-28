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

public class DetectorImageView extends ImageView {
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private int mBitmapWidth = 200;
    private int mBitmapHeight = 200;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean redEye = false;
    private boolean changeEyes = false;
    private int mDisplayStyle = 0;
    private int [] mPX = null;
    private int [] mPY = null;
    private float scaledEyeCircleSize;
    private float eyeColour;

    public DetectorImageView(Context c) {
        super(c);
        init();
    }

    public DetectorImageView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    private void init() {
        mBitmap = Bitmap.createBitmap(mBitmapWidth, mBitmapHeight, Bitmap.Config.RGB_565);
        mCanvas = new Canvas(mBitmap);
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
    }

    public void setChangeEyes() {
        changeEyes = true;
    }

    public void setEyeColour(float hueValue) {
        eyeColour = hueValue;
    }

    private boolean isIris(float red, float green, float blue) {
        if (blue > red ||
            green > red ) {
            return true;
        }
        return false;
    }

    private void applyChangeEyeColour(int i, float hue) {
        for(int x = (mPX[i] - Math.round(scaledEyeCircleSize)); x < (mPX[i] + Math.round(scaledEyeCircleSize)); x++) {
            for(int y = (mPY[i] - Math.round(scaledEyeCircleSize)); y < (mPY[i] + Math.round(scaledEyeCircleSize)); y++) {
                //hue range 0-360
                int pixel = mBitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                float [] hsv = new float[3];

                float redIntensity = ((float)red / ((green + blue) / 2));
                float blueIntensity = ((float)blue / ((green + red) / 2));
                float greenIntensity = ((float)green / ((red + blue) / 2));
                if (isIris(redIntensity, greenIntensity, blueIntensity)) {
                    Color.colorToHSV(pixel,hsv);
                    hsv[0] = hue;
                    mBitmap.setPixel(x,y,Color.HSVToColor(Color.alpha(pixel),hsv));
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

            if (mPX != null && mPY != null) {
                for (int i = 0; i < mPX.length; i++) {
                    if (redEye) {
                        applyRedEyeFix(i);
                    }
                    else if(changeEyes) {
                        applyChangeEyeColour(i, eyeColour);
                    }
                    else if (mDisplayStyle == 1) {
                        canvas.drawCircle(mPX[i], mPY[i], scaledEyeCircleSize, mPaint);
                    } else {
                        canvas.drawRect(mPX[i] - 20,  mPY[i] - 20, mPX[i] + 20,  mPY[i] + 20, mPaint);
                    }
                }
            }
        }
        redEye = false;
        changeEyes = false;
    }
}

/*int red, green, blue;
        int a1, a2, a3, a4, a5, a6, a7, a8, a9;
        for (int x = (mPX[i] - Math.round(scaledEyeCircleSize)); x < (mPX[i] + Math.round(scaledEyeCircleSize)); x++) {
            for (int y = (mPY[i] - Math.round(scaledEyeCircleSize)); y < (mPY[i] + Math.round(scaledEyeCircleSize)); y++) {

                a1 = mBitmap.getPixel(x - 1, y - 1);
                a2 = mBitmap.getPixel(x - 1, y);
                a3 = mBitmap.getPixel(x - 1, y + 1);
                a4 = mBitmap.getPixel(x, y - 1);
                a5 = mBitmap.getPixel(x, y);
                a6 = mBitmap.getPixel(x, y + 1);
                a7 = mBitmap.getPixel(x + 1, y - 1);
                a8 = mBitmap.getPixel(x + 1, y);
                a9 = mBitmap.getPixel(x + 1, y + 1);

                red = (Color.red(a1) + Color.red(a2) + Color.red(a3) + Color.red(a4) + Color.red(a6) + Color.red(a7) + Color.red(a8) + Color.red(a9)) *(-1)   + Color.red(a5)*9 ;
                green = (Color.green(a1) + Color.green(a2) + Color.green(a3) + Color.green(a4) + Color.green(a6) + Color.green(a7) + Color.green(a8) + Color.green(a9)) *(-1)  + Color.green(a5)*9 ;
                blue = (Color.blue(a1) + Color.blue(a2) + Color.blue(a3) + Color.blue(a4) + Color.blue(a6) + Color.blue(a7) + Color.blue(a8) + Color.blue(a9)) *(-1)   + Color.blue(a5)*9 ;

                red = red & 0xFF;
                blue = blue & 0xFF;
                green = green & 0xFF;

                mBitmap.setPixel(x, y, Color.rgb(red, green, blue));
            }
        } */