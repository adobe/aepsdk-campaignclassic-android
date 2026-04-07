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

import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class EventExtensionsTests {

    @Test
    fun trackingInstanceId_NonBlank_ReturnsValue() {
        val event = eventWithTrackInfo(
            mapOf(
                CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID to "instanceA"
            )
        )

        Assert.assertEquals("instanceA", event.trackingInstanceId)
    }

    @Test
    fun trackingInstanceId_BlankOrMissing_ReturnsNull() {
        val eventBlank = eventWithTrackInfo(
            mapOf(
                CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID to "   "
            )
        )
        Assert.assertNull(eventBlank.trackingInstanceId)

        val eventMissing = eventWithTrackInfo(emptyMap())
        Assert.assertNull(eventMissing.trackingInstanceId)
    }

    private fun eventWithTrackInfo(trackInfo: Map<String, String>): Event {
        return Event.Builder("t", EventType.CAMPAIGN, EventSource.REQUEST_CONTENT)
            .setEventData(
                mapOf(
                    CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO to trackInfo
                )
            )
            .build()
    }
}
