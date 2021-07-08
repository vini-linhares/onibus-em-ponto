package br.com.virtualartsa.onibusemponto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public final static String GRUPO_SALVO = "br.com.virtualartsa.onibusemponto.GRUPO_SALVO";
    public final static String TUTORIAL_SALVO = "br.com.virtualartsa.onibusemponto.TUTORIAL_SALVO";
    Context context;

    Timer timer;
    TimerTask timerTask;
    boolean fechar = false;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

    }

    @Override
    protected void onResume() {
        super.onResume();

        //onResume we start our timer so it can start when the app comes from the background

        if(fechar){
            finish();
        }else {
            startTimer();
        }

    }

    @Override
    protected void onPause() {
        stoptimertask();
        super.onPause();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, after the first 5000ms the TimerTask will run every 10000ms
        timer.schedule(timerTask, 2000, 10000); //
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void initializeTimerTask() {

        timerTask = new TimerTask() {
            public void run() {

                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {


                        //Toast toast = Toast.makeText(getApplicationContext(), "OI", Toast.LENGTH_SHORT);
                        //toast.show();
                        stoptimertask();
                        fechar = true;

                        Intent intent = new Intent(getApplicationContext(), Home.class);
                        startActivity(intent);

                    }
                });
            }
        };
    }
}
