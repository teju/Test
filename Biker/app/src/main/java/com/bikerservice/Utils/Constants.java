package com.bikerservice.Utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import org.json.JSONObject;

/**
 * Created by nz160 on 20-09-2017.
 */

public class Constants {
    public  static String regexStr = "[0-9]+";
    public static String regEx = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}\\b";
    public static String SERVER_URL="http://app.bikerservice.in/biker/api/web/";
    private static SharedPreferences prefrence;
    private static SharedPreferences.Editor editor;
    static  TextView _noti_count;

    public static void noti_count(Context activity, TextView noti_count) {
        prefrence = activity.getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        Constants._noti_count = noti_count;
        if (IsNetworkConnection.checkNetworkConnection(activity)) {

            String url = Constants.SERVER_URL + "profile/notiffication-unread-count";
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("user_type", "customer");
                jsonBody.put("user_id", prefrence.getString("user_id", ""));
                jsonBody.put("access_token", prefrence.getString("access_token", ""));
                PrintClass.printValue("SYSTEMPRINT noti_count  ", "LENGTH " + jsonBody.toString());
                new post_async(activity, "NOTIFICATIONCOUNT").execute(url, jsonBody.toString());
            } catch (Exception e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT noti_count Exception", e.toString());
            }
        } else {

        }
    }

    public static void ResponseOfNotiCount(String resultString) {

        try {
            JSONObject jsonObject = new JSONObject(resultString);
            PrintClass.printValue("SYSTEMPRINT noti_count "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                if(jsonObject.has("notifficationData")) {
                    JSONObject notifficationData = jsonObject.getJSONObject("notifficationData");
                    Constants.editor.putString("notiCount", notifficationData.getString("totalUnread"));
                    Constants.editor.commit();
                    if (Integer.parseInt(notifficationData.getString("totalUnread")) != 0) {
                        Constants._noti_count.setText(notifficationData.getString("totalUnread"));
                        Constants._noti_count.setText(View.VISIBLE);

                    } else {
                        Constants._noti_count.setVisibility(View.GONE);
                    }
                } else  {
                    Constants._noti_count.setVisibility(View.GONE);

                }
            } else {

            }
        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

}
