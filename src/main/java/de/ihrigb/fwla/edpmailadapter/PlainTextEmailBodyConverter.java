package de.ihrigb.fwla.edpmailadapter;

import java.io.IOException;

import de.ihrigb.fwla.edpmailadapter.mail.EmailBodyConverter;
import de.ihrigb.fwla.edpmailadapter.mail.EmailBodyConvertionException;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class PlainTextEmailBodyConverter implements EmailBodyConverter<String> {

	@Override
	public String convert(MimeMessage mimeMessage) {
		try {
			return getText(mimeMessage);
		} catch (MessagingException | IOException e) {
			log.error("Convertion failed.", e);
			throw new EmailBodyConvertionException("Convertion failed.", e);
		}
	}

	private String getText(Part p) throws jakarta.mail.MessagingException, IOException {
		if (p.isMimeType("text/*")) {
			Object content = p.getContent();
			return content == null ? null : content.toString();
		}

		if (p.isMimeType("multipart/alternative")) {
			// Prefer plain text over html text
			Multipart mp = (Multipart) p.getContent();
			String text = null;
			for (int i = 0; i < mp.getCount(); i++) {
				Part bp = mp.getBodyPart(i);
				if (bp.isMimeType("text/html")) {
					if (text == null) {
						text = getText(bp);
					}
					continue;
				} else if (bp.isMimeType("text/plain")) {
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
