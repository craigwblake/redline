package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

import static org.freecompany.redline.ChannelWrapper.*;

public class Scanner {

	public static void main( String[] args) throws Exception {
		ReadableChannelWrapper in = new ReadableChannelWrapper( Channels.newChannel( System.in));
		Format format = new Scanner().run( in);
		System.out.println( format);
		InputStream compressed = new GZIPInputStream( System.in);
		in = new ReadableChannelWrapper( Channels.newChannel( compressed));
		CpioHeader header;
		do {
			header = new CpioHeader();
			header.read( in);
			System.out.println( header);
			compressed.skip( Util.round( header.getFileSize(), 3));
		} while ( !header.isLast());
	}

	public Format run( ReadableChannelWrapper in) throws Exception {
		Format format = new Format();
		
		Key< Integer> lead = in.start();
		format.getLead().read( in);
		System.out.println( "Lead ended at '" + in.finish( lead) + "'.");
		
		Key< Integer> signature = in.start();
		format.getSignature().read( in);
		System.out.println( "Signature ended at '" + in.finish( signature) + "'.");

		Key< Integer> header = in.start();
		format.getHeader().read( in);
		System.out.println( "Header ended at '" + in.finish( header) + "'.");

		return format;
	}
}
