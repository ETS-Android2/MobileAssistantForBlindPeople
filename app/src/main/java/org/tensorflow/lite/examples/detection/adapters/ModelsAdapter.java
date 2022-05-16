package org.tensorflow.lite.examples.detection.adapters;

import static org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.TrainYourselfActivity.checkAndRequestPermissions;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.ml.modeldownloader.CustomModel;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.models.ObjectDetectionModel;
import org.tensorflow.lite.examples.detection.models.TrainYourselfObject;
import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelsAdapter extends RecyclerView.Adapter<ModelsAdapter.MyViewHolder> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private Activity activity;
    private List<CustomModel> objectDetectionModels;
    private String userID;


    public ModelsAdapter(List<CustomModel> objectDetectionModels, Activity activity, String userID) {
        this.objectDetectionModels = objectDetectionModels;
        this.activity = activity;
        this.userID = userID;
    }

    @NonNull
    @Override
    public ModelsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.layout_adapter_object_detection_model_item, parent, false);

        return new ModelsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CustomModel model = objectDetectionModels.get(position);

        String modelName = model.getName().replace(userID, "Özel Model ");
        holder.modelNameTv.setText(modelName);

        try {
            byte[] fileContent = new byte[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                fileContent = Files.readAllBytes(model.getFile().toPath());
            }
            InputStream stream = new MetadataExtractor(ByteBuffer.wrap(fileContent)).getAssociatedFile("labelmap.txt");

            String result = convertInputStreamToString(stream);
            List<String> labels = Arrays.asList(result.split("\n"));
            labels = labels.subList(0, labels.size() - 2);

            holder.recognizableObjectsTv.setText(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        holder.modelLayout.setOnClickListener(v -> {

        });

    }

    // Plain Java
    private static String convertInputStreamToString(InputStream is) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        // Java 1.1
        //return result.toString(StandardCharsets.UTF_8.name());

        return result.toString("UTF-8");

        // Java 10
        //return result.toString(StandardCharsets.UTF_8);

    }
    
    
    @Override
    public int getItemCount() {
        return objectDetectionModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView modelNameTv;
        TextView recognizableObjectsTv;
        LinearLayout modelLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            modelNameTv = itemView.findViewById(R.id.modelNameTv);
            recognizableObjectsTv = itemView.findViewById(R.id.recognizableObjectsTv);
            modelLayout = itemView.findViewById(R.id.modelLayout);

        }
    }

}
