package de.ihrigb.fwla.edpmailadapter;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.ihrigb.fwla.mail.Email;

public class WritingEmailHandlerTest {

	private WritingEmailHandler testee;

	private FileWriter fileWriter;
	private ValueExtractor defaultValueExtractor;
	private ValueExtractor valueExtractor;

	@BeforeEach
	public void setUp() throws Exception {
		fileWriter = Mockito.mock(FileWriter.class);
		defaultValueExtractor = Mockito.mock(ValueExtractor.class);
		valueExtractor = Mockito.mock(ValueExtractor.class);
		testee = new WritingEmailHandler(fileWriter, defaultValueExtractor, Collections.singletonList(valueExtractor));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testValueExtractorApplicable() throws Exception {

		Email<String> email = Mockito.mock(Email.class);

		Mockito.when(valueExtractor.isApplicable(email)).thenReturn(true);

		testee.handle(email);

		Mockito.verify(valueExtractor, Mockito.times(1)).isApplicable(email);
		Mockito.verify(valueExtractor, Mockito.times(1)).extract(Mockito.same(email), Mockito.any());
		Mockito.verify(defaultValueExtractor, Mockito.never()).extract(Mockito.same(email), Mockito.any());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testValueExtractorNotApplicable() throws Exception {

		Email<String> email = Mockito.mock(Email.class);

		Mockito.when(valueExtractor.isApplicable(email)).thenReturn(false);

		testee.handle(email);

		Mockito.verify(valueExtractor, Mockito.times(1)).isApplicable(email);
		Mockito.verify(valueExtractor, Mockito.never()).extract(Mockito.same(email), Mockito.any());
		Mockito.verify(defaultValueExtractor, Mockito.times(1)).extract(Mockito.same(email), Mockito.any());
	}
}
