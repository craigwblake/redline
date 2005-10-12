package org.freecompany.redline;

public enum Type {

	NULL( 0),
	CHAR( 1),
	INT8( 2),
	INT16( 3),
	INT32( 4),
	INT64( 5),
	STRING( 6),
	BINARY( 7),
	STRING_ARRAY( 8),
	I18NSTRING( 9);

	private int code;

	private Type( int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
