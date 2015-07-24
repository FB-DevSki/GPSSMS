package com.example.android.gpssms;

/**
 * Created by Sibe Jan on 21-7-2015.
 */
import android.content.SharedPreferences;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.support.v4.app.FragmentActivity;
        import android.os.Bundle;
        import android.util.Log;
        import android.widget.Toast;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.MapFragment;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.SupportMapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.MarkerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ActivityMap extends FragmentActivity implements LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private LocationManager lm;
    private Location location;
    MapFragment mapFragment;
    LocationListener mListener;
    LatLng mLatLng;
    String sPosition;
    LocationListener listener;

    private SharedPreferences preferenceSettings;
    private SharedPreferences.Editor preferencesEditor;
    private static final int PREFERENCES_PRIVATE = 0;
    public static final String PREFS_NAME = "SMS_ALARM_PREFS";

    private String ownName;
    private String ownGroup;
    private String ownAlarmCode;
    private String callingSMSPhoneNr;
    private String participantName1;
    private String participantTelNr1;
    private String participantName2;
    private String participantTelNr2;
    private String participantName3;
    private String participantTelNr3;
    private String participantName4;
    private String participantTelNr4;
    private double alarmPosLon = 0;
    private double alarmPosLat = 0;
    private double ownPosLon = 0;
    private double ownPosLat = 0;
    private Date alarmPosDateTime = null;
    private Date ownPosDateTime = null;
    private boolean preferencesValid = false;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapfragment);
        setUpMapIfNeeded();
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        // voor test

        preferenceSettings = getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);
        preferencesEditor = preferenceSettings.edit();

        //TODO moet positie lezen uit de PREFERENCES

        //if preferences file exists
        if ((preferenceSettings.getString("ownName", "0")) != null) {
            ownName = preferenceSettings.getString("ownName", "");
            ownGroup = preferenceSettings.getString("ownGroup", "");
            ownAlarmCode = preferenceSettings.getString("ownAlarmCode", "");
            callingSMSPhoneNr = preferenceSettings.getString("callingSMSPhoneNr", "");
            participantName1 = preferenceSettings.getString("participantName1", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr1", "");
            participantName1 = preferenceSettings.getString("participantName2", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr2", "");
            participantName1 = preferenceSettings.getString("participantName3", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr3", "");
            participantName1 = preferenceSettings.getString("participantName4", "");
            participantTelNr1 = preferenceSettings.getString("participantTelNr4", "");
            alarmPosLat = Double.parseDouble(preferenceSettings.getString("alarmPosLat", "0"));
            alarmPosLon = Double.parseDouble(preferenceSettings.getString("alarmPosLon", "0"));
            ownPosLat = Double.parseDouble(preferenceSettings.getString("ownPosLat", "0"));
            ownPosLon = Double.parseDouble(preferenceSettings.getString("ownPosLon", "0"));
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
            Toast.makeText(this, "ACTIVITY MAP geen preferences", Toast.LENGTH_LONG).show();
        }

        preferencesEditor.commit();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     */
    private void setUpMap() {
        preferenceSettings = getSharedPreferences(PREFS_NAME, getApplicationContext().MODE_PRIVATE);

        alarmPosLat = Double.parseDouble((preferenceSettings.getString("ownPosLat", "0")));
        alarmPosLon = Double.parseDouble((preferenceSettings.getString("ownPosLon", "0")));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(alarmPosLat, alarmPosLon), 14.0f));
        mMap.getUiSettings().setZoomGesturesEnabled(false); //kan nog wel 'pan'-en
        mMap.addMarker(new MarkerOptions().position(new LatLng(alarmPosLat,alarmPosLon)).title("Marker"));
    }


    @Override
    public void onLocationChanged(Location location) {
        if( mListener != null )
        {
            mListener.onLocationChanged( location );

            Toast.makeText(this, "Location change", Toast.LENGTH_LONG).show();

            //Move the camera to the user's location and zoom in!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(alarmPosLat,alarmPosLon), 14.0f));
            mMap.addMarker(new MarkerOptions().position(new LatLng(alarmPosLat, alarmPosLon)).title("Marker"));
        }
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
}