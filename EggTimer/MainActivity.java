package com.etoitau.eggtimer;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.Timer;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mp;
    AudioManager audioManager;
    SeekBar timeBar, volBar;
    CountDownTimer timer;
    TextView clock;
    boolean isTimerRunning;


    public void startResetTimer(View view) {
        if (isTimerRunning) {
            timer.cancel();
            isTimerRunning = false;
            setTimer();
        } else {
            setTimer();
            timer.start();
        }
    }

    public void setTimer() {
        int timeBarPosition = timeBar.getProgress();
        clock.setText(String.format(Locale.ENGLISH, "%d:%02d", timeBarPosition / 60, timeBarPosition % 60));

        timer = new CountDownTimer(timeBar.getProgress() * 1000, 1000) {
            @Override
            public void onTick(long mSecLeft) {
                isTimerRunning = true;
                int min = (int) mSecLeft / 60000;
                int sec = (int) mSecLeft % 60000 / 1000;
                clock.setText(String.format(Locale.ENGLISH, "%d:%02d", min, sec));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                mp.start();
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        clock = findViewById(R.id.clock);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        final int MAX_VOLUME = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        mp = MediaPlayer.create(this, R.raw.rooster);


        timeBar = findViewById(R.id.timeBar);
        timeBar.setMax(600); // ten minutes in seconds
        volBar = findViewById(R.id.volumeBar);
        volBar.setMax(MAX_VOLUME); // set volume control max to system max as found above

        timeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // if they adjust progress bar, set timer to new position
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setTimer();
            }

            // if they touch progress bar, cancel any current timer
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (isTimerRunning) {
                    timer.cancel();
                    isTimerRunning = false;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
