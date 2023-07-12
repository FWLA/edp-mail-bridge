package de.ihrigb.fwla.edpmailadapter.mail;

import jakarta.mail.internet.MimeMessage;

@FunctionalInterface
public interface EmailBodyConverter<T> {

	/**
	 * Convert the received message to the desired format. It may throw
	 * {@link EmailBodyConvertionException} upon failure.
	 *
	 * @param mimeMessage the mime message to be converted
	 * @return the converted value
	 */
	T convert(MimeMessage mimeMessage);
}