/**
 * Copyright 2013 Sean Kavanagh - sean.p.kavanagh6@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.keybox.manage.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.keybox.common.util.AppConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * return client configuration for AWS service calls
 */
public class AWSClientConfig {

    private static ClientConfiguration config = new ClientConfiguration();

    /**
     * set config info based on AppConfig
     */
    static {
        String awsProtocol= AppConfig.getProperty("awsProtocol");
        String awsProxyHost = AppConfig.getProperty("awsProxyHost");
        String awsProxyPort = AppConfig.getProperty("awsProxyPort");
        String awsProxyUser = AppConfig.getProperty("awsProxyUser");
        String awsProxyPassword = AppConfig.getProperty("awsProxyPassword");

        if("http".equals(awsProtocol)){
            config.setProtocol(Protocol.HTTP);
        }
        else {
            config.setProtocol(Protocol.HTTPS);
        }
        if (StringUtils.isNotEmpty(awsProxyHost)) {
            config.setProxyHost(awsProxyHost);
        }
        if (StringUtils.isNotEmpty(awsProxyPort)) {
            config.setProxyPort(Integer.parseInt(awsProxyPort));
        }
        if (StringUtils.isNotEmpty(awsProxyUser)) {
            config.setProxyUsername(awsProxyUser);
        }
        if (StringUtils.isNotEmpty(awsProxyPassword)) {
            config.setProxyPassword(awsProxyPassword);
        }

    }

    /**
     * return configuration information for AWS client
     * @return client configuration information
     */
    public static ClientConfiguration getClientConfig() {

        return config;

    }
}
