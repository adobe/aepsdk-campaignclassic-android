/*
  Copyright 2023 Adobe. All rights reserved.
  This file is licensed to you under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software distributed under
  the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
  OF ANY KIND, either express or implied. See the License for the specific language
  governing permissions and limitations under the License.
*/

package com.adobe.marketing.mobile;

import androidx.annotation.NonNull;
import com.adobe.marketing.mobile.campaignclassic.internal.CampaignClassicExtension;
import com.adobe.marketing.mobile.services.Log;
import com.adobe.marketing.mobile.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/** CampaignClassic public API class */
public class CampaignClassic {
    /**
     * The {@link Extension} class for CampaignClassic. This class is used to register the
     * CampaignClassic extension with the Mobile SDK.
     */
    public static final Class<? extends Extension> EXTENSION = CampaignClassicExtension.class;

    public static final String EXTENSION_VERSION = "3.1.2";
    private static final String LOG_TAG = "CampaignClassicExtension";
    private static final String SELF_TAG = "CampaignClassic";
    static final String REGISTER_DEVICE = "registerdevice";
    static final String TRACK_RECEIVE = "trackreceive";
    static final String TRACK_CLICK = "trackclick";
    static final String TRACK_INFO = "trackinfo";
    static final String DEVICE_TOKEN = "devicetoken";
    static final String USER_KEY = "userkey";
    static final String ADDITIONAL_PARAMETERS = "additionalparameters";

    private static final String NULL_TOKEN_MESSAGE = "The provided token is null or empty";
    private static final String NULL_TRACK_INFO_MESSAGE =
            "The provided trackInfo map is null or empty";

    private CampaignClassic() {}

    /**
     * Returns the current version of the {@code CampaignClassic} extension.
     *
     * @return {@code String} containing the current version
     */
    @NonNull public static String extensionVersion() {
        return EXTENSION_VERSION;
    }

    /**
     * Registers a device with the configured Adobe Campaign Classic server instance.
     *
     * @param token {@code String} containing unique registration token
     * @param userKey {@code String} containing the user identifier
     * @param additionalParams {@code Map} containing custom key-value pairs to be sent in the
     *     registration request
     */
    public static void registerDevice(
            @NonNull final String token,
            final String userKey,
            final Map<String, Object> additionalParams) {
        if (StringUtils.isNullOrEmpty(token)) {
            Log.error(
                    LOG_TAG,
                    SELF_TAG,
                    "Failed to register device for CampaignClassic (%s)",
                    NULL_TOKEN_MESSAGE);
            return;
        }
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(REGISTER_DEVICE, true);
        eventData.put(DEVICE_TOKEN, token);
        eventData.put(USER_KEY, userKey);
        eventData.put(ADDITIONAL_PARAMETERS, additionalParams);
        final Event event =
                new Event.Builder(
                                "CampaignClassic Register Device",
                                EventType.CAMPAIGN,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();

        MobileCore.dispatchEvent(event);
    }

    /**
     * Sends notification tracking information to the configured Adobe Campaign Classic server.
     *
     * <p>This API may be used to send tracking information when a notification is received on the
     * device. If {@code trackInfo} is null or does not contain the necessary tracking identifiers,
     * messageId {@code _mId} and deliveryId {@code _dId}, no track request is sent.
     *
     * @param trackInfo {@code Map<String, String>} containing {@code _dId} and {@code _mId}
     *     received in the message payload
     */
    public static void trackNotificationReceive(@NonNull final Map<String, String> trackInfo) {
        if (trackInfo == null || trackInfo.isEmpty()) {
            Log.error(
                    LOG_TAG,
                    SELF_TAG,
                    "Failed to track notification receive for CampaignClassic (%s)",
                    NULL_TRACK_INFO_MESSAGE);
            return;
        }

        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(TRACK_RECEIVE, true);
        eventData.put(TRACK_INFO, trackInfo);

        final Event event =
                new Event.Builder(
                                "CampaignClassic Track Notification Receive",
                                EventType.CAMPAIGN,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        MobileCore.dispatchEvent(event);
    }

    /**
     * Sends notification tracking information to the configured Adobe Campaign Classic server.
     *
     * <p>This API may be used to send tracking information when user clicks on a notification. If
     * {@code trackInfo} is null or does not contain the necessary tracking identifiers, messageId
     * {@code _mId} and deliveryId {@code _dId}, no track request is sent.
     *
     * @param trackInfo {@code Map<String, String>} containing {@code _dId} and {@code _mId}
     *     received in the message payload
     */
    public static void trackNotificationClick(@NonNull final Map<String, String> trackInfo) {
        if (trackInfo == null || trackInfo.isEmpty()) {
            Log.error(
                    LOG_TAG,
                    SELF_TAG,
                    "Failed to track notification click for CampaignClassic (%s)",
                    NULL_TRACK_INFO_MESSAGE);
            return;
        }

        final Map<String, Object> eventData = new HashMap<>();
        eventData.put(TRACK_CLICK, true);
        eventData.put(TRACK_INFO, trackInfo);

        final Event event =
                new Event.Builder(
                                "CampaignClassic Track Notification Click",
                                EventType.CAMPAIGN,
                                EventSource.REQUEST_CONTENT)
                        .setEventData(eventData)
                        .build();
        MobileCore.dispatchEvent(event);
    }
}
