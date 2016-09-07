package org.redline_rpm.changelog;

/**
 * This exception is thrown when parsing of the changelog file results in
 * an incomplete ChangeLogEntry
 * 
 * Copyright (c) 2007-2016 FreeCompany 
 */

public class IncompleteChangelogEntryException extends ChangelogParseException {

	public IncompleteChangelogEntryException() {
		super(INCOMPLETE_ENTRY);
	}

}
