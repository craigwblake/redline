package com.att.voicetone.gradle.plugins.rpm.changelog;

public class IncompleteChangelogEntryException extends ChangelogParseException {

	public IncompleteChangelogEntryException() {
		super(INCOMPLETE_ENTRY);
	}

}
