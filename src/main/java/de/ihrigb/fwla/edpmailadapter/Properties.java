package de.ihrigb.fwla.edpmailadapter;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
class Properties {

	private ReceivingProperties receive;
	private WritingProperties write;

	@Getter
	@Setter
	static class ReceivingProperties {
		private String host;
		private String username;
		private String password;
		private Set<String> whitelist;
	}

	@Getter
	@Setter
	static class WritingProperties {
		private String directory;
	}
}