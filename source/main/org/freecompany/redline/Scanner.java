package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

public class Scanner {

	public static void main( String[] args) throws Exception {
		ReadableByteChannel in = Channels.newChannel( System.in);
		Format format = new Scanner().run( in);
		System.out.println( format);
		InputStream compressed = new GZIPInputStream( System.in);
		in = Channels.newChannel( compressed); 
		CpioHeader header;
		do {
			header = new CpioHeader();
			header.read( in);
			System.out.println( header);
			compressed.skip( Util.round( header.getFileSize(), 3));
		} while ( !header.isLast());
	}

	public Format run( ReadableByteChannel in) throws IOException {
		Format format = new Format();
		format.read( in);
		return format;
	}
}
