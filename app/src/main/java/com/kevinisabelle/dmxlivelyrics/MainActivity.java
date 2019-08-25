package com.kevinisabelle.dmxlivelyrics;

import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import android.app.*;
import java.io.*;
import java.util.Set;
import java.util.UUID;

public class MainActivity  extends Activity {
    TextView out;
    BluetoothLyricsClient btClient = null;

   PowerManager.WakeLock wakeLock = null;

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //set content view AFTER ABOVE sequence (to avoid crash)


        setContentView(R.layout.activity_main);

        out = (TextView) findViewById(R.id.out);

        out.setText("\n...In onCreate()...");
        PowerManager powerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");

        wakeLock.acquire();

        btClient = new BluetoothLyricsClient(this);
        btClient.start();
    }

    public void onStart() {
        super.onStart();
        out.append("\n...In onStart()...");
        btClient.OnStart();


    }

    final Handler handler = new Handler();
    Runnable runnableUpdateUI = new Runnable() {
        @Override
        public void run() {
            Reload();

            handler.postDelayed(this, 500);
        }
    };

    public void onResume() {
        super.onResume();
        wakeLock.acquire();
        out.append("\n...In onResume...");
        btClient.OnResume();
        // Set up a pointer to the remote node using it's address.

        handler.post(runnableUpdateUI);

    }

    public void onPause() {
        super.onPause();
        wakeLock.release();
        out.append("\nOnPause\n");
        btClient.OnPause();
    }

    public void onStop() {
        super.onStop();
        out.append("\n...In onStop()...");
        btClient.OnStop();
    }

    public void onDestroy() {
        super.onDestroy();
        btClient.OnDestroy();
        out.append("\n...In onDestroy()...");
    }

    public void Reload(View view){
        Reload();
    }

    public void Reload() {
        out.setText(btClient.getCurrentLyrics());
    }

}