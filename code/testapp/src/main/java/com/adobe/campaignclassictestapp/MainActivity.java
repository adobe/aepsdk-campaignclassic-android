package com.adobe.campaignclassictestapp;

import com.adobe.marketing.mobile.CampaignClassic;
import com.adobe.marketing.mobile.MobileCore;
import com.adobe.marketing.mobile.MobilePrivacyStatus;
import com.google.firebase.messaging.FirebaseMessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

	private static String LOG_TAG = "MainActivity";
	private static String DELIVERYID = "_dId";
	private static String MESSAGEID = "_mId";
	private static String TRACKING_INSTANCE_ID = "_iNm";
	private TextView notificationPermission;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView accVersion = (TextView) findViewById(R.id.accVersion);
		String version = CampaignClassic.extensionVersion();
		accVersion.setText("Campaign classic version: " + version);

		notificationPermission = (TextView) findViewById(R.id.notificationPermission);

		TextView registrationToken = (TextView) findViewById(R.id.registrationToken);
		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
						registrationToken.setText("Unknown");
						return;
					}

					// Get new FCM registration token
					String token = task.getResult();
					registrationToken.setText(token);
				});

		askNotificationPermission();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("Test", "Running version " +  MobileCore.extensionVersion() + "MobileCore-" + CampaignClassic.extensionVersion() + "CampaignClassicCore");

		// handle push message click through
		if (getIntent().getExtras() != null) {
			Map<String, String> trackInfo = new HashMap<>();

			for (String key : getIntent().getExtras().keySet()) {
				Object value = getIntent().getExtras().get(key);

				if (key.equals(MESSAGEID)) {
					trackInfo.put(key, value.toString());
				} else if (key.equals(DELIVERYID)) {
					trackInfo.put(key, value.toString());
				} else if (key.equals(TRACKING_INSTANCE_ID)) {
					trackInfo.put(key, value.toString());
				}
			}

			CampaignClassic.trackNotificationClick(trackInfo);
		}
	}

	public void registerDeviceSameUser(View view) {
		// manually trigger device registration if needed
		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
						return;
					}

					// Get new FCM registration token
					String token = task.getResult();
					CampaignClassic.registerDevice(token, "ACC-Extension-Test-AndroidUser", null);
				});
	}

	public void registerDeviceNewUser(View view) {
		// manually trigger device registration if needed
		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
						return;
					}

					// Get new FCM registration token
					String token = task.getResult();
					CampaignClassic.registerDevice(token, "ACC-Extension-Test-NewAndroidUser", null);
				});
	}

	public void registerDeviceAdditionalParam(View view) {
		// manually trigger device registration if needed
		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						Log.w("MainActivity", "Fetching FCM registration token failed", task.getException());
						return;
					}

					// Get new FCM registration token
					String token = task.getResult();
					Map<String, Object> additionalParam = new HashMap<>();
					additionalParam.put("firstName", "someFirstName");
					additionalParam.put("lastName", "someLastName");
					additionalParam.put("region", "someRegion");
					additionalParam.put("age", 30);
					additionalParam.put("userId", 123999333);
					additionalParam.put("zipCode", 94403);
					additionalParam.put("testId", "11000.111000.2321321");
					additionalParam.put("newParam", "somethingNew");
					CampaignClassic.registerDevice(token, "ACC-Extension-Test-AndroidUser", additionalParam);
				});
	}

	public void privacyOptIn(View view) {
		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_IN);
	}

	public void privacyOptOut(View view) {
		MobileCore.setPrivacyStatus(MobilePrivacyStatus.OPT_OUT);
	}

	public void privacyOptUnknown(View view) {
		MobileCore.setPrivacyStatus(MobilePrivacyStatus.UNKNOWN);
	}

	private void askNotificationPermission() {
		// This is only necessary for API level >= 33 (TIRAMISU)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
					PackageManager.PERMISSION_GRANTED) {
				// FCM SDK (and your app) can post notifications.
				notificationPermission.setText("Notification Permission: Authorized");
			} else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
				notificationPermission.setText("Notification Permission: Not granted");
				showNotificationPermissionRationale();
			} else {
				// Directly ask for the permission
				Log.d(LOG_TAG, "Requesting notification permission");
				requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
			}
		} else {
			notificationPermission.setText("Notification Permission: Authorized");
		}
	}

	private final ActivityResultLauncher<String> requestPermissionLauncher =
			registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
				if (isGranted) {
					// FCM SDK (and your app) can post notifications.
					Log.d(LOG_TAG, "Notification permission granted");
					Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
					notificationPermission.setText("Notification Permission: Authorized");
				} else {
					if (Build.VERSION.SDK_INT >= 33) {
						if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
							notificationPermission.setText("Notification Permission: Not granted");
							showNotificationPermissionRationale();
						} else {
							Toast.makeText(this, "Grant notification permission from settings", Toast.LENGTH_SHORT).show();
							notificationPermission.setText("Notification Permission: Not granted");
						}
					}
				}
			});


	private void showNotificationPermissionRationale() {
		new AlertDialog.Builder(this)
				.setTitle("Grant notification permission")
				.setMessage("Notification permission is required to show notifications")
				.setPositiveButton("Ok", (dialog, which) -> {
					if (Build.VERSION.SDK_INT >= 33) {
						requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
					}
				})
				.setNegativeButton("Cancel", null)
				.show();
	}
}
