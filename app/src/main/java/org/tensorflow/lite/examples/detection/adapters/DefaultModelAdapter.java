package org.tensorflow.lite.examples.detection.adapters;

import static org.tensorflow.lite.examples.detection.adapters.CustomModelAdapter.IS_CUSTOM_MODEL_SELECTED;
import static org.tensorflow.lite.examples.detection.utils.Constants.SP_NAME;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.ml.modeldownloader.CustomModel;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.support.metadata.MetadataExtractor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class DefaultModelAdapter extends RecyclerView.Adapter<DefaultModelAdapter.MyViewHolder> {

    public static final String SELECTED_DEFAULT_MODEL_FILENAME = "SELECTED_DEFAULT_MODEL_FILENAME";
    private Activity activity;
    private List<String> modelFilenames;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor prefsEditor;

    public DefaultModelAdapter(List<String> modelFilenames, Activity activity) {
        this.modelFilenames = modelFilenames;
        this.activity = activity;
        mPrefs = activity.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        prefsEditor = mPrefs.edit();
    }

    @NonNull
    @Override
    public DefaultModelAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.layout_adapter_default_model_item, parent, false);

        return new DefaultModelAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String modelFileName = modelFilenames.get(position);

        holder.defaultModelNameTv.setText(modelFileName);

        holder.defaultModelLayout.setOnClickListener(v -> {
            prefsEditor.putString(SELECTED_DEFAULT_MODEL_FILENAME, modelFileName);
            prefsEditor.putBoolean(IS_CUSTOM_MODEL_SELECTED, false);
            prefsEditor.commit();
        });

    }

    @Override
    public int getItemCount() {
        return modelFilenames.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        LinearLayout defaultModelLayout;
        TextView defaultModelNameTv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            defaultModelLayout = itemView.findViewById(R.id.defaultModelLayout);
            defaultModelNameTv = itemView.findViewById(R.id.defaultModelNameTv);

        }
    }

}
