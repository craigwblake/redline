package org.freecompany.redline;

import org.freecompany.redline.header.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class Scanner {

	public static void main( String[] args) throws Exception {
		Format format = new Scanner().run( Channels.newChannel( System.in));
		System.out.println( format);
		for ( Payload payload : format.getPayloads()) System.out.println( payload);
	}

	public Format run( ReadableByteChannel in) throws IOException {
		Format format = new Format();
		format.read( in);
		return format;
	}
}
