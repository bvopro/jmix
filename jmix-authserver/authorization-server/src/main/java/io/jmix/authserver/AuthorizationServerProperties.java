/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.authserver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "jmix.authorization-server")
public class AuthorizationServerProperties {

    /**
     * Whether a default auto-configuration should be applied
     */
    boolean useDefaultConfiguration;

    /**
     * A list of jmix-specific client configurations
     */
    Map<String, JmixAuthorizationServerClient> client;

    public AuthorizationServerProperties(
            @DefaultValue("true") boolean useDefaultConfiguration,
            Map<String, JmixAuthorizationServerClient> client) {
        this.useDefaultConfiguration = useDefaultConfiguration;
        this.client = client;
    }

    public boolean isUseDefaultConfiguration() {
        return useDefaultConfiguration;
    }

    public Map<String, JmixAuthorizationServerClient> getClient() {
        return client;
    }

    /**
     * Class stores Jmix-specific settings of Authorization Server client.
     */
    public static class JmixAuthorizationServerClient {
        List<String> resourceRoles;
        List<String> rowLevelRoles;

        public JmixAuthorizationServerClient(@DefaultValue List<String> resourceRoles,
                                             @DefaultValue List<String> rowLevelRoles) {
            this.resourceRoles = resourceRoles;
            this.rowLevelRoles = rowLevelRoles;
        }

        public List<String> getResourceRoles() {
            return resourceRoles;
        }

        public List<String> getRowLevelRoles() {
            return rowLevelRoles;
        }
    }
}