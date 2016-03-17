package com.parksjg.its.gpslocation;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import au.com.bytecode.opencsv.CSVWriter;

//import static com.parksjg.its.gpslocation.R.layout.activity_first;

public class First extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, Runnable {

    // LogCat tag
    private static final String TAG = First.class.getSimpleName();

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 1000; // 10 sec
    private static int FATEST_INTERVAL = 500; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters

    // UI elements
    private TextView lblLocation;
    private Button btnStartLocationUpdates;

    List<String[]> data;
    double latitude, longitude;
    CSVWriter writer;
    String myDate;
    String startDate;
    ImageView img;
    TextView recording;

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        writeCSV();

    }

    private void setupCSV() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                myDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
                data.add(new String[]{myDate, String.valueOf(latitude), String.valueOf(longitude)});
            }
        }, 0, 1000);//put here time 1000 milliseconds=1 second

    }

    private void writeCSV() {

        try {
            File file = new File(getExternalFilesDir(null), startDate+".csv");
            writer = new CSVWriter(new FileWriter(file, true), ',');
            // Headers:
            String[] headers = "Time, Latitude, Longitude".split(",");

            writer.writeNext(headers);

            writer.writeAll(data);
            writer.flush();
            writer.close();
            Toast.makeText(First.this, "CSV Successful!", Toast.LENGTH_SHORT).show();
//                    Log.d("CSV Writer", "CSV Writer Successful!");

        } catch (IOException e) {
//            Log.d("CSV Writer", "Error writing CSV file : " + e);
            Toast.makeText(First.this, "Error writing CSV file", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
//        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);
//        writeButton = (Button) findViewById(R.id.writeButton);
        img = (ImageView) findViewById(R.id.imageView);
        img.setVisibility(View.INVISIBLE);

        recording = (TextView) findViewById(R.id.textView2);

        data = new ArrayList<String[]>();
        startDate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        displayLocation();

//        // Show location button click listener
//        btnShowLocation.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                displayLocation();
//            }
//        });

        // Toggling the periodic location updates
        btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();

                setupCSV();
            }
        });

//        writeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                run();
//                Intent intent = new Intent(getBaseContext(), Second.class);
//                intent.putExtra("START_DATE", startDate);
//                startActivity(intent);
//            }
//        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        try {
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);
        } catch(SecurityException e) {
            Log.d("TAG","Security Exception 1 ++++++++++++++++++++++++++++++++++++++");
        }

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            lblLocation.setText(latitude + ", " + longitude);

        } else {

            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            // Changing the button text
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_stop_location_updates));

            mRequestingLocationUpdates = true;

            // Starting the location updates
            startLocationUpdates();


            Log.d(TAG, "Periodic location updates started!");
            img.setImageResource(R.drawable.recording);
            img.setVisibility(View.VISIBLE);
            recording.setText("Recording Session");



        } else {
            // Changing the button text
            btnStartLocationUpdates
                    .setText(getString(R.string.btn_start_location_updates));

            mRequestingLocationUpdates = false;

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");

            run();
            Intent intent = new Intent(getBaseContext(), Second.class);
            intent.putExtra("START_DATE", startDate);
            startActivity(intent);
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {

        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch(SecurityException e) {
            Log.d("TAG","Security Exception 2 +++++++++++++++++++++++++++++++++++++++++");
        }

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        // Displaying the new location on UI
        displayLocation();
    }


}
