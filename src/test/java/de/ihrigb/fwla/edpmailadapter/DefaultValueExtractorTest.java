package de.ihrigb.fwla.edpmailadapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.ihrigb.fwla.edpmailadapter.Properties.ExtractionProperties;
import de.ihrigb.fwla.mail.Email;

public class DefaultValueExtractorTest {

	private static Map<String, String> convert(Set<Value> values) {
		return values.stream().collect(Collectors.toMap(Value::getName, Value::getValue));
	}

	private DefaultValueExtractor testee;

	@BeforeEach
	public void setUp() throws Exception {
		ExtractionProperties properties = new ExtractionProperties();
		properties.setAlarmEm("MUSTER\\s1");
		this.testee = new DefaultValueExtractor(properties);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test0() throws Exception {
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

		ValuesConsumer valuesConsumer = new ValuesConsumer();
		testee.extract(email, valuesConsumer);
		assertEquals(1, valuesConsumer.getTimesCalled());
		Map<String, String> map = convert(valuesConsumer.next());

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
	@DisplayName("Test U Stichworte")
	@SuppressWarnings("unchecked")
	public void testUStichworte() throws Exception {
		String subject = "1234567890 / U-2 - BLA - DENG BAUM, Musterstadt-Ortsteil, Musterstraße 8";
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
				+ "Einsatzanlass: U Feuer mittel M I T   S O N D E R S I G N A L\r\n"
				+ "Zielort:       \r\n"
				+ "Zeiten:        28.03.2020 22:24:22     28.03.2020 22:26:27\r\n"
				+ "EM:MUST 01  alarmiert:   Wache ab:   Einsatz an:   Einsatz ab:   Ende: \r\n"
				+ "EM:MUSTER 1  alarmiert: 22:24:23  Wache ab:   Einsatz an:   Einsatz ab:   Ende:";
		// @formatter:on

		Email<String> email = Mockito.mock(Email.class);
		Mockito.when(email.getSubject()).thenReturn(subject);
		Mockito.when(email.getBody()).thenReturn(body);

		ValuesConsumer valuesConsumer = new ValuesConsumer();
		testee.extract(email, valuesConsumer);
		assertEquals(1, valuesConsumer.getTimesCalled());
		Map<String, String> map = convert(valuesConsumer.next());

		assertEquals(16, map.size());

		assertEquals("H", map.get("EINSATZART"));
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
	@DisplayName("Test '-' in OHNE/MIT SONDERSIGNAL")
	@SuppressWarnings("unchecked")
	public void testDashSondersignal() throws Exception {
		String subject = "1234567890 / U-2 - BLA - DENG BAUM, Musterstadt-Ortsteil, Musterstraße 8";
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
				+ "Einsatzanlass: U Feuer mittel M I T-S O N D E R S I G N A L\r\n"
				+ "Zielort:       \r\n"
				+ "Zeiten:        28.03.2020 22:24:22     28.03.2020 22:26:27\r\n"
				+ "EM:MUST 01  alarmiert:   Wache ab:   Einsatz an:   Einsatz ab:   Ende: \r\n"
				+ "EM:MUSTER 1  alarmiert: 22:24:23  Wache ab:   Einsatz an:   Einsatz ab:   Ende:";
		// @formatter:on

		Email<String> email = Mockito.mock(Email.class);
		Mockito.when(email.getSubject()).thenReturn(subject);
		Mockito.when(email.getBody()).thenReturn(body);

		ValuesConsumer valuesConsumer = new ValuesConsumer();
		testee.extract(email, valuesConsumer);
		assertEquals(1, valuesConsumer.getTimesCalled());
		Map<String, String> map = convert(valuesConsumer.next());

		assertEquals(16, map.size());

		assertEquals("H", map.get("EINSATZART"));
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
}
