package de.ihrigb.fwla.edpmailadapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import de.ihrigb.fwla.edpmailadapter.Properties.WritingProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class WritingEmailHandler implements EmailHandler {

	private String currentDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss");
		return formatter.format(LocalDateTime.now());
	}

	private final WritingProperties properties;

	@Override
	public void handle(Email email) {
	
		String subject = email.getSubject();
		String body = email.getBody();

		Pattern pattern;
		Matcher matcher;

		Map<String, String> data = new HashMap<>();

		// EINSATZART
		pattern = Pattern.compile("Einsatzanlass\\:\\s+(\\w)");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("EINSATZART", matcher.group(1));
		}

		// STICHWORT
		pattern = Pattern.compile("\\d+\\s\\/\\s(.+)\\s\\-\\s");
		matcher = pattern.matcher(subject);
		if (matcher.find()) {
			data.put("STICHWORT", matcher.group(1));
		}

		// STICHWORT_KLARTEXT
		pattern = Pattern.compile("Einsatzanlass\\:\\s\\w\\s(.+)\\s(O H N E|M I T)");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("STICHWORT_KLARTEXT", matcher.group(1));
		}

		// MELDUNG
		pattern = Pattern.compile("Meldebild\\:[^\\S\\n]+(.+)\\r\\n");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("MELDUNG", matcher.group(1));
		}

		// MELDENDER
		// <<<FEHLT>>>

		// MELDEWEG_TELEFON
		// <<<FEHLT>>>

		// INTERNE_NUMMER
		pattern = Pattern.compile("(\\d+)");
		matcher = pattern.matcher(subject);
		if (matcher.find()) {
			data.put("INTERNE_NUMMER", matcher.group(1));
		}

		// ORT
		pattern = Pattern.compile("Ort\\:[^\\S\\n]+(.+)\\r\\n");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("ORT", matcher.group(1));
		}

		// ORTSTEIL
		pattern = Pattern.compile("Ortsteil\\:[^\\S\\n]+(.+)\\r\\n");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("ORTSTEIL", matcher.group(1));
		}

		// STRASSE
		pattern = Pattern.compile("Straße\\:[^\\S\\n]+(.+)\\r\\n");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("STRASSE", matcher.group(1));
		}

		// HAUSNUMMER
		// <<<FEHLT>>>

		// OBJEKTNAME
		pattern = Pattern.compile("Objekt\\:[^\\S\\n]+(.+)\\r\\n");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			data.put("OBJEKTNAME", matcher.group(1));
		}

		// SONDERSIGNAL
		pattern = Pattern.compile("Einsatzanlass\\:[^\\S\\n]+.+(O H N E|M I T).+\\r\\n");
		matcher = pattern.matcher(body);
		if (matcher.find()) {
			String group = matcher.group(1);
			if ("M I T".equals(group)) {
				data.put("SONDERSIGNAL", "1");
			} else {
				data.put("SONDERSIGNAL", "0");
			}
		}

		try {
			File tempFile = File.createTempFile("edp_", ".txt");
			log.debug("Writing to tempfile {}.", tempFile.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
					PrintStream printStream = new PrintStream(fileOutputStream)) {
				for (Entry<String, String> entry : data.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (value != null && (value = value.trim()).length() > 0) {
						printStream.println(String.format("%s=%s", key, value));
					}
				}
			}

			String filename = String.format("%s.txt", currentDateTime());
			File parentDirectory = new File(properties.getDirectory());
			File targetFile = new File(parentDirectory, filename);

			log.info("Target file {}.", targetFile.getAbsolutePath());

			FileUtils.copyFile(tempFile, targetFile);
			tempFile.delete();
		} catch (IOException e) {
			log.error("Exception during file writing.", e);
		}
	}
}
