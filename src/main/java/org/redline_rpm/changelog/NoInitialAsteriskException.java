package org.redline_rpm.changelog;

public class NoInitialAsteriskException extends ChangelogParseException {

	public NoInitialAsteriskException() {
		super(MUST_START_WITH_ASTERISK);
	}

}
