package com.biker;

import android.app.Dialog;
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
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.biker.Utils.Constants;
import com.biker.Utils.CustomToast;
import com.biker.Utils.IsNetworkConnection;
import com.biker.Utils.PrintClass;
import com.biker.Utils.post_async;
import com.biker.model.BookingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BookingCompleted extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,SwipeRefreshLayout.OnRefreshListener{
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefrence;
    List<BookingList> PaymentList_l=new ArrayList<>();
    TextView profile_name;
    private View rootView;
    int offset=0;
    int limit=5;
    BookingCompleted.PaymentHistoryRecyclerView mAdapter ;
    private int total_count=0;
    private TextView no_records;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView no_records_img;
    private static final int MAKE_CALL_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));

    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_completed);
        no_records=(TextView)findViewById(R.id.no_records);
        no_records_img=(ImageView)findViewById(R.id.no_records_img);
        rootView=findViewById(android.R.id.content);
        PaymentList_l.clear();
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
        title.setText("Booking Completed");
        View header = navigationView.getHeaderView(0);

        profile_name=(TextView)header.findViewById(R.id.profile_name);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);

        recyclerView =(RecyclerView)findViewById(R.id.payment_history);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        if(prefrence.getString("isLoggedIn", "").equals("true")) {

            // Constants.statusColor(this);
            getPaymentList("BookingCompleted");
        }else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Login");
            alertDialog.setMessage("You are not logged in !! Would You like to login ??");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.commit();
                    Intent i=new Intent(BookingCompleted.this,Login.class);
                    i.putExtra("reached_dest","false");
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
        // Constants.statusColor(this);
    }

    public void getPaymentList(String action){
        if (IsNetworkConnection.checkNetworkConnection(BookingCompleted.this)) {
            if(action.equals("BookingCompletedRefresh")) {
                swipeRefreshLayout.setRefreshing(true);
            }
            if(action.equals("BookingCompleted")) {
                PaymentList_l.clear();
            }
            String url = Constants.SERVER_URL + "booking/completed";
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
            new post_async(BookingCompleted.this,action, null).execute(url, params.toString());
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
                if (IsNetworkConnection.checkNetworkConnection(BookingCompleted.this)) {
                    Intent i = new Intent(this, UserRegister.class);
                    i.putExtra("type", "edit");
                    startActivity(i);
                } else {
                    Intent i=new Intent(this,ServerError.class);
                    startActivity(i);
                }
                // Handle the camera action
            } else if (id == R.id.booking_details) {
                Intent i = new Intent(this, BookingDetails.class);
                startActivity(i);
            } else if (id == R.id.payment_history) {
                Intent i=new Intent(this,PaymentHistory.class);
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
                        editor.putString("isLoggedIn", "false");
                        editor.putString("access_token","1234");
                        editor.commit();
                        Intent i = new Intent(BookingCompleted.this, Login.class);
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

    public void ResponseOfPaymentList(String resultString) {
        JSONObject jsonObject = null;
        try {
            swipeRefreshLayout.setRefreshing(false);

            jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfPaymentList resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                if(jsonObject.has("bookinglist")) {
                    JSONArray jsonarr_Paymentlist = jsonObject.getJSONArray("bookinglist");
                    PrintClass.printValue("ResponseOfPaymentList PaymentList ", jsonarr_Paymentlist.toString());
                    for (int i = 0; i < jsonarr_Paymentlist.length(); i++) {
                        JSONObject Payment_jObj = jsonarr_Paymentlist.getJSONObject(i);
                        BookingList PaymentList = new BookingList();
                        PaymentList.setBooking_no(Payment_jObj.getString("booking_no"));
                        PaymentList.setBooking_id(Payment_jObj.getString("booking_id"));
                        PaymentList.setVendor_id(Payment_jObj.getString("vendor_id"));
                        PaymentList.setEmail_id(Payment_jObj.getString("email_id"));
                        PaymentList.setVehicle_no(Payment_jObj.getString("vehicle_no"));
                        PaymentList.setStatus(Payment_jObj.getString("status"));
                        PaymentList.setIs_paid(Payment_jObj.getString("is_paid"));
                        PaymentList.setBooked_on(Payment_jObj.getString("booked_on"));
                        PaymentList.setAddress(Payment_jObj.getString("address"));
                        if(Payment_jObj.has("vendor_name")) {
                            PaymentList.setVendor_name(Payment_jObj.getString("vendor_name"));
                        } else {
                            PaymentList.setVendor_name("");
                        }
                        if(Payment_jObj.has("mobile_no")) {
                            PaymentList.setVendor_nuber(Payment_jObj.getString("mobile_no"));
                        } else {
                            PaymentList.setVendor_name("");
                        }
                        PaymentList_l.add(PaymentList);
                    }
                    if (jsonObject.has("totalCount")) {
                        total_count = Integer.parseInt(jsonObject.getString("totalCount"));
                    }
                    if (PaymentList_l.size() != 0) {
                        mAdapter = new BookingCompleted.PaymentHistoryRecyclerView(BookingCompleted.this);
                        recyclerView.setAdapter(mAdapter);
                        if (jsonObject.has("bookinglist")) {
                            final JSONObject finalJsonObject = jsonObject;
                            mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                @Override
                                public void onLoadMore() {
                                    Log.e("haint", "Load More");
                                    PaymentList_l.add(null);
                                    mAdapter.notifyItemInserted(PaymentList_l.size() - 1);
                                    //Load more data for reyclerview
                                    new Handler().postDelayed(new Runnable() {
                                          @Override
                                          public void run() {
                                              Log.e("haint", "Load More 2");
                                              if (finalJsonObject.has("bookinglist")) {
                                                  PrintClass.printValue("ResponseOfPaymentList onLoadMore "
                                                          , "LOOPED");
                                                  //Remove loading item
                                                  PaymentList_l.remove(PaymentList_l.size() - 1);
                                                  mAdapter.notifyItemRemoved(PaymentList_l.size());
                                                  //Load data
                                                  offset = offset + limit;
                                                  getPaymentList("BookingCompleteReload");
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
            } else {
                recyclerView.setVisibility(View.GONE);
                no_records.setVisibility(View.VISIBLE);
                no_records_img.setVisibility(View.VISIBLE);
                no_records.setText(jsonObject.getString("message"));
            }
            swipeRefreshLayout.setRefreshing(false);

        }catch (Exception e){
            swipeRefreshLayout.setRefreshing(false);
            PrintClass.printValue("ResponseOfPaymentList Exception ",e.toString());
            no_records_img.setVisibility(View.VISIBLE);
            no_records.setVisibility(View.VISIBLE);
            try {
                no_records.setText(jsonObject.getString("message"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }

    public void ResponseOfPaymentListReload(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfPaymentListReload resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonarr_Paymentlist=jsonObject.getJSONArray("bookinglist");
                PrintClass.printValue("ResponseOfPaymentListReload PaymentList ",jsonarr_Paymentlist.toString());
                for (int i=0;i<jsonarr_Paymentlist.length();i++){
                    JSONObject Payment_jObj=jsonarr_Paymentlist.getJSONObject(i);
                    BookingList PaymentList =new BookingList();
                    PaymentList.setBooking_no(Payment_jObj.getString("booking_no"));
                    PaymentList.setEmail_id(Payment_jObj.getString("email_id"));
                    PaymentList.setBooking_id(Payment_jObj.getString("booking_id"));
                    PaymentList.setVendor_id(Payment_jObj.getString("vendor_id"));
                    PaymentList.setVehicle_no(Payment_jObj.getString("vehicle_no"));
                    PaymentList.setStatus(Payment_jObj.getString("status"));
                    PaymentList.setBooked_on(Payment_jObj.getString("booked_on"));
                    PaymentList.setIs_paid(Payment_jObj.getString("is_paid"));
                    PaymentList.setAddress(Payment_jObj.getString("address"));
                    if(Payment_jObj.has("vendor_name")) {
                        PaymentList.setVendor_name(Payment_jObj.getString("vendor_name"));
                    } else {
                        PaymentList.setVendor_name("");
                    }
                    if(Payment_jObj.has("mobile_no")) {
                        PaymentList.setVendor_nuber(Payment_jObj.getString("mobile_no"));
                    } else {
                        PaymentList.setVendor_name("");
                    }
                    PaymentList_l.add(PaymentList);
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfPaymentListReload Exception ",e.toString());

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
        PaymentList_l.clear();
        getPaymentList("BookingCompletedRefresh");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case MAKE_CALL_PERMISSION_REQUEST_CODE :
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(BookingCompleted.this, "You can call the number by clicking on the button", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    class PaymentHistoryRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final Button payNow;
        private  Typeface typeface_luci;
        TextView booking_id, vendor_name, vendor_number, vehicle_no, status,booking_id_text,
                vendor_name_text,vendor_number_text,vehicle_no_text,status_text;

        public PaymentHistoryRecyclerViewHolder(View itemView) {
            super(itemView);
            typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");

            booking_id = (TextView) itemView.findViewById(R.id.booking_id);
            payNow = (Button) itemView.findViewById(R.id.payNow);
            vendor_name = (TextView) itemView.findViewById(R.id.vendor_name);
            vendor_number = (TextView) itemView.findViewById(R.id.vendor_number);
            vehicle_no = (TextView) itemView.findViewById(R.id.vehicle_no);
            status = (TextView) itemView.findViewById(R.id.status);
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
            booking_id.setTypeface(typeface_luci);
            vendor_name.setTypeface(typeface_luci);
            vehicle_no.setTypeface(typeface_luci);
            vendor_number.setTypeface(typeface_luci);
            status.setTypeface(typeface_luci);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

        }
    }
    public void giveFeedback(String booking_id,String vendor_id,String rating,String comment) {
        if (IsNetworkConnection.checkNetworkConnection(this)) {

            String url = Constants.SERVER_URL + "booking/booking-rating";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("booking_id",booking_id);
                params.put("vendor_id",vendor_id);
                params.put("rating",rating);
                params.put("comment",comment);
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(BookingCompleted.this,"BookingCompletedRateFeedBAck", null).execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }
    public void ResponseOfRating(String response) {
        JSONObject jsonObject = null;
        final Dialog mBottomSheetDialog = new Dialog(this);

        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBottomSheetDialog.setContentView(R.layout.success_message);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.show();
        TextView message = (TextView) mBottomSheetDialog.findViewById(R.id.message);
        Button ok =(Button) mBottomSheetDialog.findViewById(R.id.ok) ;
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPaymentList("BookingCompleted");
                mBottomSheetDialog.cancel();
            }
        });
        try {

            jsonObject = new JSONObject(response);
            if (jsonObject.getString("status").equalsIgnoreCase("success")) {
                message.setText("Thank you so much for your valuable fedback");

            } else {
                message.setText(jsonObject.getString("message"));
            }
        } catch (Exception e) {

        }
    }

    class PaymentHistoryRecyclerView extends RecyclerView.Adapter < RecyclerView.ViewHolder > {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private final Context context;
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading=false;
        private int visibleThreshold = 4;
        private int lastVisibleItem,
                totalItemCount;

        public PaymentHistoryRecyclerView(Context context) {
            this.context=context;

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                {
                    PrintClass.printValue("PaymentHistoryRecyclerView dy "," : "+dy);

                    final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (dy > 0 && PaymentList_l.size() <total_count) //check for scroll down
                    {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        PrintClass.printValue("PaymentHistoryRecyclerView ","lastVisibleItem : "
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
            return PaymentList_l.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(
                        BookingCompleted.this).inflate(R.layout.bookingcompleted_content, parent, false);
                return new BookingCompleted.PaymentHistoryRecyclerViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(BookingCompleted.this).inflate(R.layout.spinner, parent, false);
                return new BookingCompleted.LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BookingCompleted.PaymentHistoryRecyclerViewHolder) {
                final BookingList Payments = PaymentList_l.get(position);
                final BookingCompleted.PaymentHistoryRecyclerViewHolder userViewHolder =
                        (BookingCompleted.PaymentHistoryRecyclerViewHolder) holder;
                userViewHolder.booking_id.setText(Payments.getBooking_no());
                userViewHolder.vendor_name.setText(Payments.getVendor_name());
                userViewHolder.vendor_number.setText(Payments.getVendor_nuber());
                userViewHolder.vehicle_no.setText(Payments.getVehicle_no());
                userViewHolder.status.setText(Payments.getStatus());
                userViewHolder.vendor_number.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (checkPermission(android.Manifest.permission.CALL_PHONE)) {
                            String dial = "tel:" + Payments.getVehicle_no();
                            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
                        } else {
                            Toast.makeText(BookingCompleted.this, "Permission Call Phone denied", Toast.LENGTH_SHORT).show();
                        }
                        if (checkPermission(android.Manifest.permission.CALL_PHONE)) {
                            userViewHolder.vendor_number.setEnabled(true);
                        } else {
                            userViewHolder.vendor_number.setEnabled(false);
                            ActivityCompat.requestPermissions(BookingCompleted.this, new String[]
                                    {android.Manifest.permission.CALL_PHONE}, MAKE_CALL_PERMISSION_REQUEST_CODE);
                        }
                    }
                });

                if(Payments.getIs_paid().equals("paid") || Payments.getStatus().equalsIgnoreCase("completed") ){
                    userViewHolder.payNow.setVisibility(View.GONE);
                } else {
                    userViewHolder.payNow.setVisibility(View.VISIBLE);
                }
                userViewHolder.payNow.setTag(position);
                userViewHolder.payNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int itemPosition = (Integer) view.getTag();
                        final BookingList bookin = PaymentList_l.get(itemPosition);
                        final Dialog mBottomSheetDialog = new Dialog(context);
                        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mBottomSheetDialog.setContentView(R.layout.pay_now);
                        mBottomSheetDialog.setCancelable(true);
                        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        mBottomSheetDialog.show();
                        Typeface typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");

                        final RadioButton cashonDelivery =(RadioButton)mBottomSheetDialog.findViewById(R.id.cashonDelivery);
                        final TextView title =(TextView) mBottomSheetDialog.findViewById(R.id.title);
                        final RadioButton online =(RadioButton)mBottomSheetDialog.findViewById(R.id.online);
                        final Button pay_now =(Button)mBottomSheetDialog.findViewById(R.id.pay_now);
                        pay_now.setTypeface(typeface_luci);
                        cashonDelivery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if(online.isChecked() && b) {
                                    online.setChecked(false);
                                    cashonDelivery.setChecked(true);
                                }
                            }
                        });

                        title.setTypeface(typeface_luci);
                        cashonDelivery.setTypeface(typeface_luci);
                        online.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                if(cashonDelivery.isChecked() && b) {
                                    cashonDelivery.setChecked(false);
                                }
                            }
                        });

                        online.setTypeface(typeface_luci);
                        pay_now.setTypeface(typeface_luci);
                        pay_now.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(!online.isChecked() && !cashonDelivery.isChecked()) {
                                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                            "Please select the payment mode");
                                    return;
                                }
                                String paymentMode="";
                                if(online.isChecked()) {
                                    paymentMode="online";
                                } else {
                                    paymentMode="cash";
                                }
                                payNow(bookin.getBooking_id(),bookin.getVendor_id(),paymentMode,bookin);
                                mBottomSheetDialog.dismiss();
                            }
                        });
                    }
                });
               // userViewHolder.status.setText(Payments.getStatus());
            } else if (holder instanceof BookingCompleted.LoadingViewHolder) {
                BookingCompleted.LoadingViewHolder loadingViewHolder = (BookingCompleted.LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }
        private boolean checkPermission(String permission) {
            return ContextCompat.checkSelfPermission(BookingCompleted.this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        public void payNow(String booking_id, String vendor_id, String payment_mode, BookingList bookingList){
            if (IsNetworkConnection.checkNetworkConnection(BookingCompleted.this)) {

                String url = Constants.SERVER_URL + "booking/payments";
                JSONObject params = new JSONObject();
                JSONObject payment = new JSONObject();
                try {
                    payment.put("booking_id",booking_id);
                    payment.put("invoice","123456789");
                    payment.put("customer_id",prefrence.getString("user_id", ""));
                    payment.put("vendor_id",vendor_id);
                    payment.put("payment_mode",payment_mode);
                    payment.put("amount","750");
                    payment.put("service_tax","0");
                    payment.put("vat","0");
                    payment.put("total_amount","750");
                    payment.put("status","success");
                    payment.put("description","");
                    params.put("user_id",prefrence.getString("user_id", "") );
                    params.put("payment",payment);
                    params.put("access_token",prefrence.getString("access_token", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
                new post_async(BookingCompleted.this,"Payment",bookingList).execute(url, params.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }

        @Override
        public int getItemCount() {
            return PaymentList_l == null ? 0 : PaymentList_l.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }
}
