package com.ajithvgiri.flashlighter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    Camera.Parameters params;
    private Camera camera;
    private static boolean isFlashOn;
    private static boolean hasFlash;
    private FloatingActionButton fab;
    private MediaPlayer mediaPlayer;
    private int CAMERA_PERMISSION_CODE = 1;
    private RelativeLayout relativeLayout;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        // TODO: Move this to where you establish a user session
        logUser();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_tourch_on_white);
        // First check if device is supporting flashlight or not
        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {
            // device doesn't support flash
            // Show alert message and close the application
            AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
                    .create();
            alert.setTitle("Warning");
            alert.setMessage("Sorry, your device doesn't support flash light!");
            alert.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // closing the application
                    finish();
                }
            });
            alert.show();
            return;
        }

        // get the camera
        getCamera();
        if (isCameraAllowed()!=true) {
            Intent intent = new Intent(MainActivity.this,IntroActivity.class);
            startActivity(intent);
            finish();
        }

        imageView = (ImageView)findViewById(R.id.imageView);
        relativeLayout = (RelativeLayout)findViewById(R.id.content_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        toggleButtonImage();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("FAB","clickedd"+isFlashOn);
                    if (isFlashOn == true) {
                        // turn off flash
                        Log.v("isFlashon", String.valueOf(isFlashOn));
                        turnOffFlash();
                    } else {
                        // turn on flash
                        Log.v("isFlashoff", String.valueOf(isFlashOn));
                        turnOnFlash();
                    }
            }
        });
    }

    private void logUser() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        Crashlytics.setUserName(Build.USER);
        Log.d("BuildUser",Build.USER);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(this)
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setIcon(R.drawable.icon512)
                    .setTitle("Flash Lighter!")
                    .setDescription("What can we improve? Your feedback is always welcome.")
                    .withDialogAnimation(true)
                    .withIconAnimation(true)
                    .setHeaderDrawable(R.color.colorPrimary)
                    .setPositive("CANCEL", null)
                    .setCancelable(false)
                    .setNegative("FEEDBACK", null)

                    .build();
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    // Get the camera
    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e("Error.Failed to Open", e.getMessage());
            }
        }
    }


    // Turning On flash
    private void turnOnFlash() {
        if (!isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound
            playSound();

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isFlashOn = true;

            // changing button/switch image
            toggleButtonImage();
        }

    }


    // Turning Off flash
    private void turnOffFlash() {
        if (isFlashOn) {
            if (camera == null || params == null) {
                return;
            }
            // play sound
            playSound();

            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(params);
            camera.stopPreview();
            isFlashOn = false;

            // changing button/switch image
            toggleButtonImage();
        }
    }


    // Playing sound
    // will play button toggle sound on flash on / off
    private void playSound() {
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.switch_sound);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    /*
     * Toggle switch button images
     * changing image states to on / off
     * */
    private void toggleButtonImage() {
        if (isFlashOn) {
            fab.setImageResource(R.drawable.ic_flash_off);
           // relativeLayout.setBackgroundColor(Color.DKGRAY);
            imageView.setImageResource(R.drawable.tourch_on);
        } else {
            fab.setImageResource(R.drawable.ic_flash_on);
            //relativeLayout.setBackgroundColor(Color.WHITE);
            imageView.setImageResource(R.drawable.tourch_off);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // on pause turn off the flash
        turnOffFlash();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Resume", String.valueOf(isFlashOn));

    }


    //We are calling this method to check the permission status
    private boolean isCameraAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        Log.d("IsCameraAllowrd", String.valueOf(result));
        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;
        }else {
            return false;
        }
    }



}
