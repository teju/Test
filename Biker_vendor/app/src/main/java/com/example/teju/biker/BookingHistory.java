package com.example.teju.biker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.teju.biker.Utils.Constants;
import com.example.teju.biker.Utils.CustomToast;
import com.example.teju.biker.Utils.IsNetworkConnection;
import com.example.teju.biker.Utils.PrintClass;
import com.example.teju.biker.Utils.post_async;
import com.example.teju.biker.model.BookingList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Teju on 22/09/2017.
 */
public class BookingHistory extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private SharedPreferences.Editor editor;
    private SharedPreferences prefrence;
    List<BookingList> bookingList_l=new ArrayList<>();
    TextView profile_name;
    private View rootView;
    int offset=0;
    int limit=5;
    BookingHistoryRecyclerView mAdapter ;
    private int total_count=0;
    private TextView no_records;

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_history);
        no_records=(TextView)findViewById(R.id.no_records);
        rootView=findViewById(android.R.id.content);
        bookingList_l.clear();
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

        TextView title = (TextView) findViewById(R.id.title_val);
        title.setText("Booking History");

        View header = navigationView.getHeaderView(0);

        profile_name=(TextView)header.findViewById(R.id.profile_name);
        profile_name.setText(prefrence.getString("name", ""));
        Typeface typeface = Typeface.createFromAsset(getAssets(),
                "fonts/name_font.ttf");
        profile_name.setTypeface(typeface);

        recyclerView =(RecyclerView)findViewById(R.id.booking_history);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

       // Constants.statusColor(this);
        getBookingList("BookingHistory");
    }

    public void getBookingList(String action){
        if (IsNetworkConnection.checkNetworkConnection(BookingHistory.this)) {
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
            new post_async(BookingHistory.this,action).execute(url, params.toString());
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
                if (IsNetworkConnection.checkNetworkConnection(BookingHistory.this)) {
                    Intent i = new Intent(this, UserRegister.class);
                    i.putExtra("type", "edit");
                    startActivity(i);
                } else {
                    Intent i=new Intent(this,ServerError.class);
                    startActivity(i);
                }
                // Handle the camera action
            } else if (id == R.id.payment_history) {
                Intent i = new Intent(this, PaymentHistory.class);
                startActivity(i);
            } else if (id == R.id.setting) {
                Intent i = new Intent(this, Setting.class);
                startActivity(i);
            } else if (id == R.id.logout) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm Logout");
                alertDialog.setMessage("Are you sure you want to Logout ?");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        editor.putString("isLoggedIn","false");
                        editor.commit();
                        Intent i = new Intent(BookingHistory.this, Login.class);
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
                    bookingList.setEmail_id(booking_jObj.getString("email_id"));
                    bookingList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                    bookingList.setStatus(booking_jObj.getString("status"));
                    bookingList.setBooked_on(booking_jObj.getString("booked_on"));
                    bookingList.setAddress(booking_jObj.getString("address"));
                    bookingList_l.add(bookingList);
                }
                if(jsonObject.has("totalCount")) {
                    total_count = Integer.parseInt(jsonObject.getString("totalCount"));
                }
                if(bookingList_l.size() !=0) {
                    mAdapter = new BookingHistoryRecyclerView();
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
                                          getBookingList("BookingHistoryReload");
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
                    no_records.setText(jsonObject.getString("message"));
                }
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfBookingList Exception ",e.toString());
            no_records.setVisibility(View.VISIBLE);
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
                    bookingList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                    bookingList.setStatus(booking_jObj.getString("status"));
                    bookingList.setBooked_on(booking_jObj.getString("booked_on"));
                    bookingList.setAddress(booking_jObj.getString("address"));
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

    static  class BookingHistoryRecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView booking_id, vendor_email, booking_date, vehicle_no, status;

        public BookingHistoryRecyclerViewHolder(View itemView) {
            super(itemView);
            booking_id = (TextView) itemView.findViewById(R.id.booking_id);
            vendor_email = (TextView) itemView.findViewById(R.id.vendor_email);
            booking_date = (TextView) itemView.findViewById(R.id.booking_date);
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

    class BookingHistoryRecyclerView extends RecyclerView.Adapter < RecyclerView.ViewHolder > {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading=false;
        private int visibleThreshold = 4;
        private int lastVisibleItem,
                totalItemCount;

        public BookingHistoryRecyclerView() {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                {
                    PrintClass.printValue("BookingHistoryRecyclerView dy "," : "+dy);

                    final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (dy > 0 && bookingList_l.size() <total_count) //check for scroll down
                    {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        PrintClass.printValue("BookingHistoryRecyclerView ","lastVisibleItem : "
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
                        BookingHistory.this).inflate(R.layout.booking_history_content, parent, false);
                return new BookingHistoryRecyclerViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(BookingHistory.this).inflate(R.layout.spinner, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof BookingHistoryRecyclerViewHolder) {
                BookingList bookings = bookingList_l.get(position);
                BookingHistoryRecyclerViewHolder userViewHolder = (BookingHistoryRecyclerViewHolder) holder;
                userViewHolder.booking_id.setText(bookings.getBooking_no());
                userViewHolder.vendor_email.setText(bookings.getEmail_id());
                userViewHolder.vehicle_no.setText(bookings.getVehicle_no());
                userViewHolder.booking_date.setText(getformatteddate(bookings.getBooked_on()));
                userViewHolder.status.setText(bookings.getStatus());
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
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
}
