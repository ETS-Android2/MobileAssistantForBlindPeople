package org.tensorflow.lite.examples.detection.popups;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.models.SelectedCoordinates;

public class LabeledImagePopup {

    private final Activity activity;
    private final Bitmap image;
    private final SelectedCoordinates selectedCoordinates;
    private final Dialog dialog;
    private ImageView labeledImageIV;

    public LabeledImagePopup(Activity activity, Bitmap image, SelectedCoordinates selectedCoordinates, Dialog dialog) {
        this.activity = activity;
        this.image = image;
        this.selectedCoordinates = selectedCoordinates;
        this.dialog = dialog;

        dialog.setContentView(R.layout.popup_labeled_image);
        define();
    }

    private void define(){
        labeledImageIV = dialog.findViewById(R.id.labeledImageIV);
        Bitmap labeledImage = cropImage(image, selectedCoordinates);
        labeledImageIV.setImageBitmap(labeledImage);
    }

    public Bitmap cropImage(Bitmap src, SelectedCoordinates selectedCoordinates){
        return Bitmap.createBitmap(src,
                                  selectedCoordinates.getMinX(),
                                  selectedCoordinates.getMinY(),
                            selectedCoordinates.getMaxX() - selectedCoordinates.getMinX(),
                            selectedCoordinates.getMaxY() - selectedCoordinates.getMinY());
    }

    public void show(){
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }


}
