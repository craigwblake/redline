package org.freecompany.redline;

import java.util.*;

public class Header {

	protected Map< Tag, Object> entries = new HashMap< Tag, Object>();

	public void put( Tag tag, Object object) {
		entries.put( tag, object);
	}

	public Map< Tag, Object> getEntries() {
		return entries;
	}
}
