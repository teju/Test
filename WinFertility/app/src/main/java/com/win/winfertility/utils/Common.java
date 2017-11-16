package com.win.winfertility.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;
import com.win.winfertility.LandingActivity;
import com.win.winfertility.R;
import com.win.winfertility.dataobjects.ApiReqResult;
import com.win.winfertility.dataobjects.FCMInfoArgs;
import com.win.winfertility.dto.ApiResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Common {
    public static final String PROFILE_IMAGE = "profile_image.png";
    public static final String EMPLOYER_LOGO = "employer_logo.png";
    public static final String SERVICE_URL = "http://trackerapp.winfertility.com/api/";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static OutputStream os;

    public static void saveFCMTokenID(Context context, String token) {

        FCMInfoArgs args = new FCMInfoArgs();
        args.EmailID = Shared.getString(context, Shared.KEY_EMAIL_ID);
        args.NotificationID = token;
        if(TextUtils.isEmpty(args.EmailID) == false && TextUtils.isEmpty(token) == false) {

            Common.invokeAPI(context, ServiceMethods.SaveNotificationID, args, new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if (msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                        ApiResult result = (ApiResult) msg.obj;
                        if (TextUtils.isEmpty(result.Json) == false) {
                            ApiReqResult data = new Gson().fromJson(result.Json, ApiReqResult.class);
                            if (data != null && data.Result == 1) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }));
        }
    }

    public static void logOut(final Activity activity) {
        Notify.show(activity, "Do you want to log out?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FCMInfoArgs args = new FCMInfoArgs();
                            args.EmailID = Shared.getString(activity, Shared.KEY_EMAIL_ID);
                            args.NotificationID = "";
                            if(TextUtils.isEmpty(args.EmailID) == false ) {

                                Common.invokeAPI(activity, ServiceMethods.SaveNotificationID, args, new Handler(new Handler.Callback() {
                                    @Override
                                    public boolean handleMessage(Message msg) {
                                        if (msg != null && msg.obj != null && msg.obj instanceof ApiResult) {
                                            Common.removeProfileImage(activity);
                                            Shared.clear(activity);
                                            activity.startActivity(new Intent(activity, LandingActivity.class));
                                            ApiResult result = (ApiResult) msg.obj;
                                            if (TextUtils.isEmpty(result.Json) == false) {
                                                ApiReqResult data = new Gson().fromJson(result.Json, ApiReqResult.class);
                                                if (data != null && data.Result == 1) {
                                                    return true;
                                                }
                                            }
                                        }
                                        return false;
                                    }
                                }));
                            }                        //    Common.removeEmployerLogo(activity);

                        }
                    });
                }
            }
        }, "Yes", "No");
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>=android.os.Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    alertBuilder.create().show();
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    public static void setImageToApp(Context context, Bitmap bmp, String filename) {
        try {
            if (bmp != null) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, buffer);
                FileOutputStream stream;
                try {
                    stream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                    stream.write(buffer.toByteArray());
                    stream.close();
                } catch (Exception e) {
                }
            }
        } catch (Exception ex) {
        }
    }
    public static Bitmap getImageFromApp(Context context, String filename) {
        Bitmap bitmap = null;
        try{
            FileInputStream stream = context.openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(stream);
            stream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static void removeImageFromApp(Context context, String filename) {
        try {
            context.deleteFile(filename);
        } catch (Exception ex) {

        }
    }

    public static void setEmployerLogo(Context context, String data) {
        System.out.println("setEmployerLogo1234 "+data);
        try {
            byte[] bytes = new byte[]{};
            if (TextUtils.isEmpty(data) == false) {
                bytes = Base64.decode(data, Base64.DEFAULT);
            }
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                Common.setImageToApp(context, bmp, EMPLOYER_LOGO);
            }
        } catch (Exception ex) {
        }
    }

    public static Bitmap getEmployerLogo(Context context) {
        return Common.getImageFromApp(context, EMPLOYER_LOGO);
    }

    public static void removeEmployerLogo(Context context) {
        Common.removeImageFromApp(context, EMPLOYER_LOGO);
    }

    public static void setProfileImage(Context context, String data) {
        try {
            byte[] bytes = new byte[]{};
            if (TextUtils.isEmpty(data) == false) {
                bytes = Base64.decode(data, Base64.DEFAULT);
            }
            System.out.println("setEmployerLogo1234 "+ data);

            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) {
                Common.setImageToApp(context, bmp, PROFILE_IMAGE);
            }
        } catch (Exception ex) {
        }
    }

    public static void setProfileImage(Context context, Bitmap bmp) {
        Common.setImageToApp(context, bmp, PROFILE_IMAGE);
    }

    public static void removeProfileImage(Context context) {
        Common.removeImageFromApp(context, PROFILE_IMAGE);
    }

    public static Bitmap getProfileImage(Context context) {
        Bitmap bmp = null;
        try {
            bmp = Common.getImageFromApp(context, PROFILE_IMAGE);
            if (bmp == null) {
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.win_profile);
            }
        } catch (Exception ex) {
        }
        return bmp;
    }

    public static SimpleDateFormat WinAppDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    public static Date getDateWithoutTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    public static <T> List<T> getChildrenByType(Class<T> type, ViewGroup root){
        List<T> views=new ArrayList<>();
        for(int i=0;i<root.getChildCount();i++){
            View v=root.getChildAt(i);
            if(type.isInstance(v)){
                views.add((T)v);
            }else if(v instanceof ViewGroup){
                views.addAll(getChildrenByType(type, (ViewGroup)v));
            }
        }
        return views;
    }


    public static int convertDpToPx(int dp, Context context){
        return Math.round(dp*(context.getResources().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));
    }
    public static int convertPxToDp(int px){
        return Math.round(px/(Resources.getSystem().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));
    }
    public static String join(List<String> items, String splitter) {
        String result = "";
        try {
            if(TextUtils.isEmpty(splitter)) {
                splitter = ", ";
            }
            for(String item : items) {
                if(TextUtils.isEmpty(result) == false) {
                    result += splitter;
                }
                result += item;
            }
        }
        catch(Exception ex) {
        }
        return result;
    }
    public static String join(String[] items, String splitter) {
        String result = "";
        try {
            if(TextUtils.isEmpty(splitter)) {
                splitter = ", ";
            }
            for(String item : items) {
                if(TextUtils.isEmpty(result) == false) {
                    result += splitter;
                }
                result += item;
            }
        }
        catch(Exception ex) {
        }
        return result;
    }
    public static int daysInMonth(int month) {
        return Common.daysInMonth(0, month);
    }
    public static int daysInMonth(int year, int month) {
        int days = 0;
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DATE, 1);
            if(year > 0) {
                calendar.set(Calendar.YEAR, year);
            }
            calendar.set(Calendar.MONTH, month - 1);
            days = calendar.getActualMaximum(Calendar.DATE);
        }
        catch(Exception ex) {
        }
        return days;
    }
    public static String filterOnlyDigits(String value) {
        try {
            if (TextUtils.isEmpty(value) == false) {
                String allowed = "0123456789";
                String numbers = "";
                for (char c : value.toCharArray()) {
                    String cStr = Character.toString(c);
                    if (allowed.contains(cStr)) {
                        numbers += cStr;
                    }
                }
                long textLong = 0;
                try {
                    textLong = Long.parseLong(numbers);
                } catch (Exception ex) {
                }
                value = Long.toString(textLong);
            }
        }
        catch(Exception ex) {
        }
        return value;
    }
    public static String replaceNull(String value) {
        return Common.replaceNull(value, "");
    }
    public static String replaceNull(String value, String defaultValue) {
        if(value == null) {
            return defaultValue;
        }
        return value.trim();
    }

    public static void handleAppExitMsg(final Activity activity) {
        Notify.show(activity, "Do you want to exit?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    activity.moveTaskToBack(true);
                    activity.finish();
                }
            }
        }, "Yes", "No");
       // activity.moveTaskToBack(true);

    }
    public static void setBackground(View view, Drawable drawable) {
        if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(drawable);
        } else {
            view.setBackground(drawable);
        }
    }
    public static void setBackground(View view, int drawable) {
        if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(getDrawable(view.getContext(), drawable));
        } else {
            view.setBackground(getDrawable(view.getContext(), drawable));
        }
    }

    public static Drawable getDrawable(Context context, int drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return context.getResources().getDrawable(drawable, context.getTheme());
        } else {
            return context.getResources().getDrawable(drawable);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean available = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        catch(Exception ex) {
        }
        return available;
    }
    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
    public final static boolean isValidPhone(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return Patterns.PHONE.matcher(target).matches();
        }
    }
    public final static boolean isValidNumber(CharSequence target) {
        String allowed = "0123456789";
        if(target == null || TextUtils.isEmpty(target)) {
            return false;
        }
        else {
            for(int i = 0; i < target.length(); i++) {
                if(allowed.contains(Character.toString(target.charAt(i))) == false) {
                    return false;
                }
            }
            if(target.toString().startsWith("0")) {
                return false;
            }
        }
        return true;
    }
    public final static String suppressJsonArray(String json) {
        if(TextUtils.isEmpty(json) == false) {
            json = json.trim();
            try {
                if (json.startsWith("[")) {
                    json = json.substring(1);
                }
                if (json.endsWith("]")) {
                    json = json.substring(0, json.length() - 1);
                }
                json = json.trim();
            }
            catch(Exception ex) {
            }
        }

        return json;
    }
    public static void invokeAPI(Context context, String serviceUri, Object data) {
        Common.invokeAPI(context, serviceUri, data, null);
    }
    public static void invokeAPI(final Context context, final String serviceUri, Object data, final Handler callback) {
        if(Common.isNetworkAvailable(context)) {

            if (data == null) {
                data = new Object();
            }
            final Object finalData = data;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ApiResult response = new ApiResult();
                    try {
                        response = invokeAPIEx(context, serviceUri, finalData);
                    } catch (Exception ex) {
                    }
                    if (callback != null) {
                        callback.sendMessage(callback.obtainMessage(0, response));
                    }
                }
            }).start();
        }
        else {
            if (callback != null) {
                ApiResult response = new ApiResult();
                response.Error = "Please check your internet connection and try again.";
                callback.sendMessage(callback.obtainMessage(0, response));
            }
        }
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String tokeninvokeAPIEx(final Context context) {
        ApiResult response = new ApiResult();
        System.out.println("SYSTEMPRINT1 URLIS tokeninvokeAPIEx "+"https://trackerapp.winfertility.com/NotificationApi/Notification/GetTokenId");
        if(Common.isNetworkAvailable(context)) {

            try {
                JSONObject jsonObject=new JSONObject();
                jsonObject.put("UserName",Shared.getString(context,Shared.KEY_EMAIL_ID));
                jsonObject.put("Password",Shared.getString(context,Shared.KEY_PASSWORD));
                String input = "_Params=" + URLEncoder.encode(jsonObject.toString(), "UTF-8");
                byte[] bytes = input.getBytes();
                System.out.println("SYSTEMPRINT1 PARAMETERS tokeninvokeAPIEx "+jsonObject.toString());

                HttpURLConnection connection = null;
                InputStream stream = null;
                BufferedReader reader = null;

                try {
                    connection = (HttpURLConnection) new URL("https://trackerapp.winfertility.com/NotificationApi/Notification/GetTokenId").openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setConnectTimeout(25 * 1000);
                    connection.setReadTimeout(25 * 1000);
                    OutputStreamWriter wr= new OutputStreamWriter(connection.getOutputStream());
                    wr.write(jsonObject.toString());
                    os = connection.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    connection.connect();
                    /*----- Write data bytes -----*/
                    connection.getOutputStream().write(bytes);
                    /*----- Read result data -----*/
                    stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder result = new StringBuilder();
                    /*----- Read all json data from stream -----*/
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    JSONObject json=new JSONObject(result.toString());
                    JSONObject token=json.optJSONObject("Token");

                    /*----- Handling response data -----*/
                    System.out.println("SYSTEMPRINT1 RESPONSE tokeninvokeAPIEx "+token.opt("TokenId").toString());
                    response.Json = token.opt("TokenId").toString();

                    if (response.Json != null && response.Json.trim().length() > 0) {
                        response.IsSuccess = true;
                    }
                } catch (Exception ex) {
                    System.out.println("SYSTEMPRINT1 Exception 1 tokeninvokeAPIEx "+ex.toString());

                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception ex) {
                        System.out.println("SYSTEMPRINT1 Exception 5 tokeninvokeAPIEx "+ex.toString());

                    }
                    try {
                        if (stream != null) {
                            stream.close();
                            os.close();
                        }
                    } catch (Exception ex) {
                        System.out.println("SYSTEMPRINT1 Exception 2 tokeninvokeAPIEx"+ex.toString());
                    }
                    try {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    } catch (Exception ex) {
                        System.out.println("SYSTEMPRINT1 Exception 3 "+ex.toString());
                    }
                }
            } catch (Exception ex) {
                System.out.println("SYSTEMPRINT1 Exception 4 "+ex.toString());
            }
        }
        else {
            response.Error = "Please check your internet connection and try again.";
        }
        return  response.Json;
    }

    public static ApiResult invokeAPIEx(final Context context, final String serviceUri, Object data) {
        ApiResult response = new ApiResult();
        String token="";
        System.out.println("SYSTEMPRINT1 URLIS "+SERVICE_URL +serviceUri.toString());
        if(Common.isNetworkAvailable(context)) {
            if(!serviceUri.equals(ServiceMethods.Login) &&
                    !serviceUri.equals(ServiceMethods.CreateAccount) &&
                    !serviceUri.equals(ServiceMethods.ForgotPassword)) {
                 token = tokeninvokeAPIEx(context);
            }
            final String args = new Gson().toJson(data);
            System.out.println("SYSTEMPRINT1 PARAMETERS of "+serviceUri+" : "+args.toString());
            try {
                String input = "_Params=" + URLEncoder.encode(args, "UTF-8");
                byte[] bytes = input.getBytes();

                HttpURLConnection connection = null;
                InputStream stream = null;
                BufferedReader reader = null;

                try {
                    connection = (HttpURLConnection) new URL(SERVICE_URL + serviceUri).openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setRequestProperty("Content-Length", Integer.toString(bytes.length));
                    System.out.println("SYSTEMPRINT1 TOKENVALUES "+token.length()+" token : "+token);

                    if(token.length() !=0) {
                        connection.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    connection.setConnectTimeout(25 * 1000);
                    connection.setReadTimeout(25 * 1000);
                    connection.connect();
                    /*----- Write data bytes -----*/
                    connection.getOutputStream().write(bytes);
                    /*----- Read result data -----*/
                    stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuilder result = new StringBuilder();
                    /*----- Read all json data from stream -----*/
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    /*----- Handling response data -----*/
                    response.Json = result.toString();
                    System.out.println("SYSTEMPRINT1 RESPONSE of"+ serviceUri+" : "+result.toString());
                    if (response.Json != null && response.Json.trim().length() > 0) {
                        response.IsSuccess = true;
                    }
                } catch (Exception ex) {
                    System.out.println("SYSTEMPRINT1 Exception 1 "+ex.toString());
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (Exception ex) {
                        System.out.println("SYSTEMPRINT1 Exception 5 "+ex.toString());
                    }
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                    } catch (Exception ex) {
                        System.out.println("SYSTEMPRINT1 Exception 2 "+ex.toString());
                    }
                    try {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    } catch (Exception ex) {
                        System.out.println("SYSTEMPRINT1 Exception 3 "+ex.toString());
                    }
                }
            } catch (Exception ex) {
                System.out.println("SYSTEMPRINT1 Exception 4 "+ex.toString());
            }
        }
        else {
            response.Error = "Please check your internet connection and try again.";
        }
        return  response;
    }
}
