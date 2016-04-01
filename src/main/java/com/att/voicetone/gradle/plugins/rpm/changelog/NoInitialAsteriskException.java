package com.att.voicetone.gradle.plugins.rpm.changelog;

public class NoInitialAsteriskException extends ChangelogParseException {

	public NoInitialAsteriskException() {
		super(MUST_START_WITH_ASTERISK);
	}

}
