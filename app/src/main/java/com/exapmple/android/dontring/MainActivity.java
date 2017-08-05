package com.exapmple.android.dontring;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import android.provider.ContactsContract;
import android.widget.AdapterView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final short RESULT_PICK_CONTACT = 101;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 100;
    private Button startTimePickerButton;
    private Button smsSubmitButton;
    private CheckBox dontRingCheckbox;
    private CheckBox sendSMSCheckbox;
    private EditText writeSMSEdittext;
    private Button showWhiteListButton;
    private Button showBlackListButton;
    private Spinner whiteListSpinner;
    private Spinner blackListSpinner;



    String[] permissionsRequired = new String[]{Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.READ_CONTACTS
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setting default value for the sharedPreferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        Boolean dontRing = settings.getBoolean("dontRing", false);
        Boolean sendSMS = settings.getBoolean("sendSMS", false);
        String sms = settings.getString("sms", "");
        String startAt = settings.getString("startAt", "");
        String endAt = settings.getString("endAt", "");


        Log.i("dontRing", dontRing.toString());
        Log.i("sendSMS", sendSMS.toString());
        Log.i("sms", sms);
        Log.i("startAt", startAt);
        Log.i("endAt", endAt);
        startTimePickerButton = (Button) findViewById(R.id.startTimepickerButton);
        smsSubmitButton = (Button) findViewById(R.id.submit_button);
        writeSMSEdittext = (EditText) findViewById(R.id.sms_edittext);
        sendSMSCheckbox = (CheckBox) findViewById(R.id.send_sms_checkBox);
        dontRingCheckbox = (CheckBox) findViewById(R.id.dont_ring_checkBox);
        showWhiteListButton = (Button) findViewById(R.id.white_list_button);
        showBlackListButton = (Button) findViewById(R.id.black_list_button);
        whiteListSpinner = (Spinner) findViewById(R.id.white_list_spinner);
        blackListSpinner = (Spinner) findViewById(R.id.black_list_spinner);


// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.choiceToFindContacts, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        whiteListSpinner.setAdapter(adapter);
        whiteListSpinner.setOnItemSelectedListener(this);
        blackListSpinner.setAdapter(adapter);
        blackListSpinner.setOnItemSelectedListener(this);


        sendSMSCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //recording the click of the checkbox
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //testing if it is checked or unchecked
                if (isChecked) {
                    //if it is check
                    writeSMSEdittext.setVisibility(View.VISIBLE);
                    smsSubmitButton.setVisibility(View.VISIBLE);
                    Boolean sendSMS = true;
                    saveCheckBoxSharedPrefernces("sendSMS", sendSMS);

                } else {
                    writeSMSEdittext.setVisibility(View.GONE);
                    smsSubmitButton.setVisibility(View.VISIBLE);
                    Boolean sendSMS = false;
                    saveCheckBoxSharedPrefernces("sendSMS", sendSMS);

                }
            }
        });

        dontRingCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //recording the click of the checkbox
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //testing if it is checked or unchecked
                if (isChecked) {
                    //if it is check
                    Boolean dontRing = true;
                    saveCheckBoxSharedPrefernces("dontRing", dontRing);

                } else {
                    Boolean dontRing = false;
                    saveCheckBoxSharedPrefernces("dontRing", dontRing);

                }
            }
        });



            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this,
                    permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this,
                permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this,
                    permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, permissionsRequired[4]) != PackageManager.PERMISSION_GRANTED  ){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[2])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[3])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissionsRequired[4])) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Need Multiple Permissions");
                    builder.setMessage("This app needs Incoming and Outgoin Call Permission, Send SMS Permission and Read Contacts Permission");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(MainActivity.this,
                            permissionsRequired,
                            MY_PERMISSIONS_REQUEST_CALL_PHONE);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }

            }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Boolean allgranted = false;
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0){
                    for(int x = 0; x < grantResults.length;  x++){
                        if(grantResults[x]==PackageManager.PERMISSION_GRANTED){
                            allgranted = true;
                        } else {
                            allgranted = false;
                            break;
                        }
                    }

                }
                if(allgranted){

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(MainActivity.this,"Thanks For The Permissions", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this,"This app will not run without these permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void saveSMSSharedPreferences(View V){
        String smsVal = writeSMSEdittext.getText().toString();
        saveStringSharedPrefernces("sms", smsVal);
        writeSMSEdittext.setText("");
    }

    public void showTimePicker(View v){
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        if(v == startTimePickerButton){
            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            String startTime = returnTimeInString(hourOfDay, minute);
                            Log.i("startTime", startTime);
                            saveStringSharedPrefernces("startAt", startTime);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }else{
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            String endTime = returnTimeInString(hourOfDay, minute);
                            Log.i("endTime", endTime);
                            saveStringSharedPrefernces("endAt", endTime);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }


    }

    public String returnTimeInString(int hourOfDay, int minute){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        TimeZone timeZone = TimeZone.getDefault();

        Calendar cal = Calendar.getInstance(timeZone);

        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DATE);
        int year = cal.get(Calendar.YEAR);
        Calendar newCal = Calendar.getInstance(timeZone);
        newCal.set(year, month, day, hourOfDay, minute);
        return dateFormat.format(newCal.getTime());
    }
    public void saveCheckBoxSharedPrefernces(String key, boolean value){

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        Log.i("setteing" + key, Boolean.toString(value));
        // Commit the edits!
        editor.commit();

        Boolean val = settings.getBoolean(key, false);
        Log.i("after comiting setteing" + key, Boolean.toString(val));
    }

    public void saveStringSharedPrefernces(String key, String value){

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        Log.i("setteing" + key, value);
        // Commit the edits!
        editor.commit();

        String val = settings.getString(key, "");
        Log.i("after comiting setteing" + key, val);
    }

    public void showWhtieListEditText(View v){
        whiteListSpinner.setVisibility(View.VISIBLE);

    }

    public void showBlackListEditText(View v){
        blackListSpinner.setVisibility(View.VISIBLE);

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if(pos == 0){
            Intent i=new Intent(Intent.ACTION_PICK, CallLog.Calls.CONTENT_URI);
            //i.setType();
            startActivityForResult(i, RESULT_PICK_CONTACT);
        }else{
           Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
           ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
           startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // check whether the result is ok
        if (resultCode == RESULT_OK) {
            // Check for the request code, we might be usign multiple startActivityForReslut
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPickedFromContacts(data);
                    break;
                /*case SELECT_PHONE_NUMBER:
                    contactPickedFromLogs(data);
                    break;*/
            }
        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }
    /**
     * Query the Uri and read contact details. Handle the picked contact data.
     * @param data
     */
    private void contactPickedFromContacts(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null ;
            String name = null;
            // getData() method will have the Content Uri of the selected contact
            Uri uri = data.getData();
            //Query the content uri
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            // column index of the phone number
            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            // column index of the contact name
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);
            // Set the value to the textviews
            Log.i("ContactName", name);
            Log.i("ContactNumber", phoneNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


}
