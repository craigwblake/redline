package org.redline_rpm.changelog;

public abstract class ChangelogParseException extends Exception 
implements ParserExceptionClient{


	public ChangelogParseException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}
