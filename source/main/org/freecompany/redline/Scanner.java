package org.freecompany.redline;

import org.freecompany.redline.header.Format;
import org.freecompany.redline.payload.CpioHeader;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.zip.GZIPInputStream;

import static org.freecompany.redline.ChannelWrapper.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;
import static org.freecompany.redline.header.Header.HeaderTag.*;

/**
 * The scanner reads an RPM file and outputs useful
 * information about it's contents. The scanner will
 * output the headers of the RPM format itself, as
 * well as the individual headers for the particular
 * packaged content.
 *
 * In addition, the scanner will scan through the
 * payload and output information about each file
 * contained in the embedded CPIO payload.
 */
public class Scanner {

	/**
	 * Scans a file and prints out useful information.
	 * This utility reads from standard input, and parses
	 * the binary contents of the RPM file.
	 *
	 * @throws Exception if an error occurrs while
	 * reading the RPM file or it's contents
	 */
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

	/**
	 * Reads the headers of an RPM and returns a description of it
	 * and it's format.
	 *
	 * @param in the channel wrapper to read input from
	 * @return information describing the RPM file
	 * @throws Exception if an error occurrs reading the file
	 */
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
