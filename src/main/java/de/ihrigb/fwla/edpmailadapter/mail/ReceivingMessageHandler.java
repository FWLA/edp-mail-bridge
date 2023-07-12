package de.ihrigb.fwla.edpmailadapter.mail;

import jakarta.mail.Address;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.Optional;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceivingMessageHandler<T> implements MessageHandler {

	private final Optional<EmailSenderFilter> emailSenderFilter;
	private final EmailBodyConverter<T> emailBodyConverter;
	private final EmailHandler<T> emailHandler;

	public ReceivingMessageHandler(EmailBodyConverter<T> emailBodyConverter, EmailHandler<T> emailHandler) {
		this(Optional.empty(), emailBodyConverter, emailHandler);
	}

	public ReceivingMessageHandler(Optional<EmailSenderFilter> emailSenderFilter,
			EmailBodyConverter<T> emailBodyConverter, EmailHandler<T> emailHandler) {
		Assert.notNull(emailSenderFilter, "emailSenderFilter must not be null.");
		Assert.notNull(emailBodyConverter, "emailBodyConverter must not be null.");
		Assert.notNull(emailHandler, "emailHandler must not be null.");

		this.emailSenderFilter = emailSenderFilter;
		this.emailBodyConverter = emailBodyConverter;
		this.emailHandler = emailHandler;
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		try {
			log.info("Handling incoming email.");
			MimeMessage mimeMessage = (MimeMessage) message.getPayload();

			Address[] addresses = mimeMessage.getFrom();
			if (addresses != null) {
				String sender = ((InternetAddress) addresses[0]).getAddress();

				switch (emailSenderFilter.orElse(EmailSenderFilter.ACCEPT_ALL).filter(sender)) {
					case REJECTED:
						log.info("Mail from '{}' was rejected.", sender);
						return;
					case ACCEPTED:
					default:
						log.info("Mail from '{}' was accepted.", sender);
						break;
				}

				String subject = mimeMessage.getSubject();
				T body = emailBodyConverter.convert(mimeMessage);
				Instant timestamp = Instant.ofEpochMilli(message.getHeaders().getTimestamp());

				Email<T> email = new Email<>(sender, subject, body, timestamp);

				emailHandler.handle(email);
			} else {
				log.warn("No sender present.");
			}
		} catch (EmailBodyConvertionException e) {
			log.error("Email body convertion failed.", e);
			throw new MessagingException("Email body convertion failed.", e);
		} catch (jakarta.mail.MessagingException e) {
			log.error("Generic javax.mail MessagingException.", e);
			throw new MessagingException("Generic javax.mail MessagingException.", e);
		}
	}
}
