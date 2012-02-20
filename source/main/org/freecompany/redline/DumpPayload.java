package org.freecompany.redline;

import org.freecompany.redline.header.Format;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Convenience class for dumping the payload of an
 * RPM file to a file. This is useful in debugging
 * problems in RPM generation.
 */
public class DumpPayload {

	/**
	 * Dumps the contents of the payload for an RPM file to
	 * the provided file.  This method accepts an RPM file from
	 * standard input and dumps it's payload out to the file
	 * name provided as the first argument.
	 */
	public static void main( String[] args) throws Exception {
		ReadableByteChannel in = Channels.newChannel( System.in);
		Format format = new Scanner().run( new ReadableChannelWrapper( in));
		FileChannel out = new FileOutputStream( args[ 0]).getChannel();
		
		long position = 0;
		long read;
		while (( read = out.transferFrom( in, position, 1024)) > 0) position += read;
	}
}
