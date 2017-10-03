package com.example.teju.biker;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.teju.biker.Utils.Constants;

public class PaymentHistory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    TextView profile_name;

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_history);
       // Constants.statusColor(this);

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();

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

        TextView title = (TextView) findViewById(R.id.title_val);
        title.setText("PAyment History");

        TableLayout table_layout =(TableLayout)findViewById(R.id.tableLayout);

        TableRow headerrow = new TableRow(this);
        headerrow.setBackgroundColor(getResources().getColor(R.color.dark_gray));

        TextView headerorderId = new TextView(this);
        headerorderId.setText("SL No");
        headerorderId.setTextSize(16);
        headerorderId.setTextColor(getResources().getColor(R.color.black));
        headerorderId.setGravity(Gravity.CENTER);
        headerorderId.setPadding(8, 8, 8, 8);


        TextView headerorderDate = new TextView(this);
        headerorderDate.setText("Vendor Name");
        headerorderDate.setGravity(Gravity.CENTER);
        headerorderDate.setTextSize(16);
        headerorderDate.setTextColor(getResources().getColor(R.color.black));
        headerorderDate.setPadding(8, 8, 8, 8);

        TextView headervoucherId = new TextView(this);
        headervoucherId.setText("Vendor No");
        headervoucherId.setGravity(Gravity.CENTER);
        headervoucherId.setTextSize(16);
        headervoucherId.setTextColor(getResources().getColor(R.color.black));
        headervoucherId.setPadding(8, 8, 8, 8);

        TextView headeramount = new TextView(this);
        headeramount.setText("Vehicle No");
        headeramount.setGravity(Gravity.CENTER);
        headeramount.setTextSize(16);
        headeramount.setTextColor(getResources().getColor(R.color.black));
        headeramount.setPadding(8, 8, 8, 8);

        TextView payment_status = new TextView(this);
        payment_status.setText("Payment Status");
        payment_status.setTextSize(16);
        payment_status.setTextColor(getResources().getColor(R.color.black));
        payment_status.setGravity(Gravity.CENTER);
        payment_status.setPadding(8, 8, 8, 8);

        headerrow.addView(headerorderId, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        headerrow.addView(headerorderDate, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        headerrow.addView(headervoucherId, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        headerrow.addView(headeramount, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        headerrow.addView(payment_status, new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        table_layout.addView(headerrow);

        for (int i = 0; i < 10; i++) {
            TableRow row = new TableRow(this);
            /*if(i%2==0){
                row.setBackgroundColor(getResources().getColor(R.color.orange));
                row.setAlpha(.5f);
            }*/
            TextView sl_no = new TextView(this);
            sl_no.setGravity(Gravity.CENTER);
            sl_no.setTextSize(16);
            sl_no.setTextColor(getResources().getColor(R.color.black));
            sl_no.setPadding(8, 8, 8, 8);

            TextView vendor_name = new TextView(this);
            vendor_name.setGravity(Gravity.CENTER);
            vendor_name.setTextSize(16);
            vendor_name.setTextColor(getResources().getColor(R.color.black));
            vendor_name.setPadding(8, 8, 8, 8);

            TextView vendor_number = new TextView(this);
            vendor_number.setGravity(Gravity.CENTER);
            vendor_number.setTextSize(16);
            vendor_number.setTextColor(getResources().getColor(R.color.black));
            vendor_number.setPadding(8, 8, 8, 8);

            TextView vehicle_no = new TextView(this);
            vehicle_no.setGravity(Gravity.CENTER);
            vehicle_no.setTextSize(16);
            vehicle_no.setTextColor(getResources().getColor(R.color.black));
            vehicle_no.setPadding(8, 8, 8, 8);

            TextView status = new TextView(this);
            status.setGravity(Gravity.CENTER);
            status.setTextSize(16);
            status.setTextColor(getResources().getColor(R.color.black));
            status.setPadding(8, 8, 8, 8);

            sl_no.setText("" + i);
            vendor_name.setText("Tejaswini");
            vendor_number.setText("" + i);
            vehicle_no.setText("KA100" + i);
            status.setText("Successful");

            row.addView(sl_no,new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(vendor_name,new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(vendor_number,new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(vehicle_no,new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(status,new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));

            table_layout.addView(row);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.home) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            // Handle the camera action
        } else if (id == R.id.profile) {
            Intent i=new Intent(this,UserRegister.class);
            i.putExtra("type","edit");
            startActivity(i);
            // Handle the camera action
        } else if (id == R.id.booking_history) {
            Intent i=new Intent(this,BookingPaymentHistory.class);
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
                    Intent i=new Intent(PaymentHistory.this,Login.class);
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
}

