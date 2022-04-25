package de.ihrigb.fwla.edpmailadapter;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class Value {
	private final String name;
	private final String value;
}
