package org.freecompany.redline.header;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Signature extends AbstractHeader {

	public Signature() {
		for ( SignatureTag tag : SignatureTag.values()) tags.put( tag.getCode(), tag);
	}

	public enum SignatureTag implements Tag {
		SIGNATURES( 62, "signatures"),
		SIGSIZE( 257, "sigsize"),
		LEGACY_SIGSIZE( 1000, "sigsize"),
		PGP( 259, "pgp"),
		LEGACY_PGP( 1002, "pgp"),
		MD5( 261, "md5"),
		LEGACY_MD5( 1004, "md5"),
		GPG( 262, "gpg"),
		LEGACY_GPG( 1005, "gpg"),
		PAYLOADSIZE( 1007, "payloadsize"),
		SHA1HEADER( 269, "sha1header"),
		LEGACY_SHA1HEADER( 1010, "sha1header"),
		DSAHEADER( 267, "dsaheader"),
		LEGACY_DSAHEADER( 1011, "dsaheader"),
		RSAHEADER( 268, "rsaheader"),
		LEGACY_RSAHEADER( 1012, "rsaheader");

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
