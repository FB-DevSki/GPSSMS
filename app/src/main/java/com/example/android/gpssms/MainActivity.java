package com.example.android.gpssms;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    IncomingSms mReceiver = new IncomingSms();

    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferencesEditor;
    private static final int PREFERENCES_PRIVATE = 0;
    public static final String PREFS_NAME = "SMS_ALARM_PREFS";

    String alarmMessage;

    private ArrayList<String> status;

    //    private GoogleMap mMap;
    private String ownName ="";
    private String ownGroup ="";
    private String ownAlarmCode="";
    private String callingSMSPhoneNr = "";
    private String participantName1 = "";
    private String participantTelNr1 = "";
    private String participantName2 = "";
    private String participantTelNr2 = "";
    private String participantName3 = "";
    private String participantTelNr3 = "";
    private String participantName4 = "";
    private String participantTelNr4 = "";
    private double alarmPosLon = 0;
    private double alarmPosLat = 0;
    private double ownPosLon = 0;
    private double ownPosLat = 0;
    private Date alarmPosDateTime = null;
    private Date ownPosDateTime = null;
    private boolean preferencesValid = false;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private LocationManager lm;
    private Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferenceSettings = getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);
        preferencesEditor = preferenceSettings.edit();

        //if preferences file exists
        if ((preferenceSettings.getString("ownName", "0"))!= null) {
            ownName = preferenceSettings.getString("ownName", "");
            ownGroup = preferenceSettings.getString("ownGroup", "");
            ownAlarmCode = preferenceSettings.getString("ownAlarmCode", "");
            callingSMSPhoneNr = preferenceSettings.getString("callingSMSPhoneNr", "");
            participantName1 = preferenceSettings.getString("participantName1", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr1","");
            participantName1 = preferenceSettings.getString("participantName2", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr2","");
            participantName1 = preferenceSettings.getString("participantName3", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr3","");
            participantName1 = preferenceSettings.getString("participantName4", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr4", "");
            alarmPosLat = preferenceSettings.getLong("alarmPosLat", 0);
            alarmPosLon = preferenceSettings.getLong("alarmPosLon", 0);
            ownPosLat = preferenceSettings.getLong("alarmPosLat", 0);
            ownPosLon = preferenceSettings.getLong("alarmPosLon", 0);
            try {
                alarmPosDateTime = format.parse(preferenceSettings.getString("posDateTime", ""));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try {
                ownPosDateTime = format.parse(preferenceSettings.getString("posDateTime", ""));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            preferencesValid = true;
        } else {
            preferenceSettings = getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);
            preferencesEditor = preferenceSettings.edit();
            preferencesEditor.putString("ownName", ownName);
            preferencesEditor.putString("ownGroup", ownGroup);
            preferencesEditor.putString("ownAlarmCode", ownAlarmCode);
            preferencesEditor.putString("callingSMSPhoneNr", callingSMSPhoneNr);
            preferencesEditor.putString("participantName1", participantName1);
            preferencesEditor.putString("participantTelNr1", participantTelNr1);
            preferencesEditor.putString("participantName2", participantName2);
            preferencesEditor.putString("participantTelNr2", participantTelNr2);
            preferencesEditor.putString("participantName3", participantName3);
            preferencesEditor.putString("participantTelNr3", participantTelNr3);
            preferencesEditor.putString("participantName4", participantName4);
            preferencesEditor.putString("participantTelNr4", participantTelNr4);
            preferencesEditor.putLong("alarmPosLat", (long) alarmPosLat);
            preferencesEditor.putLong("alarmPosLon", (long) alarmPosLon);
            preferencesEditor.putLong("ownPosLat", (long) ownPosLat);
            preferencesEditor.putLong("ownPosLon", (long) ownPosLon);
            preferencesEditor.putString("alarmPosDateTime", alarmPosDateTime.toString());
            preferencesEditor.putString("alarmPosDateTime", ownPosDateTime.toString() );
            preferencesEditor.commit();

        }

        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setEnabled(false);

        //start localisation on slow rate
        boolean isGPSEnabled = false;
        boolean isNetworkEnabled = false;
        boolean canGetLocation = false;
        final long MIN_DIST_FOR_UPDATES = 10; // 10 meters
        final long MIN_TIME_BTW_UPDATES = 1000 * 60 * 1; // 1 minute

        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        // getting GPS status
        isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
        } else {
            canGetLocation = true;
            // First get location from Network Provider
            if (isNetworkEnabled) {
                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BTW_UPDATES,
                        MIN_DIST_FOR_UPDATES, this);
                Log.d("Network Enabled", "Enabled");
                if (lm != null) {
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        alarmPosLat = location.getLatitude();
                        alarmPosLon = location.getLongitude();
                    }
                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                Log.d("GPS Enabled ", "Enabled");
                if (location == null) {
                    lm.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BTW_UPDATES,
                            MIN_DIST_FOR_UPDATES, this);
                    if (lm != null) {
                        location = lm
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            alarmPosLat = location.getLatitude();
                            alarmPosLon = location.getLongitude();
                        }
                    }
                }
            }

            preferenceSettings = getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);
            preferencesEditor = preferenceSettings.edit();
            //voor test
            //alarmPosLon =8.7;  //Pforzheim
            //alarmPosLat = 48.9;

            preferencesEditor.putLong("alarmPosLon", Double.doubleToRawLongBits(alarmPosLon));
            preferencesEditor.putLong("alarmPosLat", Double.doubleToRawLongBits(alarmPosLat));
            preferencesEditor.commit();
            //set sendButton Enabled
            btnSend.setEnabled(true);
        }
    }

    public void onBtnSendClicked (View view){
        alarmMessage = "SMSALARM Lat " + alarmPosLat +", Lon " + alarmPosLon;
        SmsManager sms = SmsManager.getDefault();
        String phoneNumber = "0650907029";
        sms.sendTextMessage(phoneNumber, null, alarmMessage, null, null);
        Intent intent = new Intent(getApplicationContext(), ActivityMap.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        ownPosLat = location.getLatitude();
        ownPosLon = location.getLongitude();
        preferencesEditor.putLong("alarmPosLon", Double.doubleToRawLongBits(ownPosLon));
        preferencesEditor.putLong("alarmPosLat", Double.doubleToRawLongBits(ownPosLat));
        preferencesEditor.commit();
        Toast.makeText(this, "GPS UPDATE latitude " + alarmPosLat + " longitude "
                + alarmPosLon, Toast.LENGTH_LONG).show();
        Log.v("LOCALISATIE", "update");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
