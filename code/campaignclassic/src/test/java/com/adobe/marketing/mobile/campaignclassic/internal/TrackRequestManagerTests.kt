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
import com.adobe.marketing.mobile.EventSource
import com.adobe.marketing.mobile.EventType
import com.adobe.marketing.mobile.ExtensionApi
import com.adobe.marketing.mobile.MobilePrivacyStatus
import com.adobe.marketing.mobile.SharedStateResult
import com.adobe.marketing.mobile.SharedStateStatus
import com.adobe.marketing.mobile.services.HttpConnecting
import com.adobe.marketing.mobile.services.NetworkCallback
import com.adobe.marketing.mobile.services.NetworkRequest
import com.adobe.marketing.mobile.services.Networking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.net.HttpURLConnection

@RunWith(MockitoJUnitRunner.Silent::class)
class TrackRequestManagerTests {

    private lateinit var extensionApi: ExtensionApi
    private lateinit var networkService: Networking
    private lateinit var trackManager: TrackRequestManager

    @Before
    fun setup() {
        extensionApi = Mockito.mock(ExtensionApi::class.java)
        networkService = Mockito.mock(Networking::class.java)
        trackManager = TrackRequestManager(extensionApi, networkService)
    }

    // =================================================================================================================
    // void handleTrackRequest(final Event event, final String tagId)
    // =================================================================================================================

    @Test
    fun handleTrackRequest_NotificationReceived_happyV7MessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x",12345)},testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationClicked_happyV7MessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x",12345)},testDeliveryId,2"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationClicked_NetworkError() {
        // setup
        setConfigurationSharedState()
        val connection = Mockito.mock(HttpConnecting::class.java)
        Mockito.`when`(connection.responseCode).thenReturn(HttpURLConnection.HTTP_OK)
        Mockito.`when`(networkService.connectAsync(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenAnswer { invocation ->
                (invocation.arguments[1] as NetworkCallback).call(connection)
                null
            }

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify network call
        Mockito.verify(networkService, Mockito.times(1)).connectAsync(ArgumentMatchers.any(), ArgumentMatchers.any())
    }

    @Test
    fun handleTrackRequest_NotificationClicked_ConfigurationNotSet() {
        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_PrivacyStatusIsOptOut() {
        // setup
        setConfigurationSharedState(privacyStatus = MobilePrivacyStatus.OPT_OUT)

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_PrivacyStatusIsUnknown() {
        // setup
        setConfigurationSharedState(privacyStatus = MobilePrivacyStatus.UNKNOWN)

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_NullTrackingServer() {
        // setup
        setConfigurationSharedState(trackingServer = null)

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_EmptyTrackingServer() {
        // setup
        setConfigurationSharedState(trackingServer = "")

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_NullTrackInfo() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(trackInfo = null),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_EmptyTrackInfo() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(trackInfo = emptyMap()),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_NotificationReceived_happyV8UUIDMessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "6b6499a8-9d43-4bc5-acf0-b6aeb96846f6"),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h6b6499a8-9d43-4bc5-acf0-b6aeb96846f6,testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationClicked_happyV8UUIDMessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "6b6499a8-9d43-4bc5-acf0-b6aeb96846f6"),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h6b6499a8-9d43-4bc5-acf0-b6aeb96846f6,testDeliveryId,2"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_TrackInfoHasInvalidMessageUUID() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "1-1-1-1-1"),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_TrackInfoHasInvalidMessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "a8a"),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_TrackInfoNullMessageIdKey() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = null),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_TrackInfoEmptyMessageIdKey() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = ""),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_TrackInfoNullDeliveryIdKey() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(deliveryId = null),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_TrackInfoEmptyDeliveryIdKey() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(deliveryId = ""),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify no network call
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_NetworkServiceNotAvailable() {
        // setup
        setConfigurationSharedState()

        // test
        TrackRequestManager(extensionApi, null).handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify
        Mockito.verifyNoInteractions(networkService)
    }

    @Test
    fun handleTrackRequest_NetworkError() {
        // setup
        setConfigurationSharedState()

        val connection = Mockito.mock(HttpConnecting::class.java)
        Mockito.`when`(connection.responseCode).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST)
        Mockito.`when`(networkService.connectAsync(ArgumentMatchers.any(), ArgumentMatchers.any())).thenAnswer { invocation ->
            (invocation.arguments[1] as NetworkCallback).call(connection)
            null
        }

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify network call
        Mockito.verify(networkService, Mockito.times(1)).connectAsync(ArgumentMatchers.any(), ArgumentMatchers.any())
    }

    @Test
    fun handleTrackRequest_CustomTimeoutSet() {
        // setup
        setConfigurationSharedState(timeout = 20)

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(),
            CampaignClassicTestConstants.MESSAGE_CLICKED_TAGID
        )

        // verify network call is still made with correct payload
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1)).connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())
        Assert.assertEquals(20, networkRequestCaptor.value.connectTimeout)
        Assert.assertEquals(20, networkRequestCaptor.value.readTimeout)
    }

    // =================================================================================================================
    // 32-bit negative and 64-bit message id tests
    // =================================================================================================================
    @Test
    fun handleTrackRequest_NotificationReceived_Negative32BitMessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "-673096100"),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x", -673096100)},testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationReceived_32BitMessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "673096100"),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x", 673096100)},testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationReceived_64BitMessageId() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "57904851586134"),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x", 57904851586134)},testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationReceived_64BitMessageId_MinimumValue() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "2147483648"),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x", 2147483648)},testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    @Test
    fun handleTrackRequest_NotificationReceived_64BitMessageId_MaxValue() {
        // setup
        setConfigurationSharedState()

        // test
        trackManager.handleTrackRequest(
            getTrackRequestEvent(messageId = "9223372036854775807"),
            CampaignClassicTestConstants.MESSAGE_RECEIVED_TAGID
        )

        // verify network call
        val networkRequestCaptor = ArgumentCaptor.forClass(NetworkRequest::class.java)
        Mockito.verify(networkService, Mockito.times(1))
            .connectAsync(networkRequestCaptor.capture(), ArgumentMatchers.any())

        // verify url
        val expectedUrl = "https://testTrackingServer/r/?id=h${java.lang.String.format("%x", 9223372036854775807L)},testDeliveryId,1"
        Assert.assertEquals(expectedUrl, networkRequestCaptor.value.url)
        Assert.assertEquals(CampaignClassicTestConstants.DEFAULT_TIMEOUT, networkRequestCaptor.value.connectTimeout)
        Assert.assertNull(networkRequestCaptor.value.body)
    }

    // =================================================================================================================
    // private methods
    // =================================================================================================================

    private fun setConfigurationSharedState(
        trackingServer: String? = "testTrackingServer",
        privacyStatus: MobilePrivacyStatus = MobilePrivacyStatus.OPT_IN,
        timeout: Int = CampaignClassicTestConstants.DEFAULT_TIMEOUT
    ) {
        Mockito.`when`(
            extensionApi.getSharedState(
                ArgumentMatchers.eq(CampaignClassicTestConstants.EventDataKeys.Configuration.EXTENSION_NAME),
                ArgumentMatchers.any(),
                ArgumentMatchers.anyBoolean(),
                ArgumentMatchers.any()
            )
        ).thenReturn(
            SharedStateResult(
                SharedStateStatus.SET,
                mapOf(
                    CampaignClassicTestConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TRACKING_SERVER to trackingServer,
                    CampaignClassicTestConstants.EventDataKeys.Configuration.GLOBAL_CONFIG_PRIVACY to privacyStatus.value,
                    CampaignClassicTestConstants.EventDataKeys.Configuration.CAMPAIGNCLASSIC_TIMEOUT to timeout
                )
            )
        )
    }

    private fun getTrackRequestEvent(
        messageId: String? = "12345",
        deliveryId: String? = "testDeliveryId",
        trackInfo: Map<String, String?>? = mapOf(
            CampaignClassicTestConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_MESSAGE_ID to messageId,
            CampaignClassicTestConstants.EventDataKeys.CampaignClassic.TRACK_INFO_KEY_DELIVERY_ID to deliveryId
        )
    ): Event {
        return Event.Builder("Track Request", EventType.CAMPAIGN, EventSource.REQUEST_CONTENT)
            .setEventData(
                mapOf(
                    CampaignClassicTestConstants.EventDataKeys.CampaignClassic.TRACK_RECEIVE to true,
                    CampaignClassicTestConstants.EventDataKeys.CampaignClassic.TRACK_INFO to trackInfo
                )
            )
            .build()
    }
}
