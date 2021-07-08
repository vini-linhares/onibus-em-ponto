package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by Schiavetto on 12/09/2017.
 */

public class TTSManagerMsgSnd {

    private TextToSpeech mTts = null;
    private boolean isLoaded = false;
    public String msgInicio;
    MediaPlayer mediaInicio;
    Context context;


    public void init(Context context, String msgIniciar, MediaPlayer mediaPlayer) {
        msgInicio = msgIniciar;
        mediaInicio = mediaPlayer;
        this.context = context;
        final Context cont = context;
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
