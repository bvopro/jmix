/*
 * Copyright 2019 Haulmont.
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

package io.jmix.rest;

import io.jmix.core.annotation.Internal;
import io.jmix.rest.api.common.RestLocaleUtils;
import io.jmix.rest.security.UserPasswordTokenGranter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.ArrayList;
import java.util.List;

@Internal
@Configuration
@EnableAuthorizationServer
public class RestAuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private static final String REST_API = "rest-api";

    @Autowired
    protected RestProperties restProperties;

    @Autowired
    protected AuthenticationManager authenticationManager;

    @Autowired
    protected RestLocaleUtils restAuthUtils;

    @Autowired
    @Qualifier("rest_tokenStore")
    protected TokenStore tokenStore;

    @Bean(name = "rest_tokenEnhancer")
    public TokenEnhancer tokenEnhancer() {
        return new ComplexTokenEnhancer();
    }

    @Bean(name = "rest_tokenServices")
    public AuthorizationServerTokenServices tokenServices(AuthorizationServerEndpointsConfigurer endpoints) {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(endpoints.getTokenStore());
        defaultTokenServices.setSupportRefreshToken(restProperties.isSupportRefreshToken());
        defaultTokenServices.setReuseRefreshToken(restProperties.isReuseRefreshToken());
        defaultTokenServices.setTokenEnhancer(endpoints.getTokenEnhancer());
        defaultTokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        return defaultTokenServices;
    }

    @Bean(name = "rest_tokenGranter")
    public TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
        List<TokenGranter> tokenGranters = new ArrayList<>();

        AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
        ClientDetailsService clientDetails = endpoints.getClientDetailsService();
        OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();

        tokenGranters.add(new RefreshTokenGranter(tokenServices, clientDetails, requestFactory));

        if (authenticationManager != null) {
            tokenGranters.add(new UserPasswordTokenGranter(restAuthUtils,
                    authenticationManager, tokenServices, clientDetails, requestFactory));
        }
        return new CompositeTokenGranter(tokenGranters);
    }

    @Bean("rest_clientDetailsService")
    public ClientDetailsService clientDetailsService() {
        InMemoryClientDetailsServiceBuilder builder = new InMemoryClientDetailsServiceBuilder();
        builder
                .withClient(restProperties.getClientId())
                .secret(restProperties.getClientSecret())
                .authorizedGrantTypes(restProperties.getClientAuthorizedGrantTypes())
                .accessTokenValiditySeconds(restProperties.getClientRefreshTokenExpirationTimeSec())
                .refreshTokenValiditySeconds(restProperties.getClientRefreshTokenExpirationTimeSec())
                .scopes(REST_API);
        try {
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException("Error on building ClientDetailsService", e);
        }
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints.pathMapping("/oauth/token", "/rest/oauth/token")
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore)
                .tokenEnhancer(tokenEnhancer())
                .tokenServices(tokenServices(endpoints))
                .tokenGranter(tokenGranter(endpoints));
    }
}
