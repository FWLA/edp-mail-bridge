package de.ihrigb.fwla.edpmailadapter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import de.ihrigb.fwla.edpmailadapter.mail.Email;

@Disabled
public class ApplicationContextTest {

	private static Set<String> fileToLines(File file) throws IOException {
		return Files.lines(file.toPath()).collect(Collectors.toSet());
	}

	@TempDir
	private File tempDir;
	private WritingEmailHandler writingEmailHandler;

	@BeforeEach
	public void setUp() throws Exception {
		Properties properties = new Properties();
		properties.setWrite(new Properties.WritingProperties());
		properties.getWrite().setDirectory(tempDir.getAbsolutePath());
		properties.getExtraction().setAlarmEm("MUSTER\\s1");

		this.writingEmailHandler = new AppConfiguration().writingEmailHandler(properties);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testNormalEmail() throws Exception {
		String subject = "1234567890 / F-2 - BLA - DENG BAUM, Musterstadt-Ortsteil, Musterstraße 8";
		// @formatter:off
		String body = "Einsatznummer: 1234567890\r\n"
				+ "Einsatzort:    \r\n"
				+ "Objekt:        BLA - DENG BAUM\r\n"
				+ "Ort:           Musterstadt\r\n"
				+ "Ortsteil:      Ortsteil\r\n"
				+ "Straße:        Musterstraße 8\r\n"
				+ "Koordinaten:   POINT (1.39957217 12.63357332)\r\n"
				+ "Bemerkung:     kurz nach dem Wasser auf dem Feld\r\n"
				+ "Meldebild:     brennen Stroh / Heuballen auf freiem Feld\r\n"
				+ "Einsatzanlass: F Feuer mittel M I T   S O N D E R S I G N A L\r\n"
				+ "Zielort:       \r\n"
				+ "Zeiten:        28.03.2020 22:24:22     28.03.2020 22:26:27\r\n"
				+ "EM:MUST 01  alarmiert:   Wache ab:   Einsatz an:   Einsatz ab:   Ende: \r\n"
				+ "EM:MUSTER 1  alarmiert: 22:24:23  Wache ab:   Einsatz an:   Einsatz ab:   Ende:";
		// @formatter:on

		Email<String> email = Mockito.mock(Email.class);
		Mockito.when(email.getSubject()).thenReturn(subject);
		Mockito.when(email.getBody()).thenReturn(body);

		writingEmailHandler.handle(email);

		assertEquals(1, tempDir.list().length);
		File writtenFile = tempDir.listFiles()[0];
		Set<String> lines = fileToLines(writtenFile);
		Map<String, String> map = lines.stream().map(l -> l.split("=")).collect(Collectors.toMap(v -> v[0], v -> v[1]));

		assertEquals(16, map.size());

		assertEquals("F", map.get("EINSATZART"));
		assertEquals("2", map.get("STICHWORT"));
		assertEquals("Feuer mittel", map.get("STICHWORT_KLARTEXT"));
		assertEquals("brennen Stroh / Heuballen auf freiem Feld", map.get("MELDUNG"));
		assertEquals("brennen Stroh / Heuballen auf freiem Feld", map.get("MELDEBILD"));
		assertEquals("1234567890", map.get("INTERNE_NUMMER"));
		assertEquals("Musterstadt", map.get("ORT"));
		assertEquals("Ortsteil", map.get("ORTSTEIL"));
		assertEquals("Musterstraße 8", map.get("STRASSE"));
		assertEquals("BLA - DENG BAUM", map.get("OBJEKTNAME"));
		assertEquals("1", map.get("SONDERSIGNAL"));
		assertEquals("kurz nach dem Wasser auf dem Feld", map.get("BEMERKUNG"));
		assertEquals("1.39957217", map.get("KOORDX"));
		assertEquals("12.63357332", map.get("KOORDY"));
		assertEquals("28.03.2020 22:24:22", map.get("MELDUNG_LST"));
		assertEquals("22:24:23", map.get("ALARM_LST"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testUnwetterEmail() throws Exception {
		String subject = "UNWETTERLAGE - Sammelemail";
		// @formatter:off
		String body = "Leitstelle Musterkreis\r\n"
			+ "bei Rückfragen Tel.: 12345 / 67890\r\n"
			+ "Unwetter-Einsatzliste\r\n"
			+ "______________________________________________________________________________\r\n"
			+ "OrgEinheit/Einsatznummer:       FF Musterstadt Fax / 1210028884\r\n"
			+ "Meldungseingang/Meldender/Telefon:      29.06.2021 13:31:48 h / Herr Mustermann, Otto / 0123456789\r\n"
			+ "Einsatzstichwort/Priorität:     Wasser / 60\r\n"
			+ "Besonderheit:   10cm, 30qm\r\n"
			+ "Ort:    Musterstadt, Nebenort, Musterweg 11\r\n"
			+ "Objekt:\r\n"
			+ "______________________________________________________________________________\r\n"
			+ "OrgEinheit/Einsatznummer:       FF Musterstadt Fax / 1210028913\r\n"
			+ "Meldungseingang/Meldender/Telefon:      29.06.2021 15:36:40 h / /\r\n"
			+ "Einsatzstichwort/Priorität:     1 / 0\r\n"
			+ "Besonderheit:\r\n"
			+ "Ort:    Musterstadt, Musterstadt, Musterstraße 17\r\n"
			+ "Objekt: FEUERWEHR MUSTERSTADT\r\n"
			+ "______________________________________________________________________________\r\n"
			+ "Einsatzliste versendet am 29.06.2021 16:13:50\r\n";
		// @formatter:on

		Email<String> email = Mockito.mock(Email.class);
		Mockito.when(email.getSubject()).thenReturn(subject);
		Mockito.when(email.getBody()).thenReturn(body);

		writingEmailHandler.handle(email);

		assertEquals(2, tempDir.list().length);


		for (File writtenFile : tempDir.listFiles()) {
			Set<String> lines = fileToLines(writtenFile);
			Map<String, String> map = lines.stream().map(l -> l.split("=")).collect(Collectors.toMap(v -> v[0], v -> v[1]));

			if ("1210028884".equals(map.get("INTERNE_NUMMER"))) {
				assertEquals(11, map.size());

				assertEquals("1210028884", map.get("INTERNE_NUMMER"));
				assertEquals("29.06.2021 13:31:48", map.get("MELDUNG_LST"));
				assertEquals("Herr Mustermann, Otto", map.get("MELDENDER"));
				assertEquals("0123456789", map.get("MELDEWEG_TELEFON"));
				assertEquals("Musterstadt", map.get("ORT"));
				assertEquals("Nebenort", map.get("ORTSTEIL"));
				assertEquals("Musterweg 11", map.get("STRASSE"));
				assertEquals("H", map.get("EINSATZART"));
				assertEquals("1", map.get("STICHWORT"));
				assertEquals("Wasser", map.get("MELDEBILD"));
				assertEquals("10cm, 30qm", map.get("BEMERKUNG"));
			} else if ("1210028913".equals(map.get("INTERNE_NUMMER"))) {
				assertEquals(9, map.size());
				
				assertEquals("1210028913", map.get("INTERNE_NUMMER"));
				assertEquals("29.06.2021 15:36:40", map.get("MELDUNG_LST"));
				assertEquals("Musterstadt", map.get("ORT"));
				assertEquals("Musterstadt", map.get("ORTSTEIL"));
				assertEquals("Musterstraße 17", map.get("STRASSE"));
				assertEquals("H", map.get("EINSATZART"));
				assertEquals("1", map.get("STICHWORT"));
				assertEquals("1", map.get("MELDEBILD"));
				assertEquals("FEUERWEHR MUSTERSTADT", map.get("OBJEKTNAME"));
			} else {
				fail("File content does not match any expected values.");
			}
		}
	}
}
