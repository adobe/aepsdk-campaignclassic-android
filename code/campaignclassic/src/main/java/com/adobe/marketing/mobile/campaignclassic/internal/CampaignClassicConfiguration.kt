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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SharedStateResolution
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.util.DataReader
import com.adobe.marketing.mobile.util.DataReaderException
import org.json.JSONArray

internal data class CampaignClassicConfiguration(val event: Event, val extensionApi: ExtensionApi) {

    private val configSharedState: Map<String, Any>? = extensionApi.getSharedState(
        CampaignClassicConstants.EventDataKeys.Configuration.EXTENSION_NAME,
        event,
        false,
        SharedStateResolution.ANY
    )?.value

    /**
     * @return configured CampaignClassics marketing server [String] if available, not null, not empty
     * and of type String, null otherwise
     */
    val marketingServer: String?
        get() {
            val marketingServer = DataReader.optString(
                configSharedState,
                CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_MARKETING_SERVER,
                null
            )
            return if (marketingServer.isNullOrBlank()) {
                null
            } else {
                marketingServer
            }
        }

    /**
     * @return configured CampaignClassics integration key [String] if available, not null and not empty,
     * and of type String, null otherwise
     */
    val integrationKey: String?
        get() {
            val integrationKey = DataReader.optString(
                configSharedState,
                CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_APP_INTEGRATION_KEY,
                null
            )
            return if (integrationKey.isNullOrBlank()) {
                null
            } else {
                integrationKey
            }
        }

    /**
     * @return configured CampaignClassics tracking server [String] if available, not null and not empty,
     * and of type String, null otherwise
     */
    val trackingServer: String?
        get() {
            val trackingServer = DataReader.optString(
                configSharedState,
                CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_SERVER,
                null
            )
            return if (trackingServer.isNullOrBlank()) {
                null
            } else {
                trackingServer
            }
        }

    /**
     * @return configured CampaignClassics tracking server [String] if available, not null and not empty,
     * and of type String, null otherwise
     */
    val trackingEndpointsMapping: String?
        get() {
            val trackingEndpointsMapping = DataReader.optString(
                configSharedState,
                CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_ENDPOINT_MAPPING,
                null
            )
            return if (trackingEndpointsMapping.isNullOrBlank()) {
                null
            } else {
                trackingEndpointsMapping
            }
        }

    val trackingEndpointsMap: Map<String, String>
        get() {
            val result = mutableMapOf<String, String>()
            val mappingStr = trackingEndpointsMapping
            if (mappingStr.isNullOrBlank()) return result
            try {
                val jsonArray = JSONArray(mappingStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.optJSONObject(i)
                    val identifier = obj?.optString(CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_OBJECT_ID)
                    val endpoint = obj?.optString(CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_OBJECT_ENDPOINT)
                    if (!identifier.isNullOrBlank() && !endpoint.isNullOrBlank()) {
                        result[identifier] = endpoint
                    }
                }
            } catch (e: Exception) {
                // Malformed JSON, return empty map
                Log.debug(CampaignClassicConstants.LOG_TAG, "CampaignClassicConfiguration", "trackingEndpointsMappingMap: Malformed JSON")
            }
            return result
        }

    /**
     * @return the configured CampaignClassics network timeout [Int] if available and of type Int,
     * default timeout otherwise
     */
    val timeout: Int
        get() {
            return DataReader.optInt(
                configSharedState,
                CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TIMEOUT,
                CampaignClassicConstants.DEFAULT_TIMEOUT
            )
        }

    /**
     * @return configured [MobilePrivacyStatus] if available and of type String,
     * default [MobilePrivacyStatus.UNKNOWN] otherwise
     */
    val privacyStatus: MobilePrivacyStatus
        get() {
            return try {
                MobilePrivacyStatus.fromString(
                    DataReader.getString(
                        configSharedState,
                        CampaignClassicConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY
                    )
                )
            } catch (e: DataReaderException) {
                MobilePrivacyStatus.UNKNOWN
            }
        }
}
