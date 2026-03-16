/*
  Copyright 2022 Adobe. All rights reserved.
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
import com.adobe.marketing.mobile.util.DataReader

/**
 * @return true if this event is a Campaign Classic register event
 */
internal val Event.isRegisterEvent: Boolean
    get() = DataReader.optBoolean(
        eventData,
        CampaignClassicConstants.EventDataKeys.CampaignClassic.REGISTER_DEVICE,
        false
    )

/**
 * @return true if this event is a Campaign Classic TrackNotificationClick event
 */
internal val Event.isTrackClickEvent: Boolean
    get() = DataReader.optBoolean(
        eventData,
        CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_CLICK,
        false
    )

/**
 * @return true if this event is a Campaign Classic TrackNotificationReceive event
 */
internal val Event.isTrackReceiveEvent: Boolean
    get() = DataReader.optBoolean(
        eventData,
        CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_RECEIVE,
        false
    )

/**
 * @return messageId [String] from the event data if available and not empty, null otherwise
 */
internal val Event.messageId: String?
    get() {
        val messageId = DataReader.optString(
            trackingInfo,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_MESSAGE_ID,
            ""
        )
        return if (messageId.isNullOrBlank()) {
            null
        } else {
            messageId
        }
    }

/**
 * @return deliveryId [String] from the event data if available and not empty, null otherwise
 */
internal val Event.deliveryId: String?
    get() {
        val deliveryId = DataReader.optString(
            trackingInfo,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_DELIVERY_ID,
            ""
        )
        return if (deliveryId.isNullOrBlank()) {
            null
        } else {
            deliveryId
        }
    }

internal val Event.trackingInstanceId: String?
    get() {
        val trackingInstanceId = DataReader.optString(
            trackingInfo,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_TRACKING_INSTANCE_ID,
            ""
        )
        return if (trackingInstanceId.isNullOrBlank()) {
            null
        } else {
            trackingInstanceId
        }
    }

/**
 * @return deviceToken [String] from the event data if available and not empty, null otherwise
 */
internal val Event.deviceToken: String?
    get() {
        val deviceToken = DataReader.optString(
            eventData,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.DEVICE_TOKEN,
            null
        )
        return if (deviceToken.isNullOrBlank()) {
            null
        } else {
            deviceToken
        }
    }

/**
 * @return userKey [String] from the event data if available and not empty, null otherwise
 */
internal val Event.userKey: String?
    get() {
        val userKey = DataReader.optString(
            eventData,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.USER_KEY,
            null
        )
        return if (userKey.isNullOrBlank()) {
            null
        } else {
            userKey
        }
    }

/**
 * @return additionalParameters Map<String, Any> from event data if available, empty map otherwise
 */

internal val Event.additionalParameters: Map<String, Any>
    get() {
        val additionalParameters = DataReader.optTypedMap(
            Any::class.java,
            eventData,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.ADDITIONAL_PARAMETERS,
            null
        )
        return if (additionalParameters.isNullOrEmpty()) {
            emptyMap()
        } else {
            additionalParameters
        }
    }

/**
 * @return tracking information Map<String, String> from event data if available, null otherwise
 */
private val Event.trackingInfo: Map<String, String>?
    get() = DataReader.optStringMap(
        eventData,
        CampaignClassicConstants.EventDataKeys.CampaignClassic.TRACK_INFO,
        null
    )

/**
 * @return registrationStatus [Boolean] from the event data if available and not empty, false otherwise
 */
internal val Event.registrationStatus: Boolean
    get() {
        return DataReader.optBoolean(
            eventData,
            CampaignClassicConstants.EventDataKeys.CampaignClassic.REGISTRATION_STATUS,
            false
        )
    }
