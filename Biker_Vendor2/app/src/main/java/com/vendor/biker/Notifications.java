package com.vendor.biker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vendor.biker.Utils.Constants;
import com.vendor.biker.Utils.CustomToast;
import com.vendor.biker.Utils.IsNetworkConnection;
import com.vendor.biker.Utils.PrintClass;
import com.vendor.biker.Utils.post_async;
import com.vendor.biker.model.NotificationModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Notifications extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout swipeRefreshLayout;
    TextView profile_name;
    private TextView no_records;
    private ImageView no_records_img;
    private View rootView;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    List<NotificationModel> notifications_list=new ArrayList<>();
    int offset=0;
    int limit=100;
    private int total_count=0;
    private NotificationsRecyclerView mAdapter;
    private ImageView menu_icon;
    private ImageView noti;
    private TextView noti_indication;
    private NotificationModel notif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        init();

    }
    public void init() {
        noti_indication = (TextView)findViewById(R.id.noti_count);
        noti_indication.setVisibility(View.GONE);
        no_records=(TextView)findViewById(R.id.no_records);
        no_records_img=(ImageView)findViewById(R.id.no_records_img);
        rootView=findViewById(android.R.id.content);
        notifications_list.clear();
        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        noti = (ImageView)findViewById(R.id.noti);
        noti.setVisibility(View.GONE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView title = (TextView) findViewById(R.id.title_val);
        title.setText("Notifications");
        editor.putString("isNotiReceived", "false");
        editor.commit();
        menu_icon = (ImageView)findViewById(R.id.menu);
        menu_icon.setVisibility(View.GONE);

        recyclerView =(RecyclerView)findViewById(R.id.notifications);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        editor.putString("isNotiReceived", "false");

    }



    @Override
    public void onRefresh() {
        offset=0;
        notifications_list.clear();
        getNotifications("NotificationRefresh");
    }

    public void noti_read(String notification_id, NotificationModel notify){
        if (IsNetworkConnection.checkNetworkConnection(Notifications.this)) {


            String url = Constants.SERVER_URL + "profile/push-notiffication-update";
            JSONObject params = new JSONObject();
            try {
                params.put("user_type","customer" );
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("notification_id",notification_id);
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(Notifications.this,"NOTIFICATION_READ",notify).execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }


    public void getNotifications(String action){
        if (IsNetworkConnection.checkNetworkConnection(Notifications.this)) {

            if(action.equals("NotificationsRefresh")) {
                swipeRefreshLayout.setRefreshing(true);
            }
            if(action.equals("NotificationsCompleted")) {
                notifications_list.clear();
            }

            String url = Constants.SERVER_URL + "profile/push-notiffication";
            JSONObject params = new JSONObject();
            try {
                params.put("user_type","vendor" );
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));
                params.put("offset", String.valueOf(offset));
                params.put("limit", String.valueOf(limit));
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(Notifications.this,action, null).execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void ResponseOfNotificationsList(String resultString) {
            JSONObject jsonObject = null;
            try {
                swipeRefreshLayout.setRefreshing(false);
                jsonObject = new JSONObject(resultString);
                PrintClass.printValue("ResponseOfPaymentList resultString "," has data "+jsonObject.toString());
                if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                    if(jsonObject.has("bookinglist")) {
                        JSONArray jsonarr_notilist = jsonObject.getJSONArray("bookinglist");
                        for (int i = 0; i < jsonarr_notilist.length(); i++) {
                            JSONObject noti_jObj = jsonarr_notilist.getJSONObject(i);
                            NotificationModel notiList = new NotificationModel();
                            notiList.setBooking_no(noti_jObj.getString("booking_no"));
                            notiList.setBooking_id(noti_jObj.getString("booking_id"));
                            notiList.setVendor_id(noti_jObj.getString("vendor_id"));
                            notiList.setTitle(noti_jObj.getString("title"));
                            notiList.setBody(noti_jObj.getString("body"));
                            notiList.setAction(noti_jObj.getString("action"));
                            notiList.setCreated_on(noti_jObj.getString("created_on"));
                            notiList.setNotification_id(noti_jObj.getString("notification_id"));
                            notiList.setRead_status(noti_jObj.getString("read_status"));

                            notifications_list.add(notiList);
                        }
                        if (jsonObject.has("totalCount")) {
                            total_count = Integer.parseInt(jsonObject.getString("totalCount"));
                        }
                        if (notifications_list.size() != 0) {
                            mAdapter = new NotificationsRecyclerView(Notifications.this);
                            recyclerView.setAdapter(mAdapter);
                            if (jsonObject.has("bookinglist")) {
                                final JSONObject finalJsonObject = jsonObject;
                                mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                                    @Override
                                    public void onLoadMore() {
                                        Log.e("haint", "Load More");
                                        notifications_list.add(null);
                                        mAdapter.notifyItemInserted(notifications_list.size() - 1);
                                        //Load more data for reyclerview
                                        new Handler().postDelayed(new Runnable() {
                                                                      @Override
                                                                      public void run() {
                                                                          Log.e("haint", "Load More 2");
                                                                          if (finalJsonObject.has("bookinglist")) {
                                                                              PrintClass.printValue("ResponseOfPaymentList onLoadMore "
                                                                                      , "LOOPED");
                                                                              //Remove loading item
                                                                              notifications_list.remove(notifications_list.size() - 1);
                                                                              mAdapter.notifyItemRemoved(notifications_list.size());
                                                                              //Load data
                                                                              offset = offset + limit;
                                                                              getNotifications("NotificationRefresh");
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

    public void ResponseOfNotitReload(String resultString) {
        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfPaymentListReload resultString "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonarr_Paymentlist=jsonObject.getJSONArray("bookinglist");
                PrintClass.printValue("ResponseOfPaymentListReload PaymentList ",jsonarr_Paymentlist.toString());
                for (int i=0;i<jsonarr_Paymentlist.length();i++){
                    JSONObject noti_jObj=jsonarr_Paymentlist.getJSONObject(i);
                    NotificationModel notiList = new NotificationModel();
                    notiList.setBooking_no(noti_jObj.getString("booking_no"));
                    notiList.setBooking_id(noti_jObj.getString("booking_id"));
                    notiList.setVendor_id(noti_jObj.getString("vendor_id"));
                    notiList.setTitle(noti_jObj.getString("title"));
                    notiList.setBody(noti_jObj.getString("body"));
                    notiList.setAction(noti_jObj.getString("action"));
                    notiList.setCreated_on(noti_jObj.getString("created_on"));
                    notiList.setNotification_id(noti_jObj.getString("notification_id"));
                    notiList.setRead_status(noti_jObj.getString("read_status"));

                    notifications_list.add(notiList);
                }
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfPaymentListReload Exception ",e.toString());

        }
    }

    public void ResponseOfReadNotification(String resultString, NotificationModel notiList) {
        try {

            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("ResponseOfReadNotification resultString "," has data "
                    +jsonObject.toString()+" getAction "+notiList.getAction());
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {

            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message"));
            }
            if (notiList.getAction().contains("reached_destination")) {
                Intent i = new Intent(Notifications.this, PathGoogleMapActivity.class);
                i.putExtra("booking_id", notiList.getBooking_id());
                startActivity(i);
            } else if (notiList.getAction().contains("accepted") ||
                    notiList.getAction().contains("picked") ||
                    notiList.getAction().contains("inprocess") ||
                    notiList.getAction().contains("otp_verified")) {
                Intent i = new Intent(Notifications.this, JobList.class);
                if(notiList.getAction().contains("otp_verified")) {
                    i.putExtra("booking_id", notiList.getBooking_id());
                } else {
                    i.putExtra("booking_id", "");
                }
                startActivity(i);
            } else if (notiList.getAction().contains("delivered")) {
                Intent i = new Intent(Notifications.this, JobHistory.class);
                startActivity(i);
            } else {
                Intent i = new Intent(Notifications.this, PaymentHistory.class);
                startActivity(i);
            }
        }catch (Exception e){
            PrintClass.printValue("ResponseOfReadNotification Exception ",e.toString());

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        notifications_list.clear();
        if(prefrence.getString("isLoggedIn", "").equals("true")) {
            // Constants.statusColor(this);
            getNotifications("Notifications");
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Confirm Login");
            alertDialog.setMessage("You are not logged in !! Would You like to login ??");
            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    editor.putString("isLoggedIn","false");
                    editor.commit();
                    Intent i=new Intent(Notifications.this,Login.class);
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

    }


    class NotificationRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout content;
        private final ImageView indicator;
        private Typeface typeface_luci;
        TextView noti_date,noti_desc,noti_title;



        public NotificationRecyclerViewHolder(View itemView) {
            super(itemView);
            typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");
            indicator = (ImageView) itemView.findViewById(R.id.indicator);

            noti_date = (TextView) itemView.findViewById(R.id.noti_date);
            content = (LinearLayout) itemView.findViewById(R.id.content);
            noti_desc = (TextView) itemView.findViewById(R.id.noti_desc);
            noti_title = (TextView) itemView.findViewById(R.id.noti_title);
            noti_title.setTypeface(typeface_luci);
            noti_desc.setTypeface(typeface_luci);
            noti_date.setTypeface(typeface_luci);

        }
    }
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar1);

        }
    }
    class NotificationsRecyclerView extends RecyclerView.Adapter < RecyclerView.ViewHolder > {
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private final Context context;
        private OnLoadMoreListener mOnLoadMoreListener;
        private boolean isLoading=false;
        private int visibleThreshold = 4;
        private int lastVisibleItem,
                totalItemCount;

        public NotificationsRecyclerView(Context context) {
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
                    if (dy > 0 && notifications_list.size() < total_count) //check for scroll down
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
            return notifications_list.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM) {
                View view = LayoutInflater.from(
                        Notifications.this).inflate(R.layout.notification_content, parent, false);
                return new NotificationRecyclerViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING) {
                View view = LayoutInflater.from(Notifications.this).inflate(R.layout.spinner, parent, false);
                return new LoadingViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof NotificationRecyclerViewHolder) {
                final NotificationModel notific = notifications_list.get(position);
                final NotificationRecyclerViewHolder userViewHolder = (NotificationRecyclerViewHolder) holder;
                PrintClass.printValue("PaymentHistoryRecyclerView ","getTitle : "+notific.getTitle());

                        userViewHolder.content.setTag(position);
                userViewHolder.noti_title.setText(notific.getTitle());
                userViewHolder.noti_desc.setText(notific.getBody());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat simpleFormat = new SimpleDateFormat("MMM dd yyyy hh:mm a");
                try
                {
                    Date date = simpleDateFormat.parse(notific.getCreated_on());
                    userViewHolder.noti_date.setText(simpleFormat.format(date));
                    System.out.println("SYSTEMPRINTdate : "+simpleDateFormat.format(date));
                }
                catch (Exception ex)
                {
                    System.out.println("SYSTEMPRINT simpleDateFormat ParseException "+ex);
                }
                if(Integer.parseInt(notific.getRead_status()) == 0) {
                    userViewHolder.indicator.setColorFilter(getColor(R.color.brown)); // White Tint

                } else {
                    userViewHolder.indicator.setColorFilter(getColor(R.color.green)); // White Tint
                }


                userViewHolder.content.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int itemPosition = (Integer) view.getTag();
                        final NotificationModel notif = notifications_list.get(itemPosition);
                        System.out.println("SYSTEMPRINTdate : "+notif.getNotification_id());
                        noti_read(notif.getNotification_id(),notif);

                    }
                });
            } else if (holder instanceof LoadingViewHolder) {
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemCount() {
            return notifications_list == null ? 0 : notifications_list.size();
        }

        public void setLoaded() {
            isLoading = false;
        }
    }
}
