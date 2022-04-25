package de.ihrigb.fwla.edpmailadapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.ihrigb.fwla.edpmailadapter.Properties.UnwetterProperties;
import de.ihrigb.fwla.mail.Email;

public class UnwetterValueExtractorTest {

	private static Map<String, String> convert(Set<Value> values) {
		return values.stream().collect(Collectors.toMap(Value::getName, Value::getValue));
	}

	private UnwetterValueExtractor testee;

	@Before
	public void setUp() throws Exception {
		testee = new UnwetterValueExtractor(new UnwetterProperties());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test0() throws Exception {
		// @formatter:off
		String body = "Leitstelle Musterkreis\r\n"
			+ "bei Rückfragen Tel.: 12345 / 67890\r\n"
			+ "Unwetter-Einsatzliste\r\n"
			+ "______________________________________________________________________________\r\n"
			+ "OrgEinheit/Einsatznummer:	12 FF Musterstadt Fax / 1210048952\r\n"
			+ "Objekt:	\r\n"
			+ "Ort:	Musterstadt, Ortsteil, Musterstraße 8\r\n"
			+ "Einsatzstichwort/Priorität:	Sturm / 60\r\n"
			+ "Besonderheit:	Feuerwehr vor Ort\r\n"
			+ "______________________________________________________________________________\r\n"
			+ "Einsatzliste versendet am 21.10.2021 09:29:30\r\n";
		// @formatter:on

		Email<String> email = Mockito.mock(Email.class);
		Mockito.when(email.getBody()).thenReturn(body);

		ValuesConsumer valuesConsumer = new ValuesConsumer();
		testee.extract(email, valuesConsumer);
		assertEquals(1, valuesConsumer.getTimesCalled());
		Map<String, String> map = convert(valuesConsumer.next());

		assertEquals(8, map.size());
		
		assertEquals("1210048952", map.get("INTERNE_NUMMER"));
		assertEquals("Musterstadt", map.get("ORT"));
		assertEquals("Ortsteil", map.get("ORTSTEIL"));
		assertEquals("Musterstraße 8", map.get("STRASSE"));
		assertEquals("H", map.get("EINSATZART"));
		assertEquals("1", map.get("STICHWORT"));
		assertEquals("Sturm", map.get("MELDEBILD"));
		assertEquals("Feuerwehr vor Ort", map.get("BEMERKUNG"));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test1() throws Exception {
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
		Mockito.when(email.getBody()).thenReturn(body);

		ValuesConsumer valuesConsumer = new ValuesConsumer();
		testee.extract(email, valuesConsumer);
		assertEquals(2, valuesConsumer.getTimesCalled());

		Map<String, String> map = convert(valuesConsumer.next());
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

		map = convert(valuesConsumer.next());
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
	}
}
