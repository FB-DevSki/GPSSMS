package com.example.android.gpssms;

/**
 * Created by Sibe Jan on 21-7-2015.
 */
import android.app.Activity;
import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.media.AudioFormat;
        import android.media.AudioManager;
        import android.media.AudioTrack;
        import android.media.RingtoneManager;
        import android.net.Uri;
        import android.os.Bundle;
        import android.provider.Telephony;
        import android.telephony.SmsManager;
        import android.telephony.SmsMessage;
        import android.util.Log;
        import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class IncomingSms extends BroadcastReceiver {
    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();
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


    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferenceSettings = context.getSharedPreferences("myPrefs",
                Context.MODE_PRIVATE);
         preferencesEditor = preferenceSettings.edit();

        //retreives link to user data/preferences
        if ((preferenceSettings.getString("ownName", ""))!= null) {
            alarmPosLat = preferenceSettings.getLong("alarmPosLat", 0);
            alarmPosLon = preferenceSettings.getLong("alarmPosLon", 0);

            try {
                alarmPosDateTime = format.parse(preferenceSettings.getString("posDateTime", ""));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // Retrieves extended data from the SMSmanager intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    // parse : moet SMSALARM bevatten
                    // TODO moet positie in preferences zetten, mits de SMS goed is
                    message = message.toLowerCase();
                    if (message.contains("smsalarm") ){
                        playTone(440,2);

                        String[] separated = message.split(",");
                        //[0]='smsalarm', [1]=sendername,[2] =group, [3]=alarmcode, [4]='lat = ',
                        //[5]=latitude, [6] ='long = ', [7] = longitude
                        alarmPosLat = Double.parseDouble(separated[5]);
                        alarmPosLon = Double.parseDouble(separated[7]);
                        alarmPosDateTime = new Date();//this is own time and date not the senders (might be off) !

                        preferencesEditor = preferenceSettings.edit();
                        preferencesEditor.putLong("alarmPosLon", Double.doubleToRawLongBits(alarmPosLon));
                        preferencesEditor.putLong("alarmPosLat", Double.doubleToRawLongBits(alarmPosLat));
                        preferencesEditor.putString("alarmPosDateTime", alarmPosDateTime.toString());
                        preferencesEditor.commit();
                        Toast.makeText(context, "latitude" + separated[5] + " longitude " + separated [7], Toast.LENGTH_LONG).show();
                    }
                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);
        }
    }
    public void playTone(double freqOfTone, double duration) {
        //double duration = 1000;                // seconds
        //   double freqOfTone = 1000;           // hz
        int sampleRate = 8000;              // a number

        double dnumSamples = duration * sampleRate;
        dnumSamples = Math.ceil(dnumSamples);
        int numSamples = (int) dnumSamples;
        double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];


        for (int i = 0; i < numSamples; ++i) {      // Fill the sample array
            sample[i] = 5*Math.sin(freqOfTone * 2 * Math.PI * i / (sampleRate));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalized.
        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        int i = 0 ;

        int ramp = numSamples / 20 ;                                    // Amplitude ramp as a percent of sample count


        for (i = 0; i< ramp; ++i) {                                     // Ramp amplitude up (to avoid clicks)
            double dVal = sample[i];
            // Ramp up to maximum
            final short val = (short) ((dVal * 32767 * i/ramp));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }


        for (i = 0; i< numSamples - ramp; ++i) {                        // Max amplitude for most of the samples
            double dVal = sample[i];
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        for (i = 0; i< numSamples; ++i) {                               // Ramp amplitude down
            double dVal = sample[i];
            // Ramp down to zero
            final short val = (short) ((dVal * 32767 * (numSamples-i)/ramp ));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        AudioTrack audioTrack = null;                                   // Get audio track
        try {
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();                                          // Play the track
            audioTrack.write(generatedSnd, 0, generatedSnd.length);     // Load the track
        }
        catch (Exception e){
        }
        if (audioTrack != null) audioTrack.release();           // Track play done. Release track.
    }
}
