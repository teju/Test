package com.biker;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

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

public class PaymentHistory extends AppCompatActivity
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
    PaymentHistory.PaymentHistoryRecyclerView mAdapter ;
    private int total_count=0;
    private TextView no_records;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView no_records_img;

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));
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
        title.setText("Payment Details");
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
            getPaymentList("PaymentHistoryCompleted");
        }else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Login");
            alertDialog.setMessage("You are not logged in !! Would You like to login ??");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.commit();
                    Intent i=new Intent(PaymentHistory.this,Login.class);
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
        if (IsNetworkConnection.checkNetworkConnection(PaymentHistory.this)) {
            if(action.equals("PaymentHistoryRefresh")) {
                swipeRefreshLayout.setRefreshing(true);
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
            new post_async(PaymentHistory.this,action).execute(url, params.toString());
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
                if (IsNetworkConnection.checkNetworkConnection(PaymentHistory.this)) {
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
            }  else if (id == R.id.booking_completed) {
                Intent i=new Intent(this,BookingCompleted.class);
                startActivity(i);
            } /*else if (id == R.id.setting) {
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
                        Intent i = new Intent(PaymentHistory.this, Login.class);
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
                        mAdapter = new PaymentHistory.PaymentHistoryRecyclerView(PaymentHistory.this);
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
                                                  getPaymentList("PaymentHistoryReload");
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
        getPaymentList("PaymentHistoryRefresh");
    }

    static  class PaymentHistoryRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final Button payNow;
        TextView booking_id, vendor_name, vendor_number, vehicle_no, status;

        public PaymentHistoryRecyclerViewHolder(View itemView) {
            super(itemView);
            booking_id = (TextView) itemView.findViewById(R.id.booking_id);
            payNow = (Button) itemView.findViewById(R.id.payNow);
            vendor_name = (TextView) itemView.findViewById(R.id.vendor_name);
            vendor_number = (TextView) itemView.findViewById(R.id.vendor_number);
            vehicle_no = (TextView) itemView.findViewById(R.id.vehicle_no);
            status = (TextView) itemView.findViewById(R.id.status);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

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
                        PaymentHistory.this).inflate(R.layout.bookingcompleted_content, parent, false);
                return new PaymentHistory.PaymentHistoryRecyclerViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(PaymentHistory.this).inflate(R.layout.spinner, parent, false);
                return new PaymentHistory.LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof PaymentHistory.PaymentHistoryRecyclerViewHolder) {
                BookingList Payments = PaymentList_l.get(position);
                PaymentHistory.PaymentHistoryRecyclerViewHolder userViewHolder =
                        (PaymentHistory.PaymentHistoryRecyclerViewHolder) holder;
                userViewHolder.booking_id.setText(Payments.getBooking_no());
                userViewHolder.vendor_name.setText(Payments.getVendor_name());
                userViewHolder.vendor_number.setText(Payments.getVendor_nuber());
                userViewHolder.vehicle_no.setText(Payments.getVehicle_no());
                userViewHolder.status.setText(Payments.getStatus());
                userViewHolder.payNow.setTag(position);
                userViewHolder.payNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int itemPosition = (Integer) view.getTag();
                        final BookingList bookin = PaymentList_l.get(itemPosition);

                        final Dialog mBottomSheetDialog = new Dialog(context);
                        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mBottomSheetDialog.setContentView(R.layout.rating_feedback);
                        mBottomSheetDialog.setCancelable(true);
                        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        mBottomSheetDialog.show();
                        Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
                        final RatingBar ratings = (RatingBar) mBottomSheetDialog.findViewById(R.id.ratings);
                        final EditText feedback = (EditText) mBottomSheetDialog.findViewById(R.id.feedback);
                        ratings.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                                String rateValue = String.valueOf(ratingBar.getRating());
                                System.out.println("Rate for Module is"+rateValue);
                            }
                        });
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(ratings.getRating() == 0) {
                                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                            "Please Provide your rating");
                                } else if(feedback.getText().toString().trim().length() == 0 ) {
                                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                            "Please Provide your feedback");
                                } else {
                                    giveFeedback(bookin.getBooking_id(), bookin.getVendor_id(),
                                            String.valueOf((int)ratings.getRating()), feedback.getText().toString());
                                }
                                mBottomSheetDialog.dismiss();
                            }
                        });
                    }
                });
               // userViewHolder.status.setText(Payments.getStatus());
            } else if (holder instanceof PaymentHistory.LoadingViewHolder) {
                PaymentHistory.LoadingViewHolder loadingViewHolder = (PaymentHistory.LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        public void giveFeedback(String booking_id,String vendor_id,String rating,String comment) {
            if (IsNetworkConnection.checkNetworkConnection(context)) {

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
                new post_async(PaymentHistory.this,"RateFeedBAck").execute(url, params.toString());
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
