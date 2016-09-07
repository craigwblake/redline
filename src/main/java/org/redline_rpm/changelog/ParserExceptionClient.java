package org.redline_rpm.changelog;

/**
 * Copyright (c) 2007-2016 FreeCompany
 */

public interface ParserExceptionClient {
	public static final String MUST_START_WITH_ASTERISK =
		"Changelog must begin with *";
	public static final String INVALID_DATE =
		"Invalid Changelog Date:";	
	public static final String OUT_OF_SEQUENCE =
		"Changelog entries must be in descending date order.";
	public static final String INCOMPLETE_ENTRY = 
		"Changelog entries must contain a Date, a User, and a Description";	
}
