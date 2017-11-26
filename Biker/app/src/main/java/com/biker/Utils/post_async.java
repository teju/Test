package com.biker.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.biker.PaymentHistory;
import com.biker.R;
import com.biker.ReachedDestination;
import com.biker.ServerError;
import com.biker.UserRegister;
import com.biker.model.BookingList;


import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Teju on 19/09/2017.
 */
public class post_async extends AsyncTask<String, Integer, String> {
    static String action = "", resultString = "";
    private BookingList bookingList;
    private PaymentHistory paymentHistory;
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
    public post_async(PaymentHistory paymentHistory, String action) {
        this.action = action;
        this.context=paymentHistory;
        this.paymentHistory = paymentHistory;
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
    public post_async(BookingCompleted bookingCompleted, String action, BookingList bookingList) {
        this.action = action;
        this.context= bookingCompleted;
        this.bookingList= bookingList;
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
                            || action.equals("BookingCompletedRefresh"))) {
                this.bookingCompleted.ResponseOfPaymentList(resultString);
            } else  if (this.bookingCompleted != null && action.equalsIgnoreCase("BookingCompleteReload")) {
                this.bookingCompleted.ResponseOfPaymentListReload(resultString);
            } else  if (this.bookingCompleted != null && action.equalsIgnoreCase("Payment")) {
                ResponseOfRating(resultString,"Your payment is successful ",
                        "Payment could not be completed.Please try again  ! ");
            }else  if (this.paymentHistory != null &&
                    (action.equalsIgnoreCase("PaymentHistoryCompleted")
                            || action.equals("PaymentHistoryRefresh"))) {
                this.paymentHistory.ResponseOfPaymentList(resultString);
            } else  if (this.paymentHistory != null && action.equalsIgnoreCase("PaymentHistoryReload")) {
                this.paymentHistory.ResponseOfPaymentListReload(resultString);
            }else  if (this.paymentHistory != null && action.equalsIgnoreCase("RateFeedBAck")) {
                ResponseOfRating(resultString,"Thank you so much for your valuable fedback",
                        "Could not submit your ratings. Please try again  !!");
            }else  if (this.reachedDestination != null && action.equalsIgnoreCase("ReachedDestination")) {
                this.reachedDestination.ResponseOfReachedDestination(resultString);
            }

        } catch (Exception e) {
            dialog.cancel();
        }
    }

    public void ResponseOfRating(String response,String responsemessage,String error){
        JSONObject jsonObject = null;
        final Dialog mBottomSheetDialog = new Dialog(context);

        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBottomSheetDialog.setContentView(R.layout.success_message);
        mBottomSheetDialog.setCancelable(true);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.show();
        TextView message =(TextView)mBottomSheetDialog.findViewById(R.id.message) ;
        try {

            jsonObject = new JSONObject(response);
            if(jsonObject.getString("status").equalsIgnoreCase("success")) {

                message.setText(responsemessage);

            } else {
                message.setText(jsonObject.getString("message"));
            }
            Button ok =(Button) mBottomSheetDialog.findViewById(R.id.ok) ;
            Typeface typeface_luci = Typeface.createFromAsset(context.getAssets(), "fonts/luci.ttf");

            message.setTypeface(typeface_luci);
            ok.setTypeface(typeface_luci);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(action.equals("Payment")) {
                        final BookingList bookin = bookingList;

                        final Dialog mBottomSheetDialog = new Dialog(context);
                        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        mBottomSheetDialog.setContentView(R.layout.rating_feedback);
                        mBottomSheetDialog.setCancelable(true);
                        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        mBottomSheetDialog.show();
                        Typeface typeface_luci = Typeface.createFromAsset(context.getAssets(), "fonts/luci.ttf");

                        Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
                        Button not_now = (Button) mBottomSheetDialog.findViewById(R.id.not_now);
                        final RatingBar ratings = (RatingBar) mBottomSheetDialog.findViewById(R.id.ratings);
                        final EditText feedback = (EditText) mBottomSheetDialog.findViewById(R.id.feedback);
                        final TextView feedback_title = (TextView) mBottomSheetDialog.findViewById(R.id.feedback_title);
                        ratings.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                                String rateValue = String.valueOf(ratingBar.getRating());
                                System.out.println("Rate for Module is"+rateValue);
                            }
                        });
                        not_now.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mBottomSheetDialog.dismiss();
                                bookingCompleted.getPaymentList("BookingCompleted");

                            }
                        });

                        submit.setTypeface(typeface_luci);
                        feedback_title.setTypeface(typeface_luci);
                        feedback.setTypeface(typeface_luci);
                        submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(ratings.getRating() == 0) {
                                    bookingCompleted.getPaymentList("BookingCompleted");
                                    Toast.makeText(context,"Please Provide your rating",Toast.LENGTH_LONG).show();

                                } else if(feedback.getText().toString().trim().length() == 0 ) {
                                    Toast.makeText(context,"Please Provide your feedback",Toast.LENGTH_LONG).show();


                                } else {
                                    bookingCompleted.giveFeedback(bookin.getBooking_id(), bookin.getVendor_id(),
                                            String.valueOf((int)ratings.getRating()), feedback.getText().toString());
                                }
                                mBottomSheetDialog.dismiss();
                            }
                        });
                    } else {
                        paymentHistory.getPaymentList("PaymentHistoryCompleted");
                    }
                    mBottomSheetDialog.dismiss();
                }
            });
            PrintClass.printValue("ResponseOfPaymentList resultString ", " has data " + jsonObject.toString());
        } catch (Exception e) {
            message.setText(error);
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
                !action.equals("BookingDetailsRefresh") && !action.equals("BookingCompleteReload") &&
                !action.equals("BookingCompletedRefresh") && !action.equals("PaymentHistoryReload") && !action.equals("PaymentHistoryRefresh")) {
                dialog.show();
        }
    }

}
