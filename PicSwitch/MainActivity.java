package com.example.picswitch;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private boolean isLeftGhost = true, isInitialized = false;
    long MOVE_DIST;
    final long SPEED = 5;
    AnimatorSet leftAni, rightAni;
    ImageView leftPic;
    ImageView rightPic;


    public void switchPic(View view) {
        Log.i("info", "switchPic called");

        if (!isInitialized) {
            initializeAnimations();
            isInitialized = true;
        }

        if (isLeftGhost) {
            Log.i("isleftghost", "true");
            leftAni.start();
            isLeftGhost = false;

        } else {
            Log.i("isleftghost", "false");
            rightAni.start();
            isLeftGhost = true;
        }

    }

    private void initializeAnimations() {
        // init variables
        leftPic = findViewById(R.id.leftPic);
        rightPic = findViewById(R.id.rightPic);
        MOVE_DIST = leftPic.getWidth() * 2;

        // right starts transparent, move off the screen then set opaque
        rightPic.setTranslationX(-MOVE_DIST);
        rightPic.animate().alpha(1).setDuration(1);

        // set up animations
        Animator exitLeft = ObjectAnimator.ofFloat(leftPic, "translationX", 0f, -MOVE_DIST);
        exitLeft.setDuration(MOVE_DIST / SPEED);

        Animator exitRight = ObjectAnimator.ofFloat(rightPic, "translationX", 0f, MOVE_DIST);
        exitRight.setDuration(MOVE_DIST / SPEED);

        Animator enterLeft = ObjectAnimator.ofFloat(leftPic, "translationX", MOVE_DIST, 0f);
        enterLeft.setDuration(MOVE_DIST / SPEED);

        Animator enterRight = ObjectAnimator.ofFloat(rightPic, "translationX", -MOVE_DIST, 0f);
        enterRight.setDuration(MOVE_DIST / SPEED);

        // build animator sets
        leftAni = new AnimatorSet();
        leftAni.playSequentially(exitLeft, enterRight);
        rightAni = new AnimatorSet();
        rightAni.playSequentially(exitRight, enterLeft);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
