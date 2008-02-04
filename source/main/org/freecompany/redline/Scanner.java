package org.freecompany.redline;

import org.freecompany.redline.header.Format;
import org.freecompany.redline.payload.CpioHeader;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.zip.GZIPInputStream;

import static org.freecompany.redline.ChannelWrapper.*;
import static org.freecompany.redline.header.AbstractHeader.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;
import static org.freecompany.redline.header.Header.HeaderTag.*;

public class Scanner {

	public static void main( String[] args) throws Exception {
		ReadableChannelWrapper in = new ReadableChannelWrapper( Channels.newChannel( System.in));
		Format format = new Scanner().run( in);
		System.out.println( format);
		InputStream uncompressed = new GZIPInputStream( System.in);
		in = new ReadableChannelWrapper( Channels.newChannel( uncompressed));
		CpioHeader header;
		int total = 0;
		do {
			header = new CpioHeader();
			total = header.read( in, total);
			System.out.println( header);
			final int skip = header.getFileSize();
			if ( uncompressed.skip( skip) != skip) throw new RuntimeException( "Skip failed.");
			total += header.getFileSize();
		} while ( !header.isLast());
	}

	public Format run( ReadableChannelWrapper in) throws Exception {
		Format format = new Format();
		
		Key< Integer> lead = in.start();
		format.getLead().read( in);
		System.out.println( "Lead ended at '" + in.finish( lead) + "'.");

		Key< Integer> signature = in.start();
		int count = format.getSignature().read( in);
		int expected = ByteBuffer.wrap(( byte[]) format.getSignature().getEntry( SIGNATURES).getValues(), 8, 4).getInt() / -16;
		System.out.println( "Signature ended at '" + in.finish( signature) + "' and contained '" + count + "' headers (expected '" + expected + "').");

		Key< Integer> header = in.start();
		count = format.getHeader().read( in);
		expected = ByteBuffer.wrap(( byte[]) format.getHeader().getEntry( HEADERIMMUTABLE).getValues(), 8, 4).getInt() / -16;
		System.out.println( "Header ended at '" + in.finish( header) + " and contained '" + count + "' headers (expected '" + expected + "').");

		return format;
	}
}
