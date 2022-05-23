package org.tensorflow.lite.examples.detection.models;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrainYourselfDbObject implements Serializable {

    private String objectName;
    private List<String> storageLinksList;
    private List<SelectedCoordinates> selectedCoordinatesList;

    public TrainYourselfDbObject(String objectName, List<String> storageLinksList, List<SelectedCoordinates> selectedCoordinatesList) {
        this.objectName = objectName;
        this.storageLinksList = storageLinksList;
        this.selectedCoordinatesList = selectedCoordinatesList;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public List<String> getStorageLinksList() {
        return storageLinksList;
    }

    public void setStorageLinksList(List<String> storageLinksList) {
        this.storageLinksList = storageLinksList;
    }

    public List<SelectedCoordinates> getSelectedCoordinatesList() {
        return selectedCoordinatesList;
    }

    public void setSelectedCoordinatesList(List<SelectedCoordinates> selectedCoordinatesList) {
        this.selectedCoordinatesList = selectedCoordinatesList;
    }
}
