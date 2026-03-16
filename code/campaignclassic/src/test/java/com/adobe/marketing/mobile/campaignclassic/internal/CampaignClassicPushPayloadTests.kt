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

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class CampaignClassicPushPayloadTests {

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
