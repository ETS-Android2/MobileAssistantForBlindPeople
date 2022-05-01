package org.tensorflow.lite.examples.detection.adapters;

import static org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.TrainYourselfActivity.REQUEST_CROP_IMAGE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.CropperActivity;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {
    List<Bitmap> imagesList;
    List<Uri> imageUrisList;
    Activity activity;
    int ownerObjectIndex;

    public ImageAdapter(List<Bitmap> imagesList, List<Uri> imageUrisList, Activity activity, int ownerObjectIndex) {
        this.imagesList = imagesList;
        this.imageUrisList = imageUrisList;
        this.activity = activity;
        this.ownerObjectIndex = ownerObjectIndex;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.layout_adapter_image_item, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Bitmap image = imagesList.get(position);

        holder.imageView.setImageBitmap(image);
        holder.imageView.setOnClickListener(v -> {

            Intent intent = new Intent(activity, CropperActivity.class);
            intent.putExtra("imageUri", imageUrisList.get(position).toString());
            intent.putExtra("objectIndex", ownerObjectIndex);
            intent.putExtra("imageIndex", position);
            activity.startActivityForResult(intent, REQUEST_CROP_IMAGE);
        });
    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}