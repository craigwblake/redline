package org.redline_rpm.changelog;

/**
 * This exception is when a change log entry does not begin with an asterisk.
 * In actuality, this can only happen at the beginning of the changelog file
 * 
 * Copyright (c) 2007-2016 FreeCompany 
 */
public class NoInitialAsteriskException extends ChangelogParseException {

	public NoInitialAsteriskException() {
		super(MUST_START_WITH_ASTERISK);
	}

}
