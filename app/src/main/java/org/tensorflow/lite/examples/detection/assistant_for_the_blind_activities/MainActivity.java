package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.tensorflow.lite.examples.detection.DetectorActivity;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.utils.GoogleLogin;
import org.tensorflow.lite.examples.detection.utils.TextToSpeechUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener, RecognitionListener, GoogleApiClient.OnConnectionFailedListener {

    private final String LOG_TAG = "MainActivity";
    public static final int REQUEST_MICROPHONE = 1000;
    private static final int REQUEST_CAMERA= 1001;
    public static final int SIGN_IN_GOOGLE = 1;

    private AppCompatButton loginWithGoogleButton;
    private LinearLayout mainActivityLayout;
    private CardView introduceEnvCardview;
    private CardView readDocumentCardview;
    private CardView trainYourselfCardview;
    private CardView selectModelCardview;
    // private CardView guideCardview;
    // private CardView voiceCommandCardview;
    private SpeechRecognizer speech;
    private Intent recognizerIntent;
    private boolean isListening;

    FirebaseFunctions mFunctions;
    private TextToSpeechUtil textToSpeechUtil;
    private GoogleLogin googleLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_MICROPHONE);
        }

        loginWithGoogleButton = findViewById(R.id.login_with_google_button);
        mainActivityLayout = findViewById(R.id.main_activity_layout);
        introduceEnvCardview = findViewById(R.id.introduce_env_cardview);
        readDocumentCardview = findViewById(R.id.read_document_cardview);
        trainYourselfCardview = findViewById(R.id.train_yourself_cardview);
        selectModelCardview = findViewById(R.id.select_model_cardview);
        // guideCardview = findViewById(R.id.guide_cardview);
        // voiceCommandCardview = findViewById(R.id.voice_command_cardview);

        loginWithGoogleButton.setOnClickListener(this);
        mainActivityLayout.setOnClickListener(this);
        introduceEnvCardview.setOnClickListener(this);
        readDocumentCardview.setOnClickListener(this);
        trainYourselfCardview.setOnClickListener(this);
        selectModelCardview.setOnClickListener(this);
        // guideCardview.setOnClickListener(this);
        // voiceCommandCardview.setOnClickListener(this);

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR");  // WORKS
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        // recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        textToSpeechUtil = new TextToSpeechUtil(getApplicationContext());
        textToSpeechUtil.setupTextToSpeech();

        googleLogin = new GoogleLogin(MainActivity.this);
        googleLogin.createGoogleApiClient();
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.login_with_google_button){
            googleLogin.onGoogleButtonClicked();
        }
        else if(v.getId() == R.id.introduce_env_cardview){
            Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.read_document_cardview){
            openCamera();
        }
        else if(v.getId() == R.id.train_yourself_cardview){
            Intent intent = new Intent(MainActivity.this, TrainYourselfActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.select_model_cardview){
            Intent intent = new Intent(MainActivity.this, SelectModelActivity.class);
            startActivity(intent);
        }
        /*
        else if(v.getId() == R.id.guide_cardview){
            Intent intent = new Intent(MainActivity.this, GuideActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.voice_command_cardview){
            if(!isListening)
                speech.startListening(recognizerIntent);
            else
                speech.stopListening();

            isListening = !isListening;
            Log.i(LOG_TAG, "clicked");

        }
         */
    }

    // region Google Login



    // endregion

    // region Speech To Text

    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
    }

    @Override
    public void onError(int error) {
        String errorMessage = getErrorText(error);
        Log.i(LOG_TAG, "FAILED " + errorMessage);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        for(String match : matches){
            Log.i(LOG_TAG, match);
            match = match.trim().toLowerCase();
            if(match.contains("tanıt")){
                Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
                startActivity(intent);
            }
            else if(match.contains("oku")){
                Intent intent = new Intent(MainActivity.this, ReadDocumentActivity.class);
                startActivity(intent);
            }
            else if(match.contains("eğit")){
                Intent intent = new Intent(MainActivity.this, TrainYourselfActivity.class);
                startActivity(intent);
            }
            else if(match.contains("model") || match.contains("seç")){
                Intent intent = new Intent(MainActivity.this, SelectModelActivity.class);
                startActivity(intent);
            }
            else if(match.contains("yönlendir")){
                Intent intent = new Intent(MainActivity.this, GuideActivity.class);
                startActivity(intent);
            }
        }

    }

    public static String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "RecognitionService busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Error from server";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Didn't understand, please try again.";
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    // endregion

    // region Text Recognition

    private void openCamera(){
        // open the camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA); // After taking the picture, we will continue. Thats why we use sAForResult
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_GOOGLE) {

            googleLogin.onReturnFromGoogleScreen(data);

        }
        else if(requestCode == REQUEST_CAMERA){

            if(data == null){
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(MainActivity.this, "Loading", Toast.LENGTH_SHORT).show();
            Bundle bundle = data.getExtras();
            if(bundle == null){
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                return;
            }
            Bitmap bitmap = (Bitmap) bundle.get("data");

            // Source: https://firebase.google.com/docs/ml/android/recognize-text?utm_source=studio#before-you-begin

            // Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // Scale down bitmap size
            // bitmap = scaleBitmapDown(bitmap, 640);

            // Convert bitmap to base64 encoded string
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            mFunctions = FirebaseFunctions.getInstance();

            // Create json request to cloud vision
            JsonObject request = new JsonObject();
            // Add image to request
            JsonObject image = new JsonObject();
            image.add("content", new JsonPrimitive(base64encoded));
            request.add("image", image);
            //Add features to the request
            JsonObject feature = new JsonObject();
            feature.add("type", new JsonPrimitive("DOCUMENT_TEXT_DETECTION"));
            // Alternatively, for DOCUMENT_TEXT_DETECTION:
            //feature.add("type", new JsonPrimitive("DOCUMENT_TEXT_DETECTION"));
            JsonArray features = new JsonArray();
            features.add(feature);
            request.add("features", features);

            // Optionally, provide language hints to assist with language detection (see supported languages):
            // TODO: tr ekleyebilirim.
            JsonObject imageContext = new JsonObject();
            JsonArray languageHints = new JsonArray();
            languageHints.add("en");
            imageContext.add("languageHints", languageHints);
            request.add("imageContext", imageContext);

            Task<JsonElement> task = annotateImage(request.toString())
                    .addOnCompleteListener(new OnCompleteListener<JsonElement>() {
                        @Override
                        public void onComplete(@NonNull Task<JsonElement> task) {
                            if (!task.isSuccessful()) {
                                // Task failed with an exception
                                Log.i("log_text", task.getException().getMessage());
                            } else {
                                // Task completed successfully
                                // textAnnotations'da daha iyi sonuç var gibi.

                                JsonObject annotation = task.getResult().getAsJsonArray().get(0).getAsJsonObject().get("fullTextAnnotation").getAsJsonObject();

                                // bir şey dönmeyince null exception

                                textToSpeechUtil.startTextToSpeech(annotation.get("text").getAsString());

                                Log.i("log_text", annotation.get("text").getAsString());
                            }
                        }
                    });

        }

    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private Task<JsonElement> annotateImage(String requestJson) {
        return mFunctions
                .getHttpsCallable("annotateImage")
                .call(requestJson)
                .continueWith(new Continuation<HttpsCallableResult, JsonElement>() {
                    @Override
                    public JsonElement then(@NonNull Task<HttpsCallableResult> task) {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("log_text", "annotateImage fonk: " +  e.getMessage());

                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
    }


    // endregion


}