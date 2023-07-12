package de.ihrigb.fwla.edpmailadapter.mail;

public class EmailBodyConvertionException extends RuntimeException {
	private final static long serialVersionUID = 1L;

	public EmailBodyConvertionException(String message) {
		super(message);
	}

	public EmailBodyConvertionException(String message, Throwable cause) {
		super(message, cause);
	}
}
