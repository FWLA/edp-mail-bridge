package de.ihrigb.fwla.edpmailadapter;

import de.ihrigb.fwla.edpmailadapter.Properties.ReceivingProperties;
import de.ihrigb.fwla.mail.EmailSenderFilter;
import de.ihrigb.fwla.mail.FilterResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class MailFilter implements EmailSenderFilter {

	private final ReceivingProperties properties;

	@Override
	public FilterResult filter(String sender) {
		if (properties.getWhitelist() == null || properties.getWhitelist().isEmpty()) {
			return FilterResult.ACCEPTED;
		}
		if (StringUtils.containsIgnoreCase(properties.getWhitelist(), sender)) {
			log.debug("Mail from '{}' filtered as 'ACCEPTED'.", sender);
			return FilterResult.ACCEPTED;
		}

		log.debug("Mail from '{}' filtered as 'REJECTED'.", sender);
		return FilterResult.REJECTED;
	}
}
