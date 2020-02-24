package com.prez.config;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;

public class CustomTokenEnhancer implements TokenEnhancer {


    private final String issuer;
    private final String clientId;

    public CustomTokenEnhancer(final String issuer, String clientId) {
        this.issuer = issuer;
        this.clientId = clientId;
    }


    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        final Map<String, Object> additionalInfo = new HashMap<>();
        additionalInfo.put("sub", accessToken.getValue());
        additionalInfo.put("iss", issuer);
        additionalInfo.put("azp", clientId);
        additionalInfo.put("tokenName", "id_token");
        additionalInfo.put("realm", "/spring-security-oauth");
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}
