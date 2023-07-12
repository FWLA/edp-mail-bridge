package de.ihrigb.fwla.edpmailadapter.mail;

@FunctionalInterface
public interface EmailHandler<T> {

	/**
	 * Handle any email.
	 *
	 * @param email the email containing extracted data.
	 */
	void handle(Email<T> email);
}