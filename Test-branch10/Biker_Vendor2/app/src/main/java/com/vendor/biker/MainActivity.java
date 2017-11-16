package com.vendor.biker;

import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
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
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.IsNetworkConnection;
import com.vendor.biker.Utils.PrintClass;
import com.vendor.biker.Utils.post_async;
import com.vendor.biker.model.BookingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        ,SwipeRefreshLayout.OnRefreshListener{

    private TextView profile_name;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    private View rootView;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    int offset=0;
    int limit=5;
    List<BookingList> bookingList_l=new ArrayList<>();
    private TextView no_records;
    ImageView no_records_img;
    private int total_count=0;
    private BookingDetailsRecyclerView mAdapter;
    private BroadcastReceiver receiver;

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));
    }

    @Override
    public void onRefresh() {
        offset=0;
        bookingList_l.clear();
        getBookingList("BookingDetailsRefresh");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_history);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        //  Constants.statusColor(this);
        rootView = findViewById(android.R.id.content);
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
        View header = navigationView.getHeaderView(0);

        profile_name = (TextView) header.findViewById(R.id.profile_name);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);

        no_records = (TextView) findViewById(R.id.no_records);
        no_records_img = (ImageView) findViewById(R.id.no_records_img);

        recyclerView = (RecyclerView) findViewById(R.id.booking_history);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        if (prefrence.getString("isLoggedIn", "").equals("true")) {
            getBookingList("BookingDetails");
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final ConnectivityManager connMgr = (ConnectivityManager) context
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    System.out.println("NetworkChangeReceiverieieie " + " called");
                    final android.net.NetworkInfo wifi = connMgr
                            .getActiveNetworkInfo();
                    if (wifi != null) {
                        if (wifi.getType() == ConnectivityManager.TYPE_WIFI) {
                            // connected to wifi
                        } else if (wifi.getType() == ConnectivityManager.TYPE_MOBILE) {
                            // connected to the mobile provider's data plan
                        }
                    } else {
                        System.out.println("NetworkChangeReceiverieieie " + " isNotAvailable");
                        Intent i = new Intent(context, ServerError.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                        finish();
                    }
                }
            };
            registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Login");
            alertDialog.setMessage("You are not logged in !! Would Youlike to login ??");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.commit();
                    Intent i=new Intent(MainActivity.this,Login.class);
                    startActivity(i);
                    finish();
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
    }

    public void getBookingList(String action){
        if (IsNetworkConnection.checkNetworkConnection(MainActivity.this)) {
            if(action.equals("BookingDetailsRefresh")) {
                swipeRefreshLayout.setRefreshing(true);
            }
            String url = Constants.SERVER_URL + "vendor/requests";
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
            new post_async(MainActivity.this,action).execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void onTrimMemory(final int level) {
        System.out.println("registerForActivityCallbacks "+" level "+level);

        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            if(receiver !=null) {
                unregisterReceiver(receiver);
                receiver = null;
            }
            System.out.println("registerForActivityCallbacks "+" closed ");

        } else {
            System.out.println("registerForActivityCallbacks "+" open ");
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);

    }

    public void ResponseOfBookingList(String resultString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfBookingList resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                if(jsonObject.has("bookinglist")) {
                    recyclerView.setVisibility(View.VISIBLE);
                    no_records.setVisibility(View.GONE);
                    no_records_img.setVisibility(View.GONE);
                    JSONArray jsonarr_bookinglist = jsonObject.getJSONArray("bookinglist");
                    PrintClass.printValue("ResponseOfBookingList bookingList ", jsonarr_bookinglist.toString());
                    for (int i = 0; i < jsonarr_bookinglist.length(); i++) {
                        JSONObject booking_jObj = jsonarr_bookinglist.getJSONObject(i);
                        BookingList bookingList = new BookingList();
                        bookingList.setBooking_no(booking_jObj.getString("booking_no"));
                        bookingList.setBooking_id(booking_jObj.getString("booking_id"));
                        bookingList.setEmail_id(booking_jObj.getString("email_id"));
                        bookingList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                        bookingList.setStatus(booking_jObj.getString("status"));
                        bookingList.setBooked_on(booking_jObj.getString("booked_on"));
                        bookingList.setAddress(booking_jObj.getString("address"));
                        bookingList.setCustomer_name(booking_jObj.getString("customer_name"));
                        bookingList_l.add(bookingList);
                    }
                    if (jsonObject.has("totalCount")) {
                        total_count = Integer.parseInt(jsonObject.getString("totalCount"));
                    }
                    mAdapter = new MainActivity.BookingDetailsRecyclerView();
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
                                }, 1000);
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

        }catch (Exception e){
            swipeRefreshLayout.setRefreshing(false);
            PrintClass.printValue("ResponseOfBookingList Exception ",e.toString());
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
            PrintClass.printValue("ResponseOfBookingListReload resultString ", " has data " + jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonarr_bookinglist=jsonObject.getJSONArray("bookinglist");
                PrintClass.printValue("ResponseOfBookingListReload bookingList ",jsonarr_bookinglist.toString());
                for (int i=0;i<jsonarr_bookinglist.length();i++){
                    JSONObject booking_jObj=jsonarr_bookinglist.getJSONObject(i);
                    BookingList bookingList =new BookingList();
                    bookingList.setBooking_no(booking_jObj.getString("booking_no"));
                    bookingList.setBooking_id(booking_jObj.getString("booking_id"));
                    bookingList.setEmail_id(booking_jObj.getString("email_id"));
                    bookingList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                    bookingList.setStatus(booking_jObj.getString("status"));
                    bookingList.setBooked_on(booking_jObj.getString("booked_on"));
                    bookingList_l.add(bookingList);
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfBookingListReload Exception ", e.toString());

        }
    }

    public void ResponseOfBookingAccept(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfBookingAccept resultString ", " has data " + jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Successfully Accepted the booking request");
                bookingList_l.clear();
                offset=0;
                getBookingList("BookingDetails");
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfBookingAccept Exception ", e.toString());
        }
    }

    static  class BookingDetailsRecyclerViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout action;
        TextView booking_id, customer_name, customer_number, vehicle_no;
        Button action_accept;

        public BookingDetailsRecyclerViewHolder(View itemView) {
            super(itemView);
            booking_id = (TextView) itemView.findViewById(R.id.booking_id);
            customer_name = (TextView) itemView.findViewById(R.id.customer_name);
            customer_number = (TextView) itemView.findViewById(R.id.customer_number);
            vehicle_no = (TextView) itemView.findViewById(R.id.vehicle_no);
            action_accept = (Button) itemView.findViewById(R.id.action_accept);
            //action_reject = (Button) itemView.findViewById(R.id.action_decline);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

        }
    }

    class BookingDetailsRecyclerView extends RecyclerView.Adapter < RecyclerView.ViewHolder > {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading=false;
        private int visibleThreshold = 4;
        private int lastVisibleItem, totalItemCount;

        public BookingDetailsRecyclerView() {
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
                    if (dy > 0 && bookingList_l.size() < total_count ) //check for scroll down
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
                        MainActivity.this).inflate(R.layout.booking_history_content, parent, false);
                return new BookingDetailsRecyclerViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.spinner, parent, false);
                return new MainActivity.LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MainActivity.BookingDetailsRecyclerViewHolder) {
                PrintClass.printValue("onBindViewHolder12345 ","pos "+position);
                final BookingList bookings = bookingList_l.get(position);
                MainActivity.BookingDetailsRecyclerViewHolder userViewHolder =
                        (MainActivity.BookingDetailsRecyclerViewHolder)holder;

                userViewHolder.booking_id.setText(bookings.getBooking_no());
                userViewHolder.customer_name.setText(bookings.getCustomer_name());
                userViewHolder.vehicle_no.setText(bookings.getVehicle_no());
                userViewHolder.action_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        acceptRejectAction(bookings.getBooking_id());
                    PrintClass.printValue("SYSTEMPRINT UserRegister  ", "getBooking_id " + bookings.getBooking_id());

                }
                });
            } else if (holder instanceof MainActivity.LoadingViewHolder) {
                MainActivity.LoadingViewHolder loadingViewHolder = (MainActivity.LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return bookingList_l == null ? 0 : bookingList_l.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }

    public void acceptRejectAction(String booking_id){
        if (IsNetworkConnection.checkNetworkConnection(MainActivity.this)) {

            String url = Constants.SERVER_URL + "vendor/request-accept";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("booking_id",booking_id);
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(MainActivity.this,"RequestAccept").execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
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
            onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            if (IsNetworkConnection.checkNetworkConnection(MainActivity.this)) {
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
                    Intent i=new Intent(MainActivity.this,Login.class);
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
