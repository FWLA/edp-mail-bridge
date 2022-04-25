package de.ihrigb.fwla.edpmailadapter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;
import org.springframework.messaging.MessageHandler;
import org.springframework.web.util.UriUtils;

import de.ihrigb.fwla.edpmailadapter.Properties.ReceivingProperties;
import de.ihrigb.fwla.mail.EmailBodyConverter;
import de.ihrigb.fwla.mail.ReceivingMessageHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(Properties.class)
@ConditionalOnProperty(prefix = "app.receive", name = "host", matchIfMissing = false)
class AppConfiguration {

	@Bean
	WritingEmailHandler writingEmailHandler(Properties properties) {
		FileWriter fileWriter = new FileWriter(properties.getWrite());

		DefaultValueExtractor defaultValueExtractor = new DefaultValueExtractor(properties.getExtraction());

		List<ValueExtractor> valueExtractors = new ArrayList<>();
		valueExtractors.add(new UnwetterValueExtractor(properties.getUnwetter()));

		return new WritingEmailHandler(fileWriter, defaultValueExtractor, valueExtractors);
	}

	@Bean
	ImapMailReceiver imapMailReceiver(Properties properties) {

		java.util.Properties javaMailProperties = new java.util.Properties();
		javaMailProperties.setProperty("mail.imaps.socketFactory.class", SSLSocketFactory.class.getName());
		javaMailProperties.setProperty("mail.imap.starttls.enable", "true");
		javaMailProperties.setProperty("mail.imaps.socketFactory.fallback", "false");
		javaMailProperties.setProperty("mail.store.protocol", "imaps");
		// javaMailProperties.setProperty("mail.debug", "true");

		ReceivingProperties receivingProperties = properties.getReceive();

		String userInfo = String.format("%s:%s", receivingProperties.getUsername(), receivingProperties.getPassword());

		String url = String.format("%s://%s@%s:%d/INBOX", receivingProperties.getProtocol(),
				UriUtils.encodeUserInfo(userInfo, StandardCharsets.UTF_8),
				UriUtils.encodeHost(receivingProperties.getHost(), StandardCharsets.UTF_8),
				receivingProperties.getPort());

		log.info("Connecting to {}.", url);
		ImapMailReceiver imapMailReceiver = new ImapMailReceiver(url);
		imapMailReceiver.setJavaMailProperties(javaMailProperties);
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
	MessageHandler messageHandler(Properties properties, WritingEmailHandler writingEmailHandler) {
		EmailBodyConverter<String> emailBodyConverter = new PlainTextEmailBodyConverter();

		return new ReceivingMessageHandler<>(emailBodyConverter, writingEmailHandler);
	}
}
