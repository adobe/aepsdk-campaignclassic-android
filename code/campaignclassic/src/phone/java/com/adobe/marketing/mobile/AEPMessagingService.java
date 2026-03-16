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

package com.adobe.marketing.mobile;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.adobe.marketing.mobile.campaignclassic.internal.CampaignClassicPushBroadcastReceiver;
import com.adobe.marketing.mobile.campaignclassic.internal.CampaignClassicPushPayload;
import com.adobe.marketing.mobile.campaignclassic.internal.CampaignClassicPushTrackerActivity;
import com.adobe.marketing.mobile.notificationbuilder.NotificationBuilder;
import com.adobe.marketing.mobile.notificationbuilder.NotificationConstructionFailedException;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.StringUtils;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is the entry point for all Adobe Campaign Classic out-of-the-box push template
 * notifications received from Firebase.
 */
public class AEPMessagingService {

    private static final String LOG_TAG = "CampaignClassicExtension";
    static final String SELF_TAG = "AEPMessagingService";
    static final String TRACK_INFO_KEY_MESSAGE_ID = "_mId";
    static final String TRACK_INFO_KEY_DELIVERY_ID = "_dId";
    static final String TRACK_INFO_KEY_TRACKING_INSTANCE_ID = "_iNm";

    /**
     * Builds an {@link CampaignClassicPushPayload} then constructs a {@link Notification} using the
     * {@code RemoteMessage} payload. The built notification is then passed to the {@link
     * NotificationManagerCompat} to be displayed. If any exceptions are thrown when building the
     * {@code AEPPushPayload} or {@code Notification}, this method will return false signaling that
     * the remote message was not handled by the {@code AEPMessagingService}.
     *
     * @param context the application {@link Context}
     * @param remoteMessage the {@link RemoteMessage} containing a push notification payload
     * @return {@code boolean} signaling if the {@link AEPMessagingService} handled the remote
     *     message
     */
    public static boolean handleRemoteMessage(
            @NonNull final Context context, @NonNull final RemoteMessage remoteMessage) {
        final NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);
        CampaignClassicPushPayload payload;
        try {
            payload = new CampaignClassicPushPayload(remoteMessage);
            final String tag = payload.getTag();

            // this should never happen since tag is set to _mId in the push message
            if (StringUtils.isNullOrEmpty(tag)) {
                Log.warning(
                        LOG_TAG,
                        SELF_TAG,
                        "Failed to create a push notification, the notification tag is null or"
                                + " empty.");
                return false;
            }
            final NotificationCompat.Builder notificationBuilder =
                    NotificationBuilder.constructNotificationBuilder(
                            payload.getMessageData(),
                            CampaignClassicPushTrackerActivity.class,
                            CampaignClassicPushBroadcastReceiver.class);
            notificationManager.notify(tag.hashCode(), notificationBuilder.build());
        } catch (final IllegalArgumentException exception) {
            Log.error(
                    LOG_TAG,
                    SELF_TAG,
                    "Failed to create a push notification, an illegal argument exception occurred:"
                            + " %s",
                    exception.getLocalizedMessage());
            return false;
        } catch (final NotificationConstructionFailedException exception) {
            Log.error(
                    LOG_TAG,
                    SELF_TAG,
                    "Failed to create a push notification, a notification construction failed"
                            + " exception occurred: %s",
                    exception.getLocalizedMessage());
            return false;
        }
        trackNotificationReceive(payload);
        return true;
    }

    private static void trackNotificationReceive(final CampaignClassicPushPayload payload) {
        Log.trace(
                LOG_TAG,
                SELF_TAG,
                "Received push payload is valid, sending notification receive track request.");
        final Map<String, String> trackInfo =
                new HashMap<String, String>() {
                    {
                        put(TRACK_INFO_KEY_MESSAGE_ID, payload.getMessageId());
                        put(TRACK_INFO_KEY_DELIVERY_ID, payload.getDeliveryId());
                        put(TRACK_INFO_KEY_TRACKING_INSTANCE_ID, payload.getTrackingInstanceId());
                    }
                };
        CampaignClassic.trackNotificationReceive(trackInfo);
    }
}
