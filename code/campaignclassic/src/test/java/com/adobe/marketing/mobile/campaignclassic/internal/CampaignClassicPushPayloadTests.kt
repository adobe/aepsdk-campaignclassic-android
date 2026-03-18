/*
  Copyright 2026 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile.campaignclassic.internal

import com.adobe.marketing.mobile.notificationbuilder.PushTemplateConstants
import com.google.firebase.messaging.RemoteMessage
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class CampaignClassicPushPayloadTests {

    // =================================================================================================================
    // Map constructor validation and error scenarios
    // =================================================================================================================

    @Test
    fun constructor_NullRemoteMessageData_Throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            @Suppress("USELESS_CAST")
            CampaignClassicPushPayload(null as Map<String, String>?)
        }
    }

    @Test
    fun constructor_EmptyRemoteMessageData_Throws() {
        Assert.assertThrows(IllegalArgumentException::class.java) {
            CampaignClassicPushPayload(emptyMap())
        }
    }

    @Test
    fun constructor_MissingMessageId_Throws() {
        val data = mutableMapOf(
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_DELIVERY_ID to "d1"
        )
        Assert.assertThrows(IllegalArgumentException::class.java) {
            CampaignClassicPushPayload(data)
        }
    }

    @Test
    fun constructor_MissingDeliveryId_Throws() {
        val data = mutableMapOf(
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_MESSAGE_ID to "m1"
        )
        Assert.assertThrows(IllegalArgumentException::class.java) {
            CampaignClassicPushPayload(data)
        }
    }

    @Test
    fun constructor_WithTagInPayload_UsesTagFromData() {
        val data = minimalPayloadData().apply {
            put(PushTemplateConstants.PushPayloadKeys.TAG, "myTag")
        }
        val payload = CampaignClassicPushPayload(data)
        Assert.assertEquals("myTag", payload.tag)
        Assert.assertEquals("myTag", payload.messageData[PushTemplateConstants.PushPayloadKeys.TAG])
    }

    @Test
    fun constructor_WithoutTag_UsesMessageIdAsTag() {
        val payload = CampaignClassicPushPayload(minimalPayloadData())
        Assert.assertEquals("msg123", payload.tag)
        Assert.assertEquals("msg123", payload.messageData[PushTemplateConstants.PushPayloadKeys.TAG])
    }

    @Test
    fun constructor_MsgKeyCopiedToBodyWhenBodyMissing() {
        val data = minimalPayloadData().apply {
            put("_msg", "bodyFromMsg")
        }
        val payload = CampaignClassicPushPayload(data)
        Assert.assertEquals("bodyFromMsg", payload.messageData[PushTemplateConstants.PushPayloadKeys.BODY])
    }

    // =================================================================================================================
    // RemoteMessage constructor (property init)
    // =================================================================================================================

    @Test
    fun constructor_FromRemoteMessage_DataOnly_DelegatesToMapConstructor() {
        val rm = Mockito.mock(RemoteMessage::class.java)
        Mockito.`when`(rm.data).thenReturn(HashMap(minimalPayloadData()))
        Mockito.`when`(rm.notification).thenReturn(null)

        val payload = CampaignClassicPushPayload(rm)

        Assert.assertEquals("msg123", payload.messageId)
        Assert.assertEquals("del456", payload.deliveryId)
    }

    /**
     * Map constructor runs before [convertNotificationPayloadData]. If data has no TAG, tag is set to
     * messageId there, so notification.tag is never applied (TAG is no longer empty when merging).
     */
    @Test
    fun constructor_FromRemoteMessage_WithNotification_TagDefaultsToMessageIdBeforeNotificationMerge() {
        val data = HashMap(minimalPayloadData())
        data.remove(PushTemplateConstants.PushPayloadKeys.TAG)
        val rm = Mockito.mock(RemoteMessage::class.java)
        Mockito.`when`(rm.data).thenReturn(data)
        val notif = Mockito.mock(RemoteMessage.Notification::class.java)
        Mockito.`when`(notif.tag).thenReturn("notifTag")
        Mockito.`when`(rm.notification).thenReturn(notif)

        val payload = CampaignClassicPushPayload(rm)

        Assert.assertEquals("msg123", payload.tag)
    }

    @Test
    fun constructor_FromRemoteMessage_WithNotification_MergesBodyWhenDataBodyEmpty() {
        val data = HashMap(minimalPayloadData())
        data.remove(PushTemplateConstants.PushPayloadKeys.BODY)
        data.remove("_msg")
        val rm = Mockito.mock(RemoteMessage::class.java)
        Mockito.`when`(rm.data).thenReturn(data)
        val notif = Mockito.mock(RemoteMessage.Notification::class.java)
        Mockito.`when`(notif.body).thenReturn("bodyFromNotification")
        Mockito.`when`(rm.notification).thenReturn(notif)

        val payload = CampaignClassicPushPayload(rm)

        Assert.assertEquals("bodyFromNotification", payload.messageData[PushTemplateConstants.PushPayloadKeys.BODY])
    }

    // =================================================================================================================
    // trackingInstanceId (_iNm) parsing
    // =================================================================================================================

    @Test
    fun constructor_WithTrackingInstanceId_SetsTrackingInstanceId() {
        val data = minimalPayloadData().apply {
            put(CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID, "instance1")
        }

        val payload = CampaignClassicPushPayload(data)

        Assert.assertEquals("instance1", payload.trackingInstanceId)
    }

    @Test
    fun constructor_WithoutTrackingInstanceId_TrackingInstanceIdIsNull() {
        val data = minimalPayloadData()

        val payload = CampaignClassicPushPayload(data)

        Assert.assertNull(payload.trackingInstanceId)
    }

    @Test
    fun constructor_WithEmptyTrackingInstanceId_StoresEmptyString() {
        val data = minimalPayloadData().apply {
            put(CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID, "")
        }

        val payload = CampaignClassicPushPayload(data)

        Assert.assertEquals("", payload.trackingInstanceId)
    }

    @Test
    fun constructor_WithBlankTrackingInstanceId_StoresAsIs() {
        val data = minimalPayloadData().apply {
            put(CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID, "  ")
        }

        val payload = CampaignClassicPushPayload(data)

        Assert.assertEquals("  ", payload.trackingInstanceId)
    }

    private fun minimalPayloadData(): MutableMap<String, String> {
        return mutableMapOf(
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_MESSAGE_ID to "msg123",
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_DELIVERY_ID to "del456"
        )
    }
}
