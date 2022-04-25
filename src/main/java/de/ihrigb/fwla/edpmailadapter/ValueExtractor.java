package de.ihrigb.fwla.edpmailadapter;

import java.util.Set;
import java.util.function.Consumer;

import de.ihrigb.fwla.mail.Email;

interface ValueExtractor {
	void extract(Email<String> email, Consumer<Set<Value>> valuesConsumer);

	boolean isApplicable(Email<String> email);
}
