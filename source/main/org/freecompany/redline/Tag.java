package org.freecompany.redline;

import static org.freecompany.redline.Type.*;

public enum Tag {

	SIG_SIZE( 1000, INT32),
	SIG_MD5( 1001, BINARY),
	SIG_PGP( 1002, BINARY),
	RPM_NAME( 1000, STRING),
	RPM_VERSION( 1001, STRING),
	RPM_RELEASE( 1002, STRING),
	RPM_SUMMARY( 1004, STRING),
	RPM_DESCRIPTION( 1005, STRING),
	RPM_BUILDTIME( 1006, STRING),
	RPM_BUILDHOST( 1007, STRING),
	RPM_SIZE( 1009, STRING),
	RPM_VENDOR( 1011, STRING);

	private int code;
	private Type type;

	private Tag( int code, Type type) {
		this.code = code;
		this.type = type;
	}

	public int getCode() {
		return code;
	}

	public Type getType() {
		return type;
	}
}
