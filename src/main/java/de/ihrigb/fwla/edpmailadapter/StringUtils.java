package de.ihrigb.fwla.edpmailadapter;

import java.util.Collection;

class StringUtils {

	static boolean containsIgnoreCase(Collection<String> col, String needle) {
		if (col == null) {
			return false;
		}

		for (String str : col) {
			if (needle == null && str == null) {
				return true;
			}
			if (needle != null && needle.equalsIgnoreCase(str)) {
				return true;
			}
		}
		return false;
	}

	private StringUtils() {
	}
}
