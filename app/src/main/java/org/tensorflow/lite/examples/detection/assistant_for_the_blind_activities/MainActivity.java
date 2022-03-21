package org.tensorflow.lite.examples.detection.assistant_for_the_blind_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import org.tensorflow.lite.examples.detection.DetectorActivity;
import org.tensorflow.lite.examples.detection.R;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
                          implements View.OnClickListener, RecognitionListener {

    private final String LOG_TAG = "MainActivity";
    public static final int REQUEST_MICROPHONE = 1000;

    private LinearLayout mainActivityLayout;
    private CardView introduceEnvCardview;
    private CardView readDocumentCardview;
    private CardView trainYourselfCardview;
    private CardView selectModelCardview;
    private CardView guideCardview;
    private CardView voiceCommandCardview;
    private SpeechRecognizer speech;
    private Intent recognizerIntent;
    private boolean isListening;

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

        mainActivityLayout = findViewById(R.id.main_activity_layout);
        introduceEnvCardview = findViewById(R.id.introduce_env_cardview);
        readDocumentCardview = findViewById(R.id.read_document_cardview);
        trainYourselfCardview = findViewById(R.id.train_yourself_cardview);
        selectModelCardview = findViewById(R.id.select_model_cardview);
        guideCardview = findViewById(R.id.guide_cardview);
        voiceCommandCardview = findViewById(R.id.voice_command_cardview);

        mainActivityLayout.setOnClickListener(this);
        introduceEnvCardview.setOnClickListener(this);
        readDocumentCardview.setOnClickListener(this);
        trainYourselfCardview.setOnClickListener(this);
        selectModelCardview.setOnClickListener(this);
        guideCardview.setOnClickListener(this);
        voiceCommandCardview.setOnClickListener(this);

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

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.introduce_env_cardview){
            Intent intent = new Intent(MainActivity.this, DetectorActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.read_document_cardview){
            Intent intent = new Intent(MainActivity.this, ReadDocumentActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.train_yourself_cardview){
            Intent intent = new Intent(MainActivity.this, TrainYourselfActivity.class);
            startActivity(intent);
        }
        else if(v.getId() == R.id.select_model_cardview){
            Intent intent = new Intent(MainActivity.this, SelectModelActivity.class);
            startActivity(intent);
        }
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
    }

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
}