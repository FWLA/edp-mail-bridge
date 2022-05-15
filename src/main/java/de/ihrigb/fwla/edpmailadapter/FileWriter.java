package de.ihrigb.fwla.edpmailadapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import de.ihrigb.fwla.edpmailadapter.Properties.WritingProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class FileWriter {

	private static String currentDateTime() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd_HH-mm-ss");
		return formatter.format(LocalDateTime.now());
	}

	private static String randomString() {
		String uuid = UUID.randomUUID().toString();
		String[] uuidArr = uuid.split("-");
		return uuidArr[uuidArr.length - 1];
	}

	private final WritingProperties properties;

	void write(Set<Value> values) {

		if (values == null || values.isEmpty()) {
			log.warn("Tried to write an emtpy file. Aborting handling.");
			return;
		}

		try {
			File tempFile = File.createTempFile("edp_", ".txt");
			log.debug("Writing to tempfile {}.", tempFile.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
					PrintStream printStream = new PrintStream(fileOutputStream)) {
				for (Value value : values) {
					printStream.println(String.format("%s=%s", value.getName(), value.getValue()));
				}
			}

			String filename = String.format("%s-%s.txt", currentDateTime(), randomString());
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
