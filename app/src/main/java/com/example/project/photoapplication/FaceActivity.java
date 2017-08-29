package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.media.FaceDetector;

import java.io.IOException;

/**
 * Base code by:
 * [AUTHOR]: Chunyen Liu
 * [SDK   ]: Android SDK 2.1 and up
 * [NOTE  ]: developer.com tutorial, "Face Detection with Android APIs"
 */

public class FaceActivity extends BaseEditor {

    private DetectorImageView dIV;
    private Bitmap img;
    private Bitmap scaledImg;
    private int mFaceWidth = 200;
    private int mFaceHeight = 200;
    private static final int MAX_FACES = 1;
    private static String TAG = "TutorialOnFaceDetect";
    private static boolean DEBUG = false;
    protected static final int GUIUPDATE_SETFACE = 999;
    protected Handler mHandler = new Handler(){
        // @Override
        public void handleMessage(Message msg) {
            dIV.invalidate();
            super.handleMessage(msg);
        }
    };
    private LinearLayout layout;
    private LinearLayout buttons;
    private HorizontalScrollView coloursList;
    private Button redEyeButton;
    private Button eyeColourButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        dIV = new DetectorImageView(this);

        //initialise fields
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("Image");
        history = intent.getParcelableExtra("History");
        context = this;
        images = new Image(uri, context, history);

        layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(dIV);
        LinearLayout.LayoutParams layoutP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                          LinearLayout.LayoutParams.MATCH_PARENT);
        this.addContentView(layout, layoutP);

        //buttonsBar = findViewById(R.id.buttonsBar);

        //the buttons
        redEyeButton = (Button) findViewById(R.id.redEyeButton);
        eyeColourButton = (Button) findViewById(R.id.eyeButton);
        ((ViewGroup) redEyeButton.getParent()).removeView(redEyeButton);
        ((ViewGroup) eyeColourButton.getParent()).removeView(eyeColourButton);

        coloursList = (HorizontalScrollView) findViewById(R.id.coloursList);
        ((ViewGroup) coloursList.getParent()).removeView(coloursList);

        buttons = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        buttons.addView(redEyeButton);
        buttons.addView(eyeColourButton);
        buttons.addView(coloursList);
        LinearLayout.LayoutParams buttonsP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        this.addContentView(buttons, buttonsP);

        setEyeColourButtons();

        redEyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setRedEye();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
            }
        });

        eyeColourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                coloursList.setVisibility(View.VISIBLE);
            }
        });

        //load the photo
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            img = image.copy(Bitmap.Config.RGB_565, true);
            image.recycle();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            double scrHeight = displayMetrics.heightPixels;
            double scrWidth = displayMetrics.widthPixels;
            double scale = Math.min(scrHeight/img.getHeight(), scrWidth/img.getWidth());

            scaledImg = Bitmap.createScaledBitmap(img, (int) (img.getWidth()*scale), (int) (img.getHeight()*scale), true);

            mFaceWidth = scaledImg.getWidth(); //img
            mFaceHeight = scaledImg.getHeight(); //img
            dIV.setImageBitmap(scaledImg); //img
            img.recycle();
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        // perform face detection in setFace() in a background thread
        doLengthyCalc();

        // perform face detection and set the feature points
        //setFace();
        dIV.invalidate();
    }

    @Override
    public void showToast(String toastSting) {

    }

    @Override
    public void setSliderProgress() {

    }

    private void setEyeColourButtons() {
        findViewById(R.id.red).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(360f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.orange).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(30f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.yellow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(60f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.green).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(120f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(180f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.purple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(260f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.pink).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setEyeColour(300f);
                dIV.setChangeEyes();
                dIV.invalidate();
                images.setImage(dIV.getBitmap());
                coloursList.setVisibility(View.GONE);
            }
        });
    }

    private void setFace() {
        FaceDetector fd;
        FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
        PointF eyescenter = new PointF();
        float eyesdist = 0.0f;
        int [] fpx = null;
        int [] fpy = null;
        int count = 0;

        try {
            fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);
            count = fd.findFaces(scaledImg, faces); //img
        } catch (Exception e) {
            Log.e(TAG, "setFace(): " + e.toString());
            return;
        }

        // check if we detect any faces
        if (count > 0) {
            fpx = new int[count * 2];
            fpy = new int[count * 2];

            for (int i = 0; i < count; i++) {
                try {
                    faces[i].getMidPoint(eyescenter);
                    eyesdist = faces[i].eyesDistance();
                    dIV.setScaledEyeCircleRadius(eyesdist);

                    // set up left eye location
                    fpx[2 * i] = (int)(eyescenter.x - eyesdist / 2);
                    fpy[2 * i] = (int)eyescenter.y;

                    // set up right eye location
                    fpx[2 * i + 1] = (int)(eyescenter.x + eyesdist / 2);
                    fpy[2 * i + 1] = (int)eyescenter.y;

                    if (DEBUG)
                        Log.e(TAG, "setFace(): face " + i + ": confidence = " + faces[i].confidence()
                                + ", eyes distance = " + faces[i].eyesDistance()
                                + ", pose = ("+ faces[i].pose(FaceDetector.Face.EULER_X) + ","
                                + faces[i].pose(FaceDetector.Face.EULER_Y) + ","
                                + faces[i].pose(FaceDetector.Face.EULER_Z) + ")"
                                + ", eyes midpoint = (" + eyescenter.x + "," + eyescenter.y +")");
                } catch (Exception e) {
                    Log.e(TAG, "setFace(): face " + i + ": " + e.toString());
                }
            }
        }

        dIV.setDisplayPoints(fpx, fpy, count * 2, 1);
    }

    private void doLengthyCalc() {
        Thread t = new Thread() {
            Message m = new Message();

            public void run() {
                try {
                    setFace();
                    m.what = FaceActivity.GUIUPDATE_SETFACE;
                    FaceActivity.this.mHandler.sendMessage(m);
                } catch (Exception e) {
                    Log.e(TAG, "doLengthyCalc(): " + e.toString());
                }
            }
        };

        t.start();
    }
}
