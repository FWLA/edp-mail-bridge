package de.ihrigb.fwla.edpmailadapter;

import de.ihrigb.fwla.edpmailadapter.Properties.ReceivingProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class MailFilter {

	private final ReceivingProperties properties;

	FilterResult filter(String sender) {
		if (properties.getWhitelist() != null && !properties.getWhitelist().isEmpty()) {
			if (StringUtils.containsIgnoreCase(properties.getWhitelist(), sender)) {
				log.debug("Mail from '{}' filtered as 'HOT'.", sender);
				return FilterResult.ACCEPTED;
			}
		}

		log.debug("Mail from '{}' filtered as 'REJECTED'.", sender);
		return FilterResult.REJECTED;
	}

	static enum FilterResult {
		ACCEPTED, REJECTED;
	}
}
