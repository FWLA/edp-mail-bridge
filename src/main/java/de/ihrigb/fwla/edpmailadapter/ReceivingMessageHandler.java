package de.ihrigb.fwla.edpmailadapter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.transaction.annotation.Transactional;

import de.ihrigb.fwla.edpmailadapter.Properties.ReceivingProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ReceivingMessageHandler implements MessageHandler {

	private final ReceivingProperties properties;
	private final Set<EmailHandler> handlers;

	@Transactional
	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		try {
			log.info("Handling incoming email.");
			MimeMessage mimeMessage = (MimeMessage) message.getPayload();

			Address[] addresses = mimeMessage.getFrom();
			String sender = ((InternetAddress) addresses[0]).getAddress();
			if (addresses != null) {

				switch (new MailFilter(properties).filter(sender)) {
					case REJECTED:
						log.info("Mail from '{}' was rejected.", sender);
						return;
					case ACCEPTED:
					default:
						log.info("Mail from '{}' classified as hot.", sender);
						break;
				}

				String subject = mimeMessage.getSubject();
				String text = getText(mimeMessage);
				Instant timestamp = Instant.ofEpochMilli(message.getHeaders().getTimestamp());

				Email email = new Email(sender, subject, text, timestamp);
				this.handlers.stream().parallel().forEach(h -> h.handle(email));
			}
		} catch (Exception e) {
			log.error("Exception while handling incomming message.", e);
		}
	}

	private String getText(Part p) throws javax.mail.MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			Object content = p.getContent();
			return content == null ? null : content.toString();
		}

		if (p.isMimeType("multipart/alternative")) {
			// Prefer html text over plain text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/plain")) {
					if (text == null) {
						text = getText(bp);
					}
					continue;
				} else if (bp.isMimeType("text/html")) {
					String s = getText(bp);
					if (s != null) {
						return s;
					}
				} else {
					return getText(bp);
				}
			}
			return text;
		} else if (p.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) p.getContent();
			for (int i = 0; i < mp.getCount(); i++) {
				String s = getText(mp.getBodyPart(i));
				if (s != null) {
					return s;
				}
			}
		}

		return null;
	}
}
