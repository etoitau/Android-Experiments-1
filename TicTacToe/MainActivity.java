/*
 * Developed by Kyle Chatman
 * kchatman.com
 *
 * Licenced under a Creative Commons Attribution-NonCommercial 4.0 International License
 * https://creativecommons.org/licenses/by-nc/4.0/
 *
 *
 */

package com.etoitau.tictactoe;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public final int RED = 1, YEL = -1, TIE = 2, RED_PIECE = R.drawable.red, YEL_PIECE = R.drawable.yellow;
    public final long SPEED = 2;
    public Board board;
    public int currentPlayer = RED;
    public TextView messageView;
    public Button button;
    private long MOVE_DIST;
    private boolean isGameOver = false;

    /**
     * object modeling one of the nine board spaces
     */
    class gameCell {
        private ImageView element;
        private int value = 0;
        private AnimatorSet dropIn;

        public int getValue() {
            return value;
        }

        /**
         * create cell object and set up it's animations
         * @param row - row on board
         * @param col - column on board
         */
        public gameCell(int row, int col) {
            // find matching View
            String id = String.format("cell%d%d", row + 1, col + 1);
            Log.i("cell id", id);
            int resID = getResources().getIdentifier(id, "id", getPackageName());
            Log.i("res id", "" + resID);
            this.element = findViewById(resID);

            // set up animations - piece sits in place invisible until clicked, then shows animation
            // of dropping down into place when clicked
            Animator whiskUp = ObjectAnimator.ofFloat(element, "translationY", 0, -MOVE_DIST);
            whiskUp.setDuration(0);
            Animator show = ObjectAnimator.ofFloat(element, "alpha", 0, 1);
            show.setDuration(0);
            Animator drop = ObjectAnimator.ofFloat(element, "translationY", -MOVE_DIST, 0);
            drop.setDuration(MOVE_DIST / SPEED);
            drop.setInterpolator(new AccelerateInterpolator(1.5f));
            dropIn = new AnimatorSet();
            dropIn.playSequentially(whiskUp, show, drop);
        }

        // reset for new game
        public void reset() {
            this.value = 0;
            element.animate().alpha(0).setDuration(0);
        }

        // can the player make a move here or is it taken already
        public boolean isOpen() {
            return value == 0;
        }

        // if someone plays here, set piece to correct color and show animation
        public void play(int color) {
            Log.i("play clicked for:", element.getTag().toString());
            if (color == RED) {
                value = RED;
                element.setImageResource(RED_PIECE);
            } else {
                value = YEL;
                element.setImageResource(YEL_PIECE);
            }
            dropIn.start();
        }
    }

    // gameboard object for collecting gameCells and doing operations on them
    class Board {
        private gameCell[][] cells;
        private final int SIZE = 3;

        // populate with gameCell objects
        public Board() {
            this.cells = new gameCell[SIZE][SIZE];
            for (int row = 0; row < SIZE; row++) {
                for (int col = 0; col < SIZE; col++) {
                    this.cells[row][col] = new gameCell(row, col);
                }
            }
        }

        // reset for new game
        public void reset() {
            for (gameCell[] row : cells) {
                for (gameCell cell : row) {
                    cell.reset();
                }
            }
            isGameOver = false;
        }

        // check if there has been a winner or a tie
        public int checkWinner() {
            int sum, sum2;
            // check rows
            for (gameCell[] row : cells) {
                sum = 0;
                for (gameCell cell : row) {
                    sum += cell.getValue();
                }
                if (sum == 3 * RED) {
                    return RED;
                } else if (sum == 3 * YEL) {
                    return YEL;
                }
            }
            // check cols
            for (int col = 0; col < SIZE; col++) {
                sum = 0;
                for (int row = 0; row < SIZE; row++) {
                    sum += cells[row][col].getValue();
                }
                if (sum == 3 * RED) {
                    return RED;
                } else if (sum == 3 * YEL) {
                    return YEL;
                }
            }
            // check diags
            sum = 0;
            sum2 = 0;
            for (int i = 0; i < SIZE; i++) {
                sum += cells[i][i].getValue();
                sum2 += cells[SIZE - 1 - i][i].getValue();
            }
            if (sum == 3 * RED || sum2 == 3 * RED) {
                return RED;
            } else if (sum == 3 * YEL || sum2 == 3 * YEL) {
                return YEL;
            }
            // check draw
            for (gameCell[] row : cells) {
                for (gameCell cell : row) {
                    if (cell.isOpen()) {
                        return 0;
                    }
                }
            }
            return TIE;
        }
    }

    // ImageView game pieces call this when clicked in game
    public void onBoardClick(View spot) {
        // see what was clicked and get appproprate gameCell object
        String[] loc = spot.getTag().toString().split(" ");
        Log.i("clicked:", loc[0] + " " + loc[1]);
        String message;
        int row = Integer.valueOf(loc[0]) - 1;
        int col = Integer.valueOf(loc[1]) - 1;
        gameCell thisCell = board.cells[row][col];
        // if spot is taken, ignore
        if (!thisCell.isOpen() || isGameOver) {
            return;
        }
        // place piece in this spot
        thisCell.play(currentPlayer);
        // other player goes next time
        currentPlayer = (currentPlayer == RED) ? YEL: RED;
        // check if game is over
        int winner = board.checkWinner();
        // if no winner, turn done
        if (winner == 0) {
            return;
        }
        // if game is over, check how and show game over message
        if (winner == TIE) {
            message = "Tie Game";
        } else if (winner == RED) {
            message = "Red Wins!";
        } else {
            message = "Yellow Wins!";
        }
        isGameOver = true;
        showMessagePanel(message);
    }

    // hide message and new game button until needed
    private void hideMessagePanel() {
        messageView.setTranslationY(-MOVE_DIST);
        button.setTranslationY(-MOVE_DIST);
    }

    // show message and new game button
    private void showMessagePanel(String message) {
        messageView.setText(message);
        Animator msgDrop = ObjectAnimator.ofFloat(messageView, "translationY", -MOVE_DIST, 0);
        msgDrop.setDuration(MOVE_DIST / SPEED);
        Animator butDrop = ObjectAnimator.ofFloat(button, "translationY", -MOVE_DIST, 0);
        butDrop.setDuration(MOVE_DIST / SPEED);
        AnimatorSet ani = new AnimatorSet();
        ani.playTogether(msgDrop, butDrop);
        ani.start();
    }

    // if Play Again button is pressed:
    public void onButtonClick(View view) {
        hideMessagePanel();
        board.reset();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageView = findViewById(R.id.result);
        button = findViewById(R.id.button);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        MOVE_DIST = dm.heightPixels;
        hideMessagePanel();
        board = new Board();
    }
}
