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
package com.adobe.marketing.mobile.campaignclassic.internal;

/** This class holds all test constant values used only by the Campaign Classic extension */
final class CampaignClassicTestConstants {

    static final String EXTENSION_VERSION = "2.1.8";

    private CampaignClassicTestConstants() {}

    static final String REGISTER_API_URL_BASE = "%s/nms/mobile/1/registerAndroid.jssp";
    static final String TRACKING_API_URL_BASE = "%s/r/?id=h%x,%s,%s";
    static final String REGISTER_PARAM_DEVICE_OS_NAME = "android";
    static final int DEFAULT_TIMEOUT = 30;
    static final String REGISTER_PARAMS_FORMAT =
            "registrationToken=%s&mobileAppUuid=%s&userKey=%s&deviceImei=%s&deviceName=%s&deviceModel=%s&deviceBrand=%s&deviceManufacturer=%s&osName=%s&osVersion=%s&osLanguage=%s&additionalParams=";

    static final String MESSAGE_RECEIVED_TAGID = "1";
    static final String MESSAGE_CLICKED_TAGID = "2";

    static final String DATASTORE_KEY = "ADOBEMOBILE_CAMPAIGNCLASSIC";

    /*
       DataStoreKeys
    */
    static class DataStoreKeys {
        static final String TOKEN_HASH = "ADOBEMOBILE_STOREDDEFAULTS_TOKENHASH";

        private DataStoreKeys() {}
    }

    /*
       EventDataKeys
    */
    static final class EventDataKeys {
        private EventDataKeys() {}

        static final class Configuration {
            static final String EXTENSION_NAME = "com.adobe.module.configuration";
            static final String GLOBAL_CONFIG_PRIVACY = "global.privacy";
            static final String CAMPAIGNCLASSIC_MARKETING_SERVER =
                    "campaignclassic.marketingServer";
            static final String CAMPAIGNCLASSIC_TRACKING_SERVER = "campaignclassic.trackingServer";
            static final String CAMPAIGNCLASSIC_APP_INTEGRATION_KEY =
                    "campaignclassic.android.integrationKey";
            static final String CAMPAIGNCLASSIC_TIMEOUT = "campaignclassic.timeout";

            private Configuration() {}
        }

        static final class CampaignClassic {
            static final String EXTENSION_NAME = "com.adobe.module.campaignclassic";

            static final String REGISTER_DEVICE = "registerdevice";
            static final String TRACK_RECEIVE = "trackreceive";
            static final String TRACK_CLICK = "trackclick";
            static final String TRACK_INFO = "trackinfo";
            static final String TRACK_INFO_KEY_MESSAGE_ID = "_mId";
            static final String TRACK_INFO_KEY_DELIVERY_ID = "_dId";
            static final String DEVICE_TOKEN = "devicetoken";
            static final String USER_KEY = "userkey";
            static final String ADDITIONAL_PARAMETERS = "additionalparameters";

            private CampaignClassic() {}
        }
    }
}
