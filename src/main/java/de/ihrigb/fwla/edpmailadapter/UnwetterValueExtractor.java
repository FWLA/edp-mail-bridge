package de.ihrigb.fwla.edpmailadapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.ihrigb.fwla.edpmailadapter.Properties.UnwetterProperties;
import de.ihrigb.fwla.mail.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

class UnwetterValueExtractor implements ValueExtractor {

	private final static String SUBJECT_PREFIX = "UNWETTERLAGE - Sammelemail";
	private final static String BODY_DELIMITER = "______________________________________________________________________________";

	private final Set<ValueProvider> valueProviders;

	UnwetterValueExtractor(UnwetterProperties properties) {
		Set<ValueProvider> valueProviders = new HashSet<>();
		valueProviders.add(new StaticValueProvider(Constants.Fields.EINSATZART, properties.getEinsatzart()));
		valueProviders.add(new StaticValueProvider(Constants.Fields.STICHWORT, properties.getStichwort()));
		valueProviders.add(new RegexValueProvider(Constants.Fields.MELDEBILD,
				"Einsatzstichwort\\/Priorität\\:[^\\S\\n]+(.+)\\s\\/\\s.*\\r\\n"));
		valueProviders.add(new RegexValueProvider(Constants.Fields.INTERNE_NUMMER,
				"OrgEinheit\\/Einsatznummer\\:[^\\S\\n]+.+\\s\\/\\s(\\d+)\\r\\n"));
		valueProviders.add(new RegexValueProvider(Constants.Fields.MELDUNG_LST,
				"Meldungseingang\\/Meldender\\/Telefon\\:[^\\S\\n]*((.*)\\sh)\\s\\/\\s?([^\\/]*)\\s\\/[^\\n]?([^\\n]*)\\r\\n",
				2));
		valueProviders.add(new RegexValueProvider(Constants.Fields.MELDENDER,
				"Meldungseingang\\/Meldender\\/Telefon\\:[^\\S\\n]*((.*)\\sh)\\s\\/\\s?([^\\/]*)\\s\\/[^\\n]?([^\\n]*)\\r\\n",
				3));
		valueProviders.add(new RegexValueProvider(Constants.Fields.MELDEWEG_TELEFON,
				"Meldungseingang\\/Meldender\\/Telefon\\:[^\\S\\n]*((.*)\\sh)\\s\\/\\s?([^\\/]*)\\s\\/[^\\n]?([^\\n]*)\\r\\n",
				4));
		valueProviders.add(new RegexValueProvider(Constants.Fields.OBJEKTNAME, "Objekt\\:[^\\S\\n]+(.*)\\r\\n"));
		valueProviders
				.add(new RegexValueProvider(Constants.Fields.ORT,
						"Ort\\:[^\\S\\n]+([^\\,]+)\\,\\s([^\\,]+)\\,\\s(.+)\\r\\n", 1));
		valueProviders
				.add(new RegexValueProvider(Constants.Fields.ORTSTEIL,
						"Ort\\:[^\\S\\n]+([^\\,]+)\\,\\s([^\\,]+)\\,\\s(.+)\\r\\n", 2));
		valueProviders
				.add(new RegexValueProvider(Constants.Fields.STRASSE,
						"Ort\\:[^\\S\\n]+([^\\,]+)\\,\\s([^\\,]+)\\,\\s(.+)\\r\\n", 3));
		valueProviders.add(new RegexValueProvider(Constants.Fields.BEMERKUNG, "Besonderheit\\:[^\\S\\n]+(.*)\\r\\n"));
		this.valueProviders = Collections.unmodifiableSet(valueProviders);
	}

	@Override
	public boolean isApplicable(Email<String> email) {
		return email.getSubject().startsWith(UnwetterValueExtractor.SUBJECT_PREFIX);
	}

	@Override
	public void extract(Email<String> email, Consumer<Set<Value>> valuesConsumer) {
		String[] bodyParts = email.getBody().split(UnwetterValueExtractor.BODY_DELIMITER);

		// first and last bodyparts are useless
		for (int i = 1; i < bodyParts.length - 1; i++) {
			String bodyPart = bodyParts[i];
			valuesConsumer.accept(valueProviders.stream().map(valueProvider -> {
				Optional<String> value = valueProvider.extract(bodyPart);
				return value.map(v -> new Value(valueProvider.getName(), v)).orElse(null);
			}).filter(Objects::nonNull).collect(Collectors.toSet()));
		}
	}

	interface ValueProvider {
		String getName();

		Optional<String> extract(String str);
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	static class StaticValueProvider implements ValueProvider {
		@Getter
		private final String name;
		private final String value;

		@Override
		public Optional<String> extract(String str) {
			return Optional.of(this.value);
		}
	}

	static class RegexValueProvider implements ValueProvider {

		private static final Function<String, String> defaultSanitizer = value -> {
			String trimmed = value.trim();
			if (trimmed.length() == 0) {
				return null;
			}
			return trimmed;
		};

		@Getter
		private final String name;
		private final Pattern pattern;
		private final Function<String, String> sanitizer;
		private final int group;

		RegexValueProvider(String name, String pattern) {
			this(name, pattern, Function.identity());
		}

		RegexValueProvider(String name, String pattern, int group) {
			this(name, pattern, Function.identity(), group);
		}

		RegexValueProvider(String name, String pattern, Function<String, String> sanitizer) {
			this(name, pattern, sanitizer, 1);
		}

		RegexValueProvider(String name, String pattern, Function<String, String> sanitizer, int group) {
			this.name = name;
			this.pattern = Pattern.compile(pattern);
			this.sanitizer = sanitizer;
			this.group = group;
		}

		@Override
		public Optional<String> extract(String str) {
			Matcher matcher = pattern.matcher(str);
			if (!matcher.find()) {
				return Optional.empty();
			}

			return Optional.ofNullable(matcher.group(group)).map(defaultSanitizer).map(sanitizer);
		}
	}
}
