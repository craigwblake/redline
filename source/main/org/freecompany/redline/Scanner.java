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

		System.out.println( "Signature header:");
		AbstractHeader signature = new Signature();
	   	signature.read( in);
		for ( AbstractHeader.Entry entry : signature.entries()) System.out.println( entry);

		System.out.println( "RPM header:");
		AbstractHeader header = new Header();
	   	header.read( in);
		for ( AbstractHeader.Entry entry : header.entries()) System.out.println( entry);
	}
}
