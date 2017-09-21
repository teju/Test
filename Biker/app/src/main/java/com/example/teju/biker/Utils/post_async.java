package com.example.teju.biker.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.view.Window;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.teju.biker.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Teju on 19/09/2017.
 */
public class post_async extends AsyncTask<String, Integer, String> {
    static String action = "", resultString = "";
    private Dialog dialog;
    Context context;

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
        StringRequest strReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        sendResult(response);
                        try {
                            System.gc();
                            Runtime.getRuntime().gc();
                        } catch (Exception e) {
                            e.printStackTrace();
                            PrintClass.printValue("SYSTEMPRINT postsync " +
                                    "  Exception  ", e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("SYSTEMPRINT error " + " action " + action + " error " + error.toString());

                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("json", postString);
                params.put("restAccessToken", "GCP_IA_Rest_01");
                PrintClass.printValue("SYSTEMPRINT PARAMS", params.toString());
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(strReq);
    }

    private void sendResult(String resultString) {
        dialog.cancel();
        PrintClass.printValue("SYSTEMPRINT postsync  if  ", "action " + action +
                " resultString " + resultString);
        try {


        } catch (Exception e) {

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
        dialog.show();

    }

}
