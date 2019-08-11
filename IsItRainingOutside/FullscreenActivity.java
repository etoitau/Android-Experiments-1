package com.etoitau.isitrainingoutside;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private TextView messageTextView, locationTextView;
    private View layout;
    private boolean mVisible;

    GetWeather getWeather;
    final String API_URL = "https://api.openweathermap.org/data/2.5/weather?",
            API_ID_SUF = "&units=imperial&appid=672f53d7e0cb3e35d9ab859b442968d1";
    Date date;
    DateFormat df = new SimpleDateFormat("mm-dd hh:mm:ss");

    LocationManager locationManager;
    Location location;



    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            messageTextView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        messageTextView = findViewById(R.id.messageTextView);
        locationTextView = findViewById(R.id.locationTextView);
        layout = findViewById(R.id.layout);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Set up the user interaction to manually show or hide the system UI.
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });



    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

        // check weather
        doCheck();
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        messageTextView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    /**
     * Given lat/long (as prepared by locationToSearchString) does asynchronous call to
     * openweathermap api to check weather at location
     * Then calls printReport to show result
     */
    public class GetWeather extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... locations) {
            StringBuilder sb = new StringBuilder();
            URL url = null;

            HttpURLConnection urlConnection = null;

            try {
                url = new URL(API_URL + locations[0] + API_ID_SUF);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data != -1) {
                    sb.append((char) data);
                    data = reader.read();
                }
                return sb.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute (String result) {
            JSONObject jObj = null;
            try {
                jObj = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
                messageTextView.setText("Error");
                locationTextView.setText("Error processing response");
                return;
            }
            printReport(jObj);
        }

    }

    /**
     * given data returned by api call, updates UI
     * tells user when it last checked weather, how old the last report it got was,
     * what city the report was for, and it it's raining
     * @param data - json data from openweathermap api call
     */
    private void printReport(JSONObject data) {
        // As of: {time} in {city}
        String cityName, ut, message;
        StringBuilder sb = new StringBuilder();
        boolean hasTimePlace = false; // did api get any useful data about where and when
        Date now = Calendar.getInstance().getTime();

        // get time of weather report
        try {
            ut = data.getString("dt");
            df.setTimeZone(TimeZone.getDefault());
            date = new Date(Long.parseLong(ut) * 1000);
            sb.append("as of").append(df.format(date)).append("\n");
            hasTimePlace = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // get city of weather report
        try {
            cityName = data.getString("name");
            sb.append("in ").append(cityName);
            hasTimePlace = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // say whether it's raining and update background
        if (data.has("rain")) {
            messageTextView.setText("YES");
            layout.setBackgroundResource(R.drawable.valentin_muller_rain);
        } else {
            messageTextView.setText("NO");
            layout.setBackgroundResource(R.drawable.heather_shevlin_desert);
        }

        message = (hasTimePlace) ? sb.toString(): "Failed to get Time/Place";
        message += "\n\nlast checked: " + df.format(now) + "\n";
        message += "Tap here to refresh";
        locationTextView.setText(message);
    }

    // if user clicks to refresh, do that
    public void clickRefresh (View view) {
        Log.i("got click", "refresh");
        doCheck();
    }

    /**
     * get current gps location, use that to get current weather report, which will then update ui
     */
    public void doCheck () {
        // check/get permissions and get location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                Log.i("got location:", location.toString());
            } else {
                Log.i("got location:", "null");
            }
        }

        // get weather report
        try {
            getWeather = new GetWeather();
            getWeather.execute(locationToSearchString(location)).get();
        } catch (Exception e) {
            e.printStackTrace();
            messageTextView.setText("Error");
            locationTextView.setText("Error retrieving data");
        }
    }

    // turn Location object into a string that can be sent to weather API
    private String locationToSearchString(Location location) {
        StringBuilder sb = new StringBuilder();
        sb.append("lat=").append(location.getLatitude());
        sb.append("&lon=").append(location.getLongitude());
        return sb.toString();
    }
}
