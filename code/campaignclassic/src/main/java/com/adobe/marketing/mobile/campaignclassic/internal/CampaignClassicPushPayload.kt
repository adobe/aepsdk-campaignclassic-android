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

import com.adobe.marketing.mobile.notificationbuilder.NotificationPriority
import com.adobe.marketing.mobile.notificationbuilder.NotificationVisibility
import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.adobe.marketing.mobile.util.DataReader
import com.google.firebase.messaging.RemoteMessage

internal class CampaignClassicPushPayload {
    val messageData: MutableMap<String?, String?>
    val messageId: String?
    val deliveryId: String?
    val trackingInstanceId: String?
    var tag: String?

    private val ACC_PAYLOAD_BODY = "_msg"

    /**
     * Constructor
     *
     *
     * Provides the CampaignClassicPushPayload object
     *
     * @param message [RemoteMessage] object received from [com.google.firebase.messaging.FirebaseMessagingService]
     * @throws IllegalArgumentException if the message, message data, message id, or delivery id is
     * null
     */
    @Throws(IllegalArgumentException::class)
    constructor(message: RemoteMessage?) : this(message?.data) {
        // migrate any ACC push notification object payload keys if needed
        val notification = message?.notification ?: return
        convertNotificationPayloadData(notification)
    }

    /**
     * Constructor
     *
     *
     * Provides the CampaignClassicPushPayload object
     *
     * @param remoteMessageData Map<String, String> containing the message data present in a
     * notification received from [com.google.firebase.messaging.FirebaseMessagingService]
     * @throws IllegalArgumentException if the message data, message id, or delivery id is null
     */
    @Throws(IllegalArgumentException::class)
    constructor(remoteMessageData: Map<String, String>?) {
        if (remoteMessageData.isNullOrEmpty()) {
            throw IllegalArgumentException(
                "Failed to create CampaignClassicPushPayload, remote message data payload is null or empty."
            )
        }
        messageId = remoteMessageData[CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_MESSAGE_ID]
        if (messageId.isNullOrEmpty()) {
            throw IllegalArgumentException("Failed to create CampaignClassicPushPayload, message id is null or empty.")
        }
        deliveryId = remoteMessageData[CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_DELIVERY_ID]
        if (deliveryId.isNullOrEmpty()) {
            throw IllegalArgumentException("Failed to create CampaignClassicPushPayload, delivery id is null or empty.")
        }
        trackingInstanceId = remoteMessageData[CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID]

        this.messageData = remoteMessageData.toMutableMap()

        // get the tag from the payload. if no tag was present in the payload use the message id
        // instead as its guaranteed to always be present.
        if (!(messageData[PushTemplateConstants.PushPayloadKeys.TAG]).isNullOrEmpty()) {
            tag = messageData[PushTemplateConstants.PushPayloadKeys.TAG]
        } else {
            messageData[PushTemplateConstants.PushPayloadKeys.TAG] = messageId
            tag = messageId
        }

        // convert _msg to adb_body if needed
        if ((messageData[PushTemplateConstants.PushPayloadKeys.BODY]).isNullOrEmpty()) {
            this.messageData[PushTemplateConstants.PushPayloadKeys.BODY] = DataReader.optString(messageData, ACC_PAYLOAD_BODY, null)
        }
    }

    private fun convertNotificationPayloadData(notification: RemoteMessage.Notification) {
        // Migrate the 13 ACC KVP to "adb" prefixed keys.
        // Note, the key value pairs present in the data payload are preferred over the notification
        // key value pairs.
        // The notification key value pairs will only be added to the message data if the
        // corresponding key
        // does not have a value.
        // message.android.notification.icon to adb_small_icon
        // message.android.notification.sound to adb_sound
        // message.android.notification.tag	to adb_tag
        // message.android.notification.click_action to adb_uri
        // message.android.notification.channel_id to adb_channel_id
        // message.android.notification.ticker to adb_ticker (NEW)
        // message.android.notification.sticky to adb_sticky (NEW)
        // message.android.notification.visibility to adb_n_visibility
        // message.android.notification.notification_priority to adb_n_priority
        // message.android.notification.notification_count to adb_n_count
        // message.notification.body to adb_body
        // message.notification.title to adb_title
        // message.notification.image to adb_image
        if ((messageData[PushTemplateConstants.PushPayloadKeys.TAG]).isNullOrEmpty()) {
            tag = notification.tag
            messageData[PushTemplateConstants.PushPayloadKeys.TAG] = tag
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.SMALL_ICON]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.SMALL_ICON] = notification.icon
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.SOUND]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.SOUND] = notification.sound
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.ACTION_URI]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.ACTION_URI] = notification.clickAction
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.CHANNEL_ID]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.CHANNEL_ID] = notification.channelId
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.TICKER]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.TICKER] = notification.ticker
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.STICKY]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.STICKY] =
                notification.sticky.toString()
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.VISIBILITY]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.VISIBILITY] =
                NotificationVisibility.fromValue(notification.visibility).stringValue
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.PRIORITY]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.PRIORITY] =
                NotificationPriority.fromValue(notification.notificationPriority).stringValue
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.BADGE_COUNT]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.BADGE_COUNT] =
                notification.notificationCount.toString()
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.BODY]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.BODY] = notification.body
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.TITLE]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.TITLE] = notification.title
        }
        if ((messageData[PushTemplateConstants.PushPayloadKeys.IMAGE_URL]).isNullOrEmpty()) {
            messageData[PushTemplateConstants.PushPayloadKeys.IMAGE_URL] =
                notification.imageUrl.toString()
        }
    }
}
