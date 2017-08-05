package com.exapmple.android.dontring;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static android.R.attr.timeZone;

/**
 * Created by aritra on 7/9/2017.
 */

public class PhoneStateReceiver extends BroadcastReceiver {
    /*@Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Receiver", "received call");
        try {
            System.out.println("Receiver start");
            Toast.makeText(context," Receiver start ", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }*/
    private static final String PREFS_NAME = "MyPrefsFile";
    public static String TAG="PhoneStateReceiver";
    public boolean dontRing;
    public boolean sendSMS;
    public String sms;
    public String startAt;
    public String endAt;
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);

        dontRing = settings.getBoolean("dontRing", false);
        sendSMS = settings.getBoolean("sendSMS", false);
        sms = settings.getString("sms", "");
        startAt = settings.getString("startAt", "");
        endAt = settings.getString("endAt", "");

        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            Log.d(TAG,"PhoneStateReceiver**Call State=" + state);

            if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                Log.d(TAG,"PhoneStateReceiver**Idle");
            } else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // Incoming call
                boolean sendMessage = true;
                String incomingNumber =
                        intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG,"PhoneStateReceiver**Incoming call " + incomingNumber);

                if (!killCall(context, incomingNumber, sendMessage)) { // Using the method defined earlier
                    Log.d(TAG,"PhoneStateReceiver **Unable to kill incoming call");
                }

            } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                Log.d(TAG,"PhoneStateReceiver **Offhook");
            }
        } else if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            // Outgoing call
            String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.d(TAG,"PhoneStateReceiver **Outgoing call " + outgoingNumber);

            setResultData(null); // Kills the outgoing call

        } else {
            Log.d(TAG,"PhoneStateReceiver **unexpected intent.action=" + intent.getAction());
        }
    }

    public boolean killCall(Context context, String incomingNumber, Boolean sendMessage) {
        try {
            // Get the boring old TelephonyManager
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            // Get the getITelephony() method
            Class classTelephony = Class.forName(telephonyManager.getClass().getName());
            Method methodGetITelephony = classTelephony.getDeclaredMethod("getITelephony");

            // Ignore that the method is supposed to be private
            methodGetITelephony.setAccessible(true);

            // Invoke getITelephony() to get the ITelephony interface
            Object telephonyInterface = methodGetITelephony.invoke(telephonyManager);

            // Get the endCall method from ITelephony
            Class telephonyInterfaceClass =
                    Class.forName(telephonyInterface.getClass().getName());
            Method methodEndCall = telephonyInterfaceClass.getDeclaredMethod("endCall");

            // Invoke endCall()
            Log.i("dontRing value", Boolean.toString(dontRing));
            if(dontRing){
                Calendar startCal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                startCal.setTime(sdf.parse(startAt));// all done
                Log.i("startAt",  startAt);

                Calendar endCal = Calendar.getInstance();
                endCal.setTime(sdf.parse(endAt));// all done
                Log.i("endAt",  endAt);

                TimeZone timeZone = TimeZone.getDefault();
                Calendar calNow = Calendar.getInstance(timeZone);
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String now = dateFormat.format(calNow.getTime());
                Log.i("endAt", endAt);
                Log.i("NOW", now);
                Log.i("enddateCompare", Integer.toString(calNow.compareTo(endCal)));
                Log.i("enddateCompare", Integer.toString(calNow.compareTo(startCal)));
                if(calNow.compareTo(endCal) < 0 && calNow.compareTo(startCal) > 1){
                    methodEndCall.invoke(telephonyInterface);
                    if(sendSMS){
                        sendMessage(context, incomingNumber, sms);

                    }
                }else{
                    SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("dontRing", false);
                    editor.putBoolean("sendSMS", false);
                    editor.putString("sms", "");
                    editor.putString("startAt", "");
                    editor.putString("endAt", "");
                    // Commit the edits!
                    editor.commit();
                }

            }
        } catch (Exception ex) { // Many things can go wrong with reflection calls
            Log.d(TAG,"PhoneStateReceiver **" + ex.toString());
            return false;
        }
        return true;
    }

    public void sendMessage(Context context, String incomingNumber, String msg){
        try {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(incomingNumber, null, msg, null, null);
            Toast.makeText(context, "Message Sent",
                    Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(context,ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }

        return;
    }
}
