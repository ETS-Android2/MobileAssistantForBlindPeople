package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SizeF;
import android.view.Menu;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;

import org.tensorflow.lite.examples.detection.R;

import java.io.File;

public class GuideActivity extends AppCompatActivity {

    android.hardware.Camera camera;
    int focusLength;
    android.hardware.Camera.Parameters params;

    public int PICTURE_ACTIVITY_CODE = 1;
    Camera.Parameters cameraParameters;

    private final float focalLength = (float) 5.23;  // Benim telefonumda bu şekilde.
    private final float sensorHeight = (float) 3.168;      // mm cinsinden.
    private final float laptopRealHeight = 450;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);

        camera = Camera.open();
        // launchTakePhoto();


        getCameraSensorHeight(0);
        getCameraSensorHeight(1);
        getCameraSensorHeight(2);
        getCameraSensorHeight(3);

    }

    private void launchTakePhoto()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraParameters = camera.getParameters();
        Camera.CameraInfo myinfo = new Camera.CameraInfo();
        float l = cameraParameters.getFocalLength();
        System.out.println("My Focus Length:--"+l);
        startActivityForResult(intent, PICTURE_ACTIVITY_CODE);
    }


    private float getCameraSensorHeight(int camNum)
    {
        SizeF size = new SizeF(0,0);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > camNum) {
                CameraCharacteristics character = manager.getCameraCharacteristics(cameraIds[camNum]);
                size = character.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                Log.i("guidanceLog", size + "");
            }
        }
        catch (CameraAccessException e)
        {
            Log.i("GuideActivity", e.getMessage(), e);
        }
        return size.getHeight();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICTURE_ACTIVITY_CODE)
        {
            if (resultCode == RESULT_OK)
            {


            }
        }
    }


}