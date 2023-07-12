package de.ihrigb.fwla.edpmailadapter.mail;

/**
 * Allows filtering of emails.
 */
@FunctionalInterface
public interface EmailSenderFilter {

	public static final EmailSenderFilter ACCEPT_ALL = __ -> FilterResult.ACCEPTED;
	public static final EmailSenderFilter REJECT_ALL = __ -> FilterResult.REJECTED;

	FilterResult filter(String sender);
}
