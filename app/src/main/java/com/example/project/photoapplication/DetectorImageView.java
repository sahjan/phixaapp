package com.example.project.photoapplication;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Class that extends ImageView. Responsible for displaying
 * the image, as well as displaying the detected points for the eyes.
 *
 * Base code by:
 * [AUTHOR]: Chunyen Liu
 * [SDK   ]: Android SDK 2.1 and up
 * [NOTE  ]: developer.com tutorial, "Face Detection with Android APIs"
 * http://www.developer.com/ws/android/programming/face-detection-with-android-apis.html
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
    private float scaledEyeCircleRadius;
    private float eyeColour;

    public DetectorImageView(Context c) {
        super(c);
        init();
    }

    public DetectorImageView(Context c, AttributeSet attrs) {
        super(c, attrs);
        init();
    }

    /**
     * initialises the DetectorImageView by setting
     * up the canvas, paint and Bitmap.
     */
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

    /**
     * Sets the size of the circle to be drawn on
     * the Canvas, depending on the distance between
     * the eyes. This ensures the circle is appropriately
     * scaled for images with differently sized faces
     * and eyes.
     * @param eyesDistance the distance between the eyes
     */
    public void setScaledEyeCircleRadius(float eyesDistance) {
        scaledEyeCircleRadius = eyesDistance / 4;
    }

    /**
     * Applies the red-eye filter by reading pixels within
     * the area of the circle that is drawn around the eyes.
     * Checks red value of a pixel for a red intensity over a
     * specified threshold.
     * see https://stackoverflow.com/questions/133675/red-eye-reduction-algorithm
     * @param i the face
     */
    private void applyRedEyeFix(int i) {
        for(int x = (mPX[i] - Math.round(scaledEyeCircleRadius)); x < (mPX[i] + Math.round(scaledEyeCircleRadius)); x++) {
            for(int y = (mPY[i] - Math.round(scaledEyeCircleRadius)); y < (mPY[i] + Math.round(scaledEyeCircleRadius)); y++) {

                int pixel = mBitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                //calculate the red intensity by dividing the amount of red by the average of green and blue.
                float redIntensity = ((float)red / ((green + blue) / 2));
                if (redIntensity > 2.2f) {
                    //set the pixel with a decreased red intensity.
                    mBitmap.setPixel(x, y, Color.rgb(45, green, blue));
                }
            }
        }
    }

    public void setChangeEyes() {
        changeEyes = true;
    }

    /**
     * Sets eyeColour to a specified number between
     * 0-360.
     * @param hueValue
     */
    public void setEyeColour(float hueValue) {
        //guard against values outside 0-360.
        if (hueValue < 0 || hueValue > 360) {
            hueValue = 0;
        }
        eyeColour = hueValue;
    }

    /**
     * This method detects the iris by checking a pixel for blue
     * or green values higher than the red. Skin around the eyes
     * is likely to have significantly more red than B or G so this
     * method is useful for safegaurding against applying colour change
     * to the area outside the eye.
     * @param red the amount of red in the pixel
     * @param green the amount of blue in the pixel
     * @param blue the amount of green in the pixel
     * @return true or false depending on the green or blue intensity
     */
    private boolean isIris(float red, float green, float blue) {
        if (blue > red ||
            green > red) {
            return true;
        }
        return false;
    }

    /**
     * This method changes the hue of the iris pixels depending
     * on the value passed in the hue parameter.
     * @param i face
     * @param hue the value of the hue to tint the eye colour to.
     */
    private void applyChangeEyeColour(int i, float hue) {
        for(int x = (mPX[i] - Math.round(scaledEyeCircleRadius)); x < (mPX[i] + Math.round(scaledEyeCircleRadius)); x++) {
            for(int y = (mPY[i] - Math.round(scaledEyeCircleRadius)); y < (mPY[i] + Math.round(scaledEyeCircleRadius)); y++) {
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

    /**
     * This method sets up detected face features for display
     * @param xx x coordinates of each eye detected
     * @param yy y coordinates of each eye detected
     * @param total total number of faces detected
     * @param style either 0 or 1. Sets to 1 when background calculation completed in calculatePositionInBG().
     */
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
                        canvas.drawCircle(mPX[i], mPY[i], scaledEyeCircleRadius, mPaint);
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