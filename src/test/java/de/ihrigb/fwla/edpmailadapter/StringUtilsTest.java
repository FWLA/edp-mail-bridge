package de.ihrigb.fwla.edpmailadapter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void testContainsIgnoreCaseNullCollection() throws Exception {
		assertFalse("containsIgnoreCase should return false.", StringUtils.containsIgnoreCase(null, null));
	}

	@Test
	public void testContainsIgnoreCaseContainsNullNeedle() throws Exception {
		assertTrue("containsIgnoreCase should return true.",
				StringUtils.containsIgnoreCase(Arrays.asList("value", null), null));
	}

	@Test
	public void testContainsIgnoreCaseNotContainsNullNeedle() throws Exception {
		assertFalse("containsIgnoreCase should return true.",
				StringUtils.containsIgnoreCase(Arrays.asList("value1", "value2"), null));
	}

	@Test
	public void testContainsIgnoreCaseContainsStringNeedle() throws Exception {
		assertTrue("containsIgnoreCase should return true.",
				StringUtils.containsIgnoreCase(Arrays.asList("value1", "value2"), "value2"));
	}

	@Test
	public void testContainsIgnoreCaseNotContainsStringNeedle() throws Exception {
		assertFalse("containsIgnoreCase should return true.",
				StringUtils.containsIgnoreCase(Arrays.asList("value1", "value2"), "value3"));
	}
}
