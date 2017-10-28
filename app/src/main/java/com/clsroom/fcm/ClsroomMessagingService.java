package com.clsroom.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.clsroom.MainActivity;
import com.clsroom.R;
import com.clsroom.model.Notifications;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.net.URL;
import java.util.Map;

import static com.clsroom.model.Notifications.DATE_TIME;
import static com.clsroom.model.Notifications.LEAVE_ID;
import static com.clsroom.model.Notifications.SENDER_ID;
import static com.clsroom.model.Notifications.SENDER_NAME;
import static com.clsroom.model.Notifications.SENDER_PHOTO_URL;

public class ClsroomMessagingService extends FirebaseMessagingService
{
    private static String TAG = "ClsroomMessagingService";
    private NotificationManager mNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0)
        {
            Notifications notification = new Notifications();

            notification.setMessage(data.get(Notifications.MESSAGE));
            notification.dateTime(data.get(DATE_TIME));

            notification.setSenderId(data.get(SENDER_ID));
            notification.setSenderName(data.get(SENDER_NAME));
            notification.setSenderPhotoUrl(data.get(SENDER_PHOTO_URL));

            notification.setNotesId(data.get(Notifications.NOTES_ID));
            notification.setLeaveId(data.get(LEAVE_ID));
            notification.setLeaveRefType(data.get(Notifications.LEAVE_REF_TYPE));

            new ShowNormalNotification().execute(notification);
        }
    }

    private class ShowNormalNotification extends AsyncTask<Notifications, Void, Void>
    {
        Notifications mModel;
        Bitmap mLargeIcon;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            Log.d(TAG, "showNotification : onPreExecute");
        }

        @Override
        protected Void doInBackground(Notifications... params)
        {
            Log.d(TAG, "showNotification : doInBackground");
            mModel = params[0];
            mLargeIcon = getCircleBitmapFromUrl(mModel.getSenderPhotoUrl());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            Log.d(TAG, "showNotification : onPostExecute");
            showNotification(mModel, mLargeIcon);
        }
    }

    private void showNotification(Notifications notification, Bitmap mLargeIcon)
    {
        Log.d(TAG, "showNotification : " + notification + ", " + mLargeIcon);
        Intent contentIntent = new Intent(this, MainActivity.class);

        contentIntent.putExtra(MainActivity.NOTIFICATION_OBJECT, notification);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(notification.getSenderName())
                .setSmallIcon(R.mipmap.notebook_placeholder)
                .setAutoCancel(true)
                .setContentText(notification.getMessage())
                .setContentIntent(contentPendingIntent);

        if (mLargeIcon != null)
        {
            mBuilder.setLargeIcon(mLargeIcon);
        }

        Notification notificationDefault = new Notification();
        notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound
        mBuilder.setDefaults(notificationDefault.defaults);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

    private Bitmap getCircleBitmapFromUrl(String photoUrl)
    {
        Log.d(TAG, "showNotification : getCircleBitmapFromUrl");
        try
        {
            URL url = new URL(photoUrl);
            return getSquareBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, " Error : " + e.getMessage());
        }
        return BitmapFactory.decodeResource(this.getResources(), R.mipmap.notes);
    }

    private Bitmap getSquareBitmap(Bitmap srcBmp)
    {
        Log.d(TAG, "showNotification : getSquareBitmap");
        Bitmap dstBmp = Bitmap.createBitmap(
                srcBmp,
                0,
                srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                srcBmp.getWidth(),
                srcBmp.getWidth()
                                           );
        if (srcBmp.getWidth() >= srcBmp.getHeight())
        {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
                                        );

        }
        return Bitmap.createScaledBitmap(dstBmp, 120, 120, true);
    }
}
