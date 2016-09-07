package org.redline_rpm.changelog;

/**
 * This exception is thrown when Changelog entries are not in descending order by date
 * Copyright (c) 2007-2016 FreeCompany 
 */

public class DatesOutOfSequenceException extends ChangelogParseException {

	public DatesOutOfSequenceException() {
		super(OUT_OF_SEQUENCE);
	}

}
