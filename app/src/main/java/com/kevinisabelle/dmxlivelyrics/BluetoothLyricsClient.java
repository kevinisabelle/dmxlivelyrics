package com.kevinisabelle.dmxlivelyrics;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothLyricsClient extends Thread {

    private String currentLyrics = "";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private InputStream inStream = null;
    private Activity activity = null;
    private boolean isRunning = false;

    private Set<BluetoothDevice> devices = null;
    BluetoothDevice device = null;

    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothLyricsClient(Activity act) {
        this.activity = act;
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        CheckBTState();
    }

    @Override
    public void run() {

        // Create a data stream so we can talk to server.
        //out.setText("\n...Sending message to server...");
        String message = "getLyrics\n";
        //out.setText("\n\n...The message that we will send to the server is: "+message);

        while(true) {

            if (isRunning) {
                try {
                    outStream = btSocket.getOutputStream();
                    inStream = btSocket.getInputStream();
                } catch (IOException e) {
                    AlertBox("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
                }

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

                byte[] msgBuffer = message.getBytes();
                try {
                    outStream.write(msgBuffer);
                    //outStream.flush();
                } catch (IOException e) {
                    String msg = "In onResume() and an exception occurred during write: " + e.getMessage();

                    AlertBox("Fatal Error", msg);
                }


                try {
                    if (bReader.ready()) {
                        String data = bReader.readLine();

                        if (data != "") {
                            currentLyrics = data.replaceAll("_", "\n");
                        }

                        while (bReader.ready()) {
                            bReader.readLine();
                        } //Flush rest of data.
                    }
                } catch (IOException e) {
                    String msg = "In onResume() and an exception occurred during readline: " + e.getMessage();
                    //AlertBox("Fatal Error", msg);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
        }

    }

    public void OnStart() {

        currentLyrics = "*** STARTING... ***";


    }

    public void OnPause() {

        isRunning = false;
        InputStream inStream;
        currentLyrics = "*** PAUSING ***";
        /*try {

            inStream = btSocket.getInputStream();
            BufferedReader bReader=new BufferedReader(new InputStreamReader(inStream));
            String lineRead=bReader.readLine();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }*/

        /*if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                AlertBox("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }*/

        try     {
            btSocket.close();
        } catch (IOException e2) {
            AlertBox("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }



    }

    public String getCurrentLyrics() {
        return currentLyrics;
    }

    public void OnResume() {

        currentLyrics = "*** Connecting... ***";
        devices =  btAdapter.getBondedDevices();
        device = devices.iterator().next();
        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            AlertBox("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();
            currentLyrics = "*** Connected : " + device.getName() + "***";
            //out.append("\n...Connection established and data link opened...");
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                AlertBox("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        isRunning = true;
    }

    public void OnStop() {

        isRunning = false;
        currentLyrics = "*** STOPPING ***";
    }

    public void OnDestroy() {

        isRunning = false;

    }

    private void CheckBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            AlertBox("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                //out.append("\n...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    public void AlertBox( String title, String message ){
        /*new AlertDialog.Builder(activity)
                .setTitle( title )
                .setMessage( message + " Press OK to exit." )
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //finish();
                    }
                }).show();*/
    }

}
