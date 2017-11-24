package com.vendor.biker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.IsNetworkConnection;
import com.vendor.biker.Utils.PrintClass;
import com.vendor.biker.Utils.post_async;
import com.vendor.biker.model.JobListModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JobList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener ,
        SwipeRefreshLayout.OnRefreshListener{

    private RecyclerView mRecyclerView;
    private jobAdapter mjobAdapter;
    private TextView profile_name;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    private View rootView;
    private SwipeRefreshLayout swipeRefreshLayout;
    int offset=0;
    int limit=5;
    List<JobListModel> jobList_l=new ArrayList<>();
    private TextView no_records;
    private int total_count=0;
    private ImageView no_records_img;
    private String booking_id="";
    List<String> al = new ArrayList<>();
    Set<String> hs = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_list);
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        //  Constants.statusColor(this);
        rootView=findViewById(android.R.id.content);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        no_records =(TextView) findViewById(R.id.no_records);
        no_records_img =(ImageView) findViewById(R.id.no_records_img);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
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

        mRecyclerView = (RecyclerView) findViewById(R.id.job_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        booking_id=getIntent().getStringExtra("booking_id");

        if(!booking_id.equals("")){
            if (prefrence.getString("isLoggedIn", "").equals("true")) {
                changeStatus("picked", booking_id);
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm Login");
                alertDialog.setMessage("You are not logged in !! Would You like to login ??");
                alertDialog.setPositiveButton("YES",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        editor.putString("isLoggedIn","false");
                        editor.commit();
                        Intent i=new Intent(JobList.this,Login.class);
                        startActivity(i);
                        finish();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mRecyclerView.setVisibility(View.GONE);
                        no_records.setVisibility(View.VISIBLE);
                        no_records_img.setVisibility(View.VISIBLE);
                        no_records.setText("You are not logged In !!");
                    }
                });
                alertDialog.show();
            }
        } else {
            if (prefrence.getString("isLoggedIn", "").equals("true")) {
                getJobList("jobListDetails");
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("Confirm Login");
                alertDialog.setMessage("You are not logged in !! Would You like to login ??");
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) {
                        editor.putString("isLoggedIn","false");
                        editor.commit();
                        Intent i=new Intent(JobList.this,Login.class);
                        startActivity(i);
                        finish();
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        mRecyclerView.setVisibility(View.GONE);
                        no_records.setVisibility(View.VISIBLE);
                        no_records_img.setVisibility(View.VISIBLE);
                        no_records.setText("You are not logged In !!");
                    }
                });
                alertDialog.show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        profile_name.setText(prefrence.getString("name", ""));

    }

    public void changeStatus(String status, String booking_id){
        if (IsNetworkConnection.checkNetworkConnection(JobList.this)) {

            String url = Constants.SERVER_URL + "vendor/change-status";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("booking_id",booking_id);
                params.put("status",status.toLowerCase());
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(JobList.this,"changeStatus").execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void ResponseOfChangeStatus(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfChangeStatus resultString ", " has data "
                    + jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "Status Changes Successfully");
                offset =0 ;
                jobList_l.clear();
                if (prefrence.getString("isLoggedIn", "").equals("true")) {
                    getJobList("jobListDetails");
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Confirm Login");
                    alertDialog.setMessage("You are not logged in !! Would Youlike to login ??");
                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            editor.putString("isLoggedIn","false");
                            editor.commit();
                            Intent i=new Intent(JobList.this,Login.class);
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
        }
        catch (Exception e){
            PrintClass.printValue("ResponseOfChangeStatus Exception ", e.toString());
        }
    }

    public void getJobList(String action){
        if (IsNetworkConnection.checkNetworkConnection(JobList.this)) {
            if(action.equals("JobListDetailsRefresh")) {
                swipeRefreshLayout.setRefreshing(true);
            }
            String url = Constants.SERVER_URL + "vendor/ongoing-requests";
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
            new post_async(JobList.this,action).execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void ResponseOfJobList(String resultString) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfBookingList resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                if(jsonObject.has("bookinglist")) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    no_records.setVisibility(View.GONE);
                    no_records_img.setVisibility(View.GONE);
                    JSONArray jsonarr_joblist=jsonObject.getJSONArray("bookinglist");
                    PrintClass.printValue("ResponseOfBookingList bookingList ",jsonarr_joblist.toString());
                    for (int i=0;i<jsonarr_joblist.length();i++){
                        JSONObject booking_jObj=jsonarr_joblist.getJSONObject(i);
                        JobListModel jobList =new JobListModel();
                        jobList.setBooking_no(booking_jObj.getString("booking_no"));
                        jobList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                        jobList.setAddress(booking_jObj.getString("address"));
                        jobList.setCustomer_name(booking_jObj.getString("customer_name"));
                        jobList.setCustomer_number(booking_jObj.getString("customer_name"));
                        jobList.setBooking_id(booking_jObj.getString("booking_id"));
                        jobList.setOtp(booking_jObj.getString("otp"));
                        jobList.setMobile_no(booking_jObj.getString("mobile_no"));
                        jobList.setStatus(booking_jObj.getString("status"));
                        if(booking_jObj.has("address")) {
                            JSONObject address=booking_jObj.getJSONObject("address");
                            jobList.setLatitude(address.getString("lattitude"));
                            jobList.setLongitude(address.getString("longitude"));
                        }
                        jobList_l.add(jobList);
                    }
                    if(jsonObject.has("totalCount")) {
                        total_count = Integer.parseInt(jsonObject.getString("totalCount"));
                    }
                    mjobAdapter = new jobAdapter();
                    mRecyclerView.setAdapter(mjobAdapter);
                    mjobAdapter.notifyDataSetChanged();
                    mjobAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                        @Override
                        public void onLoadMore() {
                            Log.e("haint", "Load More");
                            jobList_l.add(null);
                            mjobAdapter.notifyItemInserted(jobList_l.size() - 1);
                            //Load more data for reyclerview
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                Log.e("haint", "Load More 2");
                                    PrintClass.printValue("ResponseOfjobList onLoadMore "
                                            , "LOOPED");
                                    //Remove loading item
                                    jobList_l.remove(jobList_l.size() - 1);
                                    mjobAdapter.notifyItemRemoved(jobList_l.size());
                                    //Load data
                                    offset = offset + limit;
                                    getJobList("jobDetailsReload");
                                }
                            }, 1000);
                        }
                    });
                } else {
                   mRecyclerView.setVisibility(View.GONE);
                    no_records.setVisibility(View.VISIBLE);
                    no_records_img.setVisibility(View.VISIBLE);
                    no_records.setText(jsonObject.getString("message"));

                }
            } else {
                mRecyclerView.setVisibility(View.GONE);
                no_records.setVisibility(View.VISIBLE);
                no_records_img.setVisibility(View.VISIBLE);
                no_records.setText(jsonObject.getString("message"));
            }
            swipeRefreshLayout.setRefreshing(false);

        }catch (Exception e){
            swipeRefreshLayout.setRefreshing(false);
            PrintClass.printValue("ResponseOfjobList Exception ",e.toString());
            no_records_img.setVisibility(View.VISIBLE);
            no_records.setVisibility(View.VISIBLE);
            try {
                no_records.setText(jsonObject.getString("message"));
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }
    @Override
    public void onRefresh() {
        offset=0;
        jobList_l.clear();
        getJobList("jobDetailsRefresh");
    }

    public void ResponseOfjobListReload(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfjobListReload resultString ", " has data " + jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonarr_joblist=jsonObject.getJSONArray("bookinglist");
                PrintClass.printValue("ResponseOfjobListReload jobList ",jsonarr_joblist.toString());
                for (int i=0;i<jsonarr_joblist.length();i++){
                    JSONObject booking_jObj=jsonarr_joblist.getJSONObject(i);
                    JobListModel jobList =new JobListModel();
                    jobList.setBooking_no(booking_jObj.getString("booking_no"));
                    jobList.setVehicle_no(booking_jObj.getString("vehicle_no"));
                    jobList.setAddress(booking_jObj.getString("address"));
                    jobList.setCustomer_name(booking_jObj.getString("customer_name"));
                    jobList.setCustomer_number(booking_jObj.getString("customer_name"));
                    jobList.setBooking_id(booking_jObj.getString("booking_id"));
                    jobList.setMobile_no(booking_jObj.getString("mobile_no"));
                    jobList.setOtp(booking_jObj.getString("otp"));
                    jobList.setStatus(booking_jObj.getString("status"));
                    if(booking_jObj.has("address")) {
                        JSONObject address=booking_jObj.getJSONObject("address");
                        jobList.setLatitude(address.getString("lattitude"));
                        jobList.setLongitude(address.getString("longitude"));
                    }
                    jobList_l.add(jobList);
                }
                mjobAdapter.notifyDataSetChanged();
                mjobAdapter.setLoaded();
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfBookingListReload Exception ", e.toString());

        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.profile) {
            if (IsNetworkConnection.checkNetworkConnection(JobList.this)) {
                Intent i = new Intent(this, UserRegister.class);
                i.putExtra("type", "edit");
                startActivity(i);
            } else {
                Intent i=new Intent(this,ServerError.class);
                startActivity(i);

            }
            // Handle the camera action
        }  else if (id == R.id.home) {
            Intent i=new Intent(this,MainActivity.class);
            startActivity(i);
        } else if (id == R.id.job_history) {
            Intent i=new Intent(this,JobHistory.class);
            startActivity(i);
        } else if (id == R.id.refer) {
            Intent i=new Intent(this,ReferUser.class);
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
                    Intent i=new Intent(JobList.this,Login.class);
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

    static class jobViewHolder extends RecyclerView.ViewHolder {
        Button map;
        TextView booking_id,customer_name,customer_number,vehicle_no,otp,status;
        public jobViewHolder(View itemView) {
            super(itemView);
            map =(Button)itemView.findViewById(R.id.map);
            booking_id =(TextView)itemView.findViewById(R.id.booking_id);
            customer_name =(TextView)itemView.findViewById(R.id.customer_name);
            customer_number =(TextView)itemView.findViewById(R.id.vendor_number);
            vehicle_no =(TextView)itemView.findViewById(R.id.vehicle_no);
            otp =(TextView)itemView.findViewById(R.id.otp);
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

    class jobAdapter extends RecyclerView.Adapter < RecyclerView.ViewHolder > {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading;
        private int visibleThreshold = 5;
        private int lastVisibleItem,
                totalItemCount;

        public jobAdapter() {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }


                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy)
                {

                    final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (dy > 0 && jobList_l.size() < total_count) //check for scroll down
                    {
                        totalItemCount = linearLayoutManager.getItemCount();
                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
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
            this.mOnLoadMoreListener = mOnLoadMoreListener;
        }

        @Override
        public int getItemViewType(int position) {
            return jobList_l.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(JobList.this).inflate(R.layout.job_list_content, parent, false);
                return new jobViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(JobList.this).inflate(R.layout.spinner, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof jobViewHolder) {
                final jobViewHolder jobViewHolder = (jobViewHolder) holder;
                final JobListModel bookings = jobList_l.get(position);

                jobViewHolder.booking_id.setText(bookings.getBooking_no());
                jobViewHolder.customer_name.setText(bookings.getCustomer_name());
                jobViewHolder.customer_number.setText(bookings.getMobile_no());
                jobViewHolder.otp.setText(bookings.getOtp());
                jobViewHolder.vehicle_no.setText(bookings.getVehicle_no());
                jobViewHolder.map.setTag(position);
                jobViewHolder.status.setTag(position);
                jobViewHolder.status.setText(bookings.getStatus().toUpperCase());

                if(bookings.getStatus().equalsIgnoreCase("Picked")) {
                    jobViewHolder.status.setVisibility(View.VISIBLE);
                    jobViewHolder.map.setVisibility(View.GONE);
                    jobViewHolder.status.setBackgroundColor(getResources().getColor(R.color.light_red2));
                } else if(bookings.getStatus().equalsIgnoreCase("InProcess")) {
                    jobViewHolder.status.setVisibility(View.VISIBLE);
                    jobViewHolder.map.setVisibility(View.GONE);
                    jobViewHolder.status.setBackgroundColor(getResources().getColor(R.color.yellow));
                } else if(bookings.getStatus().equalsIgnoreCase("Completed")) {
                    jobViewHolder.map.setVisibility(View.GONE);
                    jobViewHolder.status.setVisibility(View.VISIBLE);
                    jobViewHolder.status.setBackgroundColor(getResources().getColor(R.color.blue));
                } else if(bookings.getStatus().equalsIgnoreCase("Delivered")) {
                    jobViewHolder.map.setVisibility(View.GONE);
                    jobViewHolder.status.setVisibility(View.VISIBLE);
                    jobViewHolder.status.setBackgroundColor(getResources().getColor(R.color.green));
                }

                jobViewHolder.status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int itemPosition = (Integer) v.getTag();
                        final PopupMenu popupMenu = new PopupMenu(JobList.this, v, Gravity.FILL_HORIZONTAL);
                        final Menu menu = popupMenu.getMenu();
                        popupMenu.getMenuInflater().inflate(R.menu.menu_main, menu);
                        popupMenu.show();
                        if (jobList_l.get(itemPosition).getStatus().equalsIgnoreCase("Picked")) {
                            menu.getItem(0).setEnabled(false);
                            menu.getItem(2).setEnabled(false);
                        } else if (jobList_l.get(itemPosition).getStatus().equalsIgnoreCase("InProcess")) {
                            menu.getItem(0).setEnabled(false);
                            menu.getItem(1).setEnabled(false);
                        } else if (jobList_l.get(itemPosition).getStatus().equalsIgnoreCase("Completed")) {
                            menu.getItem(0).setEnabled(false);
                            menu.getItem(1).setEnabled(false);
                        }
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                changeStatus(item.getTitle().toString(), jobList_l.get(itemPosition).getBooking_id());
                                jobViewHolder.status.setText(item.getTitle().toString().toUpperCase());
                                return true;
                            }
                        });

                    }
                });
                jobViewHolder.map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int index = (Integer) view.getTag();
                        final JobListModel bookng = jobList_l.get(index);

                        Intent i=new Intent(JobList.this,PathGoogleMapActivity.class);
                        i.putExtra("latitude",bookng.getLatitude());
                        i.putExtra("longitude",bookng.getLongitude());
                        i.putExtra("booking_id",bookng.getBooking_id());
                        startActivity(i);
                        finish();

                    }
                });
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return jobList_l == null ? 0 : jobList_l.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }

}
