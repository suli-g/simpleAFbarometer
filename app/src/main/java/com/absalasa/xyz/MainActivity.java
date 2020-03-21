package com.absalasa.xyz;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //variables
    boolean State = false;//Checks StartStopBtn state
    int i = 0;
    TextView StateView;
    Button  StartStopBtn;
    Double XCoord;
    Double YCoord;
    Double ZCoord;
    GPSdata XYZ;
    Location Location;
    StringBuilder FILE = new StringBuilder();
    FileOutputStream Out;
    Context contx;
    File filelocation;
    Uri path;
    Intent fileIntent;

    //pause loop for X seconds
    public static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.err.format("IOException: %s%n", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Variables
        StateView = (TextView) findViewById(R.id.textView);
        StartStopBtn = (Button) findViewById(R.id.button);

        //click listeners
        StartStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (State==false){
                StartStopBtn.setText("Stop");
                State=true;
                StateView.setText("     Recording GPS data." + "\n" + "Press"+" STOP "+"to save data");
                i++;
                XYZ = new GPSdata(getApplicationContext());
                Location = XYZ.getLocation();
                FILE = new StringBuilder();FILE.append("X,Y,Z");
                GPSdataLoop LOOP = new GPSdataLoop();
                LOOP.execute();
            }else {
                StartStopBtn.setText("Start");
                State=false;
                StateView.setText("Saved to: " + getFilesDir());
                i++;
                if (i > 0 && State == false) {//make sure button was pressed more than once

                    //Out = null;
                    try {
                        Out = openFileOutput("XYZ.csv", Context.MODE_PRIVATE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        //saving file
                        Out.write(FILE.toString().getBytes());
                        //Toast.makeText(MainActivity.this, "Saved to: " + getFilesDir() + "/" + "XYZ.csv", Toast.LENGTH_LONG).show();
                        Out.close();

                        //exporting file
                        contx = getApplicationContext();
                        filelocation = new File(getFilesDir(), "XYZ.csv");
                        path =  FileProvider.getUriForFile(contx, "com.absalasa", filelocation);
                        fileIntent = new Intent(Intent.ACTION_SEND);
                        fileIntent.setType("text/csv");
                        fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                        startActivity(Intent.createChooser(fileIntent, "mail"));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            }//onclick
        });//listener
    }//OnCreate

    public void GPS (){
        XYZ = new GPSdata(getApplicationContext());
        Location = XYZ.getLocation();
            }

    //runs loop until stopped by StartStopBtn
    public class GPSdataLoop extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //GPS();
        }
        protected void onPostExecute() {

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (Location != null) {
                    while (State == true) {//State == true

                        Location = XYZ.getLocation();
                        //get coordinates
                        XCoord = Location.getLatitude();
                        YCoord = Location.getLongitude();
                        ZCoord = Location.getAltitude();
                        //append file
                        FILE.append("\n" + String.valueOf(XCoord) + "," + String.valueOf(YCoord) + "," + String.valueOf(ZCoord));
                        //XCoord = null;
                        //YCoord = null;
                        //ZCoord = null;
                        i++;
                        pause(2000);
                        //XYZ = new GPSdata(getApplicationContext());
                        //Location = XYZ.getLocation();
                        //GPS();
                        if (State == false) break;
                    }//while loop
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }//background
    }//GPS data loop


    public class GPSdata implements LocationListener {
        Context context;


        public GPSdata(Context c) {
            context = c;
        }

        public Location getLocation() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context,"Permission denied",Toast.LENGTH_SHORT).show();
                return null;
            }
            LocationManager LM = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = LM.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(isGPSEnabled){
                LM.requestLocationUpdates(LM.GPS_PROVIDER,500,0,this);
                Location L = LM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return L;
            }else{
                Toast.makeText(context,"Enable GPS",Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        public void onLocationChanged(Location location) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


}//Mainactivity
