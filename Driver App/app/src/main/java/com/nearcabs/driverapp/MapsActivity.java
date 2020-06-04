package com.nearcabs.driverapp;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.onesignal.OneSignal;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    int flag = 0;
    boolean isTripDataSet = false, tripStarted = false;
    ;
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
    Button btnStartRide, btnEndRide;
    ImageView cab;
    RelativeLayout booking_details;
    LinearLayout ll_booking_info, ll_call;
    String customer_name, otp, fare, cab_id, ride_id;
    TextView txtcustomer_name, txtpickup_location, txtFare;
    String PREFS_NAME = "auth_info";
    ProgressDialog progressDialog;
    Handler handler;
    ExampleNotificationReceivedHandler exampleNotificationReceivedHandler;
    ExampleNotificationOpenedHandler exampleNotificationOpenedHandler;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        booking_details = (RelativeLayout) findViewById(R.id.booking_details);

        ll_booking_info = (LinearLayout) findViewById(R.id.ll_book_info);
        ll_call = (LinearLayout) findViewById(R.id.ll_call);

        txtcustomer_name = (TextView) findViewById(R.id.txtcustomer_name);
        txtpickup_location = (TextView) findViewById(R.id.txtpickup_address);
        txtFare = (TextView) findViewById(R.id.txtFare);
        txtFare.setVisibility(View.GONE);

        btnStartRide = (Button) findViewById(R.id.btnStartRide);
        btnStartRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), OTP.class);
                intent.putExtra("customerName", customer_name);
                intent.putExtra("pickupLocation", source_location.getText().toString());
                intent.putExtra("destinationLocation", destination_location.getText().toString());
                intent.putExtra("realOTP", otp);
                intent.putExtra("fare", fare);
                intent.putExtra("ride_id", ride_id);
                startActivity(intent);
                txtFare.setVisibility(View.VISIBLE);
                btnStartRide.setVisibility(View.GONE);
                btnEndRide.setVisibility(View.VISIBLE);
                tripStarted = true;
            }
        });
        btnStartRide.setVisibility(View.GONE);

        btnEndRide = (Button) findViewById(R.id.btnEndRide);
        btnEndRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String api_url = getString(R.string.server_url) + "/api/end_ride.php";

                    String end_ride_request = "ride_id=" + URLEncoder.encode(ride_id, "UTF-8") + "&cab_id=" + URLEncoder.encode(cab_id, "UTF-8");

                    JSONObject response_data = call_api(api_url, end_ride_request);

                    if (response_data.getString("status").equals("1")) {
                        Toast.makeText(getApplicationContext(), "Trip Ended", Toast.LENGTH_LONG).show();
                        tripStarted = false;

                        handler.postDelayed(runnable, 1);


                        btnEndRide.setVisibility(View.GONE);

                        txtFare.setVisibility(View.GONE);
                        txtcustomer_name.setText("No Bookings");
                        txtpickup_location.setText("Please wait for the booking");

                        exampleNotificationReceivedHandler.customerName = "No bookings";
                        exampleNotificationReceivedHandler.pickupLocation = "Please wait for the booking";
                        exampleNotificationReceivedHandler.customerPhone = "0000000000";

                        exampleNotificationOpenedHandler.customerName = "No bookings";
                        exampleNotificationOpenedHandler.pickupLocation = "Please wait for the booking";
                        exampleNotificationOpenedHandler.customerPhone = "0000000000";


                    }
                } catch (Exception e) {
//                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        btnEndRide.setVisibility(View.GONE);


        cab = (ImageView) findViewById(R.id.cab);


        markerPoints = new ArrayList<LatLng>();

        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(threadPolicy);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        source_location = (EditText) findViewById(R.id.source_location);
        destination_location = (EditText) findViewById(R.id.destination_location);


        exampleNotificationReceivedHandler = new ExampleNotificationReceivedHandler();
        exampleNotificationOpenedHandler = new ExampleNotificationOpenedHandler();

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationReceivedHandler(exampleNotificationReceivedHandler)
                .setNotificationOpenedHandler(exampleNotificationOpenedHandler)
                .init();

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                if (registrationId != null) {
//                    Toast.makeText(getApplicationContext(), userId, Toast.LENGTH_LONG).show();
                    Log.d("debug", "registrationId:" + userId);


                    try {
                        String api_url = getString(R.string.server_url) + "/api/driver_set_one_signal_id.php";

                        String driver_set_one_signal_id_request = "driver_id=" + URLEncoder.encode(sharedPreferences.getString("id", null), "UTF-8") + "&one_signal_id=" + URLEncoder.encode(userId, "UTF-8");

                        JSONObject response_data = call_api(api_url, driver_set_one_signal_id_request);

//                        Toast.makeText(getApplicationContext(), response_data.toString(), Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        ll_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + exampleNotificationReceivedHandler.customerPhone));

                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);

            }
        });


        handler = new Handler();

        handler.postDelayed(runnable, 5000);

//        booking_details.setVisibility(View.GONE);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Starting ride...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgress(0);
        progressDialog.setCancelable(false);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        try {
            String api_url = getString(R.string.server_url) + "/api/check_current_booking.php";

            String check_current_booking_request = "driver_id=" + URLEncoder.encode(sharedPreferences.getString("id", null), "UTF-8");

            JSONObject response_data = call_api(api_url, check_current_booking_request);

//            Toast.makeText(getApplicationContext(), response_data.toString(), Toast.LENGTH_LONG).show();

            if (response_data.getString("status").equals("1")) {
                exampleNotificationReceivedHandler.customerName = response_data.getJSONObject("data").getString("customerName");
                exampleNotificationReceivedHandler.customerPhone = response_data.getJSONObject("data").getString("customerName");
                exampleNotificationReceivedHandler.startLat = Double.parseDouble(response_data.getJSONObject("data").getString("src_lat"));
                exampleNotificationReceivedHandler.startLng = Double.parseDouble(response_data.getJSONObject("data").getString("src_lng"));
                exampleNotificationReceivedHandler.endLat = Double.parseDouble(response_data.getJSONObject("data").getString("dest_lat"));
                exampleNotificationReceivedHandler.endLng = Double.parseDouble(response_data.getJSONObject("data").getString("dest_lng"));

                exampleNotificationReceivedHandler.fare = response_data.getJSONObject("data").getString("fare");
                exampleNotificationReceivedHandler.otp = response_data.getJSONObject("data").getString("otp");
                exampleNotificationReceivedHandler.ride_id = response_data.getJSONObject("data").getString("ride_id");
                exampleNotificationReceivedHandler.cab_id = response_data.getJSONObject("data").getString("cab_id");

                customer_name = exampleNotificationReceivedHandler.customerName.toString();
                txtcustomer_name.setText(customer_name);
                otp = exampleNotificationReceivedHandler.otp;
                fare = exampleNotificationReceivedHandler.fare;
                ride_id = exampleNotificationReceivedHandler.ride_id;
                cab_id = exampleNotificationReceivedHandler.cab_id;

                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses;
                    addresses = geocoder.getFromLocation(exampleNotificationReceivedHandler.startLat, exampleNotificationReceivedHandler.startLng, 1);
                    String srccityName = addresses.get(0).getAddressLine(0);
                    String srcstateName = addresses.get(0).getAddressLine(1);

                    txtpickup_location.setText(srccityName + ", " + srcstateName);

                    source_location.setText(srccityName + ", " + srcstateName);

                    LatLng latLng = new LatLng(exampleNotificationReceivedHandler.startLat, exampleNotificationReceivedHandler.startLng);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);

                    source_location_marker = mMap.addMarker(markerOptions);

                    addresses = geocoder.getFromLocation(exampleNotificationReceivedHandler.endLat, exampleNotificationReceivedHandler.endLng, 1);
                    String destcityName = addresses.get(0).getAddressLine(0);
                    String deststateName = addresses.get(0).getAddressLine(1);

                    destination_location.setText(destcityName + ", " + deststateName);

                    LatLng latLng1 = new LatLng(exampleNotificationReceivedHandler.endLat, exampleNotificationReceivedHandler.endLng);
                    MarkerOptions markerOptions1 = new MarkerOptions();
                    markerOptions1.position(latLng1);

                    destination_location_marker = mMap.addMarker(markerOptions1);

                    if (!source_location.getText().toString().equals("") && !destination_location.getText().toString().equals("")) {
                        String url = getDirectionsUrl(source_location_marker.getPosition(), destination_location_marker.getPosition());
                        DownloadTask downloadTask = new DownloadTask();

                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }


                } catch (IOException e) {
//                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }


                txtFare.setText("Rs. " + fare);

                if (tripStarted) {
                    txtFare.setVisibility(View.VISIBLE);
                    btnStartRide.setVisibility(View.GONE);
                    btnEndRide.setVisibility(View.VISIBLE);
                } else {
                    txtFare.setVisibility(View.GONE);
                    btnStartRide.setVisibility(View.VISIBLE);
                    btnEndRide.setVisibility(View.GONE);
                }

            }


        } catch (Exception e) {
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }


    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                String api_url = getString(R.string.server_url) + "/api/set_cab_location.php";

                String set_cab_location_request = "cab_id=" + URLEncoder.encode(sharedPreferences.getString("cab_no", null), "UTF-8") + "&lat=" + URLEncoder.encode(String.valueOf(mLastLocation.getLatitude()), "UTF-8") + "&lng=" + URLEncoder.encode(String.valueOf(mLastLocation.getLongitude()), "UTF-8") + "&bearing=" + URLEncoder.encode(String.valueOf(mLastLocation.getBearing()), "UTF-8");

                JSONObject response_data = call_api(api_url, set_cab_location_request);

//                Toast.makeText(getApplicationContext(), response_data.toString(), Toast.LENGTH_LONG).show();

                if (!tripStarted) {
                    handler.postDelayed(this, 10000);
                }

            } catch (Exception e) {
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    };

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

            // Drawing polyline in the Google Map for the i-th route
            try {
                mMap.addPolyline(lineOptions);
            } catch (Exception e) {
//                Do Nothing
            }
        }
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


        if (!exampleNotificationReceivedHandler.customerPhone.equals("0000000000") && !isTripDataSet) {
            customer_name = exampleNotificationReceivedHandler.customerName.toString();
            txtcustomer_name.setText(customer_name);
            otp = exampleNotificationReceivedHandler.otp;
            fare = exampleNotificationReceivedHandler.fare;
            ride_id = exampleNotificationReceivedHandler.ride_id;
            cab_id = exampleNotificationReceivedHandler.cab_id;

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses;
                addresses = geocoder.getFromLocation(exampleNotificationReceivedHandler.startLat, exampleNotificationReceivedHandler.startLng, 1);
                String srccityName = addresses.get(0).getAddressLine(0);
                String srcstateName = addresses.get(0).getAddressLine(1);

                txtpickup_location.setText(srccityName + ", " + srcstateName);

                source_location.setText(srccityName + ", " + srcstateName);

                LatLng latLng = new LatLng(exampleNotificationReceivedHandler.startLat, exampleNotificationReceivedHandler.startLng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                source_location_marker = mMap.addMarker(markerOptions);

                addresses = geocoder.getFromLocation(exampleNotificationReceivedHandler.endLat, exampleNotificationReceivedHandler.endLng, 1);
                String destcityName = addresses.get(0).getAddressLine(0);
                String deststateName = addresses.get(0).getAddressLine(1);

                destination_location.setText(destcityName + ", " + deststateName);

                LatLng latLng1 = new LatLng(exampleNotificationReceivedHandler.endLat, exampleNotificationReceivedHandler.endLng);
                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position(latLng1);

                destination_location_marker = mMap.addMarker(markerOptions1);

                if (!source_location.getText().toString().equals("") && !destination_location.getText().toString().equals("")) {
                    String url = getDirectionsUrl(source_location_marker.getPosition(), destination_location_marker.getPosition());
                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            txtFare.setText("Rs. " + fare);
            isTripDataSet = true;
            btnStartRide.setVisibility(View.VISIBLE);

        } else if (!exampleNotificationOpenedHandler.customerPhone.equals("0000000000") && !isTripDataSet) {
            customer_name = exampleNotificationOpenedHandler.customerName.toString();
            txtcustomer_name.setText(customer_name);
            otp = exampleNotificationOpenedHandler.otp;
            fare = exampleNotificationOpenedHandler.fare;
            ride_id = exampleNotificationOpenedHandler.ride_id;
            cab_id = exampleNotificationOpenedHandler.cab_id;

            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses;
                addresses = geocoder.getFromLocation(exampleNotificationOpenedHandler.startLat, exampleNotificationOpenedHandler.startLng, 1);
                String srccityName = addresses.get(0).getAddressLine(0);
                String srcstateName = addresses.get(0).getAddressLine(1);

                txtpickup_location.setText(srccityName + ", " + srcstateName);

                source_location.setText(srccityName + ", " + srcstateName);

                LatLng latLng = new LatLng(exampleNotificationOpenedHandler.startLat, exampleNotificationOpenedHandler.startLng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                source_location_marker = mMap.addMarker(markerOptions);

                addresses = geocoder.getFromLocation(exampleNotificationOpenedHandler.endLat, exampleNotificationOpenedHandler.endLng, 1);
                String destcityName = addresses.get(0).getAddressLine(0);
                String deststateName = addresses.get(0).getAddressLine(1);

                destination_location.setText(destcityName + ", " + deststateName);

                LatLng latLng1 = new LatLng(exampleNotificationOpenedHandler.endLat, exampleNotificationOpenedHandler.endLng);
                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position(latLng1);

                destination_location_marker = mMap.addMarker(markerOptions1);

                if (!source_location.getText().toString().equals("") && !destination_location.getText().toString().equals("")) {
                    String url = getDirectionsUrl(source_location_marker.getPosition(), destination_location_marker.getPosition());
                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


            isTripDataSet = true;
            btnStartRide.setVisibility(View.VISIBLE);

        }


        mLastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(mMap.getCameraPosition().zoom)                   // Sets the zoom
                .bearing(location.getBearing())                // Sets the orientation of the camera to east
                .tilt(90)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
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
            return false;
        } else {
            return true;
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
}
