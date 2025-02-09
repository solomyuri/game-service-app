package com.solomyuri.game_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.solomyuri.game_service.config.properties.SsoClientProperties;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Slf4j
@Configuration
@EnableConfigurationProperties(SsoClientProperties.class)
public class AppConfig {

    @Bean()
    WebClient webClient(WebClient.Builder builder,
                        OAuth2AuthorizedClientManager authorizedClientManager,
                        SsoClientProperties ssoClientProperties) {

	final String BASE_URL = String.format("%s://%s:%s",
	        ssoClientProperties.getScheme(), ssoClientProperties.getHost(), ssoClientProperties.getPort());

	ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Filter = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
	        authorizedClientManager);

	oauth2Filter.setDefaultClientRegistrationId("auth-client");

	return builder.baseUrl(BASE_URL)
	        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
	                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG,
	                        AdvancedByteBufFormat.TEXTUAL)))
	        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs()
	                .maxInMemorySize(1024 * 1024 * 10))
	        .filter(oauth2Filter)
	        .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                 OAuth2AuthorizedClientRepository authorizedClientRepository) {

	OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
	        .clientCredentials()
	        .build();

	DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
	        clientRegistrationRepository, authorizedClientRepository);
	authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

	return authorizedClientManager;
    }
}
