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
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class CampaignClassicConfigurationTests {

    private lateinit var extensionApi: ExtensionApi
    private lateinit var event: Event

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
        event = Event.Builder("Test", EventType.CAMPAIGN, EventSource.REQUEST_CONTENT).build()
    }

    // =================================================================================================================
    // trackingEndpointsMap
    // =================================================================================================================

    @Test
    fun trackingEndpointsMap_ValidJsonArray_ReturnsCorrectMap() {
        val mappingJson = """[{"identifier":"inst1","endpoint":"https://ep1.com"},{"identifier":"inst2","endpoint":"https://ep2.com"}]"""
        setConfigurationSharedState(trackingEndpointsMapping = mappingJson)

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertEquals(2, config.trackingEndpointsMap.size)
        Assert.assertEquals("https://ep1.com", config.trackingEndpointsMap["inst1"])
        Assert.assertEquals("https://ep2.com", config.trackingEndpointsMap["inst2"])
    }

    @Test
    fun trackingEndpointsMap_NullMapping_ReturnsEmptyMap() {
        setConfigurationSharedState(trackingEndpointsMapping = null)

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertTrue(config.trackingEndpointsMap.isEmpty())
    }

    @Test
    fun trackingEndpointsMap_BlankMapping_ReturnsEmptyMap() {
        setConfigurationSharedState(trackingEndpointsMapping = "")

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertTrue(config.trackingEndpointsMap.isEmpty())
    }

    @Test
    fun trackingEndpointsMap_MalformedJson_ReturnsEmptyMap() {
        setConfigurationSharedState(trackingEndpointsMapping = "not valid json [[{")

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertTrue(config.trackingEndpointsMap.isEmpty())
    }

    @Test
    fun trackingEndpointsMap_EmptyArray_ReturnsEmptyMap() {
        setConfigurationSharedState(trackingEndpointsMapping = "[]")

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertTrue(config.trackingEndpointsMap.isEmpty())
    }

    @Test
    fun trackingEndpointsMap_ObjectWithMissingIdentifier_SkipsEntry() {
        val mappingJson = """[{"endpoint":"https://ep1.com"},{"identifier":"inst2","endpoint":"https://ep2.com"}]"""
        setConfigurationSharedState(trackingEndpointsMapping = mappingJson)

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertEquals(1, config.trackingEndpointsMap.size)
        Assert.assertEquals("https://ep2.com", config.trackingEndpointsMap["inst2"])
    }

    @Test
    fun trackingEndpointsMap_ObjectWithMissingEndpoint_SkipsEntry() {
        val mappingJson = """[{"identifier":"inst1"},{"identifier":"inst2","endpoint":"https://ep2.com"}]"""
        setConfigurationSharedState(trackingEndpointsMapping = mappingJson)

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertEquals(1, config.trackingEndpointsMap.size)
        Assert.assertEquals("https://ep2.com", config.trackingEndpointsMap["inst2"])
    }

    /**
     * Array elements that are not JSON objects: optJSONObject(i) is null;
     */
    @Test
    fun trackingEndpointsMap_ArrayOfPrimitives_EmptyMap() {
        setConfigurationSharedState(trackingEndpointsMapping = "[1,2,3]")

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertTrue(config.trackingEndpointsMap.isEmpty())
    }

    @Test
    fun trackingEndpointsMap_MixedObjectsAndPrimitives_ParsesOnlyObjects() {
        val mappingJson = """[1,"x",{"identifier":"ok","endpoint":"https://ok.com"}]"""
        setConfigurationSharedState(trackingEndpointsMapping = mappingJson)

        val config = CampaignClassicConfiguration(event, extensionApi)

        Assert.assertEquals(1, config.trackingEndpointsMap.size)
        Assert.assertEquals("https://ok.com", config.trackingEndpointsMap["ok"])
    }

    private fun setConfigurationSharedState(trackingEndpointsMapping: String? = null) {
        val configMap = mutableMapOf<String, Any?>(
            CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_SERVER to "defaultServer"
        )
        if (trackingEndpointsMapping != null) {
            configMap[CampaignClassicConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_ENDPOINT_MAPPING] = trackingEndpointsMapping
        }
        Mockito.`when`(
            extensionApi.getSharedState(
                ArgumentMatchers.eq(CampaignClassicConstants.EventDataKeys.Configuration.EXTENSION_NAME),
                ArgumentMatchers.any(),
                ArgumentMatchers.anyBoolean(),
                ArgumentMatchers.any()
            )
        ).thenReturn(SharedStateResult(SharedStateStatus.SET, configMap))
    }
}
