package com.etoitau.pokemonquiz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView[] choiceArray = new TextView[4];
    TextView messageView;
    ImageView imageView;
    List<Monster> pokedex = new ArrayList<>();
    List<Integer> indexes = new ArrayList<>();
    int keyIndex, nextKey;
    Random rand = new Random();
    boolean isPaused = false;

    public class Monster {
        private String name = null;
        private Drawable picture = null;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Drawable getPicture() {
            return picture;
        }

        public void setPicture(Drawable picture) {
            this.picture = picture;
        }
    }


    private void fillLibrary() throws IOException {
        AssetManager am = this.getAssets();
        String folder = "monster_images";
        String[] files = am.list(folder);

        for (String file : files) {
            Monster monster = new Monster();
            monster.setName(processName(file));
            monster.setPicture(Drawable.createFromStream(am.open(folder + "/" + file), null));
            pokedex.add(monster);
            Log.i("got: ", monster.getName());
        }
        // get set of pokedex indexes
        for (int i = 0; i < pokedex.size(); i++) {
            indexes.add(i);
        }

    }

    private String processName(String raw) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (i == 0) {
                name.append(raw.substring(0,1).toUpperCase());
            } else if (raw.charAt(i) == '0') {
                name.append('\'');
            } else if (raw.charAt(i) == '_') {
                name.append(" ");
                i++;
                name.append(raw.substring(i, i + 1).toUpperCase());
            }
            else if (raw.charAt(i) == '.') {
                break;
            } else {
                name.append(raw.charAt(i));
            }
        }
        return name.toString();
    }

    public void iChooseYou(View view) {
        if (isPaused) {
            getChallenge();
            isPaused = false;
        } else {
            if (Integer.parseInt(view.getTag().toString()) == keyIndex) {
                messageView.setText("Yep! Good job.");
            } else {
                messageView.setText("Nope. Nice try.");
            }
            for (int i = 0; i < 4; i++) {
                if (i != keyIndex) {
                    choiceArray[i].setText("");
                }
            }
            isPaused = true;
        }
    }

    public void findButtons() {
        for (int i = 0; i < choiceArray.length; i++) {
            int resID = getResources().getIdentifier("choice" + i, "id", getPackageName());
            choiceArray[i] = findViewById(resID);
        }
    }

    private void getChallenge() {
        Collections.shuffle(indexes);
        keyIndex = rand.nextInt(4);

        Monster monster = pokedex.get(indexes.get(keyIndex));
        imageView.setImageDrawable(monster.getPicture());

        for (int i = 0; i < 4; i++) {
            choiceArray[i].setText(pokedex.get(indexes.get(i)).getName());
        }
        messageView.setText("Which is it?");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageView = findViewById(R.id.messageView);
        messageView.setText("Loading...");
        findButtons();
        imageView = findViewById(R.id.imageView);

        try {
            fillLibrary();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (pokedex.size() < 4) {
            messageView.setText("Error loading");
            return;
        } else {
            messageView.setText("Which is it?");
        }

        getChallenge();

    }
}
