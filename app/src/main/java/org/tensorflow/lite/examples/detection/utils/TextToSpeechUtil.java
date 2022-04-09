package org.tensorflow.lite.examples.detection.utils;

import android.content.Context;
import android.util.Log;

import java.util.Locale;

public class TextToSpeechUtil {

    private Context mContext;
    private android.speech.tts.TextToSpeech textToSpeech;
    private int textToSpeechMode;

    public TextToSpeechUtil(Context context){
        mContext = context;
    }

    public void setupTextToSpeech(){

        textToSpeech = new android.speech.tts.TextToSpeech(mContext, status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                // TODO Burası önemli, burayı değiştirdim, TR değil, istiklalde öyle.
                Locale loc = new Locale("TR");
                textToSpeech.setLanguage(loc);

                // textToSpeech.setSpeechRate(mPrefs.getFloat("listening_speed", (float) 0.85));
            } else {
                Log.i("TTS", "Initilization Failed!");
            }
        });
    }

    public void startTextToSpeech(String speech){
        textToSpeechMode = 1;

        textToSpeech.speak(speech, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
    }

    public boolean isSpeaking(){
        return textToSpeech.isSpeaking();
    }

}
