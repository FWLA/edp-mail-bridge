package de.ihrigb.fwla.edpmailadapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.ihrigb.fwla.edpmailadapter.Properties.OAuthProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class ClientCredentialsTokenProvider {
	private final OAuthProperties properties;
	private final RestTemplate restTemplate;

	private String token;

	ClientCredentialsTokenProvider(OAuthProperties properties) {
		this.properties = properties;
		this.restTemplate = new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(5))
				.setReadTimeout(Duration.ofSeconds(5)).build();
	}

	String getToken() {
		if (this.isTokenExpired()) {
			this.fetchNewToken();
		}
		return this.token;
	}

	private boolean isTokenExpired() {
		if (this.token == null) {
			return true;
		}
		try {
			String payload = new String(Base64.getDecoder().decode(this.token.split("\\.")[1]), StandardCharsets.UTF_8);
			JsonNode root = new ObjectMapper().readTree(payload);
			long expEpochSeconds = root.get("exp").asLong();
			Date exp = new Date(1000 * expEpochSeconds);
			return exp.before(new Date());
		} catch (IOException e) {
			log.warn("Exception during JWT deserialization.", e);
			return true;
		}
	}

	private void fetchNewToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("client_id", this.properties.getClientId());
		body.add("client_secret", this.properties.getClientSecret());
		body.add("scope", this.properties.getScope());
		body.add("grant_type", "client_credentials");

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

		ResponseEntity<TokenResponse> responseEntity = this.restTemplate
				.postForEntity(this.properties.getTokenEndpoint(), entity, TokenResponse.class);
		this.token = responseEntity.getBody().getAccessToken();
	}

	@Getter
	static class TokenResponse {
		@JsonProperty("token_type")
		private String tokenType;
		@JsonProperty("expires_in")
		private int expiresIn;
		@JsonProperty("ext_expires_in")
		private int extExpiresIn;
		@JsonProperty("access_token")
		private String accessToken;
	}
}
