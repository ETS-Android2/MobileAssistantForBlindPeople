package org.tensorflow.lite.examples.detection.models;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class TrainYourselfObject implements Serializable {

    private String objectName;
    // private List<Bitmap> imagesList;
    private List<SelectedCoordinates> selectedCoordinatesList;
    private List<Uri> imageUrisList;
    private List<String> storageLinksList;

    public TrainYourselfObject(String objectName) {
        this.objectName = objectName;
        // this.imagesList = new ArrayList<>();
        this.imageUrisList = new ArrayList<>();
        this.storageLinksList = new ArrayList<>();
        this.selectedCoordinatesList = new ArrayList<>();
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void addImageToImageList(Bitmap bitmap){
        // imagesList.add(bitmap);
        selectedCoordinatesList.add(new SelectedCoordinates(0, 0, bitmap.getWidth(), bitmap.getHeight()));
    }

    public void addImageUriToImageUrisList(Uri uri){
        imageUrisList.add(uri);
    }

    /*
    public List<Bitmap> getImagesList() {
        return imagesList;
    }

    public void setImagesList(List<Bitmap> imagesList) {
        this.imagesList = imagesList;
    }
    */
    

    public void addNewLinkToStorageLinksList(String storageLink){
        storageLinksList.add(storageLink);
    }

    public List<SelectedCoordinates> getSelectedCoordinatesList() {
        return selectedCoordinatesList;
    }

    public void setSelectedCoordinatesList(List<SelectedCoordinates> selectedCoordinatesList) {
        this.selectedCoordinatesList = selectedCoordinatesList;
    }

    public List<Uri> getImageUrisList() {
        return imageUrisList;
    }

    public void setImageUrisList(List<Uri> imageUrisList) {
        this.imageUrisList = imageUrisList;
    }

    public List<String> getStorageLinksList() {
        return storageLinksList;
    }

    public void setStorageLinksList(List<String> storageLinksList) {
        this.storageLinksList = storageLinksList;
    }
}
