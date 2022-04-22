package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import org.tensorflow.lite.examples.detection.R;

import java.io.File;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    public static final int RESULT_OK_CROP_IMAGE = 102;
    private Uri fileUri;
    private int objectIndex;
    private int imageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        readIntent();

        String destinationUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.Options options = new UCrop.Options();
        options.setMaxBitmapSize(10000);

        UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(), destinationUri)))
                .withAspectRatio(0, 0)
                .withOptions(options)
                .useSourceImageAspectRatio()
                .start(CropperActivity.this);
    }

    private void readIntent() {
        Intent intent = getIntent();
        if(intent.getExtras() != null){
            String result = intent.getStringExtra("imageUri");
            fileUri = Uri.parse(result);
            objectIndex = intent.getIntExtra("objectIndex", -1);
            imageIndex = intent.getIntExtra("imageIndex", -1);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP){
            final Uri resultUri = UCrop.getOutput(data);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result", resultUri + "");
            returnIntent.putExtra("objectIndex", objectIndex);
            returnIntent.putExtra("imageIndex", imageIndex);
            setResult(RESULT_OK_CROP_IMAGE, returnIntent);
            finish();
        }
        else if(resultCode == UCrop.RESULT_ERROR){
            final Throwable cropError = UCrop.getError(data);
            Toast.makeText(CropperActivity.this, cropError.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}