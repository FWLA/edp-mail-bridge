package de.ihrigb.fwla.edpmailadapter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.ihrigb.fwla.edpmailadapter.Properties.ExtractionProperties;
import de.ihrigb.fwla.edpmailadapter.ValueExtraction.RegexValueProvider.Source;
import de.ihrigb.fwla.mail.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

class ValueExtraction {

	private final Set<ValueProvider> valueProviders;

	ValueExtraction(ExtractionProperties properties) {
		// MELDENDER
		// <<<FEHLT>>>

		// MELDEWEG_TELEFON
		// <<<FEHLT>>>

		// HAUSNUMMER
		// <<<FEHLT>>>

		Set<ValueProvider> valueProviders = new HashSet<>();
		valueProviders.add(new RegexValueProvider("EINSATZART", "Einsatzanlass\\:\\s+(\\w)", v -> {
			if ("U".equals(v)) {
				return "H";
			}
			return v;
		}));
		valueProviders.add(new RegexValueProvider("STICHWORT", "\\d+\\s\\/\\s\\w\\-(.+?)\\s\\-\\s", Source.SUBJECT));
		valueProviders
				.add(new RegexValueProvider("STICHWORT_KLARTEXT", "Einsatzanlass\\:\\s\\w\\s(.+)\\s(O H N E|M I T)"));
		valueProviders.add(new RegexValueProvider("MELDUNG", "Meldebild\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("MELDEBILD", "Meldebild\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("INTERNE_NUMMER", "(\\d+)", Source.SUBJECT));
		valueProviders.add(new RegexValueProvider("ORT", "Ort\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("ORTSTEIL", "Ortsteil\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("STRASSE", "Straße\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("OBJEKTNAME", "Objekt\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("BEMERKUNG", "Bemerkung\\:[^\\S\\n]+(.+)\\r\\n"));
		valueProviders.add(new RegexValueProvider("KOORDX",
				"Koordinaten\\:[^\\S\\n]+POINT\\s\\((\\d+\\.\\d+)\\s\\d+\\.\\d+\\)\\r\\n"));
		valueProviders.add(new RegexValueProvider("KOORDY",
				"Koordinaten\\:[^\\S\\n]+POINT\\s\\(\\d+\\.\\d+\\s(\\d+\\.\\d+)\\)\\r\\n"));
		valueProviders.add(
				new RegexValueProvider("SONDERSIGNAL", "Einsatzanlass\\:[^\\S\\n]+.+(O H N E|M I T).+\\r\\n", value -> {
					if ("M I T".equals(value)) {
						return "1";
					}
					return "0";
				}));
		valueProviders.add(new RegexValueProvider("MELDUNG_LST",
				"Zeiten\\:[^\\S\\n]+(\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d\\:\\d\\d\\:\\d\\d).+\\r\\n"));
		if (properties.getAlarmEm() != null) {
			valueProviders.add(new RegexValueProvider("ALARM_LST",
					"EM\\:" + properties.getAlarmEm() + "\\s+alarmiert\\:\\s(\\d\\d\\:\\d\\d\\:\\d\\d).+(\\r\\n)?"));
		}
		this.valueProviders = Collections.unmodifiableSet(valueProviders);
	}

	Set<Value> extract(Email<String> email) {
		return valueProviders.stream().map(valueProvider -> {
			Optional<String> value = valueProvider.extract(email);
			return value.map(v -> new Value(valueProvider.getName(), v)).orElse(null);
		}).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	static class Value {
		private final String name;
		private final String value;
	}

	static interface ValueProvider {
		String getName();

		Optional<String> extract(Email<String> email);
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
		private final Source source;
		private final Function<String, String> sanitizer;
		private final int group;

		RegexValueProvider(String name, String pattern) {
			this(name, pattern, Function.identity());
		}

		RegexValueProvider(String name, String pattern, Function<String, String> sanitizer) {
			this(name, pattern, Source.BODY, sanitizer);
		}

		RegexValueProvider(String name, String pattern, Source source) {
			this(name, pattern, source, Function.identity(), 1);
		}

		RegexValueProvider(String name, String pattern, Source source, Function<String, String> sanitizer) {
			this(name, pattern, source, sanitizer, 1);
		}

		RegexValueProvider(String name, String pattern, Source source, Function<String, String> sanitizer, int group) {
			this.name = name;
			this.pattern = Pattern.compile(pattern);
			this.source = source;
			this.sanitizer = sanitizer;
			this.group = group;
		}

		@Override
		public Optional<String> extract(Email<String> email) {
			String text;
			switch (source) {
				case SUBJECT:
					text = email.getSubject();
					break;
				case BODY:
					text = email.getBody();
					break;
				default:
					return Optional.empty();
			}

			Matcher matcher = pattern.matcher(text);
			if (!matcher.find()) {
				return Optional.empty();
			}

			return Optional.ofNullable(matcher.group(group)).map(defaultSanitizer).map(sanitizer);
		}

		enum Source {
			SUBJECT, BODY;
		}
	}
}
