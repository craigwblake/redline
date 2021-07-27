package org.redline_rpm.header;

public class Signature extends AbstractHeader {

	public Signature() {
		for ( SignatureTag tag : SignatureTag.values()) tags.put( tag.getCode(), tag);
	}

	protected boolean pad() { return true; }

	public enum SignatureTag implements Tag {
		SIGNATURES( 62, 7, "signatures"),
		SIGSIZE( 257, 4, "sigsize"),
		LEGACY_SIGSIZE( 1000, 4, "sigsize"),
		// Version 3 OpenPGP Signature Packet RSA signature of the header and payload areas
		PGP( 259, 7, "pgp"),
		LEGACY_PGP( 1002, 7, "pgp"),
		MD5( 261, 7, "md5"),
		LEGACY_MD5( 1004, 7, "md5"),
		// Version 3 OpenPGP Signature Packet DSA signature of the header and payload areas
		GPG( 262, 7, "gpg"),
		LEGACY_GPG( 1005, 7, "gpg"),
		PAYLOADSIZE( 1007, 4, "payloadsize"),
		RESERVEDSPACE( 1008, 4, "reservedspace"),
		// SHA digest of just the header section
		SHA1HEADER( 269, STRING_ENTRY, "sha1header"),
		LEGACY_SHA1HEADER( 1010, STRING_ENTRY, "sha1header"),
		// DSA signature of just the header section, depends on GPG
		DSAHEADER( 267, 7, "dsaheader"),
		LEGACY_DSAHEADER( 1011, 7, "dsaheader"),
		// RSA signature of just the header section, depends on PGP
		RSAHEADER( 268, 7, "rsaheader"),
		SHA256HEADER( 273, STRING_ENTRY, "sha256header"),
		LEGACY_RSAHEADER( 1012, 7, "rsaheader"),
		FILEDIGESTALGO( 5011, INT32_ENTRY, "filedigestalgo");

		private int code;
		private int type;
		private String name;

		private SignatureTag( final int code, final int type, final String name) {
			this.code = code;
			this.type = type;
			this.name = name;
		}

		public int getCode() { return code; }
		public int getType() { return type; }
		public String getName() { return name; }
		
		public boolean isArrayType() {
			if (this.type == NULL_ENTRY) {
				return false;
			}
			return true;
		}
	}
}
