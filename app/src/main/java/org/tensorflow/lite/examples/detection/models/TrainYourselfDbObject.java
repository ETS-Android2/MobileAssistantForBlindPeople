package org.tensorflow.lite.examples.detection.models;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TrainYourselfDbObject implements Serializable {

    private String objectName;
    private List<String> storageLinksList;

    public TrainYourselfDbObject(String objectName, List<String> storageLinksList) {
        this.objectName = objectName;
        this.storageLinksList = storageLinksList;
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
}
