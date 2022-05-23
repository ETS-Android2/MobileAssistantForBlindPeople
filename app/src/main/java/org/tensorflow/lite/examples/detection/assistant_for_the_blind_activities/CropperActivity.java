package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.helpers.RotateImage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    public static final int RESULT_OK_CROP_IMAGE = 102;
    private Uri fileUri;
    private int objectIndex;
    private int imageIndex;
    private int imageWidth;
    private int imageHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        readIntent();

        String destinationUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.Options options = new UCrop.Options();
        options.setMaxBitmapSize(10000);
        options.setFreeStyleCropEnabled(false);
        options.setBrightnessEnabled(false);
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.NONE, UCropActivity.SCALE); // scale, rotate, aspect
        // rotate engelle yapıya uydurmak için. DONE.

        UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(), destinationUri)))
                // .withAspectRatio(0, 0)
                .withMaxResultSize(imageWidth, imageHeight)
                .withOptions(options)
                // .useSourceImageAspectRatio()
                .start(CropperActivity.this);


    }

    private void readIntent() {
        Intent intent = getIntent();
        if(intent.getExtras() != null){
            String result = intent.getStringExtra("imageUri");
            fileUri = Uri.parse(result);
            objectIndex = intent.getIntExtra("objectIndex", -1);
            imageIndex = intent.getIntExtra("imageIndex", -1);
            imageWidth = intent.getIntExtra("imageWidth", -1);
            imageHeight = intent.getIntExtra("imageHeight", -1);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP){
            int minX = (int) data.getExtras().get(UCrop.EXTRA_OUTPUT_OFFSET_X);
            int minY = (int) data.getExtras().get(UCrop.EXTRA_OUTPUT_OFFSET_Y);
            int maxX = minX + (int) data.getExtras().get(UCrop.EXTRA_OUTPUT_IMAGE_WIDTH);
            int maxY = minY + (int) data.getExtras().get(UCrop.EXTRA_OUTPUT_IMAGE_HEIGHT);

            Toast.makeText(CropperActivity.this, minX + " " + minY + " " + maxX + " " + maxY, Toast.LENGTH_SHORT).show();

            final Uri resultUri = UCrop.getOutput(data);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultUri + "");
            returnIntent.putExtra("objectIndex", objectIndex);
            returnIntent.putExtra("imageIndex", imageIndex);
            returnIntent.putExtra("minX", minX);
            returnIntent.putExtra("minY", minY);
            returnIntent.putExtra("maxX", maxX);
            returnIntent.putExtra("maxY", maxY);
            setResult(RESULT_OK_CROP_IMAGE, returnIntent);
            finish();
        }
        else if(resultCode == UCrop.RESULT_ERROR){
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(CropperActivity.this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}