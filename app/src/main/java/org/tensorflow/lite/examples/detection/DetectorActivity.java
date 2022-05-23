/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import static org.tensorflow.lite.examples.detection.adapters.CustomModelAdapter.IS_CUSTOM_MODEL_SELECTED;
import static org.tensorflow.lite.examples.detection.adapters.CustomModelAdapter.SELECTED_CUSTOM_MODEL;
import static org.tensorflow.lite.examples.detection.adapters.DefaultModelAdapter.SELECTED_DEFAULT_MODEL_FILENAME;
import static org.tensorflow.lite.examples.detection.utils.Constants.SP_NAME;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.examples.detection.customview.OverlayView;
import org.tensorflow.lite.examples.detection.customview.OverlayView.DrawCallback;
import org.tensorflow.lite.examples.detection.env.BorderedText;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;
import org.tensorflow.lite.examples.detection.models.DetectedObject;
import org.tensorflow.lite.examples.detection.tflite.Detector;
import org.tensorflow.lite.examples.detection.tflite.TFLiteObjectDetectionAPIModel2;
import org.tensorflow.lite.examples.detection.tracking.MultiBoxTracker;
import org.tensorflow.lite.examples.detection.utils.TextToSpeechUtil;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
  private static final Logger LOGGER = new Logger();

  // Configuration values for the prepackaged SSD model.
  private static final int TF_OD_API_INPUT_SIZE = 300;
  private static final boolean TF_OD_API_IS_QUANTIZED = true;
  private static String TF_OD_API_MODEL_FILE = "detect.tflite";
  // private static final String TF_OD_API_MODEL_FILE = "modelKaldiracVeSarj.tflite";
  // private static final String TF_OD_API_MODEL_FILE = "modelSonD.tflite";
  private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
  private static final DetectorMode MODE = DetectorMode.TF_OD_API;
  // Minimum detection confidence to track a detection.
  private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
  private static final boolean MAINTAIN_ASPECT = false;
  private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
  private static final boolean SAVE_PREVIEW_BITMAP = false;
  private static final float TEXT_SIZE_DIP = 10;
  OverlayView trackingOverlay;
  private Integer sensorOrientation;

  private Detector detector;

  private long lastProcessingTimeMs;
  private Bitmap rgbFrameBitmap = null;
  private Bitmap croppedBitmap = null;
  private Bitmap cropCopyBitmap = null;

  private boolean computingDetection = false;

  private long timestamp = 0;

  private Matrix frameToCropTransform;
  private Matrix cropToFrameTransform;

  private MultiBoxTracker tracker;

  private BorderedText borderedText;
  private TextToSpeechUtil textToSpeechUtil;

  private final float focalLength = (float) 5.23;  // Benim telefonumda bu şekilde. Bu direkt kameradan döndüğü.
  private final float sensorHeight = (float) 5.5488;      // mm cinsinden.   // 0 -> 5.5488   1 -> 3.168
  private int cropSize;
  private List<DetectedObject> modelObjects;
  private SharedPreferences mPrefs;
  private SharedPreferences.Editor prefsEditor;

  @Override
  public void onPreviewSizeChosen(final Size size, final int rotation) {
    final float textSizePx =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
    borderedText = new BorderedText(textSizePx);
    borderedText.setTypeface(Typeface.MONOSPACE);

    mPrefs = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
    prefsEditor = mPrefs.edit();

    tracker = new MultiBoxTracker(this);
    textToSpeechUtil = new TextToSpeechUtil(getApplicationContext());
    textToSpeechUtil.setupTextToSpeech();

    // TODO get detectedObject sizes and turkish names
    modelObjects = new ArrayList<>();
    try (BufferedReader br =
                 new BufferedReader(
                         new InputStreamReader(
                                 getAssets().open("labelmap.txt"), Charset.defaultCharset()))) { // metadata.getAssociatedFile(labelFilename)
      String line = null;
      while ((line = br.readLine()) != null) {
        if(line.equals("???"))
          continue;

        String[] components = line.split(",");
        modelObjects.add(new DetectedObject(components[0], components[2], Integer.parseInt(components[1])));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    cropSize = TF_OD_API_INPUT_SIZE;

    boolean isCustomModel = mPrefs.getBoolean(IS_CUSTOM_MODEL_SELECTED, false);

    if(!isCustomModel){
      String selectedDefaultModelFilename = mPrefs.getString(SELECTED_DEFAULT_MODEL_FILENAME, "");
      if(!selectedDefaultModelFilename.equals(""))
        TF_OD_API_MODEL_FILE = selectedDefaultModelFilename;
    }
    else{
      TF_OD_API_MODEL_FILE = mPrefs.getString(SELECTED_CUSTOM_MODEL, "");
    }

    try {
      detector =
          TFLiteObjectDetectionAPIModel2.create(
              this,
              TF_OD_API_MODEL_FILE,
              isCustomModel,
              TF_OD_API_LABELS_FILE,
              TF_OD_API_INPUT_SIZE,
              TF_OD_API_IS_QUANTIZED);
      cropSize = TF_OD_API_INPUT_SIZE;
    } catch (final IOException e) {
      e.printStackTrace();
      LOGGER.e(e, "Exception initializing Detector!");
      Toast toast =
          Toast.makeText(
              getApplicationContext(), "Detector could not be initialized", Toast.LENGTH_SHORT);
      toast.show();
      finish();
    }

    previewWidth = size.getWidth();
    previewHeight = size.getHeight();

    sensorOrientation = rotation - getScreenOrientation();
    LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

    LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
    rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
    croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

    frameToCropTransform =
        ImageUtils.getTransformationMatrix(
            previewWidth, previewHeight,
            cropSize, cropSize,
            sensorOrientation, MAINTAIN_ASPECT);

    cropToFrameTransform = new Matrix();
    frameToCropTransform.invert(cropToFrameTransform);

    trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
    trackingOverlay.addCallback(
        new DrawCallback() {
          @Override
          public void drawCallback(final Canvas canvas) {
            tracker.draw(canvas);
            if (isDebug()) {
              tracker.drawDebug(canvas);
            }
          }
        });

    tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
  }

  @Override
  protected void processImage() {
    ++timestamp;
    final long currTimestamp = timestamp;
    trackingOverlay.postInvalidate();

    // No mutex needed as this method is not reentrant.
    if (computingDetection) {
      readyForNextImage();
      return;
    }
    computingDetection = true;
    LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

    rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

    readyForNextImage();

    final Canvas canvas = new Canvas(croppedBitmap);
    canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
    // For examining the actual TF input.
    if (SAVE_PREVIEW_BITMAP) {
      ImageUtils.saveBitmap(croppedBitmap);
    }

    runInBackground(
        new Runnable() {
          @Override
          public void run() {

            try{
              LOGGER.i("Running detection on image " + currTimestamp);
              final long startTime = SystemClock.uptimeMillis();

              final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);

              // TODO: Seslendirmeye detay eklenecek. Tam olarak nerede tespit etti?
              // TODO: Sadece ekrandaki yeri mi verilebiliyor? Location'a dair bir şey var mı bakılacak?
              // TODO: Türkçe okuyor, tanıdıklarının isimleri ingilizce. Onlar türkçe'ye çevrilebilir.

              //if(!textToSpeechUtil.isSpeaking())
              //textToSpeechUtil.startTextToSpeech(results.get(0).getTitle());

              lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

              cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
              final Canvas canvas = new Canvas(cropCopyBitmap);
              final Paint paint = new Paint();
              paint.setColor(Color.RED);
              paint.setStyle(Style.STROKE);
              paint.setStrokeWidth(2.0f);

              float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
              switch (MODE) {
                case TF_OD_API:
                  minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                  break;
              }

              final List<Detector.Recognition> mappedRecognitions =
                      new ArrayList<Detector.Recognition>();

              for (final Detector.Recognition result : results) {
                final RectF location = result.getLocation();
                if (location != null && result.getConfidence() >= minimumConfidence) {
                  // Sonuç minimum confidence'tan daha yüksekse

                  float objectHeightInPixels = location.height();
                  int imageHeightInPixels = cropSize;
                  int realObjectHeightInMm = 0;
                  String objectTurkishName = "";

                /*
                for(int i = 0; i < modelObjects.size(); i++){
                    if(modelObjects.get(i).getObjectName().equals(result.getTitle())){
                       realObjectHeightInMm = modelObjects.get(i).getObjectRealSizeInMm();
                       objectTurkishName = modelObjects.get(i).getObjectNameTr();
                       break;
                    }
                },

                 */
                  // Log.i("distanceLog", focalLength + " focalLength");
                  // Log.i("distanceLog", laptopRealHeight + " laptopRealHeight");
                  // Log.i("distanceLog", previewHeight + " previewHeight");
                  // Log.i("distanceLog", objectHeightInPixels + " objectHeightInPixels");
                  // Log.i("distanceLog", imageHeightInPixels + " imageHeightInPixels");
                  // Log.i("distanceLog", sensorHeight + " sensorHeight");

                  float distanceInMm = (focalLength * (float)realObjectHeightInMm * imageHeightInPixels) / (objectHeightInPixels * sensorHeight);

                  // result.setTitle(objectTurkishName + " " + (float)(distanceInMm / 10) + " cm");

                  Log.i("distanceLog", objectTurkishName + " : " + distanceInMm);

                  //Toast.makeText(DetectorActivity.this, objectTurkishName + " : " + distanceInMm, Toast.LENGTH_SHORT).show();

                  canvas.drawRect(location, paint);

                  cropToFrameTransform.mapRect(location);

                  result.setLocation(location);
                  mappedRecognitions.add(result);
                }
              }

              tracker.trackResults(mappedRecognitions, currTimestamp);
              trackingOverlay.postInvalidate();

              computingDetection = false;
            }
            catch(Exception ex){
              Log.i("DetectorActivity", ex.getMessage());
            }

          }
        });
  }

  @Override
  protected int getLayoutId() {
    return R.layout.tfe_od_camera_connection_fragment_tracking;
  }

  @Override
  protected Size getDesiredPreviewFrameSize() {
    return DESIRED_PREVIEW_SIZE;
  }

  @Override
  public void onClick(View v) {

  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

  }

  // Which detection model to use: by default uses Tensorflow Object Detection API frozen
  // checkpoints.
  private enum DetectorMode {
    TF_OD_API;
  }

  @Override
  protected void setUseNNAPI(final boolean isChecked) {
    runInBackground(
        () -> {
          try {
            detector.setUseNNAPI(isChecked);
          } catch (UnsupportedOperationException e) {
            LOGGER.e(e, "Failed to set \"Use NNAPI\".");
            runOnUiThread(
                () -> {
                  Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
          }
        });
  }

  @Override
  protected void setNumThreads(final int numThreads) {
    runInBackground(() -> detector.setNumThreads(numThreads));
  }
}
