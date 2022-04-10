package org.tensorflow.lite.examples.detection.models;

import java.io.Serializable;
import java.util.List;

public class TrainYourselfDbRequestObject implements Serializable {

    private List<TrainYourselfDbObject> dbObjects;
    boolean isRequestCompleted;

    public TrainYourselfDbRequestObject(List<TrainYourselfDbObject> dbObjects, boolean isRequestCompleted) {
        this.dbObjects = dbObjects;
        this.isRequestCompleted = isRequestCompleted;
    }

    public List<TrainYourselfDbObject> getDbObjects() {
        return dbObjects;
    }

    public void setDbObjects(List<TrainYourselfDbObject> dbObjects) {
        this.dbObjects = dbObjects;
    }

    public boolean isRequestCompleted() {
        return isRequestCompleted;
    }

    public void setRequestCompleted(boolean requestCompleted) {
        isRequestCompleted = requestCompleted;
    }
}
