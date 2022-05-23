package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.adapters.CustomModelAdapter;
import org.tensorflow.lite.examples.detection.adapters.DefaultModelAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SelectModelActivity extends AppCompatActivity {

    List<CustomModel> downloadedModels;
    FirebaseAuth mAuth;
    DefaultModelAdapter defaultModelAdapter;
    CustomModelAdapter customModelAdapter;
    RecyclerView defaultModelsRecyclerView;
    RecyclerView customModelsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        defaultModelsRecyclerView = findViewById(R.id.defaultModelsRecyclerView);
        customModelsRecyclerView = findViewById(R.id.customModelsRecyclerView);
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        downloadedModels = new ArrayList<>();
        final String[] modelToDownloadName = {""};

        try {
            String[] filesInAssets = getAssets().list("");
            List<String> tfliteModelFilesInAssets = new ArrayList<>();

            for(String filename : filesInAssets)
                if(filename.endsWith(".tflite"))
                    tfliteModelFilesInAssets.add(filename);

            defaultModelAdapter = new DefaultModelAdapter(tfliteModelFilesInAssets, SelectModelActivity.this);
            defaultModelsRecyclerView.setAdapter(defaultModelAdapter);
            defaultModelsRecyclerView.setLayoutManager(new LinearLayoutManager(SelectModelActivity.this));

        } catch (IOException e) {
            e.printStackTrace();
        }

        customModelAdapter = new CustomModelAdapter(downloadedModels, SelectModelActivity.this, mAuth.getUid());
        customModelsRecyclerView.setAdapter(customModelAdapter);
        customModelsRecyclerView.setLayoutManager(new LinearLayoutManager(SelectModelActivity.this));

        // Get downloaded models and download if there's new model
        FirebaseModelDownloader.getInstance()
                .listDownloadedModels()
                .addOnSuccessListener(customModels -> {
                    for(CustomModel model: customModels){
                        addNewModel(model);

                        Log.i("deneme", "indirilmiş: " + model.getName());
                    }

                    boolean modelAlreadyDownloaded = true;
                    modelToDownloadName[0] = mAuth.getCurrentUser().getUid() + "-" + "1";
                    int modelIndex = 1;

                    while(modelAlreadyDownloaded){
                        if(isModelAlreadyDownloaded(modelIndex)){
                            modelIndex++;
                            modelToDownloadName[0] = mAuth.getCurrentUser().getUid() + "-" + modelIndex;
                        }
                        else{
                            modelAlreadyDownloaded = false;
                        }
                    }
                    Log.i("deneme", "modelToDownloadName: " + modelToDownloadName[0]);

                    CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                            // .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                            .build();
                    FirebaseModelDownloader.getInstance()
                            .getModel(modelToDownloadName[0], DownloadType.LATEST_MODEL, conditions)
                            .addOnSuccessListener(model -> {
                                // Download complete. Depending on your app, you could enable the ML
                                // feature, or switch from the local model to the remote model, etc.

                                // The CustomModel object contains the local path of the model file,
                                // which you can use to instantiate a TensorFlow Lite interpreter.
                                addNewModel(model);

                                File modelFile = model.getFile();
                                if (modelFile != null) {
                                    // InputStream stream = MetadataExtractor.getAssociatedFile(modelFile);

                                    // Interpreter interpreter = new Interpreter(modelFile);
                                }
                                Log.i("deneme", "download complete: " + modelFile.getName());
                            })
                            .addOnFailureListener(e -> {
                                Log.i("deneme", e.toString());
                            });

                })
                .addOnFailureListener(e -> Log.i("deneme", e.getMessage()));
    }

    private boolean isModelAlreadyDownloaded(int modelIndex){

        for(CustomModel objectDetectionModel : downloadedModels){
            if(objectDetectionModel.getName().endsWith(String.valueOf(modelIndex)))
                return true;
        }

        return false;
    }

    private void addNewModel(CustomModel model){
        downloadedModels.add(model);
        customModelAdapter.notifyDataSetChanged();
    }



}