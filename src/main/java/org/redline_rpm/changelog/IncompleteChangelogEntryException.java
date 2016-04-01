package org.redline_rpm.changelog;

public class IncompleteChangelogEntryException extends ChangelogParseException {

	public IncompleteChangelogEntryException() {
		super(INCOMPLETE_ENTRY);
	}

}
