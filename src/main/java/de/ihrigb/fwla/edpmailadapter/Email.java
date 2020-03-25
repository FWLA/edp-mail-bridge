package de.ihrigb.fwla.edpmailadapter;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class Email {
	private final String sender;
	private final String subject;
	private final String body;
	private final Instant timestamp;
}