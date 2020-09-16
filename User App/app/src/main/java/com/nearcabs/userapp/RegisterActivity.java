package com.nearcabs.userapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    EditText txtName, txtPhone, txtEmail, txtPassword;
    Button btnRegister;
    TextView txtAlreadyRegistered;
    String PREFS_NAME = "auth_info";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(threadPolicy);

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (sharedPreferences.getString("id", null) != null && sharedPreferences.getString("name", null) != null && sharedPreferences.getString("phone", null) != null && sharedPreferences.getString("email", null) != null) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            finish();
        }


        txtName = (EditText) findViewById(R.id.txtName);
        txtPhone = (EditText) findViewById(R.id.txtPhone);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        btnRegister = (Button) findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InsertData();
            }
        });

        txtAlreadyRegistered = (TextView) findViewById(R.id.txtAlreadyRegistered);
        txtAlreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    public void InsertData() {
        try {
            URL url = new URL(getString(R.string.server_url) + "/user_signup.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            String inputName = txtName.getText().toString();
            String inputPhone = txtPhone.getText().toString();
            String inputEmail = txtEmail.getText().toString();
            String inputPassword = txtPassword.getText().toString();

            String data = "name=" + URLEncoder.encode(inputName, "UTF-8") + "&phone=" + URLEncoder.encode(inputPhone, "UTF-8") + "&email=" + URLEncoder.encode(inputEmail, "UTF-8") + "&password=" + URLEncoder.encode(inputPassword, "UTF-8");
//            data= URLEncoder.encode(data,"UTF-8");
//            Toast.makeText(getApplicationContext(),data,Toast.LENGTH_LONG).show();
            writer.write(data);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String response = "";
            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }
//            line=bufferedReader.readLine();

//            Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
//            conn.connect();

            JSONObject response_data = new JSONObject(response);
            if (response_data.getString("status").equals("1")) {
//                Toast.makeText(getApplicationContext(), response_data.getString("data").toString(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), response_data.getString("data"), Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }


    }

}
