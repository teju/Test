package com.vendor.biker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.IsNetworkConnection;

public class ReferUser extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener{

    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    private View rootView;
    TextView profile_name;
    private Button refer;
    private TextView phone_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refer_user);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        //  Constants.statusColor(this);
        rootView=findViewById(android.R.id.content);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        refer=(Button)findViewById(R.id.refer);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        phone_no=(TextView)findViewById(R.id.phone_no);
        profile_name=(TextView)header.findViewById(R.id.profile_name);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);
        refer.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                if (!phone_no.getText().toString().matches(Constants.regexStr) || phone_no.getText().toString().length() != 10) {
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "Invalid phone number");
                }
                 else {
                    refer();

                }
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void refer(){
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);

        smsIntent.setData(Uri.parse("smsto:"));
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address"  , new String (phone_no.getText().toString()));
        smsIntent.putExtra("sms_body"  , prefrence.getString("referel_code",""));

        try {
            startActivity(smsIntent);
            finish();
            Log.i("Finished sending SMS...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(ReferUser.this,
                    "SMS faild, please try again later.", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            if (IsNetworkConnection.checkNetworkConnection(ReferUser.this)) {
                Intent i = new Intent(this, UserRegister.class);
                i.putExtra("type", "edit");
                startActivity(i);
            } else {
                Intent i=new Intent(this,ServerError.class);
                startActivity(i);
            }
            // Handle the camera action
        }  else if (id == R.id.job_list) {
            Intent i=new Intent(this,JobList.class);
            i.putExtra("booking_id","");
            startActivity(i);
        } else if (id == R.id.home) {
            Intent i=new Intent(this,MainActivity.class);
            startActivity(i);
        } else if (id == R.id.job_history) {
            Intent i=new Intent(this,JobHistory.class);
            startActivity(i);
        } else if (id == R.id.logout) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Logout");
            alertDialog.setMessage("Are you sure you want to Logout ?");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.putString("access_token","1234");
                    editor.commit();
                    Intent i=new Intent(ReferUser.this,Login.class);
                    startActivity(i);
                    finish();
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
}
