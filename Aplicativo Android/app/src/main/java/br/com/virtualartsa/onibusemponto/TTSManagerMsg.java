package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * Created by Schiavetto on 05/09/2017.
 */

public class TTSManagerMsg  extends AppCompatActivity{

    private TextToSpeech mTts = null;
    private boolean isLoaded = false;
    public String msgInicio;


    public void init(Context context, String msgIniciar) {
        msgInicio = msgIniciar;
        try {
            mTts = new TextToSpeech(context, onInitListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextToSpeech.OnInitListener onInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTts.setLanguage(Locale.getDefault());
                isLoaded = true;
                initQueue(msgInicio);

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("error", "This Language is not supported");
                }
            } else {
                Log.e("error", "Initialization Failed!");
            }

            /*
            UtteranceProgressListener utteranceProgressListener = new UtteranceProgressListener() {

                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "onStart ( utteranceId :"+utteranceId+" ) ");
                }

                @Override
                public void onError(String utteranceId) {
                    Log.d(TAG, "onError ( utteranceId :"+utteranceId+" ) ");
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "onDone ( utteranceId :"+utteranceId+" ) ");
                }
            };
            */
            //
            /*
            mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

                @Override
                public void onUtteranceCompleted(final String utteranceId) {
                    System.out.println("Completed");

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "aqui", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            */
            //
        }
    };



    public void shutDown() {
        mTts.shutdown();
    }
    public void stop() {
        mTts.stop();
    }
    public boolean speaking() {
        return mTts.isSpeaking();
    }

    public void addQueue(String text) {
        //CharSequence t = text;
        //String id = "1";
        if (isLoaded) {
            mTts.speak(text, TextToSpeech.QUEUE_ADD, null);
            //mTts.speak(t, TextToSpeech.QUEUE_ADD, null, id);
        }
        else{
            Log.e("error", "TTS Not Initialized");
        }
    }

    public void initQueue(String text) {

        if (isLoaded)
            mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        else
            Log.e("error", "TTS Not Initialized");
    }

}
