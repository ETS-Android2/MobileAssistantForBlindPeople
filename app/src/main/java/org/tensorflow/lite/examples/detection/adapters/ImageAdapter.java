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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.CropperActivity;
import org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities.TrainYourselfActivity;
import org.tensorflow.lite.examples.detection.helpers.RotateImage;
import org.tensorflow.lite.examples.detection.models.SelectedCoordinates;
import org.tensorflow.lite.examples.detection.popups.LabeledImagePopup;
import org.tensorflow.lite.examples.detection.utils.PathUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

    Activity activity;
    Dialog dialog;
    List<SelectedCoordinates> imageSelectedCoordinatesList;
    List<Uri> imageUrisList;
    int ownerObjectIndex;

    public ImageAdapter(Activity activity,
                        Dialog dialog,
                        List<SelectedCoordinates> imageSelectedCoordinatesList,
                        List<Uri> imageUrisList,
                        int ownerObjectIndex) {
        this.activity = activity;
        this.dialog = dialog;
        this.imageSelectedCoordinatesList = imageSelectedCoordinatesList;
        this.imageUrisList = imageUrisList;
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
        Uri uri = imageUrisList.get(position);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            String filePath = PathUtil.getPath(activity.getApplicationContext(), uri);
            bitmap = RotateImage.getCorrectlyOrientedImage(activity, bitmap, filePath);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        SelectedCoordinates selectedCoordinates = imageSelectedCoordinatesList.get(position);

        // holder.imageView.setImageBitmap(image);
        Bitmap finalBitmap = bitmap;
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(activity, CropperActivity.class);
            intent.putExtra("imageUri", imageUrisList.get(position).toString());
            intent.putExtra("objectIndex", ownerObjectIndex);
            intent.putExtra("imageIndex", position);
            intent.putExtra("imageWidth", finalBitmap.getWidth());
            intent.putExtra("imageHeight", finalBitmap.getHeight());
            activity.startActivityForResult(intent, REQUEST_CROP_IMAGE);
        });

        holder.deleteImageIcon.setOnClickListener(v -> {
            // imagesList.remove(position);
            imageUrisList.remove(position);
            imageSelectedCoordinatesList.remove(position);
            notifyDataSetChanged();
        });

        Bitmap finalImage = bitmap;
        holder.showLabeledImageIcon.setOnClickListener(v -> {
            LabeledImagePopup labeledImagePopup = new LabeledImagePopup(activity, finalImage, selectedCoordinates, dialog);
            labeledImagePopup.show();
        });

    }

    @Override
    public int getItemCount() {
        return imageUrisList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteImageIcon;
        ImageButton showLabeledImageIcon;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            deleteImageIcon = itemView.findViewById(R.id.deleteImageIcon);
            showLabeledImageIcon = itemView.findViewById(R.id.showLabeledImageIcon);
        }
    }

}