package com.biker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.biker.Utils.Constants;
import com.biker.Utils.CustomToast;
import com.biker.Utils.IsNetworkConnection;
import com.biker.Utils.PrintClass;
import com.biker.Utils.post_async;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Teju on 19/09/2017.
 */
public class UserRegister extends AppCompatActivity {

    private View rootView;
    private EditText name,phone,email;
    private SharedPreferences prefrence;
    private SharedPreferences.Editor editor;
    String type="";
    boolean isUpdate=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        Typeface typeface_luci = Typeface.createFromAsset(getAssets(), "fonts/luci.ttf");

        prefrence = getSharedPreferences("My_Pref", 0);
        editor = prefrence.edit();
        isUpdate=false;
        rootView=findViewById(android.R.id.content);
        name=(EditText)findViewById(R.id.name);
        Button click=(Button)findViewById(R.id.buttonclick);
        phone=(EditText)findViewById(R.id.phone);
        email=(EditText)findViewById(R.id.email);
        name.setTypeface(typeface_luci);
        phone.setTypeface(typeface_luci);
        email.setTypeface(typeface_luci);
        click.setTypeface(typeface_luci);

        type=getIntent().getStringExtra("type");
        PrintClass.printValue("UserRegisterPrint type ",type);

        if(type.equals("edit")) {
            getProfileInfo();
            click.setText("UPDATE");
        } else {
            name.setText("");
            email.setText("");
            phone.setText("");
        }
    }

    public void getProfileInfo(){
        if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
            String url = Constants.SERVER_URL + "profile/user-profile";
            JSONObject params = new JSONObject();
            try {
                params.put("user_id",prefrence.getString("user_id", "") );
                params.put("access_token",prefrence.getString("access_token", ""));

            } catch (JSONException e) {
                e.printStackTrace();
                PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
            }
            PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + params.toString());
            new post_async(UserRegister.this, "UserGetProfileInfo").execute(url, params.toString());
        } else {
            new CustomToast().Show_Toast(getApplicationContext(), rootView,
                    "No Internet Connection");
        }
    }

    public void submit(View v){
        if(type.equals("edit")) {
            update();
        } else {
            register();
        }
    }

    public void register(){
        if(validate()) {
            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                //   String url = Constants.SERVER_URL + "action=ReferFriend";
                String url = Constants.SERVER_URL + "user/register";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
                try {
                    params.put("first_name",name.getText().toString() );
                    params.put("email", email.getText().toString());
                    params.put("mobile_no",phone.getText().toString());
                    params.put("user_type", "customer");
                    jsonBody.put("ApiSignupForm", params);
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                new post_async(UserRegister.this, "UserRegister").execute(url, jsonBody.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void update(){
        if(validate()) {
            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
                //   String url = Constants.SERVER_URL + "action=ReferFriend";
                String url = Constants.SERVER_URL + "profile/user-update-profile";
                JSONObject jsonBody = new JSONObject();
                JSONObject params = new JSONObject();
                try {
                    params.put("first_name",name.getText().toString() );
                    params.put("email", email.getText().toString());
                    params.put("mobile_no",phone.getText().toString());
                    jsonBody.put("UpdateProfileForm", params);
                    jsonBody.put("user_id", prefrence.getString("user_id", ""));
                    jsonBody.put("access_token", prefrence.getString("access_token", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                    PrintClass.printValue("SYSTEMPRINT PARAMS", e.toString());
                }
                PrintClass.printValue("SYSTEMPRINT UserRegister  ", "LENGTH " + jsonBody.toString());
                new post_async(UserRegister.this, "UserUpdate").execute(url, jsonBody.toString());
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
        }
    }

    public void ResponseOfRegister(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
                editor.putString("user_id",  jsonObject.getString("user_id"));
                editor.commit();
                if(type.equals("edit")) {
                    isUpdate=true;
                    getProfileInfo();
                } else {
                    Intent i = new Intent(this, Login.class);
                    i.putExtra("reached_dest","false");
                    startActivity(i);
                    finish();
                }
            } else {
                JSONObject errorjsonObject = jsonObject.getJSONObject("errors");
                if(errorjsonObject.has("mobile_no")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("mobile_no");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0));
                    PrintClass.printValue("UserRegisterREsponse jsonObject errorjsonObject " +
                            "", " has data " + jsonArray.getString(0));
                    return;
                } else if(errorjsonObject.has("email")) {
                    JSONArray jsonArray =errorjsonObject.getJSONArray("email");
                    new CustomToast().Show_Toast(getApplicationContext(), rootView,
                            jsonArray.getString(0) );
                    return;
                }
            }
        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public void ResponseOfUserInfo(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            PrintClass.printValue("UserRegisterREsponse jsonObject "," has data "+jsonObject.toString());
            if(jsonObject.getString("status").equalsIgnoreCase("success")){
                JSONObject userInfo=jsonObject.getJSONObject("userDetails");
                name.setText(userInfo.getString("first_name"));
                email.setText(userInfo.getString("email"));
                phone.setText(userInfo.getString("mobile_no"));
                editor.putString("name",userInfo.getString("first_name"));
                editor.commit();
                if(isUpdate){
                    Intent i = new Intent(UserRegister.this, MainActivity.class);
                    startActivity(i);
                    isUpdate=false;
                }
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        jsonObject.getString("message") );
            }

        } catch (Exception e){
            System.out.println("SYSTEMPRINT error UserRegister "+e.toString());
        }
    }

    public boolean validate(){

        String getName=name.getText().toString();
        String getEmail=email.getText().toString();
        String getPhone=phone.getText().toString();

        Pattern p = Pattern.compile(Constants.regEx);
        Matcher m = p.matcher(getEmail);

        PrintClass.printValue("PRINTINGHRVLUES ", "name : " + getName + " phone " + getPhone + " email " + getEmail);
        if(getName.length() == 0) {
            phone.setError(null);
            name.setError("First name cannot be empty");
            return false;
        } else if(getPhone.length()==0){
            name.setError(null);
            phone.setError("Phone Number cannot be empty");
            return false;
        } else if(!getPhone.matches(Constants.regexStr) || getPhone.length()!=10) {
            name.setError(null);
            phone.setError("Your Phone Number is Invalid.");
            return false;

        } else if(getEmail.length() > 0) {
            if(!m.find()) {
                name.setError(null);
                phone.setError(null);
                email.setError("Email Id Invalid");
                return false;
            } else {
                return true;
            }
        } else {
            name.setError(null);
            phone.setError(null);
            email.setError(null);
            return  true;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        name.setText("");
        phone.setText("");
        email.setText("");
    }
}
