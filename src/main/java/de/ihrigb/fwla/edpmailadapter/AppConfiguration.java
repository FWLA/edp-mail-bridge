package de.ihrigb.fwla.edpmailadapter;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.util.UriUtils;

import de.ihrigb.fwla.edpmailadapter.Properties.ReceivingProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(Properties.class)
class AppConfiguration {

	@Bean
	WritingEmailHandler writingEmailHandler(Properties properties) {
		return new WritingEmailHandler(properties.getWrite());
	}

	@Bean
	ImapMailReceiver imapMailReceiver(Properties properties) {
		ReceivingProperties receivingProperties = properties.getReceive();

		String userInfo = String.format("%s:%s", receivingProperties.getUsername(), receivingProperties.getPassword());

		String url = String.format("imap://%s@%s:143/inbox", UriUtils.encodeUserInfo(userInfo, StandardCharsets.UTF_8),
				UriUtils.encodeHost(receivingProperties.getHost(), StandardCharsets.UTF_8));

		log.info("Connecting to {}.", url);
		ImapMailReceiver imapMailReceiver = new ImapMailReceiver(url);
		imapMailReceiver.setShouldMarkMessagesAsRead(Boolean.TRUE);
		return imapMailReceiver;
	}

	@Bean
	ImapIdleChannelAdapter imapIdleChannelAdapter(DirectChannel directChannel, ImapMailReceiver imapMailReceiver) {
		ImapIdleChannelAdapter imapIdleChannelAdapter = new ImapIdleChannelAdapter(imapMailReceiver);
		imapIdleChannelAdapter.setOutputChannel(directChannel);
		imapIdleChannelAdapter.setAutoStartup(true);
		return imapIdleChannelAdapter;
	}

	@Bean(name = "receiveEmailChannel")
	DirectChannel directChannel(MessageHandler messageHandler) {
		DirectChannel directChannel = new DirectChannel();
		directChannel.subscribe(messageHandler);
		return directChannel;
	}

	@Bean
	MessageHandler messageHandler(Properties properties) {
		return new ReceivingMessageHandler(properties.getReceive(), Collections.singleton(writingEmailHandler(properties)));
	}
}
