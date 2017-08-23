package com.example.project.photoapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

public class LayerEditorMainPage extends AppCompatActivity {

    private Uri path;
    private Bitmap image;
    private EditHistory history;
    private int index;
    private ImageView view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer_editor_main_page);
        Intent intent = getIntent();
        path = intent.getParcelableExtra("Image");
        history = intent.getParcelableExtra("History");
        index = intent.getIntExtra("Index", 0);
        Log.e("Index", Integer.toString(index));
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        image = b;
        view = (ImageView) findViewById(R.id.layerImageView);
        view.setImageBitmap(image);
        Button delete = (Button) findViewById(R.id.Delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // delete the layer at this index from the history
                // result okay
                // return to pager activity
                // on activity result pager.deleteitem at index
                // if result code = n delete the item at that
                Log.e("Index", Integer.toString(index));
                history.removeLayer(index);
                Intent data = new Intent(getApplicationContext(), EffectsFilterActivity.class);
                data.putExtra("History", history);
                data.putExtra("Index", index);
                Log.e("Result", "button clicked");
                startActivity(data);
                // pass history back
            }
        });
    }
}
