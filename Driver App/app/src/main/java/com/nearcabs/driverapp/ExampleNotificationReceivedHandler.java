package com.nearcabs.driverapp;

import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;

/**
 * Created by Anup on 7/22/2017.
 */

class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
    public String customerName = "No bookings", pickupLocation = "Please wait for the booking", customerPhone = "0000000000", fare = "0", otp = "0000", ride_id = "000", cab_id = "000";
    Double startLat = 0.0, startLng = 0.0, endLat = 0.0, endLng = 0.0;

    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;

        if (data != null) {
            customerName = data.optString("customerName", null);
            customerPhone = data.optString("customerPhone", null);
            startLat = Double.parseDouble(data.optString("src_lat", null));
            startLng = Double.parseDouble(data.optString("src_lng", null));
            endLat = Double.parseDouble(data.optString("dest_lat", null));
            endLng = Double.parseDouble(data.optString("dest_lng", null));
            fare = data.optString("fare", null);
            otp = data.optString("otp", null);
            ride_id = data.optString("ride_id", null);
            cab_id = data.optString("cab_id", null);
            if (customerName != null)
                Log.i("OneSignalExample", "customkey set with value: ");
        }
    }
}