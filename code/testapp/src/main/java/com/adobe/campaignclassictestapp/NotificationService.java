package com.adobe.campaignclassictestapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.adobe.marketing.mobile.AEPMessagingService;
import com.adobe.marketing.mobile.CampaignClassic;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NotificationService extends FirebaseMessagingService {

	private static String LOG_TAG = "NotificationService";
	private static String DELIVERYID = "_dId";
	private static String MESSAGEID = "_mId";
	private static String NOTIFICATION_TEXT = "notificationText";
	private static String NOTIFICATION_URL = "NotificationUrl";

	@Override
	public void onNewToken(@NonNull String token) {
		// Get updated InstanceID token.
		super.onNewToken(token);
		Log.d(LOG_TAG, "Refreshed firebase token: " + token);

		CampaignClassic.registerDevice(token, "ACC-Extension-Test-AndroidUser", null);
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Log.d(LOG_TAG, "From: " + remoteMessage.getFrom());
		if (AEPMessagingService.handleRemoteMessage(this, remoteMessage)) {
			// Campaign extension has handled the notification
		} else {
			// Handle notification from other sources
			displayNotification(remoteMessage);
		}
	}

	private void displayNotification(RemoteMessage remoteMessage) {
		String channelId = "my_package_channel_1";
		if (remoteMessage.getNotification() != null && remoteMessage.getNotification().getChannelId()!=null) {
			channelId = remoteMessage.getNotification().getChannelId();
		}
		String attachmentUrl = remoteMessage.getData().get("attachment-url");
		Bitmap downloadedImage = null;

		// track message receive here
		Map<String, String> payloadData = remoteMessage.getData();
		String message = payloadData.get("_msg");
		String url = payloadData.get("url");
		String messageId = payloadData.get(MESSAGEID);
		String deliveryId = payloadData.get(DELIVERYID);
		Map<String, String> trackInfo = new HashMap<>();
		trackInfo.put(MESSAGEID, messageId);
		trackInfo.put(DELIVERYID, deliveryId);
		CampaignClassic.trackNotificationReceive(trackInfo);


		//download image from push message if it exists
		if (attachmentUrl != null) {
			downloadedImage = getBitmapFromUrl(attachmentUrl);
		}

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// set channel id for Android 8.0 and above
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel mChannel = new NotificationChannel(channelId,  "acc push channel", NotificationManager.IMPORTANCE_HIGH);
			notificationManager.createNotificationChannel(mChannel);
		}

		// setup intent for click through
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(NOTIFICATION_TEXT, message);
		intent.putExtra(NOTIFICATION_URL, url);
		intent.putExtra(DELIVERYID, deliveryId);
		intent.putExtra(MESSAGEID, messageId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
		} else {
			pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}

		String title = "Push Message Received";
		if (remoteMessage.getNotification() != null) {
			title = remoteMessage.getNotification().getTitle();

		}
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
				.setSmallIcon(android.R.drawable.ic_popup_reminder)
				.setContentTitle(title)
				.setContentText(message)
				.setContentIntent(pendingIntent)
				.setPriority(Notification.PRIORITY_DEFAULT);

		if (downloadedImage != null) {
			builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(downloadedImage));
		}

		notificationManager.notify(1002, builder.build());
	}

	private Bitmap getBitmapFromUrl(String imageUrl) {
		try {
			URL url = new URL(imageUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap bitmap = BitmapFactory.decodeStream(input);
			return bitmap;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}