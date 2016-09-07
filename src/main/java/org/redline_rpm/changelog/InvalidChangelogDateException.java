package org.redline_rpm.changelog;

/**
 * This exception is thrown when the date portion of a change log
 * cannot be parsed.  This includes a date whose Day of Week is 
 * incorrect for the rest of the date.
 * 
 * Copyright (c) 2007-2016 FreeCompany 
 */
public class InvalidChangelogDateException extends ChangelogParseException {

	public InvalidChangelogDateException(String message) {
		super(INVALID_DATE+message);
	}

}
