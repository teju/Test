package com.biker.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.Window;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.biker.BookingDetails;
import com.biker.Login;
import com.biker.MainActivity;
import com.biker.BookingCompleted;
import com.biker.R;
import com.biker.ReachedDestination;
import com.biker.ServerError;
import com.biker.UserRegister;


import java.io.UnsupportedEncodingException;

/**
 * Created by Teju on 19/09/2017.
 */
public class post_async extends AsyncTask<String, Integer, String> {
    static String action = "", resultString = "";
    private ReachedDestination reachedDestination;
    private BookingCompleted bookingCompleted;
    private BookingDetails bookingDetails;
    private MainActivity mainActivity;
    private UserRegister userRegister;
    private Login login;
    private Dialog dialog;
    Context context;

    public post_async(UserRegister userRegister, String action) {
        this.action = action;
        this.context=userRegister;
        this.userRegister = userRegister;
    }

    public post_async(BookingDetails bookingDetails, String action) {
        this.action = action;
        this.context=bookingDetails;
        this.bookingDetails = bookingDetails;
    }
    public post_async(ReachedDestination reachedDestination, String action) {
        this.action = action;
        this.context=reachedDestination;
        this.reachedDestination = reachedDestination;
    }
    public post_async(BookingCompleted bookingCompleted, String action) {
        this.action = action;
        this.context= bookingCompleted;
        this.bookingCompleted = bookingCompleted;
    }
    public post_async(Login login, String action) {
        this.action = action;
        this.context=login;
        this.login = login;
    }
    public post_async(MainActivity mainActivity, String action) {
        this.action = action;
        this.context=mainActivity;
        this.mainActivity = mainActivity;
    }
    @Override
    protected String doInBackground(String... params) {
        PrintClass.printValue("SYSTEMPRINT POST SYNC  ", "LENGTH " + params.length);
        invoke(params[0], params[1]);
        return null;
    }

    public void invoke(String url, final String postString) {

        PrintClass.printValue("SYSTEMPRINT POST SYNC invoke ", url + " action " + action);
        String s = "";
        RequestQueue queue = Volley.newRequestQueue(context);

        final String mRequestBody = postString;
        PrintClass.printValue("SYSTEMPRINT PARAMS", mRequestBody);

        StringRequest strReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        sendResult(response);
                        try {
                            System.gc();
                            Runtime.getRuntime().gc();
                        } catch (Exception e) {
                            dialog.cancel();

                            e.printStackTrace();
                            PrintClass.printValue("SYSTEMPRINT postsync " +
                                    "  Exception  ", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.cancel();
                        if(!action.equals("notificationService")) {
                            Intent i = new Intent(context, ServerError.class);
                            context.startActivity(i);
                        }
                        System.out.println("SYSTEMPRINT error " + " action " + action +
                                " error " + error.toString());
                    }
                }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    dialog.cancel();

                    System.out.println("SYSTEMPRINT error  Unsupported Encoding while trying to get " +
                            "the bytes of %s using %s"+ mRequestBody+"utf-8");

                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s " +
                            "using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

        };


        strReq.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(strReq);
    }

    private void sendResult(String resultString) {
        PrintClass.printValue("SYSTEMPRINT postsync  if  ", "action " + action +
                " resultString " + resultString);
        try {
            dialog.cancel();
            if (this.userRegister != null && action.equalsIgnoreCase("UserRegister")) {
                this.userRegister.ResponseOfRegister(resultString);
            } else  if (this.userRegister != null && action.equalsIgnoreCase("UserGetProfileInfo")) {
                this.userRegister.ResponseOfUserInfo(resultString);
            } else  if (this.userRegister != null && action.equalsIgnoreCase("UserUpdate")) {
                this.userRegister.ResponseOfRegister(resultString);
            } else  if (this.login != null && action.equalsIgnoreCase("Login")) {
                this.login.ResponseOfLogin(resultString);
            } else  if (this.login != null && action.equalsIgnoreCase("LoginOtp")) {
                this.login.ResponseOfLoginOtp(resultString);
            } else  if (this.mainActivity != null && action.equalsIgnoreCase("BookingRequest")) {
                this.mainActivity.ResponseOfBooking(resultString);
            } else  if (this.bookingDetails != null &&
                    (action.equalsIgnoreCase("BookingDetails")
                            || action.equals("BookingDetailsRefresh"))) {
                this.bookingDetails.ResponseOfBookingList(resultString);
            } else  if (this.bookingDetails != null && action.equalsIgnoreCase("BookingDetailsReload")) {
                this.bookingDetails.ResponseOfBookingListReload(resultString);
            } else  if (this.bookingCompleted != null &&
                    (action.equalsIgnoreCase("BookingCompleted")
                            || action.equals("PaymentHistoryRefresh"))) {
                this.bookingCompleted.ResponseOfPaymentList(resultString);
            } else  if (this.bookingCompleted != null && action.equalsIgnoreCase("PaymentHistoryReload")) {
                this.bookingCompleted.ResponseOfPaymentListReload(resultString);
            }else  if (this.reachedDestination != null && action.equalsIgnoreCase("ReachedDestination")) {
                this.reachedDestination.ResponseOfReachedDestination(resultString);
            }

        } catch (Exception e) {
            dialog.cancel();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.spinner);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        if(!action.equals("BookingDetailsReload") &&
                !action.equals("BookingDetailsRefresh") && !action.equals("PaymentHistoryReload") &&
                !action.equals("PaymentHistoryRefresh") ) {
                dialog.show();
        }
    }

}
