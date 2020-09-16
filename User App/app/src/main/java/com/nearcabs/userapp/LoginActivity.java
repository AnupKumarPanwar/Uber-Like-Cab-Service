package com.nearcabs.userapp;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class LoginActivity extends AppCompatActivity {
    EditText txtEmail, txtPassword;
    Button btnLogin;
    String PREFS_NAME = "auth_info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(threadPolicy);


        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtPassword = (EditText) findViewById(R.id.txtPassword);

        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    public void login() {
        try {
            URL url = new URL(getString(R.string.server_url) + "/user_login.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String inputEmail = txtEmail.getText().toString();
            String inputPassword = txtPassword.getText().toString();

            String data = "email=" + URLEncoder.encode(inputEmail, "UTF-8") + "&password=" + URLEncoder.encode(inputPassword, "UTF-8");

            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            os.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line = "";
            String response = "";

            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }

            JSONObject response_data = new JSONObject(response);
            if (response_data.getString("status").equals("1")) {
                String id = response_data.getJSONObject("data").getString("id");
                String name = response_data.getJSONObject("data").getString("name");
                String phone = response_data.getJSONObject("data").getString("phone");
                String email = response_data.getJSONObject("data").getString("email");

                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("id", id);
                editor.putString("name", name);
                editor.putString("phone", phone);
                editor.putString("email", email);

                editor.commit();

//                Toast.makeText(getApplicationContext(), response_data.getString("data").toString(),Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("name", name);
                intent.putExtra("phone", phone);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(), response_data.getString("data"), Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }
}
