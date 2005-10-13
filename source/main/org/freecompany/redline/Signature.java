package org.freecompany.redline;

import java.io.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import static org.freecompany.redline.Type.*;

public class Signature extends AbstractHeader< Signature.Tag> {

	public enum Tag {

		SIG_SIZE( 1000, INT32, "Signature Size"),
		SIG_MD5( 1001, BINARY, "Signature MD5 Sum"),
		SIG_PGP( 1002, BINARY, "Signature PGP");

		private static Map< Integer, Tag> tags = new ConcurrentHashMap< Integer, Tag>();
		private int code;
		private Type type;
		private String name;

		private Tag( int code, Type type, String name) {
			this.code = code;
			this.type = type;
			this.name = name;
		}

		public int getCode() {
			return code;
		}

		public Type getType() {
			return type;
		}

		public String toString() {
			return name;
		}

		public static Tag getTag( int code) {
			return tags.get( code);
		}
	}

	protected Tag getTag( int code) {
		return Tag.getTag( code);
	}
}
