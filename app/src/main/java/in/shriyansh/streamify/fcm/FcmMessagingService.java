package in.shriyansh.streamify.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;

import in.shriyansh.streamify.R;
import in.shriyansh.streamify.activities.MainActivity;
import in.shriyansh.streamify.database.DbMethods;
import in.shriyansh.streamify.network.Urls;
import in.shriyansh.streamify.utils.Constants;
import in.shriyansh.streamify.utils.TimeUtils;
import in.shriyansh.streamify.utils.Utils;
import in.shriyansh.streamify.utils.PreferenceUtils;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Service to receive FCM Messages
 * Created by shriyanshgautam on 24/08/17.
 */

public class FcmMessagingService extends FirebaseMessagingService implements Urls {
    private static final String TAG = FcmMessagingService.class.getSimpleName();

    public static final String EXTRA_NOTIFICATION_TYPE_KEY = "extra_notification_type_key";
    public static final int NOTIFICATION_TYPE_NEWS = 0;
    public static final int NOTIFICATION_TYPE_EVENT = 1;
    private static final String EXTRA_MESSAGE = "message";

    private DbMethods dbMethods;


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        dbMethods = new DbMethods(this);
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled here in onMessageReceived whether the app is in the foreground or background.
        // Data messages are the type traditionally used with GCM. Notification messages are only
        // received here in onMessageReceived when the app is in the foreground. When the app is in
        // the background an automatically generated notification is displayed. When the user taps
        // on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends
        // notification messages. For more see:
        // https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());


                // Handle message within 10 seconds
                handleNow(remoteMessage.getData().toString());


        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "FCM Refreshed token: " + token);

        // Save updated token
        PreferenceUtils.setStringPreference(this,PreferenceUtils.PREF_FCM_TOKEN, token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType,
         "{\n\t\"email\":\"" +
                 PreferenceUtils.getStringPreference(this,PreferenceUtils.PREF_USER_EMAIL) +
                 "\",\n\t\"fcmToken\":\"" + token +"\"\n}");
        Request request = new Request.Builder()
          .url(Urls.FCM_UPDATE)
          .post(body)
          .addHeader("Content-Type", "application/json")
          .build();

        try{
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful())
                Log.e(TAG, "onNewToken failed:" + response.body().string());
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(String data) {
        Log.d(TAG, "Short lived task is done.");
        try {
            JSONObject jsonData = new JSONObject(data);
            if (jsonData.getInt("type") == FCM_TYPE_NOTIFICATION) {
                JSONArray notificationJsonArray = new JSONArray();
                notificationJsonArray.put(jsonData.getJSONObject("notification"));
                long count = dbMethods.insertNotifications(notificationJsonArray);
                if (count != 0) {
                    sendNotification(data,getApplicationContext());
                    broadcast(getApplicationContext(),"notification_received");
                }
            }

            if (jsonData.getInt("type") == FCM_TYPE_EVENT) {
                JSONArray eventJsonArray = new JSONArray();
                eventJsonArray.put(jsonData.getJSONObject("event"));
                long count = dbMethods.insertEvents(eventJsonArray);
                if (count != 0) {
                    sendNotification(data,getApplicationContext());
                    broadcast(getApplicationContext(),"event_received");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void broadcast(Context context, String msg) {
        Intent intent = new Intent(Constants.DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, msg);
        context.sendBroadcast(intent);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody,Context context) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            String title;
            String subtitle;
            String description;
            String streamUrl;
            int smallIcon = R.drawable.newspaper48;
            JSONObject data = new JSONObject(messageBody);
            if (data.getInt("type") == FCM_TYPE_NOTIFICATION) {
                intent.putExtra(EXTRA_NOTIFICATION_TYPE_KEY,NOTIFICATION_TYPE_NEWS);
                JSONObject notification = data.getJSONObject("notification");
                title = notification.getString("title");
                subtitle = notification.getString("stream");

                // TODO Stub image for now
                streamUrl = "https://www.pexels.com/photo/face-facial-hair-fine-looking-guy-614810/";
//                streamUrl = Utils.getUsableDropboxUrl(event.getJSONObject("author")
//                        .getString("image"));

                description = notification.getString("description");
            } else if (data.getInt("type") == FCM_TYPE_EVENT) {
                intent.putExtra(EXTRA_NOTIFICATION_TYPE_KEY,NOTIFICATION_TYPE_EVENT);
                JSONObject event = data.getJSONObject("event");
                title = event.getString("title");
                subtitle = event.getString("location");
                description = event.getString("description");

                // TODO Stub image for now
                streamUrl = "https://www.pexels.com/photo/face-facial-hair-fine-looking-guy-614810/";
//                streamUrl = Utils.getUsableDropboxUrl(event.getJSONObject("author")
//                        .getString("image"));

            } else {
                return;
            }


            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0
                    /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder;
            try {
                notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(smallIcon)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setTicker(title + " : " + subtitle)
                        .setColor(getResources().getColor(R.color.ColorPrimary))
                        .setLargeIcon(Picasso.with(context).load(streamUrl).get())
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(description))
                        .setContentIntent(pendingIntent);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
                notificationBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(smallIcon)
                        .setContentTitle(title)
                        .setContentText(subtitle)
                        .setTicker(title + " : " + subtitle)
                        .setColor(getResources().getColor(R.color.ColorPrimary))
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.ic_ic_logo))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(description))
                        .setContentIntent(pendingIntent);

            }

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}

