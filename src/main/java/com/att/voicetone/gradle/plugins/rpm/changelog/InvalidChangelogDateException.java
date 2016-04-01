package com.att.voicetone.gradle.plugins.rpm.changelog;

public class InvalidChangelogDateException extends ChangelogParseException {

	public InvalidChangelogDateException(String message) {
		super(INVALID_DATE+message);
	}

}
