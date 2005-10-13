package org.freecompany.redline;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Signature extends AbstractHeader {

	static { for ( SignatureTag tag : SignatureTag.values()) TAGS.put( tag.getCode(), tag); }

	public enum SignatureTag implements Tag {
		SIGSIZE( 1000, "sigsize"),
		PGP( 1002, "pgp"),
		MD5( 1004, "md5"),
		GPG( 1005, "gpg"),
		PAYLOADSIZE( 1007, "payloadsize"),
		SHA1HEADER( 1010, "sha1header"),
		DSAHEADER( 1011, "dsaheader"),
		RSAHEADER( 1012, "rsaheader");

		private int code;
		private String name;

		private SignatureTag( final int code, final String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() { return code; }
		public String getName() { return name; }
	}
}
