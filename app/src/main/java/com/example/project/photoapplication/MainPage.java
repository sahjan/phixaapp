package com.example.project.photoapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class MainPage extends BaseEditor implements GLSurfaceView.Renderer {

    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        //Initialise the renderer and tell it to only render when Explicit
        //requested with the RENDERMODE_WHEN_DIRTY option
        mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
        mEffectView.setEGLContextClientVersion(2);
        mEffectView.setRenderer(this);
        mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        // Initialise all the activity fields to relevant values
        Intent intent = getIntent();
        uri = intent.getParcelableExtra("Image");
        mCurrentEffect = R.id.none;
        context = this;
        history = new EditHistory();

        images = new Image(uri, context);

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
        builder.setTitle("Exit editor")
                .setMessage("All your changes will be lost. Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        images.recycle();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        dialog = builder.create();

        if (!isEffectApplied()) {
            images.setPreviousImage();
        }

        effectHandler = new Effects();

        // BUTTONS

        // Transform Button, when clicked, moves to transform activity
        findViewById(R.id.transformButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, TransformActivity.class);
                intent.putExtra("Image", uri);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Adjust Button, when clicked, moves to adjust Activity
        findViewById(R.id.adjustButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, AdjustActivity.class);
                intent.putExtra("Image", uri);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Brush Button, when clicked, moves to drawing Activity
        findViewById(R.id.brushButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, Drawing.class);
                intent.putExtra("Image", uri);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        // Overlay button, when clicked, moves to overlay Activity
        findViewById(R.id.overlayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainPage.this, OverlayActivity.class);
                intent.putExtra("Image", uri);
                startActivityForResult(intent, 1);
                overridePendingTransition(R.anim.enter_anim, R.anim.exit_anim);
                images.recycle();
            }
        });

        findViewById(R.id.moreOpt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        //keeps app from crashing when reopening after exiting or locking the device.
        if (images.getImage().isRecycled()) {
            images = new Image(uri, context);
        }
        mEffectView.onResume();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        dialog.show();
    }

    public void setSliderProgress(){
        MainPage.this.getmEffectView().post(new Runnable() {
            @Override
            public void run() {
                slider.setProgress(50);
            }
        });
    }

    public void showToast(final String toastString){
        MainPage.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainPage.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                Intent intent = data;
                uri = intent.getParcelableExtra("Image1");
                images = new Image(uri, context);
                mEffectView.requestRender();
            }
        }
    }

}


