package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.examples.detection.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SelectModelActivity extends AppCompatActivity {

    List<String> downloadedModelNames;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        downloadedModelNames = new ArrayList<>();
        final String[] modelToDownloadName = {""};

        FirebaseModelDownloader.getInstance()
                .listDownloadedModels()
                .addOnSuccessListener(customModels -> {
                    for(CustomModel model: customModels){
                        downloadedModelNames.add(model.getName());
                        Log.i("deneme", "indirilmiş: " + model.getName());
                    }

                    boolean modelAlreadyDownloaded = true;
                    modelToDownloadName[0] = mAuth.getCurrentUser().getUid() + "-" + "1";
                    int modelIndex = 1;

                    while(modelAlreadyDownloaded){
                        if(downloadedModelNames.contains(modelToDownloadName[0])){
                            modelIndex++;
                            modelToDownloadName[0] = mAuth.getCurrentUser().getUid() + "-" + modelIndex;
                        }
                        else{
                            modelAlreadyDownloaded = false;
                        }
                    }
                    Log.i("deneme", "modelToDownloadName: " + modelToDownloadName[0]);

                    CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                            .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                            .build();
                    FirebaseModelDownloader.getInstance()
                            .getModel(modelToDownloadName[0], DownloadType.LATEST_MODEL, conditions)
                            .addOnSuccessListener(model -> {
                                // Download complete. Depending on your app, you could enable the ML
                                // feature, or switch from the local model to the remote model, etc.

                                // The CustomModel object contains the local path of the model file,
                                // which you can use to instantiate a TensorFlow Lite interpreter.
                                File modelFile = model.getFile();
                                if (modelFile != null) {
                                    // interpreter = new Interpreter(modelFile);
                                }
                                Log.i("deneme", "download complete: " + modelFile.getName());
                            })
                            .addOnFailureListener(e -> {
                                Log.i("deneme", e.toString());
                            });

                })
                .addOnFailureListener(e -> Log.i("deneme", e.getMessage()));

    }
}