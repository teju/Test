package com.example.teju.biker;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teju.biker.Utils.Constants;
import com.example.teju.biker.Utils.CustomToast;
import com.example.teju.biker.Utils.IsNetworkConnection;
import com.example.teju.biker.Utils.PrintClass;
import com.example.teju.biker.Utils.post_async;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private LatLng dest;
    private ArrayList<LatLng> markerPoints;
    private GoogleMap map;
    private LocationManager locationManager;
    private EditText vehicle_no, email,editText;
    private View rootView;
    private SharedPreferences prefrence;
    private int backpress = 0;
    double getLatitude=0,getLongitude=0;
    String getAddress="Not Found";
    private SharedPreferences.Editor editor;
    TextView profile_name;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
      //  Constants.statusColor(this);
        rootView=findViewById(android.R.id.content);
        editText=(EditText)findViewById(R.id.editText);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        profile_name=(TextView)header.findViewById(R.id.profile_name);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission. ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                getLatLong();
            }
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
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                getLatitude=latitude;
                getLongitude=longitude;
                Geocoder geocoder;
                List<Address> addresses = new ArrayList<>();
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Location Not Found", Toast.LENGTH_LONG).show();
                    return;
                }
                getAddress = addresses.get(0).getAddressLine(0);
                PrintClass.printValue("LATLOGVALUE ", "LAT :" + location.getLatitude() +
                        " longitude " + getAddress);
                showMap(latitude,longitude);
            } else {
                Toast.makeText(getApplicationContext(), "Location Not Found", Toast.LENGTH_LONG).show();
                PrintClass.printValue("LATLOGVALUE ", "location null");
            }

        }
    }

    public void showMap(double latitude,double longitude){
        dest = new LatLng(latitude, longitude);
        // Initializing
        markerPoints = new ArrayList<LatLng>();
        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        // Getting Map for the SupportMapFragment
        map = fm.getMap();
        System.out.println("GOOGLEMAOP " + map);
        if (map != null) {
            System.out.println("GOOGLEMAOP not null " + map);
            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();
            // Setting the position of the marker
            options.position(dest);
            map.addMarker(options);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(dest,
                    15));
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission. ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    editText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editText.setText("");
                            search();
                        }
                    });
                }
            }

        }
    }

    @Override
    protected void onRestart() {
        PrintClass.printValue("ONRESUMEISBEING","CALLED");
        super.onResume();
        if(editText.getText().toString().length()==0) {
            if (checkLocationPermission()) {
                if (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    getLatLong();
                }
            }
        }
        profile_name.setText(prefrence.getString("name", ""));

    }

    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private void search() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Service Not Available");
            PrintClass.printValue("SEARCHAUTOCOMPLETE GooglePlayServicesRepairableException" +
                    " ", "" + e.toString());

            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "Service Not Available");
            PrintClass.printValue("SEARCHAUTOCOMPLETE GooglePlayServicesNotAvailableException" +
                    " ", e.toString() );

            // TODO: Handle the error.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PrintClass.printValue("SEARCHAUTOCOMPLETE requestCode ","" +requestCode);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            PrintClass.printValue("SEARCHAUTOCOMPLETE requestCode"," ok ");

            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                PrintClass.printValue("SEARCHAUTOCOMPLETE ","" +place.getAddress());
                Geocoder coder = new Geocoder(this);
                List<Address> address;
                editText.setText(place.getAddress());
                try {
                    address = coder.getFromLocationName(place.getAddress().toString(), 5);
                    getAddress=place.getAddress().toString();
                    if (address == null) {
                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                "Address Not Found");
                        getLatLong();
                    }
                    Address location = address.get(0);
                    showMap(location.getLatitude(),location.getLongitude());
                    getLongitude=location.getLatitude();
                    getLongitude=location.getLongitude();
                } catch (Exception e){
                    getLatLong();
                }
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                PrintClass.printValue("SEARCHAUTOCOMPLETE ","" +status.getStatusMessage());
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Address Not Found");
                getLatLong();

            } else if (resultCode == RESULT_CANCELED) {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Address Not Found");
                getLatLong();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            this.finish();

        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            Intent i=new Intent(this,UserRegister.class);
            i.putExtra("type","edit");
            startActivity(i);
            // Handle the camera action
        } else if (id == R.id.booking_history) {
            Intent i=new Intent(this,BookingPaymentHistory.class);
            startActivity(i);
        } else if (id == R.id.payment_history) {
            Intent i=new Intent(this,PaymentHistory.class);
            startActivity(i);
        } else if (id == R.id.setting) {
            Intent i=new Intent(this,Setting.class);
            startActivity(i);
        } else if (id == R.id.logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Logout");
            alertDialog.setMessage("Are you sure you want to Logout ?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    Intent i=new Intent(MainActivity.this,Login.class);
                    startActivity(i);
                }
            });
            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

    public void Submit(View view) {
        final Dialog mBottomSheetDialog = new Dialog(this);
        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBottomSheetDialog.setContentView(R.layout.books_service);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.show();
        Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
        vehicle_no = (EditText) mBottomSheetDialog.findViewById(R.id.vehicle_no);
        email = (EditText) mBottomSheetDialog.findViewById(R.id.email);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    if (IsNetworkConnection.checkNetworkConnection(MainActivity.this)) {

                        String url = Constants.SERVER_URL + "booking/request";

                        JSONObject jsonBody = new JSONObject();
                        JSONObject params = new JSONObject();
                        JSONObject params2 = new JSONObject();
                        try {
                            params.put("email_id",email.getText().toString() );
                            params.put("vehicle_no", vehicle_no.getText().toString());
                            params2.put("address",getAddress);
                            params2.put("lattitude",String.valueOf(getLatitude));
                            params2.put("longitude",String.valueOf(getLongitude));
                            jsonBody.put("Booking", params);
                            jsonBody.put("Address", params2);
                            jsonBody.put("user_id", prefrence.getString("user_id", ""));
                            jsonBody.put("access_token", prefrence.getString("access_token", ""));
                            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                            new post_async(MainActivity.this, "BookingRequest").execute(url, jsonBody.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                            PrintClass.printValue("SYSTEMPRINT PARAMS Exception", e.toString());
                        }
                        mBottomSheetDialog.cancel();
                    } else {
                        new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                "No Internet Connection");
                    }
                }
            }
        });
    }

    public void ResponseOfBooking(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                Intent i = new Intent(MainActivity.this, BookingSuccessful.class);
                startActivity(i);
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }
        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }


    public boolean validate(){

        String getVehicleNo=vehicle_no.getText().toString();
        String getEmail=email.getText().toString();

        Pattern p = Pattern.compile(Constants.regEx);
        Matcher m = p.matcher(getEmail);

        if(getVehicleNo.length() == 0) {
            vehicle_no.setError("Vehicle Number cannot be empty");
            return false;
        } else if(getEmail.length()==0){
            vehicle_no.setError(null);
            email.setError("Email Id cannot be empty");
            return false;
        } else if(!m.find()) {
            vehicle_no.setError(null);
            email.setError("Email Id Invalid");
            return false;

        } else {
            vehicle_no.setError(null);
            email.setError(null);
            return  true;
        }
    }
}




