package de.ihrigb.fwla.edpmailadapter;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
class Properties {

	private OAuthProperties oauth;
	private ReceivingProperties receive;
	private WritingProperties write;
	private ExtractionProperties extraction = new ExtractionProperties();
	private UnwetterProperties unwetter = new UnwetterProperties();

	@Getter
	@Setter
	static class ReceivingProperties {
		private String host;
		private int port;
		private String username;
		private Set<String> whitelist;
	}

	@Getter
	@Setter
	static class OAuthProperties {
		private String tokenEndpoint;
		private String clientId;
		private String clientSecret;
		private String scope;
	}

	@Getter
	@Setter
	static class WritingProperties {
		private String directory;
	}

	@Getter
	@Setter
	static class ExtractionProperties {
		private String alarmEm;
	}

	@Getter
	@Setter
	static class UnwetterProperties {
		private String einsatzart = "H";
		private String stichwort = "1";
	}
}