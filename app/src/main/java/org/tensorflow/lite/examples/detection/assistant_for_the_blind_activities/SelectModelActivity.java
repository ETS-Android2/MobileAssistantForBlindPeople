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

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.adapters.ModelsAdapter;
import org.tensorflow.lite.examples.detection.models.ObjectDetectionModel;
import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SelectModelActivity extends AppCompatActivity {

    List<CustomModel> downloadedModels;
    FirebaseAuth mAuth;
    ModelsAdapter modelsAdapter;
    RecyclerView models_recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        models_recyclerView = findViewById(R.id.models_recyclerView);
        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        downloadedModels = new ArrayList<>();
        final String[] modelToDownloadName = {""};
        modelsAdapter = new ModelsAdapter(downloadedModels, SelectModelActivity.this, mAuth.getUid());
        models_recyclerView.setAdapter(modelsAdapter);
        models_recyclerView.setLayoutManager(new LinearLayoutManager(SelectModelActivity.this));

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
        modelsAdapter.notifyDataSetChanged();
    }



}