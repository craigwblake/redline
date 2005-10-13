package org.freecompany.redline;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class Scanner {
	private static final int LEAD_SIZE = 96;

	private ReadableByteChannel in;
	public void load( ReadableByteChannel in) throws IOException {
		ByteBuffer lead = Util.fill( in, LEAD_SIZE);

		Util.check(( byte) 0xed, lead.get());
		Util.check(( byte) 0xab, lead.get());
		Util.check(( byte) 0xee, lead.get());
		Util.check(( byte) 0xdb, lead.get());

		int major = 0xff & lead.get();
		int minor = 0xff & lead.get();
		System.out.println( "Version: " + major + "." + minor);
		System.out.println( "Type: " + lead.getShort());
		System.out.println( "Arch: " + lead.getShort());
		lead.position( lead.position() + 66);
		System.out.println( "OS: " + lead.getShort());
		System.out.println( "Sig type: " + lead.getShort());
		if ( lead.remaining() != 16) System.err.println( "expected 16 remaining, found " + lead.remaining());

		AbstractHeader signature = new Signature( in);
		AbstractHeader header = new Header( in);

		System.out.println( "Signature header:");
		while ( true) {
			Header.Entry entry = signature.nextEntry();
			if ( entry == null) break;
			System.out.println( entry);
		}

		System.out.println( "RPM header:");
		while ( true) {
			Header.Entry entry = header.nextEntry();
			if ( entry == null) break;
			System.out.println( entry);
		}
	}
}
