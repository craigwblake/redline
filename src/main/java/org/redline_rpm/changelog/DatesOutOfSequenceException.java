package org.redline_rpm.changelog;

public class DatesOutOfSequenceException extends ChangelogParseException {

	public DatesOutOfSequenceException() {
		super(OUT_OF_SEQUENCE);
	}

}
