package ca.uqac.drawbd;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity
{
    private Toolbar toolbar;
    private ImageButton btnColor, btnBrush, btnEraser, btnOpacity, btnLockRotation, btnPrev, btnNext, btnCopy, btnDelete, btnNew, btnSave, btnAnimate;
    private TextView indexText;
    private DrawingView drawView;
    private boolean locked;

    private float brushSmall, brushMedium, brushLarge;

    private Handler handler = new Handler();
    private Runnable animation = new Runnable()
    {
        @Override
        public void run()
        {
            if (running = drawView.nextFrame()) {
                handler.postDelayed(this, speed);
            } else {
                btnAnimate.setImageResource(R.drawable.ic_animate);
                drawView.stop();
            }
            indexText.setText(String.valueOf(drawView.frame()));
        }
    };;

    private int speed = 100;
    private boolean running;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout colorsLayout = findViewById(R.id.colors);
        btnColor = (ImageButton) colorsLayout.getChildAt(0);

        btnBrush = findViewById(R.id.btn_brush);
        btnEraser = findViewById(R.id.btn_eraser);
        btnOpacity = findViewById(R.id.btn_opacity);
        btnLockRotation = findViewById(R.id.btn_lock_rotation);
        btnPrev = findViewById(R.id.btn_prev);
        btnNext = findViewById(R.id.btn_next);
        btnCopy = findViewById(R.id.btn_copy);
        btnDelete = findViewById(R.id.btn_delete);
        btnNew = findViewById(R.id.btn_new);
        btnSave = findViewById(R.id.btn_save);
        btnAnimate = findViewById(R.id.btn_animate);

        drawView = findViewById(R.id.drawing);

        btnColor.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
        drawView.setColor(btnColor.getTag().toString());

        indexText = findViewById(R.id.indexImage);

        brushSmall = getResources().getInteger(R.integer.small_size);
        brushMedium = getResources().getInteger(R.integer.medium_size);
        brushLarge = getResources().getInteger(R.integer.large_size);

        drawView.setBrushSize(brushMedium);

        btnBrush.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle(R.string.msg_brush_size);
                dialog.setContentView(R.layout.brush_chooser);

                ImageButton smallBtn = dialog.findViewById(R.id.small_brush);
                smallBtn.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        drawView.setBrushSize(brushSmall);
                        dialog.dismiss();
                    }
                });

                ImageButton mediumBtn = dialog.findViewById(R.id.medium_brush);
                mediumBtn.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        drawView.setBrushSize(brushMedium);
                        dialog.dismiss();
                    }
                });

                ImageButton largeBtn = dialog.findViewById(R.id.large_brush);
                largeBtn.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        drawView.setBrushSize(brushLarge);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnEraser.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (drawView.erase()) {
                    btnEraser.setImageResource(R.drawable.ic_eraser_on);
                } else {
                    btnEraser.setImageResource(R.drawable.ic_eraser);
                }
            }
        });

        btnOpacity.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setTitle(R.string.msg_opacity_level);
                dialog.setContentView(R.layout.opacity_chooser);

                final TextView text = dialog.findViewById(R.id.opq_txt);
                final SeekBar opacity = dialog.findViewById(R.id.opacity_seek);

                opacity.setMax(100);

                text.setText(String.format("%s%%", drawView.getPaintAlpha()));
                opacity.setProgress(drawView.getPaintAlpha());

                opacity.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
                {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                    {
                        text.setText(String.format("%s%%", progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });

                Button btnSelect = dialog.findViewById(R.id.btn_select);
                btnSelect.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        drawView.setPaintAlpha(opacity.getProgress());
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        btnLockRotation.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (locked) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Unlock orientation ?")
                            .setMessage("(It will erase your animation!)")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    locked = false;

                                    btnLockRotation.setImageResource(R.drawable.ic_screen_rotation);
                                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.cancel();
                                }
                            })
                            .show();
                } else {
                    locked = true;
                    btnLockRotation.setImageResource(R.drawable.ic_screen_lock_rotation);
                    switch (getResources().getConfiguration().orientation) {
                        case Configuration.ORIENTATION_PORTRAIT:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            break;
                        case Configuration.ORIENTATION_LANDSCAPE:
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            break;
                        default:
                            break;
                    }
                }
            }
        });

        btnLockRotation.callOnClick();

        btnPrev.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                indexText.setText(String.valueOf(drawView.prev()));
            }
        });

        btnNext.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                indexText.setText(String.valueOf(drawView.next()));
            }
        });

        btnCopy.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                indexText.setText(String.valueOf(drawView.copy()));
            }
        });

        btnDelete.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                indexText.setText(String.valueOf(drawView.delete()));
            }
        });

        btnNew.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("New drawing")
                        .setMessage("Start new drawing (you will lose the current drawing)?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                drawView.clear();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        });

        btnSave.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //save drawing
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(MainActivity.this);
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //save drawing
                        drawView.toggleSaveMode(true);
                        drawView.startAnimationFrom(0);
                        do{
                            drawView.setDrawingCacheEnabled(true);
                            //attempt to save
                            String imgSaved = MediaStore.Images.Media.insertImage(
                                    getContentResolver(), drawView.getDrawingCache(),
                                    UUID.randomUUID().toString() + ".png", "drawing");
                            //feedback
                            Log.d("INFO:", imgSaved);

                            if (imgSaved != null) {
                                Toast savedToast = Toast.makeText(getApplicationContext(),
                                        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                                savedToast.show();
                            } else {
                                Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                        "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                                unsavedToast.show();
                            }
                            drawView.destroyDrawingCache();
                        } while(drawView.nextFrame());
                        drawView.setDrawingCacheEnabled(true);
                        //attempt to save
                        String imgSaved = MediaStore.Images.Media.insertImage(
                                getContentResolver(), drawView.getDrawingCache(),
                                UUID.randomUUID().toString() + ".png", "drawing");
                        if (imgSaved != null) {
                            Toast savedToast = Toast.makeText(getApplicationContext(),
                                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                            savedToast.show();
                        } else {
                            Toast unsavedToast = Toast.makeText(getApplicationContext(),
                                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                            unsavedToast.show();
                        }
                        drawView.destroyDrawingCache();
                        drawView.toggleSaveMode(false);
                        drawView.stop();
                    }
                });
                saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });
                saveDialog.show();

            }
        });

        btnAnimate.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                running = !running;
                if (running && drawView.startAnimationFrom(0)) {
                    btnAnimate.setImageResource(R.drawable.ic_animate_on);
                    indexText.setText(String.valueOf(drawView.frame()));
                    handler.postDelayed(animation, speed);
                } else {
                    btnAnimate.setImageResource(R.drawable.ic_animate);
                    drawView.stop();
                }
            }
        });
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        handler.removeCallbacks(animation);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        btnLockRotation.callOnClick();
    }

    public void selectColor(View view)
    {
        if (view != btnColor) {
            ImageButton imgView = (ImageButton) view;
            drawView.setColor(view.getTag().toString());

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
            btnColor.setImageDrawable(getResources().getDrawable(R.drawable.color_button));
            btnColor = imgView;
        }
    }
}
