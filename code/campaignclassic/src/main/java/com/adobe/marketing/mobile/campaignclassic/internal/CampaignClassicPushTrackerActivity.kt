/*
  Copyright 2024 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.campaignclassic.internal

import android.app.Activity
import android.app.RemoteInput
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adobe.marketing.mobile.CampaignClassic
import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilder
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.services.ServiceProvider

internal class CampaignClassicPushTrackerActivity : Activity() {
    private val SELF_TAG = "CampaignClassicPushTrackerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null) {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Intent is null. Ignoring to track or take action on push notification" +
                    " interaction."
            )
            finish()
            return
        }
        val action = intent.action
        if (action.isNullOrEmpty()) {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Intent action is null or empty. Ignoring to track or take action on push" +
                    " notification interaction."
            )
            finish()
            return
        }
        when (action) {
            PushTemplateConstants.NotificationAction.CLICKED -> handlePushClicked(intent)
            PushTemplateConstants.NotificationAction.INPUT_RECEIVED -> handleInputReceived(intent)
            PushTemplateConstants.NotificationAction.DISMISSED -> handleDismissed(intent)
            else -> {}
        }
        finish()
    }

    /**
     * Handles the notification dismissal action
     *
     * @param intent the intent received from dismissing the notification
     */
    private fun handleDismissed(intent: Intent) {
        CampaignClassic.trackNotificationClick(getTrackInfo(intent))

        // remove the notification if tag is available
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: return
        val tag = intent.getStringExtra(PushTemplateConstants.PushPayloadKeys.TAG)
        val notificationManager = NotificationManagerCompat.from(context)
        if (tag.isNullOrEmpty()) {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Unable to remove notification for ${application.packageName}, the tag is null or empty,"
            )
            return
        }
        Log.trace(
            CampaignClassicConstants.LOG_TAG,
            SELF_TAG,
            "Removing notification with tag $tag"
        )
        notificationManager.cancel(tag.hashCode())
    }

    /**
     * Handles input received by input box push template
     *
     * @param intent the intent received from submitting the input in input box push template
     *
     */
    private fun handleInputReceived(intent: Intent) {
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: return
        val notificationManager = NotificationManagerCompat.from(context)
        val tag = intent.extras?.getString(PushTemplateConstants.PushPayloadKeys.TAG)
        if (tag.isNullOrEmpty()) {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Tag is null or empty for the notification with action ${intent.action}," +
                    "default to removing all displayed notifications for ${application.packageName}"
            )
            return
        }
        val results: Bundle? = RemoteInput.getResultsFromIntent(intent)
        if (results != null) {
            val remoteInputResultKey =
                intent.getStringExtra(PushTemplateConstants.PushPayloadKeys.INPUT_BOX_RECEIVER_NAME)
            val inputReplyResult = results.getCharSequence(remoteInputResultKey)
            Log.trace(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Input $inputReplyResult received for notification with tag $tag."
            )
            val intentExtra = Bundle()
            intentExtra.putString(remoteInputResultKey, inputReplyResult.toString())
            openApplication(intentExtra)
        }
        Log.trace(
            CampaignClassicConstants.LOG_TAG,
            SELF_TAG,
            "Recreating notification with tag $tag."
        )
        val builder: NotificationCompat.Builder = NotificationBuilder.constructNotificationBuilder(
            intent,
            CampaignClassicPushTrackerActivity::class.java,
            CampaignClassicPushBroadcastReceiver::class.java
        )
        notificationManager.notify(tag.hashCode(), builder.build())
    }

    /**
     * Handles clicks on push notification.
     *
     * @param intent the intent received from interacting with buttons on push notification
     */
    private fun handlePushClicked(intent: Intent) {
        CampaignClassic.trackNotificationClick(getTrackInfo(intent))
        executePushAction(intent)
    }

    /**
     * Retrieves the Campaign Classic push notification tracking information from the received
     * notification's [Intent]
     *
     * @param intent the intent received from the push notification
     * @return Map<String, String> containing the notification's tracking information
     */
    private fun getTrackInfo(intent: Intent): Map<String, String> {
        val trackInfo = mutableMapOf<String, String>()
        val extras = intent.extras ?: return trackInfo
        for (key in extras.keySet()) {
            val value = extras.getString(key)
            if (value != null) {
                when (key) {
                    CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_MESSAGE_ID,
                    CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_DELIVERY_ID,
                    CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID -> {
                        trackInfo[key] = value
                    }
                }
            }
        }
        return trackInfo
    }

    /**
     * Reads the URI and executes the action that is configured for the push notification.
     *
     *
     * If no URI is configured, by default the application will be opened.
     *
     * @param intent the intent received from the push notification
     */
    private fun executePushAction(intent: Intent) {
        val actionUri =
            intent.getStringExtra(PushTemplateConstants.TrackingKeys.ACTION_URI)
        if (actionUri.isNullOrEmpty()) {
            openApplication(intent.extras)
        } else {
            openUri(actionUri, intent.extras)
        }

        // remove the notification if sticky notifications are false
        val isStickyNotification = intent.getStringExtra(PushTemplateConstants.PushPayloadKeys.STICKY)?.toBoolean() ?: false
        val tag = intent.getStringExtra(PushTemplateConstants.PushPayloadKeys.TAG)
        if (isStickyNotification) {
            Log.trace(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "the sticky notification setting is true, will not remove the notification" +
                    " with tag $tag."
            )
            return
        }
        val context = ServiceProvider.getInstance().appContextService.applicationContext
            ?: return
        val notificationManager = NotificationManagerCompat.from(context)
        if (tag.isNullOrEmpty()) {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "the sticky notification setting is false but the tag is null or empty," +
                    " default to removing all displayed notifications for ${application.packageName}"
            )
            return
        }
        Log.trace(
            CampaignClassicConstants.LOG_TAG,
            SELF_TAG,
            "the sticky notification setting is false, removing notification with tag $tag"
        )
        notificationManager.cancel(tag.hashCode())
    }

    /**
     * Use this method to create an intent to open the application. If the application is already
     * open and in the foreground, the action will resume the current activity.
     *
     * @param intentExtras the extras to add to the intent
     */
    private fun openApplication(intentExtras: Bundle?) {
        val currentActivity = ServiceProvider.getInstance().appContextService.currentActivity
        val launchIntent = if (currentActivity != null) {
            Intent(currentActivity, currentActivity.javaClass)
        } else {
            Log.debug(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "There is no active activity. Starting the launcher Activity."
            )
            packageManager.getLaunchIntentForPackage(packageName)
        }
        if (launchIntent != null) {
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intentExtras?.let { launchIntent.putExtras(intentExtras) }
            startActivity(launchIntent)
        } else {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Unable to create an intent to open the application from the notification" +
                    " interaction."
            )
        }
    }

    /**
     * Use this method to create an intent to open the the provided URI.
     *
     * @param uri the uri to open
     * @param intentExtras the extras to add to the intent
     */
    private fun openUri(uri: String, intentExtras: Bundle?) {
        try {
            val deeplinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            deeplinkIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intentExtras?.let { deeplinkIntent.putExtras(intentExtras) }
            startActivity(deeplinkIntent)
        } catch (e: ActivityNotFoundException) {
            Log.warning(
                CampaignClassicConstants.LOG_TAG,
                SELF_TAG,
                "Unable to open the URI from the notification interaction. URI: $uri"
            )
        }
    }
}
