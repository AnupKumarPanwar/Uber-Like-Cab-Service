package com.anupkumarpanwar.driver;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OTP extends AppCompatActivity {
    TextView otptxtCutomerName, otptxtPickup, otptxtDestination, otptxtFare;
    EditText txtotp;
    Button otpbtnStartRide, done;
    String inputOTP, realOTP, fare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);


        otptxtCutomerName=(TextView)findViewById(R.id.otptxtCutomerName);
        otptxtPickup=(TextView)findViewById(R.id.otptxtPickup);
        otptxtDestination=(TextView)findViewById(R.id.otptxtDestination);
        otptxtFare=(TextView)findViewById(R.id.otptxtFare);

        txtotp=(EditText)findViewById(R.id.txtotp);

        otpbtnStartRide=(Button)findViewById(R.id.otpbtnStartRide);
        done=(Button)findViewById(R.id.btnDone);

        done.setVisibility(View.GONE);

        final Bundle bundle=getIntent().getExtras();

        otptxtCutomerName.setText(bundle.getString("customerName"));
        otptxtPickup.setText(bundle.getString("pickupLocation"));
        otptxtDestination.setText(bundle.getString("destinationLocation"));

        inputOTP=txtotp.getText().toString();
        realOTP=bundle.getString("realOTP");
        fare=bundle.getString("fare");


        otpbtnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputOTP=txtotp.getText().toString();
                if (String.valueOf(inputOTP).equals(realOTP))
                {
                    otpbtnStartRide.setVisibility(View.GONE);
                    otptxtFare.setText("Rs. "+fare);
                    done.setVisibility(View.VISIBLE);

                }
                else
                {
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
}
