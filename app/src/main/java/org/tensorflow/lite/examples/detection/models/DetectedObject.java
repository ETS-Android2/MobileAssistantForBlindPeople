package org.tensorflow.lite.examples.detection.models;

import java.util.Map;

public class DetectedObject {

    private String objectName;
    private String objectNameTr;
    private int objectRealSizeInMm;

    public DetectedObject(String objectName, String objectNameTr, int objectRealSizeInMm) {
        this.objectName = objectName;
        this.objectNameTr = objectNameTr;
        this.objectRealSizeInMm = objectRealSizeInMm;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectNameTr() {
        return objectNameTr;
    }

    public void setObjectNameTr(String objectNameTr) {
        this.objectNameTr = objectNameTr;
    }

    public int getObjectRealSizeInMm() {
        return objectRealSizeInMm;
    }

    public void setObjectRealSizeInMm(int objectRealSizeInMm) {
        this.objectRealSizeInMm = objectRealSizeInMm;
    }
}
