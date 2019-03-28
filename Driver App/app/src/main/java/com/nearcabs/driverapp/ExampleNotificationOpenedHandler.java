package com.nearcabs.driverapp;

import android.util.Log;

import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

/**
 * Created by Anup on 7/25/2017.
 */

class ExampleNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    // This fires when a notification is opened by tapping on it.
    String customerName = "No bookings", pickupLocation = "Please wait for the booking", customerPhone = "0000000000", fare = "0", otp = "0000", ride_id = "000", cab_id = "000";
    Double startLat = 0.0, startLng = 0.0, endLat = 0.0, endLng = 0.0;

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        OSNotificationAction.ActionType actionType = result.action.type;
        JSONObject data = result.notification.payload.additionalData;

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

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);

        // The following can be used to open an Activity of your choice.
        // Replace - getApplicationContext() - with any Android Context.
        // Intent intent = new Intent(getApplicationContext(), YourActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        // startActivity(intent);

        // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
        //   if you are calling startActivity above.
     /*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */
    }
}