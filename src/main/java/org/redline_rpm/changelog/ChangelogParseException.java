package org.redline_rpm.changelog;

/**
 * Base Class of all exceptions thrown by the ChangeLogParser
 *
 * Copyright (c) 2007-2016 FreeCompany 
 */

public abstract class ChangelogParseException extends Exception 
implements ParserExceptionClient{

	public ChangelogParseException(String message) {
		super(message);
	}
}
