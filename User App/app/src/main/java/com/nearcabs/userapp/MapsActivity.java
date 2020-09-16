package com.nearcabs.userapp;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    int flag = 0;
    ;
    boolean isSourceSet = false, tripStarted = false;
    EditText source_location, destination_location;
    String TAG = "LocationSelect";
    int AUTOCOMPLETE_SOURCE = 1, AUTOCOMPLETE_DESTINATITON = 2;
    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker, source_location_marker, destination_location_marker;
    Marker nearby_cab;
    LocationRequest mLocationRequest;
    ArrayList<LatLng> markerPoints;
    Button btnBookNow;
    ImageView cab;
    RelativeLayout driver_info;
    LinearLayout ll_call, ll_share, ll_cancel;
    String driver_name, cab_no, cab_id, otp, fare, driver_phone, ride_id;
    TextView cab_no_a, cab_no_b, ride_otp, ride_driver_name, ride_fare;
    String PREFS_NAME = "auth_info";
    ProgressDialog progressDialog;
    Timer timer;
    Handler handler;
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MarkerOptions markerOptions1;
            try {

                String api_url = getString(R.string.server_url) + "/get_cab_location.php";

                String get_cab_location_request = "cab_id=" + URLEncoder.encode(cab_id, "UTF-8") + "&ride_id=" + URLEncoder.encode(ride_id, "UTF-8");

                JSONObject response_data = call_api(api_url, get_cab_location_request);

//                Toast.makeText(getApplicationContext(), response_data.toString(), Toast.LENGTH_LONG).show();

                if (response_data.getString("status").equals("1")) {
                    if (nearby_cab != null) {
                        nearby_cab.remove();
                    }

                    markerOptions1 = new MarkerOptions();
                    JSONObject get_cab_position_response_data = response_data.getJSONObject("data");


                    LatLng nearby_cab_position = new LatLng(Double.parseDouble(get_cab_position_response_data.getString("cab_lat")), Double.parseDouble(get_cab_position_response_data.getString("cab_lng")));
                    markerOptions1.position(nearby_cab_position);

                    BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
                    Bitmap b = bitmapDrawable.getBitmap();
                    Bitmap smallCar = Bitmap.createScaledBitmap(b, 60, 72, false);
                    markerOptions1.icon(BitmapDescriptorFactory.fromBitmap(smallCar));
                    markerOptions1.rotation(Float.parseFloat(get_cab_position_response_data.getString("cab_bearing")));

                    nearby_cab = mMap.addMarker(markerOptions1);

                    handler.postDelayed(this, 10000);
                } else if (response_data.getString("status").equals("2")) {
                    ll_cancel.setClickable(false);
                    handler.removeCallbacksAndMessages(runnable);
                    if (nearby_cab != null) {
                        nearby_cab.remove();
                    }

                    tripStarted = true;
                    cab.setVisibility(View.VISIBLE);
                } else if (response_data.getString("status").equals("3")) {
                    ll_cancel.setClickable(false);
                    handler.removeCallbacksAndMessages(runnable);
                } else {
                    handler.postDelayed(this, 10000);
                }

            } catch (Exception e) {
                handler.postDelayed(this, 10000);
            }


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        handler = new Handler();

        driver_info = (RelativeLayout) findViewById(R.id.driver_details);

        driver_info.setVisibility(View.GONE);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Booking...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);

        ll_call = (LinearLayout) findViewById(R.id.ll_call);
        ll_share = (LinearLayout) findViewById(R.id.ll_share);
        ll_cancel = (LinearLayout) findViewById(R.id.ll_cancel);

        cab_no_a = (TextView) findViewById(R.id.cab_no_a);
        cab_no_b = (TextView) findViewById(R.id.cab_no_b);
        ride_driver_name = (TextView) findViewById(R.id.driver_name);
        ride_otp = (TextView) findViewById(R.id.ride_otp);
        ride_fare = (TextView) findViewById(R.id.ride_fare);

        btnBookNow = (Button) findViewById(R.id.btnBookNow);
        btnBookNow.setVisibility(View.GONE);


        cab = (ImageView) findViewById(R.id.cab);
        cab.setVisibility(View.GONE);

        timer = new Timer();

        markerPoints = new ArrayList<LatLng>();

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(threadPolicy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        source_location = (EditText) findViewById(R.id.source_location);
        destination_location = (EditText) findViewById(R.id.destination_location);


        btnBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();

                try {

                    String api_url = getString(R.string.server_url) + "/book_cab.php";

                    double src_lat = source_location_marker.getPosition().latitude;
                    double src_lng = source_location_marker.getPosition().longitude;

                    double dest_lat = destination_location_marker.getPosition().latitude;
                    double dest_lng = destination_location_marker.getPosition().longitude;

                    SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

                    String user_id = sharedPreferences.getString("id", null);

                    String book_now_request = "user_id=" + URLEncoder.encode(user_id, "UTF-8") + "&src_lat=" + URLEncoder.encode(String.valueOf(src_lat), "UTF-8") + "&src_lng=" + URLEncoder.encode(String.valueOf(src_lng), "UTF-8") + "&dest_lat=" + URLEncoder.encode(String.valueOf(dest_lat), "UTF-8") + "&dest_lng=" + URLEncoder.encode(String.valueOf(dest_lng), "UTF-8");

                    JSONObject response_data = call_api(api_url, book_now_request);
                    if (response_data.getString("status").equals("1")) {
                        if (nearby_cab != null) {
                            nearby_cab.remove();
                        }

//                        MarkerOptions markerOptions1=new MarkerOptions();
                        JSONObject book_cab_response_data = response_data.getJSONObject("data");

                        ride_id = book_cab_response_data.getString("ride_id");

                        cab_no = book_cab_response_data.getString("cab_no");
                        cab_id = book_cab_response_data.getString("cab_id");


                        driver_name = book_cab_response_data.getString("driver_name");


                        driver_phone = book_cab_response_data.getString("driver_phone");

                        otp = "OTP : " + book_cab_response_data.getString("otp");


                        fare = "Rs. " + book_cab_response_data.getString("fare");

                        cab_no_a.setText(cab_no.split(" ")[0]);
                        cab_no_b.setText(cab_no.split(" ")[1]);

                        ride_driver_name.setText(driver_name);
                        ride_otp.setText(otp);

                        ride_fare.setText(fare);


                        ll_call.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + driver_phone));

                                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                startActivity(callIntent);
                            }
                        });


                        ll_share.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                String shareBody = driver_name + " (" + driver_phone + ") is on the way in Cab number " + cab_no + ". You are paying " + fare + " for this ride. Share " + otp + " with the driver to start the ride.";
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Near Cabs Booking");
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                            }
                        });


                        ll_cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                try {
                                    String cancel_api_url = getString(R.string.server_url) + "/cancel_book_cab.php";
                                    String cancel_book_now_request = "ride_id=" + URLEncoder.encode(ride_id, "UTF-8") + "&cab_id=" + URLEncoder.encode(cab_id, "UTF-8");

                                    JSONObject cancel_response_data = call_api(cancel_api_url, cancel_book_now_request);
                                    if (cancel_response_data.getString("status").equals("1")) {
                                        Toast.makeText(getApplicationContext(), "Booking Cancelled", Toast.LENGTH_SHORT).show();
                                        driver_info.setVisibility(View.GONE);
                                        btnBookNow.setVisibility(View.VISIBLE);
                                    }
                                } catch (Exception e) {
//                                 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                        btnBookNow.setVisibility(View.GONE);
                        driver_info.setVisibility(View.VISIBLE);


                        progressDialog.hide();

//                        updateNearbyCabPosition();

                        handler.postDelayed(runnable, 0);


                    } else {
                        Toast.makeText(getApplicationContext(), "No cabs nearby", Toast.LENGTH_LONG).show();
                    }


                } catch (Exception e) {
//                    Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
                }


            }
        });


//        Toast.makeText(getApplicationContext(), this.toString(), Toast.LENGTH_LONG).show();
        final Activity activity = this;

        source_location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    try {
//                        Intent intent =
//                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(activity);
//                        startActivityForResult(intent, AUTOCOMPLETE_SOURCE);
                        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
                        builder.setAndroidApiKey(getResources().getString(R.string.android_api_key))
                                .setMapsApiKey(getResources().getString(R.string.google_maps_key));
                        startActivityForResult(builder.build(activity), AUTOCOMPLETE_SOURCE);
                    } catch (Exception e) {
                        // TODO: Handle the error.
//                        Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        source_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    Intent intent =
//                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(activity);
//                    startActivityForResult(intent, AUTOCOMPLETE_SOURCE);
                    PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
                    builder.setAndroidApiKey(getResources().getString(R.string.android_api_key))
                            .setMapsApiKey(getResources().getString(R.string.google_maps_key));
                    startActivityForResult(builder.build(activity), AUTOCOMPLETE_SOURCE);
                } catch (Exception e) {
                    // TODO: Handle the error.
//                    Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });


        destination_location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
//                Toast.makeText(getApplicationContext(), "Destination", Toast.LENGTH_SHORT).show();
                if (b) {
                    try {
//                        Intent intent =
//                                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(activity);
//                        startActivityForResult(intent, AUTOCOMPLETE_DESTINATITON);
                        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
                        builder.setAndroidApiKey(getResources().getString(R.string.android_api_key))
                                .setMapsApiKey(getResources().getString(R.string.google_maps_key));
                        startActivityForResult(builder.build(activity), AUTOCOMPLETE_DESTINATITON);
                    } catch (Exception e) {
                        // TODO: Handle the error.
//                        Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        destination_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
//                    Intent intent =
//                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(activity);
//                    startActivityForResult(intent, AUTOCOMPLETE_DESTINATITON);
                    PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
                    builder.setAndroidApiKey(getResources().getString(R.string.android_api_key))
                            .setMapsApiKey(getResources().getString(R.string.google_maps_key));
                    startActivityForResult(builder.build(activity), AUTOCOMPLETE_DESTINATITON);
                } catch (Exception e) {
                    // TODO: Handle the error.
//                    Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
//            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_SOURCE) {
            if (resultCode == RESULT_OK) {
                Place place = PingPlacePicker.getPlace(data);
                Log.i(TAG, "Place: " + place.getName());
                source_location.setText(place.getName());

                if (source_location_marker != null) {
                    source_location_marker.remove();
                }

                LatLng latLng = place.getLatLng();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Source");
//                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
//                Bitmap b = bitmapDrawable.getBitmap();
//                Bitmap smallCar = Bitmap.createScaledBitmap(b, 150, 81, false);

//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallCar));
//                markerOptions.rotation(location.getBearing());
                source_location_marker = mMap.addMarker(markerOptions);

                setNearbyCabsOnMap(latLng);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        } else if (requestCode == AUTOCOMPLETE_DESTINATITON) {
            if (resultCode == RESULT_OK) {
//                Place place = PlaceAutocomplete.getPlace(this, data);
                Place place = PingPlacePicker.getPlace(data);
                Log.i(TAG, "Place: " + place.getName());
                destination_location.setText(place.getName());

                if (destination_location_marker != null) {
                    destination_location_marker.remove();
                }

                LatLng latLng = place.getLatLng();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Destination");
//                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
//                Bitmap b = bitmapDrawable.getBitmap();
//                Bitmap smallCar = Bitmap.createScaledBitmap(b, 150, 81, false);

//                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallCar));
//                markerOptions.rotation(location.getBearing());
                destination_location_marker = mMap.addMarker(markerOptions);


                if (!source_location.getText().toString().equals("") && !destination_location.getText().toString().equals("")) {
                    String url = getDirectionsUrl(source_location_marker.getPosition(), destination_location_marker.getPosition());
                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);

                    btnBookNow.setVisibility(View.VISIBLE);
                } else {
                    btnBookNow.setVisibility(View.GONE);
                }

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    public void setNearbyCabsOnMap(LatLng latLng) {
        try {
            String api_url = getString(R.string.server_url) + "/get_near_cabs.php";

            double user_lat = latLng.latitude;
            double user_lng = latLng.longitude;

            String pickup_location_cabs_request = "user_lat=" + URLEncoder.encode(String.valueOf(user_lat), "UTF-8") + "&user_lng=" + URLEncoder.encode(String.valueOf(user_lng), "UTF-8");

            JSONObject response_data = call_api(api_url, pickup_location_cabs_request);
            if (response_data.getString("status").equals("1")) {
                if (nearby_cab != null) {
                    nearby_cab.remove();
                }

                MarkerOptions markerOptions1 = new MarkerOptions();
                JSONObject nearby_cab_position_data = response_data.getJSONObject("data");

//              cab_no=nearby_cab_position_data.getString("cab_no");
//              cab_no_a.setText(cab_no.split(" ")[0]);
//              cab_no_b.setText(cab_no.split(" ")[1]);
//
//              driver_name=nearby_cab_position_data.getString("driver_name");
//              ride_driver_name.setText(driver_name);
//
//              driver_phone=nearby_cab_position_data.getString("driver_phone");


                LatLng nearby_cab_position = new LatLng(Double.parseDouble(nearby_cab_position_data.getString("cab_lat")), Double.parseDouble(nearby_cab_position_data.getString("cab_lng")));
                markerOptions1.position(nearby_cab_position);

                BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.car);
                Bitmap b = bitmapDrawable.getBitmap();
                Bitmap smallCar = Bitmap.createScaledBitmap(b, 60, 72, false);
                markerOptions1.icon(BitmapDescriptorFactory.fromBitmap(smallCar));
                markerOptions1.rotation(mLastLocation.getBearing());

                nearby_cab = mMap.addMarker(markerOptions1);
            } else {
                Toast.makeText(getApplicationContext(), "No cabs nearby", Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
//          Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
//            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setTrafficEnabled(true);

//        mMap.getUiSettings().setScrollGesturesEnabled(false);
//        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        mMap.setTrafficEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 18.0f));
        mMap.setMyLocationEnabled(false);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (!isSourceSet) {
            try {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                String cityName = addresses.get(0).getAddressLine(0);
                String stateName = addresses.get(0).getAddressLine(1);
                source_location.setText(cityName + ", " + stateName);

                if (source_location_marker != null) {
                    source_location_marker.remove();
                }

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title("Source");
                source_location_marker = mMap.addMarker(markerOptions);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng)      // Sets the center of the map to Mountain View
                        .zoom(mMap.getCameraPosition().zoom)                   // Sets the zoom
                        .bearing(location.getBearing())                // Sets the orientation of the camera to east
                        .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                setNearbyCabsOnMap(latLng);

                isSourceSet = true;
            } catch (Exception e) {
//                Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }


        }

        if (tripStarted) {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)      // Sets the center of the map to Mountain View
                    .zoom(mMap.getCameraPosition().zoom)                   // Sets the zoom
                    .bearing(location.getBearing())                // Sets the orientation of the camera to east
                    .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(6);
                lineOptions.color(Color.BLUE);
            }

            try {
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            } catch (Exception e) {
                // Do nothing
            }
        }
    }
}
