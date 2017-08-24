package com.example.project.photoapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class Drawing extends AppCompatActivity implements ColourPickerDialog.OnColorChangedListener {

    private Bitmap image;
    private Uri path;
    private DrawableView view;
    private Paint mPaint;
    private ColourPickerDialog c;
    private SeekBar seek;
    private BlurMaskFilter mBlur;
    private float x, y;
    private boolean blur = false;
    private boolean colourDropper = false;
    private int currentColour = Color.BLACK;
    private Button accept;
    private Button currentColourBut;
    private Context context;
    private EditHistory history;
    private FileManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_drawing);
        Intent intent = getIntent();
        path = intent.getParcelableExtra("Image");
        history = intent.getParcelableExtra("History");
        Bitmap b = null;
        try {
            b = MediaStore.Images.Media.getBitmap(this.getContentResolver(), path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPaint = new Paint();//
        mPaint.setColor(0xFFFF0000);
        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
        context = this;
        fm = new FileManager(context);


        c = new ColourPickerDialog(this, this, mPaint.getColor());

        image = b.copy(Bitmap.Config.ARGB_8888, true);

        view = (DrawableView) findViewById(R.id.canvas);
        view.setDrawingCacheEnabled(true);


        RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_drawing);
        layout.post(new Runnable() {
            @Override
            public void run() {
                setView();
            }
        });

        findViewById(R.id.sizeImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seek.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.colourPickerImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourDropper = true;
                accept.setVisibility(View.VISIBLE);
                view.setSelecting();
            }
        });

        findViewById(R.id.colourWheelImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourDropper = false;
                c.show();
                c.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }
        });

        findViewById(R.id.blurImgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setblur();
                if (blur) {
                    blur = false;
                    showToast("Blur disabled");
                } else {
                    blur = true;
                    showToast("Blur enabled");
                }
            }
        });


        accept = (Button) findViewById(R.id.but4);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setViewColour();
                setSelecting();
                showToast("Colour Selected!");
                accept.setVisibility(View.GONE);

            }
        });

        currentColourBut = (Button) findViewById(R.id.currentColour);
        currentColourBut.setBackgroundColor(currentColour);

        ImageButton options = (ImageButton) findViewById(R.id.moreOpt);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOptions(view);
            }
        });


        seek = (SeekBar) findViewById(R.id.adjustSlider);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int sliderProgress, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                changeSize(seek.getProgress());
            }
        });


        View.OnTouchListener image_Listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (colourDropper) {
                        getSize();
                        float screenX = event.getX();
                        float screenY = event.getY();
                        float viewX = screenX;
                        float viewY = screenY;
                        int[] viewCoords = new int[2];
                        view.getLocationOnScreen(viewCoords);
                        x = viewX - viewCoords[0];
                        y = viewY - (viewCoords[1] - 380);
                        if (y > image.getHeight()) {
                            y = image.getHeight() - 1;
                        }
                        if (y < 0) {
                            y = 0;
                        }
                        Log.e("Coords", Float.toString(x) + " " + Float.toString(y));
                        getBlurColour();
                        return false;
                    }
                }
                return false;
            }
        };

        view.setOnTouchListener(image_Listener);

    }


    public void showOptions(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.inflate(R.menu.more_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.save:
                        save();
                        break;

                    case R.id.undo:
                        // tell the user when there are no effects to undo.
                        undo();
                        break;

                    case R.id.open:
                        OpenDialog.openDialog(context);
                        break;

                    case R.id.Redo:

                        redo();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    /**
     * Save the image in the canvas
     */
    public void save() {
        fm.saveBitmap(getBitmap());
    }

    /**
     * @param bitmap  - The image to save as a file
     * @param context - the context of the activity calling the method
     */
    public void save(Bitmap bitmap, Context context, int index, String type) {
        FileManager fm = new FileManager(context);
        fm.startSave(context, bitmap, index, type);
        if (type.equals("normal")) {
            showToast("File Saved");
        }
    }

    /**
     * Set the view to the size of the image, scaled to fit the view correctly. Then set the background to the regular image.
     * This then prevents it from being stretched out of shape and we can get the touch coordinates actual value.
     */

    public void setView() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(getScaleSize()[1], getScaleSize()[0]);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        view.setLayoutParams(lp);
        view.setBackground(new BitmapDrawable(getResources(), image));
    }

    /**
     * Set the colour in the centre of the colour wheel to the colour and set the colour to draw in the view to the selected colour
     *
     * @param color - The colour to set
     */
    public void colorChanged(int color) {
        mPaint.setColor(color);
        view.setColour(color);
    }

    /**
     * Change the size of the brush
     *
     * @param width - the width to set the brush or blur size to
     */
    public void changeSize(float width) {
        if (blur) {
            view.createBlurFilter(seek.getProgress());
        } else {
            view.setStrokeWidth(width);
        }
    }


    /**
     * Activate blurring in the view
     */
    public void setblur() {
        view.setBlur();
    }


    /**
     * Get the colour at the pixel clicked
     */
    public void getBlurColour() {
        int pix = image.getPixel((int) x, (int) y);
        int red = Color.red(pix);
        int green = Color.green(pix);
        int blue = Color.blue(pix);
        int pixColour = Color.rgb(red, green, blue);
        currentColourBut.setBackgroundColor(pixColour);
        currentColour = pixColour;
    }

    /**
     * Create a scaled bitmap that fits the view properly
     *
     * @return - The mutable scaled bitmap
     */
    public Bitmap getSize() {
        image = Bitmap.createScaledBitmap(image, getScaleSize()[1], getScaleSize()[0], false);
        image = image.copy(Bitmap.Config.ARGB_8888, true);
        return image;
    }

    /**
     * Set the drawing colour in the view to the selected colour
     */
    public void setViewColour() {
        view.setColour(currentColour);
    }

    /**
     * Set selecting to true
     */
    public void setSelecting() {
        view.setSelecting();
        if (colourDropper) {
            colourDropper = false;
        } else {
            colourDropper = true;
        }
    }

    /**
     * Call the views undo method
     */
    public void undo() {
        view.undo();
    }

    /**
     * Call the views redo method
     */
    public void redo() {
        view.redo();
    }

    /**
     * Get the height and width values of the view and calculate the correct width and height
     * for the image to be scaled to to maintain aspect ratio
     *
     * @return int[] - An array containing the width and height values
     */
    public int[] getScaleSize() {
        int w = view.getWidth();
        int h = view.getMeasuredHeight();
        Log.e("dimensions", h + " " + w);
        int scaledHeight = w * image.getHeight() / image.getWidth();
        int scaledWidth = w;
        int[] scaledValues = {
                scaledHeight, scaledWidth
        };
        return scaledValues;
    }


    /**
     * Show a toast with the passed string
     *
     * @param toastString - The string to show in the toast
     */
    public void showToast(final String toastString) {
        Drawing.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(Drawing.this, toastString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Bitmap getBitmap() {
        Bitmap b = view.getDrawingCache();
        return b;
    }


    @Override
    public void onBackPressed() {
        EditUtils.clearPrivateStorage(context);
        Intent data = new Intent();
        save(getBitmap(), context, 17, "layer");
        android.os.SystemClock.sleep(200);
        File[] f = fm.getFileList(getFilesDir().toString());
        Uri i = Uri.fromFile(f[0]);
        data.putExtra("Image1", i);
        data.putExtra("History", history);
        // add data to Intent
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }
}