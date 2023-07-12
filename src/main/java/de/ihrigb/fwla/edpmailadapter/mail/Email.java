package de.ihrigb.fwla.edpmailadapter.mail;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class Email<T> {
	private final String sender;
	private final String subject;
	private final T body;
	private final Instant timestamp;
}
