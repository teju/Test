package com.example.teju.biker;

import android.content.Intent;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.teju.biker.Utils.Constants;
import com.example.teju.biker.Utils.CustomToast;
import com.example.teju.biker.Utils.IsNetworkConnection;
import com.example.teju.biker.Utils.PrintClass;
import com.example.teju.biker.Utils.post_async;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Teju on 19/09/2017.
 */
public class UserRegister extends AppCompatActivity {

    private View rootView;
    private EditText name,phone,email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        rootView=findViewById(android.R.id.content);
        name=(EditText)findViewById(R.id.name);
        phone=(EditText)findViewById(R.id.phone);
        email=(EditText)findViewById(R.id.email);

        name.setText("");
        email.setText("");
        phone.setText("");
    }

    public void register(View v){
       if(validate()) {

            if (IsNetworkConnection.checkNetworkConnection(UserRegister.this)) {
          /*  //   String url = Constants.SERVER_URL + "action=ReferFriend";
            String url = SyncStateContract.Constants.SERVER_URL + "user/referFriend.htm";
            String json = "[{\"" + "User_Id" + "\":" + "\"" + prefrence.getString("User_Id", "") + "\"" + ",\""
                    + "Device_Type" + "\":" + "\"" +"android"+ "\"" + "}]";
            new post_async(UserRegister.this, "UserRegister").execute(url, json);*/
                Intent i = new Intent(this, Login.class);
                startActivity(i);
            } else {
                new CustomToast().Show_Toast(getApplicationContext(), rootView,
                        "No Internet Connection");
            }
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
