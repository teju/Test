package com.example.teju.biker;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.teju.biker.Utils.Constants;
import com.example.teju.biker.Utils.CustomToast;
import com.example.teju.biker.Utils.IsNetworkConnection;
import com.example.teju.biker.Utils.PrintClass;
import com.example.teju.biker.Utils.post_async;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements View.OnClickListener{

    private View rootView;
    private EditText phone,otp;
    private TextView sign_up;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private BroadcastReceiver receiver;
    private SharedPreferences notiprefrence;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        MultiDex.install(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ConnectivityManager connMgr = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                System.out.println("NetworkChangeReceiverieieie "+" called");
                final android.net.NetworkInfo wifi = connMgr
                        .getActiveNetworkInfo();
                if (wifi != null) {
                    if (wifi.getType() == ConnectivityManager.TYPE_WIFI) {
                        // connected to wifi
                    } else if (wifi.getType() == ConnectivityManager.TYPE_MOBILE) {
                        // connected to the mobile provider's data plan
                    }
                } else {
                    System.out.println("NetworkChangeReceiverieieie "+" isNotAvailable");
                    Intent i=new Intent(context,ServerError.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
        };
        registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

        rootView=findViewById(android.R.id.content);
        phone=(EditText)findViewById(R.id.phone);
        sign_up=(TextView)findViewById(R.id.sign_up);
        sign_up.setOnClickListener(this);

        prefrence = getSharedPreferences("My_Pref", 0);
        notiprefrence = getSharedPreferences(getString(R.string.fcm_pref), 0);
        editor = prefrence.edit();

        if(Build.VERSION.SDK_INT < 23){
            //your code here
        } else {
            requestContactPermission();
        }
    }

    public void notificationService(){
        if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
            String url = Constants.SERVER_URL + "user/push-notification";
            JSONObject params = new JSONObject();
            JSONObject jsonobject = new JSONObject();
            try {
                params.put("google_fcm_id",notiprefrence.getString(getString(R.string.fcm_token), "") );
                params.put("mobile_no",phone.getText().toString());
                jsonobject.put("PushNotification",params);
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonobject.toString());
            new post_async(Login.this,"notificationService").execute(url, jsonobject.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void requestContactPermission() {

        int hasContactPermission =ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECEIVE_SMS);

        if(hasContactPermission != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]
                    {android.Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
        }else {
            //Toast.makeText(AddContactsActivity.this, "Contact Permission is already granted", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                // Check if the only required permission has been granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,"Contact Permission is Granted",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Contact permission was NOT granted.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        phone.setText("");
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

    public void login(View v){
        String getPhone=phone.getText().toString();

        if(getPhone.length()==0){
            phone.setError("Phone Number cannot be empty");
        } else if(!getPhone.matches(Constants.regexStr) || getPhone.length()!=10) {
            phone.setError("Your Phone Number is Invalid.");
        } else {
            phone.setError(null);
            if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
                String url = Constants.SERVER_URL + "user/login";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
            try {
                params.put("username",phone.getText().toString() );
                params.put("user_type", "customer");
                jsonBody.put("LoginForm", params);
            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            new post_async(Login.this, "Login").execute(url, jsonBody.toString());

            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void ResponseOfLogin(String response){
        try {
            final JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                final Dialog mBottomSheetDialog = new Dialog(this);
                mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mBottomSheetDialog.setContentView(R.layout.otp);
                mBottomSheetDialog.setCancelable(true);
                mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                mBottomSheetDialog.show();
                mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);
                Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
                TextView textView=(TextView)mBottomSheetDialog.findViewById(R.id.txtview);
                textView.setText(jsonObject.getString("message"));
                otp = (EditText) mBottomSheetDialog.findViewById(R.id.otp);
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String getotp = otp.getText().toString();
                        if (getotp.length() == 0) {
                            otp.setError("Enter Your Otp" );
                        } else {
                            otp.setError(null);
                            if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
                                String url = Constants.SERVER_URL + "user/otp";
                                JSONObject jsonBody = new JSONObject();
                                JSONObject userparams = new JSONObject();
                                JSONObject otpparams = new JSONObject();
                                try {
                                    userparams.put("username",phone.getText().toString() );
                                    userparams.put("user_type", "customer");
                                    otpparams.put("otp_code", otp.getText().toString());
                                    jsonBody.put("LoginForm", userparams);
                                    jsonBody.put("OtpForm", otpparams);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    PrintClass.printValue("SYSTEMPRINT PARAMS JSONException ", e.toString());
                                }
                                PrintClass.printValue("SYSTEMPRINT PARAMS", jsonBody.toString());

                                new post_async(Login.this, "LoginOtp").execute(url, jsonBody.toString());

                                mBottomSheetDialog.cancel();
                            } else {
                                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                        "No Internet Connection");
                            }
                        }
                    }
                });

            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public void ResponseOfLoginOtp(String response){
        try {
            final JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse ResponseOfLoginOtp "," has data "+jsonObject.toString());
            if(!jsonObject.getString("status").equalsIgnoreCase("failed")){
                editor.putString("isLoggedIn","true");
                editor.putString("user_id",jsonObject.getString("user_id"));
                editor.putString("name",jsonObject.getString("name"));
                editor.putString("access_token",jsonObject.getString("access_token"));
                editor.commit();
                notificationService();
                Intent i = new Intent(Login.this, MainActivity.class);
                startActivity(i);
                finish();
            } else {

                JSONObject errorjsonObject = jsonObject.getJSONObject("errors");

                if(errorjsonObject.has("otp_code")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("otp_code");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0));
                    PrintClass.printValue("UserRegisterREsponse jsonObject errorjsonObject " +
                            ""," has data "+jsonArray.getString(0));
                    return;
                }
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    @Override
    public void onClick(View v) {
        Intent i = new Intent(this, UserRegister.class);
        i.putExtra("type", "signup");
        startActivity(i);
    }


}
