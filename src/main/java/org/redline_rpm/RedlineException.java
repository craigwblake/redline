package org.redline_rpm;

import java.io.IOException;

@SuppressWarnings("serial")
public class RedlineException extends IOException {
	public RedlineException(String message) {
		super(message);
	}

	public RedlineException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
