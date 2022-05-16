package org.tensorflow.lite.examples.detection.models;

import java.util.List;

public class ObjectDetectionModel {

    private String modelName;
    private List<String> recognizableObjectNames;

    public ObjectDetectionModel(String modelName, List<String> recognizableObjectNames) {
        this.modelName = modelName;
        this.recognizableObjectNames = recognizableObjectNames;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<String> getRecognizableObjectNames() {
        return recognizableObjectNames;
    }

    public void setRecognizableObjectNames(List<String> recognizableObjectNames) {
        this.recognizableObjectNames = recognizableObjectNames;
    }
}
