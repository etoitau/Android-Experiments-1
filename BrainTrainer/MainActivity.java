package com.etoitau.braintrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

/**
 * Answer as many simple addition problems as you can in the time limit
 */
public class MainActivity extends AppCompatActivity {

    TextView timerView, problemView, scoreView, messageView, startView;
    TextView[] optionViews;
    Button replayButton;
    CountDownTimer timer;
    boolean isGameInProgress = false;
    int ansIndex, correct = 0, problemCount = 0;
    final int NUMBER_SIZE = 10;
    final long TIME_LIMIT = 30; // configure game

    // when game is first started from into screen
    public void clickGo(View view) {
        isGameInProgress = true;
        startTimer();
        startView.setVisibility(View.INVISIBLE);
        timerView.setVisibility(View.VISIBLE);
        getProblem();
        problemView.setVisibility(View.VISIBLE);
        scoreView.setVisibility(View.VISIBLE);
        for (View opt : optionViews) { opt.setVisibility(View.VISIBLE); }
    }

    // when user chooses an answer
    public void clickOpt(View view) {
        if (!isGameInProgress) { return; }
        problemCount++;
        String message;
        if (Integer.parseInt(view.getTag().toString()) == ansIndex) {
            message = "Correct!";
            correct++;
        } else {
            message = "Wrong :(";
        }
        messageView.setText(message);
        messageView.setVisibility(View.VISIBLE);
        scoreView.setText(String.format(Locale.ENGLISH, "%d/%d", correct, problemCount));
        getProblem();
    }

    // when time is up and "play again" button is pressed
    public void clickReset(View view) {
        correct = 0;
        problemCount = 0;
        scoreView.setText(String.format(Locale.ENGLISH, "%d/%d", correct, problemCount));
        isGameInProgress = true;
        startTimer();
        replayButton.setVisibility(View.INVISIBLE);
        messageView.setText("");
        getProblem();
        problemView.setVisibility(View.VISIBLE);
    }

    // get a new math problem for the quiz
    public void getProblem() {
        Random rand = new Random();
        // get numbers for problem
        int a = rand.nextInt(NUMBER_SIZE + 1);
        int b = rand.nextInt(NUMBER_SIZE + 1);
        // decide where to put correct answer and put it there
        ansIndex = rand.nextInt(4);
        optionViews[ansIndex].setText(String.valueOf(a + b));
        // fill other spaces with other random answers that are not correct
        for (int i = 0; i < 4; i++) {
            if (i == ansIndex)
                continue;
            int optVal = a + b;
            while (optVal == a + b) {
                optVal = rand.nextInt(2 * NUMBER_SIZE + 1);
            }
            optionViews[i].setText(String.valueOf(optVal));
        }
        // display problem
        problemView.setText(String.format(Locale.ENGLISH, "%d + %d", a, b));
    }

    // configure and start game timer
    public void startTimer() {
        timer = new CountDownTimer(TIME_LIMIT * 1000, 1000) {
            @Override
            public void onTick(long l) {
                timerView.setText(String.format(Locale.ENGLISH,"%02ds", l / 1000));
            }

            @Override
            public void onFinish() {
                isGameInProgress = false;
                problemView.setVisibility(View.INVISIBLE);
                messageView.setText("Done!");
                messageView.setVisibility(View.VISIBLE);
                replayButton.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerView = findViewById(R.id.timeLeftView);
        problemView = findViewById(R.id.problemView);
        scoreView = findViewById(R.id.scoreView);
        messageView = findViewById(R.id.messageView);
        startView = findViewById(R.id.goView);
        optionViews = new TextView[4];
        optionViews[0] = findViewById(R.id.otp0View);
        optionViews[1] = findViewById(R.id.opt1View);
        optionViews[2] = findViewById(R.id.otp2View);
        optionViews[3] = findViewById(R.id.opt3View);
        replayButton = findViewById(R.id.replayButton);
    }
}
