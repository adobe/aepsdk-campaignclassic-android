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

package com.adobe.marketing.mobile.campaignclassic.internal;

/** This class holds all test constant values used only by the Campaign Classic extension */
final class CampaignClassicTestConstants {

    private CampaignClassicTestConstants() {}

    static final int DEFAULT_TIMEOUT = 30;
    static final String MESSAGE_RECEIVED_TAGID = "1";
    static final String MESSAGE_CLICKED_TAGID = "2";

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
            static final String CAMPAIGNCLASSIC_TRACKING_ENDPOINT_MAPPING =
                    "campaignclassic.trackingEndpointsMapping";
            static final String CAMPAIGNCLASSIC_APP_INTEGRATION_KEY =
                    "campaignclassic.android.integrationKey";
            static final String CAMPAIGNCLASSIC_TIMEOUT = "campaignclassic.timeout";

            private Configuration() {}
        }

        static final class CampaignClassic {
            static final String REGISTER_DEVICE = "registerdevice";
            static final String TRACK_RECEIVE = "trackreceive";
            static final String TRACK_CLICK = "trackclick";
            static final String TRACK_INFO = "trackinfo";
            static final String TRACK_INFO_KEY_MESSAGE_ID = "_mId";
            static final String TRACK_INFO_KEY_DELIVERY_ID = "_dId";
            static final String TRACK_INFO_KEY_TRACKING_INSTANCE_ID = "_iNm";
            static final String DEVICE_TOKEN = "devicetoken";
            static final String USER_KEY = "userkey";
            static final String ADDITIONAL_PARAMETERS = "additionalparameters";

            private CampaignClassic() {}
        }
    }
}
