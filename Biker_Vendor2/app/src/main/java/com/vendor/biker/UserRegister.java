package com.vendor.biker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.IsNetworkConnection;
import com.vendor.biker.Utils.PrintClass;
import com.vendor.biker.Utils.post_async;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

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
    private boolean agreed = false;
    LinearLayout terms_conditions,myProfile;
    TextView sign_up_text,earnings;
    private ImageView logo;
    private CheckBox agree_main;
    RatingBar ratings;
    private Button click;
    private EditText profilePassword;
    private LinearLayout userDetails;
    private LinearLayout voucherDetails;
    private boolean show  = true;
    private Button shButton;
    private ImageView noti;
    private TextView noti_indication;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        agreed = false;
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        show = prefrence.getBoolean("show", false);

        isUpdate=false;
        rootView=findViewById(android.R.id.content);
        name=(EditText)findViewById(R.id.name);
        click=(Button)findViewById(R.id.buttonclick);
        shButton=(Button)findViewById(R.id.shButton);
        phone=(EditText)findViewById(R.id.phone);
        email=(EditText)findViewById(R.id.email);
        profilePassword=(EditText)findViewById(R.id.profilePassword);
        logo = (ImageView) findViewById(R.id.logo);
        ImageView menu = (ImageView) findViewById(R.id.menu);
        sign_up_text = (TextView) findViewById(R.id.sign_up_text);
        earnings = (TextView) findViewById(R.id.earnings);
        service_center_name=(EditText)findViewById(R.id.service_center_name);
        terms_conditions = (LinearLayout) findViewById(R.id.terms_conditions);
        voucherDetails = (LinearLayout) findViewById(R.id.voucherDetails);
        userDetails = (LinearLayout) findViewById(R.id.userDetails);
        myProfile = (LinearLayout) findViewById(R.id.myProfile);
         agree_main = (CheckBox)findViewById(R.id.agree);
        final TextView terms_condi_text = (TextView) findViewById(R.id.terms);
        final Typeface typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");
        final Typeface italic = Typeface.createFromAsset(getAssets(), "fonts/italic.ttf");

        ratings=(RatingBar)findViewById(R.id.ratings) ;
        type=getIntent().getStringExtra("type");
        PrintClass.printValue("UserRegisterPrint type ",type);
        menu.setVisibility(View.GONE);
        noti = (ImageView)findViewById(R.id.noti);

        if(type.equals("edit")) {
            noti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i=new Intent(UserRegister.this, Notifications.class);
                    startActivity(i);
                }
            });
            getProfileInfo();
            click.setText("UPDATE");
            shButton.setVisibility(View.VISIBLE);
            terms_conditions.setVisibility(View.GONE);
            sign_up_text.setVisibility(View.GONE);
            logo.setVisibility(View.GONE);
            earnings.setText("\u20B9 "+prefrence.getString("amount",""));
            myProfile.setVisibility(View.VISIBLE);
            profilePassword.setVisibility(View.GONE);
            if(prefrence.getString("avg_rating", "").length() != 0) {
                ratings.setRating(Float.parseFloat(prefrence.getString("avg_rating", "")));
            }
            if(show) {
                shButton.setBackground(getResources().getDrawable(R.drawable.show));
                userDetails.setVisibility(View.VISIBLE);
                voucherDetails.setVisibility(View.VISIBLE);
                click.setVisibility(View.VISIBLE);
            } else {
                shButton.setBackground(getResources().getDrawable(R.drawable.hide));
                userDetails.setVisibility(View.GONE);
                voucherDetails.setVisibility(View.GONE);
                click.setVisibility(View.GONE);
            }
        } else {
            noti.setVisibility(View.GONE);
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
        agree_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(agree_main.isChecked()){
                    agreed= true;
                } else  {
                    agreed = false;
                }
            }
        });

        terms_condi_text.setTypeface(italic);
        terms_conditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(UserRegister.this);
                dialog.setCancelable(false);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.spinner);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();
                if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {

                    RequestQueue queue = Volley.newRequestQueue(UserRegister.this);
                    String url = "http://app.bikerservice.in/biker/api/web/user/customer-terms-condition";

// Request a string response from the provided URL.
                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    dialog.dismiss();
                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        if (jsonObject.getString("status").equals("success")) {
                                            final Dialog mBottomSheetDialog = new Dialog(UserRegister.this);
                                            mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            mBottomSheetDialog.setContentView(R.layout.activity_terms_conditions);
                                            mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT);
                                            mBottomSheetDialog.show();
                                            Button ok = (Button) mBottomSheetDialog.findViewById(R.id.ok);
                                            ok.setTypeface(typeface_luci);

                                            ok.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    agreed=true;
                                                    mBottomSheetDialog.dismiss();
                                                    agree_main.setChecked(agreed);
                                                }
                                            });

                                            HtmlTextView termCon = (HtmlTextView) mBottomSheetDialog.findViewById(R.id.terms_conditions);
                                            // Spanned result = Html.fromHtml(childText);
                                            // txtListChild.setText(result);
                                            termCon.setTypeface(typeface_luci);
                                            String text= jsonObject.getJSONArray("terms").toString().replace("\\r\\n", "<p>");
                                            text = text.replaceAll("\"", " ");
                                            text = text.replaceAll("\\[", "").replaceAll("\\]","");
                                            termCon.setHtml(text, new HtmlHttpImageGetter(termCon));

                                            // Display the first 500 characters of the response string.
                                        } else {
                                            dialog.dismiss();
                                            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                                    "Something went wrong please try again later");
                                        }
                                    } catch (Exception e) {
                                        dialog.dismiss();
                                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                                "Something went wrong please try again later");
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                        }
                    });
// Add the request to the RequestQueue.
                    queue.add(stringRequest);

                } else {
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "No Internet Connection");
                }
            }
        });
        System.out.println("MYPROFILEPASSWORD showHideDetails "+show);


    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showHideDetails(View view) {
        show = prefrence.getBoolean("show", false);
        String profilePswd = prefrence.getString("profile_password","");;
        System.out.println("MYPROFILEPASSWORD showHideDetails onclick " + profilePswd);

        if(profilePswd.length() == 0) {
            updatePassword();
        } else {
            if (show) {
                shButton.setBackground(getResources().getDrawable(R.drawable.hide));
                userDetails.setVisibility(View.GONE);
                click.setVisibility(View.GONE);
                voucherDetails.setVisibility(View.GONE);
                editor.putBoolean("show", false);
            } else {
                verifyPassword();
            }
            editor.commit();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ResponseOfUpdateProfilePassword(String resultString, String string) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfUpdateProfilePassword jsonObject ", " has data " + jsonObject.toString());
            if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));
                editor.putString("profile_password", string);
                editor.putBoolean("show", false);
                editor.commit();
                userDetails.setVisibility(View.GONE);
                click.setVisibility(View.GONE);
                voucherDetails.setVisibility(View.GONE);
                shButton.setBackground(getResources().getDrawable(R.drawable.hide));

            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));
            }

        } catch (Exception e) {
            System.out.println("SYSTEMPRINT ResponseOfUpdateProfilePassword error UserRegister " + e.toString());
        }
    }

    public void updatePassword(){
        final Dialog mBottomSheetDialog = new Dialog(this);
        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBottomSheetDialog.setContentView(R.layout.activity_profile_password);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.show();
        mBottomSheetDialog.getWindow().setGravity(Gravity.CENTER);
        Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
        final EditText otp = (EditText) mBottomSheetDialog.findViewById(R.id.otp);
        System.out.println("MYPROFILEPASSWORD entered : "+otp.getText().toString()
                + "saved "+prefrence.getString("profile_password",""));
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otp.getText().toString().length() == 0) {
                    otp.setError("Enter Your Password" );
                } else if(otp.getText().toString().length() < 6 || otp.getText().toString().length() >10) {
                    otp.setError("Invalid Password" );
                } else {
                    if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                        String url = Constants.SERVER_URL + "profile/update-profile-password";
                        JSONObject params = new JSONObject();
                        try {
                            params.put("user_id", prefrence.getString("user_id", ""));
                            params.put("access_token", prefrence.getString("access_token", ""));
                            params.put("profile_password", otp.getText().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                        }
                        PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
                        new post_async(UserRegister.this, "UpdateProfilePassword",otp.getText().toString()).execute(url, params.toString());
                        mBottomSheetDialog.cancel();
                    } else {
                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                "No Internet Connection");
                    }
                }
            }
        });
    }

    public void  verifyPassword() {
        final Dialog mBottomSheetDialog = new Dialog(this);
        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBottomSheetDialog.setContentView(R.layout.activity_profile_password);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.show();
        mBottomSheetDialog.getWindow().setGravity(Gravity.CENTER);
        Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
        final EditText otp = (EditText) mBottomSheetDialog.findViewById(R.id.otp);
        System.out.println("MYPROFILEPASSWORD entered : "+otp.getText().toString()
                + "saved "+prefrence.getString("profile_password",""));
        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                if (otp.getText().toString().length() == 0) {
                    otp.setError("Enter Your Password" );
                } else {
                    if (otp.getText().toString().equals(prefrence.getString("profile_password", ""))) {
                        userDetails.setVisibility(View.VISIBLE);
                        click.setVisibility(View.VISIBLE);
                        voucherDetails.setVisibility(View.VISIBLE);
                        editor.putBoolean("show", true);
                        editor.commit();
                        mBottomSheetDialog.cancel();
                        shButton.setBackground(getResources().getDrawable(R.drawable.show));

                    } else {
                        new CustomToast().Show_Toast(UserRegister.this, rootView, "Invalid Profile Password");
                    }
                }
            }
        });
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
                String url = Constants.SERVER_URL + "user/vendor-signup";
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
                    params.put("profile_password", profilePassword.getText().toString());
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
                    params.put("profile_password", profilePassword.getText().toString());
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
                } else if(errorjsonObject.has("first_name")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("first_name");
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
                if(userInfo.has("avg_rating")) {
                    editor.putString("avg_rating", userInfo.getString("avg_rating"));
                }
                if(userInfo.has("total_amount")) {
                    editor.putString("amount", userInfo.getString("total_amount"));
                }
                editor.commit();
                System.out.println("ResponseOfUserInfo error UserRegister "+prefrence.getString("avg_rating",""));
                Float rating = Float.parseFloat(prefrence.getString("avg_rating",""));
                ratings.setRating(rating);
                earnings.setText("\u20B9 "+prefrence.getString("amount",""));
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
        String getProfilePswd = profilePassword.getText().toString();
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
        }  else if(!type.equals("edit") && !agreed) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Please accept to terms & conditions");
            return false;
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
        if(type.equals("edit")) {
            getProfileInfo();
            click.setText("UPDATE");
            terms_conditions.setVisibility(View.GONE);
            sign_up_text.setVisibility(View.GONE);
            logo.setVisibility(View.GONE);
            earnings.setText("\u20B9 "+prefrence.getString("amount",""));
            myProfile.setVisibility(View.VISIBLE);
            if(prefrence.getString("avg_rating", "").length() != 0) {
                ratings.setRating(Float.parseFloat(prefrence.getString("avg_rating", "")));
            }
            TextView noti_count = (TextView) findViewById(R.id.noti_count);
            Constants.noti_count(this,noti_count);

        } else {
            noti_indication = (TextView)findViewById(R.id.noti_count);
            noti_indication.setVisibility(View.GONE);
            name.setText("");
            email.setText("");
            phone.setText("");
        }
    }
}
