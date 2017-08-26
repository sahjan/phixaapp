package com.example.project.photoapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.media.FaceDetector;

//import com.google.android.gms.vision.face.FaceDetector;

import java.io.IOException;

/**
 * Created by Sahjan on 22/08/2017.
 */

public class FaceActivity extends AppCompatActivity {

    private DetectorImageView dIV;
    private Bitmap mFaceBitmap;
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
    private Uri uri;
    private LinearLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        dIV = new DetectorImageView(this);
        //dIV.setScaleType(ImageView.ScaleType.FIT_XY);

        layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.addView(dIV);
        LinearLayout.LayoutParams layoutP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                          LinearLayout.LayoutParams.MATCH_PARENT);

        this.addContentView(layout, layoutP);

        findViewById(R.id.redEyeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dIV.setRedEye();
                dIV.invalidate();
            }
        });

        // load the photo
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("Image");
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            mFaceBitmap = image.copy(Bitmap.Config.RGB_565, true);
            image.recycle();

            mFaceWidth = mFaceBitmap.getWidth();
            mFaceHeight = mFaceBitmap.getHeight();
            dIV.setImageBitmap(mFaceBitmap);
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

    public void setFace() {
        FaceDetector fd;
        FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
        PointF eyescenter = new PointF();
        float eyesdist = 0.0f;
        int [] fpx = null;
        int [] fpy = null;
        int count = 0;

        try {
            fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);
            count = fd.findFaces(mFaceBitmap, faces);
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
                    dIV.setScaledEyeCircleSize(eyesdist);

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
