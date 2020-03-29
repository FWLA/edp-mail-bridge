package de.ihrigb.fwla.edpmailadapter;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;

import de.ihrigb.fwla.edpmailadapter.MailFilter.FilterResult;
import de.ihrigb.fwla.edpmailadapter.Properties.ReceivingProperties;

public class MailFilterTest {

	private MailFilter testee;

	@Test
	public void testFilterNullWhitelist() throws Exception {
		ReceivingProperties properties = new ReceivingProperties();
		testee = new MailFilter(properties);

		assertEquals(FilterResult.ACCEPTED, testee.filter("sender"));
	}

	@Test
	public void testFilterEmptyWhitelist() throws Exception {
		ReceivingProperties properties = new ReceivingProperties();
		properties.setWhitelist(Collections.emptySet());
		testee = new MailFilter(properties);

		assertEquals(FilterResult.ACCEPTED, testee.filter("sender"));
	}

	@Test
	public void testFilterRejected() throws Exception {
		ReceivingProperties properties = new ReceivingProperties();
		properties.setWhitelist(Collections.singleton("rejected"));
		testee = new MailFilter(properties);

		assertEquals(FilterResult.REJECTED, testee.filter("sender"));
	}

	@Test
	public void testFilterAccepted() throws Exception {
		ReceivingProperties properties = new ReceivingProperties();
		properties.setWhitelist(Collections.singleton("sender"));
		testee = new MailFilter(properties);

		assertEquals(FilterResult.ACCEPTED, testee.filter("sender"));
	}
}
