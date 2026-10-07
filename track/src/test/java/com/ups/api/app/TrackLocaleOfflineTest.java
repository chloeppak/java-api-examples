package com.ups.api.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies the Track locale is driven by configuration, without any network access.
 */
class TrackLocaleOfflineTest {

	private static final String OAUTH_BASE_URL = "http://localhost.test/";
	private static final String TRACK_BASE_URL = "http://localhost.test/api";
	private static final String INQUIRY_NUMBER = "1Z0000000000000000";

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(RestTemplateAutoConfiguration.class))
			.withUserConfiguration(AppConfig.class)
			.withPropertyValues(
					"api.oauth.partner.client.id=client",
					"api.oauth.partner.secret=secret",
					"api.oauth.base.url=" + OAUTH_BASE_URL,
					"api.track.inquiryNumber=" + INQUIRY_NUMBER);

	@Test
	void localeDefaultsToEnUsWhenNotConfigured() {
		contextRunner.run(context -> assertEquals("en_US", context.getBean(AppConfig.class).getLocale()));
	}

	@Test
	void localeIsReadFromProperty() {
		contextRunner.withPropertyValues("api.track.locale=de_DE")
				.run(context -> assertEquals("de_DE", context.getBean(AppConfig.class).getLocale()));
	}

	@ParameterizedTest
	@ValueSource(strings = { "en_US", "fr_FR", "es_MX", "ja_JP" })
	void configuredLocaleIsSentAsQueryParam(String locale) {
		RestTemplate restTemplate = new RestTemplate();
		MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
		server.expect(ExpectedCount.between(0, 1), requestTo(OAUTH_BASE_URL + "security/v1/oauth/token"))
				.andRespond(withSuccess("{\"access_token\":\"token\",\"expires_in\":\"3600\"}",
						MediaType.APPLICATION_JSON));
		server.expect(ExpectedCount.once(),
				requestTo(org.hamcrest.Matchers.startsWith(TRACK_BASE_URL + "/track/v1/details/" + INQUIRY_NUMBER)))
				.andExpect(method(HttpMethod.GET))
				.andExpect(queryParam("locale", locale))
				.andRespond(withSuccess("{\"trackResponse\":{}}", MediaType.APPLICATION_JSON));

		AppConfig appConfig = new AppConfig();
		appConfig.setClientID("client");
		appConfig.setSecret("secret");
		appConfig.setOauthBaseUrl(OAUTH_BASE_URL);
		appConfig.setTrackBaseUrl(TRACK_BASE_URL);
		appConfig.setInquiryNumber(INQUIRY_NUMBER);
		appConfig.setTransactionSrc("testing");
		appConfig.setTokenExipryToleranceInSec(5);
		appConfig.setLocale(locale);

		// Track caches its API client in a ThreadLocal; use a fresh thread so no other test's client is reused.
		CompletableFuture.runAsync(() -> {
			try {
				new Track(restTemplate, appConfig).run();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}, r -> new Thread(r).start()).join();

		server.verify();
	}
}
