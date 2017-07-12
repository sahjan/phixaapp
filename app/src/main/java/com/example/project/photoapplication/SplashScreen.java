package com.example.project.photoapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // NB : Delay 2 secs before transferring to Loader View
     Thread timeDelay = new Thread() {
         @Override
         public void run() {
             try {
                 sleep(2000);
                 Intent intent = new Intent(getApplicationContext(),Loader.class);
                 startActivity(intent);
                 finish();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     };

        timeDelay.start();

    }
}
