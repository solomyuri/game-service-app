package com.solomyuri.game_service.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "sso-client")
@RequiredArgsConstructor
@Getter
public class SsoClientProperties {

    private final String scheme;
    private final String host;
    private final String port;
    private final String usersPath;
}
