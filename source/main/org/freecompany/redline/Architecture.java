package org.freecompany.redline;

public enum Architecture {

	I386( 1);

	private int code;

	private Architecture( int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
