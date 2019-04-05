package com.bikerservice.biker;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bikerservice.biker.Utils.AppSignatureHelper;
import com.bikerservice.biker.Utils.Constants;
import com.bikerservice.biker.Utils.CustomToast;
import com.bikerservice.biker.Utils.IsNetworkConnection;
import com.bikerservice.biker.Utils.PrintClass;
import com.bikerservice.biker.Utils.SmsListener;
import com.bikerservice.biker.Utils.SmsReceiver;
import com.bikerservice.biker.Utils.post_async;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends AppCompatActivity implements View.OnClickListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RESOLVE_HINT = 1000;
    private View rootView;
    private EditText phone,otp;
    private TextView sign_up;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private BroadcastReceiver receiver;
    private SharedPreferences notiprefrence;
    String getPhoneNo="";
    private Typeface typeface_luci;
    private String reached_dest="false";
    private ImageView noti;
    private Parcelable credential;
    private TextView resend_otp;
    private Dialog mBottomSheetDialog;
    private Button cancel,login_button;
    private String getPhone;
    private boolean dialog_shown = false;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        MultiDex.install(this);
        reached_dest=getIntent().getStringExtra("reached_dest");

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
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            "No Internet Connection");
                }
            }
        };

       registerReceiver(receiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");

        rootView=findViewById(android.R.id.content);
        phone=(EditText)findViewById(R.id.phone);
        phone.setTypeface(typeface_luci);
        sign_up=(TextView)findViewById(R.id.sign_up);
        login_button = (Button) findViewById(R.id.login_button);
        sign_up.setOnClickListener(this);
        login_button.setTypeface(typeface_luci);

        mBottomSheetDialog = new Dialog(this);
        mBottomSheetDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBottomSheetDialog.setContentView(R.layout.otp);
        mBottomSheetDialog.setCanceledOnTouchOutside(false);
        mBottomSheetDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow().setGravity(Gravity.BOTTOM);

        prefrence = getSharedPreferences("My_Pref", 0);
        notiprefrence = getSharedPreferences(getString(R.string.fcm_pref), 0);
        editor = prefrence.edit();
        if(Build.VERSION.SDK_INT < 23){
            //your code here
        } else {
            try {
                requestHint();
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
            requestContactPermission();
        }
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void notificationService(){
        AppSignatureHelper appSignatureHelper = new AppSignatureHelper(this);
        if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
            String url = Constants.SERVER_URL + "user/push-notification";
            JSONObject params = new JSONObject();
            JSONObject jsonobject = new JSONObject();
            String hash_code = appSignatureHelper.getAppSignatures().get(0);
            hash_code = hash_code.replaceAll("\\[", "").replaceAll("\\]","");
            try {
                params.put("google_fcm_id",notiprefrence.getString(getString(R.string.fcm_token), "") );
                params.put("hash_code",hash_code);
                params.put("mobile_no",getPhoneNo);
                params.put("access_token", prefrence.getString("access_token", ""));
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
    private void requestHint() throws IntentSender.SendIntentException {
        requestContactPermission();

        GoogleApiClient apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

    /*    PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(
                apiClient, hintRequest);
        startIntentSenderForResult(intent.getIntentSender(),
                RESOLVE_HINT, null, 0, 0, 0);*/
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESOLVE_HINT) {
            if (resultCode == RESULT_OK) {
                credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                // credential.getId();  <-- will need to process phone number string
            }
        }
    }
    private void requestContactPermission() {

        SmsRetrieverClient client = SmsRetriever.getClient(this);

// Starts SmsRetriever, waits for ONE matching SMS message until timeout
// (5 minutes).
        Task<Void> task = client.startSmsRetriever();

// Listen for success/failure of the start Task.
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Login.this, "SMS Permission Failed", Toast.LENGTH_LONG).show();

            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        getApplicationContext().registerReceiver(new SmsReceiver(), intentFilter);
//        int hasContactPermission =ActivityCompat.checkSelfPermission(this,android.Manifest.permission.RECEIVE_SMS);
//
//        if(hasContactPermission != PackageManager.PERMISSION_GRANTED ) {
//            ActivityCompat.requestPermissions(this, new String[]
//                    {android.Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
//        }else {
//            //Toast.makeText(AddContactsActivity.this, "Contact Permission is already granted", Toast.LENGTH_LONG).show();
//        }
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
        dialog_shown = false;
    }

    public void onTrimMemory(final int level) {
        System.out.println("registerForActivityCallbacks "+" level "+level);

        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            if(receiver !=null) {
                unregisterReceiver(receiver);
                receiver = null;
            }
            System.out.println("registerForActivityCallbacks " + " closed ");
        } else {
            System.out.println("registerForActivityCallbacks "+" open ");
        }
    }

    public void login(){
        getPhone= phone.getText().toString();

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
                getPhoneNo=phone.getText().toString();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void ResponseOfLogin(String response){
        try {
            final JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                notificationService();

                otpDialog(jsonObject);
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public void otpDialog(JSONObject jsonObject) {
        try {
            System.out.println("otpDialog mBottomSheetDialog "+dialog_shown);

            if(!dialog_shown){
                dialog_shown = true;
                mBottomSheetDialog.show();
            }
            Button submit = (Button) mBottomSheetDialog.findViewById(R.id.submit);
            cancel = (Button) mBottomSheetDialog.findViewById(R.id.cancel);
            submit.setTypeface(typeface_luci);

            TextView textView = (TextView) mBottomSheetDialog.findViewById(R.id.txtview);
            resend_otp = (TextView) mBottomSheetDialog.findViewById(R.id.resend_otp);
            textView.setTypeface(typeface_luci);
            textView.setText(jsonObject.getString("message"));
            otp = (EditText) mBottomSheetDialog.findViewById(R.id.otp);
            otp.setTypeface(typeface_luci);
            SmsReceiver.bindListener(new SmsListener() {
                @Override
                public void messageReceived(String messageText) {
                    String msgArr[] = messageText.split("\\s");
                    messageText = msgArr[1];
                    //Toast.makeText(Login.this, "Message: " + messageText, Toast.LENGTH_LONG).show();
                    otp.setText(messageText);
                }
            });
            resend_otp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    login();
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mBottomSheetDialog.dismiss();
                    dialog_shown = false;
                    otp.setText("");
                }
            });
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String getotp = otp.getText().toString();
                    if (getotp.length() == 0) {
                        otp.setError("Enter Your Otp");
                    } else {
                        otp.setError(null);
                        if (IsNetworkConnection.checkNetworkConnection(Login.this)) {
                            String url = Constants.SERVER_URL + "user/otp";
                            JSONObject jsonBody = new JSONObject();
                            JSONObject userparams = new JSONObject();
                            JSONObject otpparams = new JSONObject();
                            try {
                                userparams.put("username", getPhoneNo);
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

                        } else {
                            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                                    "No Internet Connection");
                        }
                    }
                }
            });
        }catch (Exception e){
            System.out.println("otpDialog Exception "+e.toString());
        }

    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startActivity(getIntent());

    }
    public void ResponseOfLoginOtp(String response){
        try {
            final JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse ResponseOfLoginOtp "," has data "+jsonObject.toString());
            if(!jsonObject.getString("status").equalsIgnoreCase("failed")){
                editor.putString("isLoggedIn","true");
                editor.putString("user_id",jsonObject.getString("user_id"));
                if(jsonObject.has("email")) {
                    editor.putString("email",jsonObject.getString("email"));
                }
                editor.putString("name",jsonObject.getString("name"));
                editor.putString("access_token",jsonObject.getString("access_token"));
                editor.commit();
                mBottomSheetDialog.dismiss();
                dialog_shown = false;

                if(reached_dest.equalsIgnoreCase("false")) {
                    Intent i = new Intent(Login.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(Login.this, ReachedDestination.class);
                    startActivity(i);
                    finish();

                }
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
            System.out.println("SYSTEMPRINT error "+e.toString());
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


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
