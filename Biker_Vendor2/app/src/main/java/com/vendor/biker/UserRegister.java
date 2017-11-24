package com.vendor.biker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.IsNetworkConnection;
import com.vendor.biker.Utils.PrintClass;
import com.vendor.biker.Utils.post_async;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Teju on 19/09/2017.
 */
public class UserRegister extends AppCompatActivity {

    private View rootView;
    private EditText name,phone,email;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    String type="";
    private EditText service_center_name;
    private LocationManager locationManager;
    private double getLatitude=0;
    private double getLongitude=0;
    private String getAddress="";
    boolean isUpdate=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        isUpdate=false;
        rootView=findViewById(android.R.id.content);
        name=(EditText)findViewById(R.id.name);
        Button click=(Button)findViewById(R.id.buttonclick);
        phone=(EditText)findViewById(R.id.phone);
        email=(EditText)findViewById(R.id.email);
        service_center_name=(EditText)findViewById(R.id.service_center_name);

        type=getIntent().getStringExtra("type");
        PrintClass.printValue("UserRegisterPrint type ",type);

        if(type.equals("edit")) {
            getProfileInfo();
            click.setText("UPDATE");
        } else {
            name.setText("");
            email.setText("");
            phone.setText("");
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission. ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    getLatLong();
                }
            }
        } catch (Exception e){
            PrintClass.printValue("PathGoogleMapActivity Exception ",e.toString());
        }
    }
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission. ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission. ACCESS_FINE_LOCATION)) {
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        getLatLong();
                        getProfileInfo();
                    }

                } else {

                }
                return;
            }
        }
    }

    public void getLatLong(){
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage("Yout GPS seems to be disabled, do you want to enable it?");
            alert.setPositiveButton("Back", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.setNegativeButton("Go to Settings", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    Intent I = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(I);
                }
            });
            AlertDialog al_gps = alert.create();
            al_gps.show();
        } else {
            Location location = getLastKnownLocation();
            if (location != null) {
                getLatitude= location.getLatitude();
                getLongitude = location.getLongitude();
                Geocoder geocoder;
                List<Address> addresses = new ArrayList<>();
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(getLatitude, getLongitude, 1);
                    // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Location Not Found", Toast.LENGTH_LONG).show();
                    return;
                }
                getAddress = addresses.get(0).getAddressLine(0);
                PrintClass.printValue("LATLOGVALUE ", "LAT :" + location.getLatitude() +
                        " longitude " + getAddress);
            } else {
                Toast.makeText(getApplicationContext(), "Location Not Found", Toast.LENGTH_LONG).show();
                PrintClass.printValue("LATLOGVALUE ", "location null");
            }
        }
    }
    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

            }
            Location l = locationManager.getLastKnownLocation(provider);
            PrintClass.printValue("LATLOGVALUE ", "last known location, provider: %s, location: %s" + provider +
                    l);
            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                PrintClass.printValue("LATLOGVALUE ", "found best last known location: %s" + l);
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }

    public void getProfileInfo(){
        if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
            String url = Constants.SERVER_URL + "profile/user-profile";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));

            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(UserRegister.this, "UserGetProfileInfo").execute(url, params.toString());

        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void submit(View v){
        if(type.equals("edit")) {
            update();
        } else {
            register();
        }
    }

    public void register(){
        if(validate()) {
            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                //   String url = Constants.SERVER_URL + "action=ReferFriend";
                String url = Constants.SERVER_URL + "user/vendor-register";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
                JSONObject params2 = new JSONObject();
                try {
                    params.put("first_name",name.getText().toString() );
                    params.put("email", email.getText().toString());
                    params.put("mobile_no",phone.getText().toString());
                    params.put("user_type", "vendor");
                    params.put("service_center_name", service_center_name.getText().toString());
                    params2.put("address",getAddress);
                    params2.put("lattitude", getLatitude);
                    params2.put("longitude", getLongitude);
                    jsonBody.put("ApiSignupForm", params);
                    jsonBody.put("Address", params2);
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                new post_async(UserRegister.this, "UserRegister").execute(url, jsonBody.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void update(){
        if(validate()) {
            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                //   String url = Constants.SERVER_URL + "action=ReferFriend";
                String url = Constants.SERVER_URL + "profile/user-update-profile";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
                JSONObject params2 = new JSONObject();
                try {
                    params.put("first_name",name.getText().toString() );
                    params.put("email", email.getText().toString());
                    params.put("mobile_no",phone.getText().toString());
                    params.put("service_center_name", service_center_name.getText().toString());
                    params2.put("address",getAddress);
                    params2.put("lattitude", getLatitude);
                    params2.put("longitude", getLongitude);
                    jsonBody.put("UpdateProfileForm", params);
                    jsonBody.put("Address", params2);
                    jsonBody.put("user_id", prefrence.getString("user_id", ""));
                    jsonBody.put("access_token", prefrence.getString("access_token", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                new post_async(UserRegister.this, "UserUpdate").execute(url, jsonBody.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void ResponseOfRegister(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
                editor.putString("user_id",  jsonObject.getString("user_id"));
                editor.commit();
                if(type.equals("edit")) {
                    isUpdate=true;
                    getProfileInfo();
                } else {
                    Intent i = new Intent(this, Login.class);
                    startActivity(i);
                    finish();
                }
            } else {
                JSONObject errorjsonObject = jsonObject.getJSONObject("errors");

                if(errorjsonObject.has("mobile_no")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("mobile_no");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0));
                    PrintClass.printValue("UserRegisterREsponse jsonObject errorjsonObject " +
                            "", " has data " + jsonArray.getString(0));
                    return;
                } else if(errorjsonObject.has("email")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("email");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0) );
                    return;
                }
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public void ResponseOfUserInfo(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                JSONObject userInfo=jsonObject.getJSONObject("userDetails");
                name.setText(userInfo.getString("first_name"));
                email.setText(userInfo.getString("email"));
                phone.setText(userInfo.getString("mobile_no"));
                service_center_name.setText(userInfo.getString("service_center_name"));
                editor.putString("name",userInfo.getString("first_name"));
                editor.commit();
                if(isUpdate){
                    Intent i = new Intent(UserRegister.this, MainActivity.class);
                    startActivity(i);
                    finish();
                    isUpdate=false;
                }
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public boolean validate() {

        String getName = name.getText().toString();
        String getEmail = email.getText().toString();
        String getPhone = phone.getText().toString();
        String getServiceCenterName = service_center_name.getText().toString();

        Pattern p = Pattern.compile(Constants.regEx);
        Matcher m = p.matcher(getEmail);

        PrintClass.printValue("PRINTINGHRVLUES ", "name : " + getName + " phone " + getPhone + " email " + getEmail);
        if (getName.length() == 0) {
            phone.setError(null);
            name.setError("First name cannot be empty");
            return false;
        } else if (getPhone.length() == 0) {
            name.setError(null);
            phone.setError("Phone Number cannot be empty");
            return false;
        } else if (getServiceCenterName.length() == 0) {
            phone.setError(null);
            service_center_name.setError("Service Centre Name cannot be empty");
            return false;
        } else if (!getPhone.matches(Constants.regexStr) || getPhone.length() != 10) {
            service_center_name.setError(null);
            phone.setError("Your Phone Number is Invalid.");
            return false;
        } else if (getEmail.length() > 0) {
            if (!m.find()) {
                name.setError(null);
                phone.setError(null);
                email.setError("Email Id Invalid");
                return false;
            } else {
                return true;
            }
        } else if (getAddress.equals("")) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                   "Address Not Found. Please Turn on your Gps");
            return false;

        } else {
            name.setError(null);
            phone.setError(null);
            email.setError(null);
            return true;

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        name.setText("");
        phone.setText("");
        email.setText("");
    }
}
