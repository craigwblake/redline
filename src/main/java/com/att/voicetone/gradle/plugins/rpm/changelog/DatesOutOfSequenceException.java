package com.att.voicetone.gradle.plugins.rpm.changelog;

public class DatesOutOfSequenceException extends ChangelogParseException {

	public DatesOutOfSequenceException() {
		super(OUT_OF_SEQUENCE);
	}

}
