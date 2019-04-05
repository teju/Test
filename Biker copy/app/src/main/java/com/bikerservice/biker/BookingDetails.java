package com.bikerservice.biker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bikerservice.biker.Utils.Constants;
import com.bikerservice.biker.Utils.CustomToast;
import com.bikerservice.biker.Utils.IsNetworkConnection;
import com.bikerservice.biker.Utils.PrintClass;
import com.bikerservice.biker.Utils.post_async;
import com.bikerservice.biker.model.BookingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingDetails extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefrence;
    List<com.bikerservice.biker.model.BookingList> bookingList_l=new ArrayList<>();
    TextView profile_name;
    private View rootView;
    int offset=0;
    int limit=5;
    BookingDetails.BookingDetailsRecyclerView mAdapter ;
    private int total_count=0;
    private TextView no_records;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView no_records_img;
    private Typeface typeface_luci;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;
    private ImageView noti;
    private ImageView noti_indication;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));
        try {
            TextView noti_count = (TextView) findViewById(R.id.noti_count);
            Constants.noti_count(this, noti_count);
        } catch (Exception e){
            PrintClass.printValue("SYSTEMPRINTExceptionPARAMS", e.toString());

        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_history);
        try {

            no_records = (TextView) findViewById(R.id.no_records);

            no_records_img = (ImageView) findViewById(R.id.no_records_img);
            rootView = findViewById(android.R.id.content);
            bookingList_l.clear();
            prefrence = getSharedPreferences("My_Pref", 0);
            editor = prefrence.edit();
            swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(this);

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

            TextView title = (TextView) findViewById(R.id.title_val);
            title.setText("Booking Details");
            View header = navigationView.getHeaderView(0);

            profile_name = (TextView) header.findViewById(R.id.profile_name);
            profile_name.setText(prefrence.getString("name", ""));
            Typeface typeface = Typeface.createFromAsset(getAssets(),
                    "fonts/name_font.ttf");
            profile_name.setTypeface(typeface);

            recyclerView = (RecyclerView) findViewById(R.id.booking_history);
            recyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            if (prefrence.getString("isLoggedIn", "").equals("true")) {

                // Constants.statusColor(this);
                getBookingList("BookingDetails");
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm Login");
                alertDialog.setMessage("You are not logged in !! Would You like to login ??");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putString("isLoggedIn", "false");
                        editor.commit();
                        Intent i = new Intent(BookingDetails.this, Login.class);
                        i.putExtra("reached_dest", "false");
                        startActivity(i);
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        recyclerView.setVisibility(View.GONE);
                        no_records.setVisibility(View.VISIBLE);
                        no_records_img.setVisibility(View.VISIBLE);
                        no_records.setText("You are not logged In !!");
                    }
                });
                alertDialog.show();
            }
            noti = (ImageView) findViewById(R.id.noti);
            noti.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(BookingDetails.this, Notifications.class);
                    startActivity(i);
                }
            });
        } catch (Exception e){
            PrintClass.printValue("SYSTEMPRINTExceptionPARAMS", e.toString());

        }

    }


    public void getBookingList(String action){
        if (IsNetworkConnection.checkNetworkConnection(BookingDetails.this)) {
            if(action.equals("BookingDetailsRefresh")) {
                swipeRefreshLayout.setRefreshing(true);
            }
            if(action.equals("BookingDetails")) {
                bookingList_l.clear();
            }
            String url = Constants.SERVER_URL + "booking/list";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("offset",String.valueOf(offset));
                params.put("limit",String.valueOf(limit));
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(BookingDetails.this,action).execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }
    int id;

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if(id != item.getItemId()) {
            id = item.getItemId();
            if (id == R.id.home) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                // Handle the camera action
            } else if (id == R.id.profile) {
                if (IsNetworkConnection.checkNetworkConnection(BookingDetails.this)) {
                    Intent i = new Intent(this, UserRegister.class);
                    i.putExtra("type", "edit");
                    startActivity(i);
                } else {
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "No Internet Connection");
                }
                // Handle the camera action
            } else if (id == R.id.payment_history) {
                Intent i=new Intent(this,PaymentHistory.class);
                startActivity(i);
            } else if (id == R.id.booking_completed) {
                Intent i=new Intent(this,BookingCompleted.class);
                startActivity(i);
            }/*else if (id == R.id.setting) {
                Intent i = new Intent(this, Setting.class);
                startActivity(i);
            } */else if (id == R.id.logout) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm Logout");
                alertDialog.setMessage("Are you sure you want to Logout ?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putString("isLoggedIn","false");
                        editor.putString("access_token","1234");
                        editor.commit();
                        Intent i = new Intent(BookingDetails.this, Login.class);
                        i.putExtra("reached_dest","false");
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
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void ResponseOfBookingList(String resultString) {
        JSONObject jsonObject = null;
        try {

            jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfBookingList resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonarr_bookinglist=jsonObject.getJSONArray("bookinglist");
                PrintClass.printValue("ResponseOfBookingList bookingList ",jsonarr_bookinglist.toString());
                for (int i=0;i<jsonarr_bookinglist.length();i++){
                    JSONObject booking_jObj=jsonarr_bookinglist.getJSONObject(i);
                    BookingList bookingList =new BookingList();
                    bookingList.setBooking_no(booking_jObj.getString("booking_no"));
                    bookingList.setBooking_id(booking_jObj.getString("booking_id"));
                    bookingList.setEmail_id(booking_jObj.getString("email_id"));
                    bookingList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                    bookingList.setStatus(booking_jObj.getString("status"));
                    bookingList.setBooked_on(booking_jObj.getString("booked_on"));
                    if(booking_jObj.has("mobile_no")) {
                        bookingList.setVendor_name(booking_jObj.getString("vendor_name"));
                    } else {
                        bookingList.setVendor_name("");
                    }
                    if(booking_jObj.has("mobile_no")) {
                        bookingList.setVendor_nuber(booking_jObj.getString("mobile_no"));
                    } else {
                        bookingList.setVendor_name("");
                    }
                    bookingList_l.add(bookingList);
                }
                if(jsonObject.has("totalCount")) {
                    total_count = Integer.parseInt(jsonObject.getString("totalCount"));
                }
                if(bookingList_l.size() !=0) {
                    mAdapter = new BookingDetails.BookingDetailsRecyclerView(this);
                    recyclerView.setAdapter(mAdapter);
                    if (jsonObject.has("bookinglist")) {
                        final JSONObject finalJsonObject = jsonObject;
                        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                            @Override
                            public void onLoadMore() {
                            Log.e("haint", "Load More");
                            bookingList_l.add(null);
                            mAdapter.notifyItemInserted(bookingList_l.size() - 1);
                            //Load more data for reyclerview
                            new Handler().postDelayed(new Runnable() {
                                  @Override
                                  public void run() {
                                      Log.e("haint", "Load More 2");
                                      if (finalJsonObject.has("bookinglist")) {
                                          PrintClass.printValue("ResponseOfBookingList onLoadMore "
                                                  , "LOOPED");
                                          //Remove loading item
                                          bookingList_l.remove(bookingList_l.size() - 1);
                                          mAdapter.notifyItemRemoved(bookingList_l.size());
                                          //Load data
                                          offset = offset + limit;
                                          getBookingList("BookingDetailsReload");
                                      }
                                  }
                              },
                        1000);
                            }
                        });

                    }
                } else {
                    recyclerView.setVisibility(View.GONE);
                    no_records.setVisibility(View.VISIBLE);
                    no_records_img.setVisibility(View.VISIBLE);
                    no_records.setText(jsonObject.getString("message"));
                }
            } else {
                recyclerView.setVisibility(View.GONE);
                no_records.setVisibility(View.VISIBLE);
                no_records_img.setVisibility(View.VISIBLE);
                no_records.setText(jsonObject.getString("message"));
            }
            swipeRefreshLayout.setRefreshing(false);

        } catch (Exception e){
            swipeRefreshLayout.setRefreshing(false);
            PrintClass.printValue("ResponseOfBookingList Exception ", e.toString());
            recyclerView.setVisibility(View.GONE);
            no_records.setVisibility(View.VISIBLE);
            no_records_img.setVisibility(View.VISIBLE);
            try {
                no_records.setText(jsonObject.getString("message"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void ResponseOfBookingListReload(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfBookingListReload resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonarr_bookinglist=jsonObject.getJSONArray("bookinglist");
                PrintClass.printValue("ResponseOfBookingListReload bookingList ",jsonarr_bookinglist.toString());
                for (int i=0;i<jsonarr_bookinglist.length();i++){
                    JSONObject booking_jObj=jsonarr_bookinglist.getJSONObject(i);
                    BookingList bookingList =new BookingList();
                    bookingList.setBooking_no(booking_jObj.getString("booking_no"));
                    bookingList.setEmail_id(booking_jObj.getString("email_id"));
                    bookingList.setBooking_id(booking_jObj.getString("booking_id"));
                    bookingList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                    bookingList.setStatus(booking_jObj.getString("status"));
                    bookingList.setBooked_on(booking_jObj.getString("booked_on"));
                    bookingList.setAddress(booking_jObj.getString("address"));
                    if(booking_jObj.has("vendor_name")) {
                        bookingList.setVendor_name(booking_jObj.getString("vendor_name"));
                    } else {
                        bookingList.setVendor_name("");
                    }
                    if(booking_jObj.has("mobile_no")) {
                        bookingList.setVendor_nuber(booking_jObj.getString("mobile_no"));
                    } else {
                        bookingList.setVendor_name("");
                    }
                    bookingList_l.add(bookingList);
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfBookingListReload Exception ",e.toString());

        }
    }

    private String getformatteddate(String dateget) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date expiry = null;
        String yourDate = "";
        try {

            expiry = formatter.parse(dateget);
        } catch (Exception e) {
            e.printStackTrace();
        }
        SimpleDateFormat format = new SimpleDateFormat("d");
        if (expiry != null) {
            String date = format.format(expiry);
            if (date.endsWith("1") && !date.endsWith("11"))
                format = new SimpleDateFormat("d'st' MMM, yyyy hh:mm a");
            else if (date.endsWith("2") && !date.endsWith("12"))
                format = new SimpleDateFormat("d'nd' MMM, yyyy  hh:mm a");
            else if (date.endsWith("3") && !date.endsWith("13"))
                format = new SimpleDateFormat("d'rd' MMM, yyyy  hh:mm a");
            else
                format = new SimpleDateFormat("d'th' MMM, yyyy  hh:mm a");
            yourDate = format.format(expiry);
        }
        return yourDate;
    }

    @Override
    public void onRefresh() {
        offset=0;
        bookingList_l.clear();
        getBookingList("BookingDetailsRefresh");
    }

    class BookingDetailsRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final Button payNow;
        TextView booking_id, vendor_name, vendor_number, vehicle_no, status,
                booking_id_text,vendor_name_text,vendor_number_text,vehicle_no_text,status_text;

        public BookingDetailsRecyclerViewHolder(View itemView) {
            super(itemView);
            typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");

            booking_id = (TextView) itemView.findViewById(R.id.booking_id);
            booking_id.setTypeface(typeface_luci);
            vendor_name = (TextView) itemView.findViewById(R.id.vendor_name);
            vendor_name.setTypeface(typeface_luci);
            vendor_number = (TextView) itemView.findViewById(R.id.vendor_number);
            vendor_number.setTypeface(typeface_luci);
            vehicle_no = (TextView) itemView.findViewById(R.id.vehicle_no);
            vehicle_no.setTypeface(typeface_luci);
            status = (TextView) itemView.findViewById(R.id.status);
            payNow = (Button) itemView.findViewById(R.id.payNow);
            status.setTypeface(typeface_luci);
            booking_id_text=(TextView)itemView.findViewById(R.id.booking_id_text);
            booking_id_text.setTypeface(typeface_luci);
            vendor_name_text=(TextView)itemView.findViewById(R.id.vendor_name_text);
            vendor_name_text.setTypeface(typeface_luci);
            vendor_number_text=(TextView)itemView.findViewById(R.id.vendor_number_text);
            vendor_number_text.setTypeface(typeface_luci);
            vehicle_no_text=(TextView)itemView.findViewById(R.id.vehicle_no_text);
            vehicle_no_text.setTypeface(typeface_luci);
            status_text=(TextView)itemView.findViewById(R.id.status_text);
            status_text.setTypeface(typeface_luci);
            payNow.setTypeface(typeface_luci);
            status_text.setTypeface(typeface_luci);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(BookingDetails.this, "You can call the number by clicking on the button", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
    class BookingDetailsRecyclerView extends RecyclerView.Adapter < RecyclerView.ViewHolder > {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private final Context context;
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading=false;
        private int visibleThreshold = 4;
        private int lastVisibleItem,
                totalItemCount;

        public BookingDetailsRecyclerView(Context context) {
            this.context=context;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                {
                    PrintClass.printValue("BookingDetailsRecyclerView dy "," : "+dy);

                    final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (dy > 0 && bookingList_l.size() <total_count) //check for scroll down
                    {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        PrintClass.printValue("BookingDetailsRecyclerView ","lastVisibleItem : "
                                +lastVisibleItem+ " totalItemCount : "
                                +totalItemCount+" visibleThreshold : "+visibleThreshold+" isLoading : "+isLoading);

                        if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            if (mOnLoadMoreListener != null) {
                                mOnLoadMoreListener.onLoadMore();
                            }
                            isLoading = true;
                        }
                    }
                }
            });
        }

        public void setOnLoadMoreListener(OnLoadMoreListener mOnLoadMoreListener) {
            isLoading = false;
            this.mOnLoadMoreListener = mOnLoadMoreListener;
        }

        @Override
        public int getItemViewType(int position) {
            return bookingList_l.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(
                        BookingDetails.this).inflate(R.layout.booking_history_content, parent, false);
                return new BookingDetails.BookingDetailsRecyclerViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(BookingDetails.this).inflate(R.layout.spinner, parent, false);
                return new BookingDetails.LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BookingDetails.BookingDetailsRecyclerViewHolder) {
                final BookingList bookings = bookingList_l.get(position);
                final BookingDetails.BookingDetailsRecyclerViewHolder userViewHolder =
                        (BookingDetails.BookingDetailsRecyclerViewHolder) holder;
                userViewHolder.booking_id.setText(bookings.getBooking_no());
                userViewHolder.vendor_name.setText(bookings.getVendor_name());
                userViewHolder.vehicle_no.setText(bookings.getVehicle_no());
                userViewHolder.vendor_number.setText(bookings.getVendor_nuber());
                userViewHolder.payNow.setTag(position);
                userViewHolder.status.setText(bookings.getStatus());
                userViewHolder.vendor_number.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (checkPermission(android.Manifest.permission.CALL_PHONE)) {
                            String dial = "tel:" + bookings.getVehicle_no();
                            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                        } else {
                            Toast.makeText(BookingDetails.this, "Permission Call Phone denied", Toast.LENGTH_SHORT).show();
                        }
                        if (checkPermission(android.Manifest.permission.CALL_PHONE)) {
                            userViewHolder.vendor_number.setEnabled(true);
                        } else {
                            userViewHolder.vendor_number.setEnabled(false);
                            ActivityCompat.requestPermissions(BookingDetails.this, new String[]
                                    {android.Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        }


                    }
                });

            } else if (holder instanceof BookingDetails.LoadingViewHolder) {
                BookingDetails.LoadingViewHolder loadingViewHolder = (BookingDetails.LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        private boolean checkPermission(String permission) {
            return ContextCompat.checkSelfPermission(BookingDetails.this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        @Override
        public int getItemCount() {
            return bookingList_l == null ? 0 : bookingList_l.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }
}
