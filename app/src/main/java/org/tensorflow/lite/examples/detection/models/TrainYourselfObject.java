package org.tensorflow.lite.examples.detection.models;

import android.graphics.Bitmap;
import android.os.Parcelable;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class TrainYourselfObject implements Serializable {

    private String objectName;
    private List<Bitmap> imagesList;
    private List<String> storageLinksList;

    public TrainYourselfObject(String objectName, List<Bitmap> imagesList) {
        this.objectName = objectName;
        this.imagesList = imagesList;
        this.storageLinksList = new ArrayList<>();
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void addImageToImageList(Bitmap bitmap){
        imagesList.add(bitmap);
    }

    public List<Bitmap> getImagesList() {
        return imagesList;
    }

    public void setImagesList(List<Bitmap> imagesList) {
        this.imagesList = imagesList;
    }

    public void addNewLinkToStorageLinksList(String storageLink){
        storageLinksList.add(storageLink);
    }

    public List<String> getStorageLinksList() {
        return storageLinksList;
    }

    public void setStorageLinksList(List<String> storageLinksList) {
        this.storageLinksList = storageLinksList;
    }
}
