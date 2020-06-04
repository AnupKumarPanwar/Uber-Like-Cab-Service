package com.nearcabs.driverapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class OTP extends AppCompatActivity {
    TextView otptxtCutomerName, otptxtPickup, otptxtDestination, otptxtFare;
    EditText txtotp;
    Button otpbtnStartRide, done;
    String inputOTP, realOTP, fare, ride_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);


        otptxtCutomerName = (TextView) findViewById(R.id.otptxtCutomerName);
        otptxtPickup = (TextView) findViewById(R.id.otptxtPickup);
        otptxtDestination = (TextView) findViewById(R.id.otptxtDestination);
        otptxtFare = (TextView) findViewById(R.id.otptxtFare);

        txtotp = (EditText) findViewById(R.id.txtotp);

        otpbtnStartRide = (Button) findViewById(R.id.otpbtnStartRide);
        done = (Button) findViewById(R.id.btnDone);

        done.setVisibility(View.GONE);

        final Bundle bundle = getIntent().getExtras();

        otptxtCutomerName.setText(bundle.getString("customerName"));
        otptxtPickup.setText(bundle.getString("pickupLocation"));
        otptxtDestination.setText(bundle.getString("destinationLocation"));

        inputOTP = txtotp.getText().toString();
        realOTP = bundle.getString("realOTP");
        fare = bundle.getString("fare");

        ride_id = bundle.getString("ride_id");


        otpbtnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOTP = txtotp.getText().toString();
                if (String.valueOf(inputOTP).equals(realOTP)) {


                    try {
                        String api_url = getString(R.string.server_url) + "/api/start_ride.php";

                        String end_ride_request = "ride_id=" + URLEncoder.encode(ride_id, "UTF-8");

                        JSONObject response_data = call_api(api_url, end_ride_request);

                        if (response_data.getString("status").equals("1")) {
                            Toast.makeText(getApplicationContext(), "Trip Started", Toast.LENGTH_LONG).show();
                            otpbtnStartRide.setVisibility(View.GONE);
                            txtotp.setEnabled(false);
                            otptxtFare.setText("Rs. " + fare);
                            done.setVisibility(View.VISIBLE);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "Wrong OTP", Toast.LENGTH_LONG).show();

                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onBackPressed() {
    }


    public JSONObject call_api(String api_url, String request_data) {
        try {
            URL url = new URL(api_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));

            writer.write(request_data);
            writer.flush();
            writer.close();
            os.close();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String response = "";
            while ((line = bufferedReader.readLine()) != null) {
                response += line;
            }

            Log.d("API response", response);

            JSONObject response_data = new JSONObject(response);
            return response_data;

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

        return null;
    }


}
