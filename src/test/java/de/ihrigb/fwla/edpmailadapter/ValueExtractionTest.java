package de.ihrigb.fwla.edpmailadapter;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import de.ihrigb.fwla.edpmailadapter.ValueExtraction.Value;

public class ValueExtractionTest {

	private static Map<String, String> convert(Set<Value> values) {
		return values.stream().collect(Collectors.toMap(Value::getName, Value::getValue));
	}

	@Test
	public void test1() throws Exception {
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
				+ "EM:MUSTER 1  alarmiert:   Wache ab:   Einsatz an:   Einsatz ab:   Ende:";
		// @formatter:on

		Email email = new Email("sender", subject, body, Instant.now());

		Set<Value> values = ValueExtraction.extract(email);
		Map<String, String> map = convert(values);

		assertEquals(10, map.size());

		assertEquals("F", map.get("EINSATZART"));
		assertEquals("2", map.get("STICHWORT"));
		assertEquals("Feuer mittel", map.get("STICHWORT_KLARTEXT"));
		assertEquals("brennen Stroh / Heuballen auf freiem Feld", map.get("MELDUNG"));
		assertEquals("1234567890", map.get("INTERNE_NUMMER"));
		assertEquals("Musterstadt", map.get("ORT"));
		assertEquals("Ortsteil", map.get("ORTSTEIL"));
		assertEquals("Musterstraße 8", map.get("STRASSE"));
		assertEquals("BLA - DENG BAUM", map.get("OBJEKTNAME"));
		assertEquals("1", map.get("SONDERSIGNAL"));
	}

}
