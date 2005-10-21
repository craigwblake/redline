package org.freecompany.redline.payload;

import org.freecompany.redline.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

/**
 * This class provides a means to read file content from the compressed CPIO stream
 * that is the body of an RPM distributable.  Iterative calls to to read header will
 * result in a header description being returned which includes a count of how many bytes
 * to read from the channel for the file content.
 */
public class CpioReader {

	protected static final int CPIO_HEADER = 76;

	protected ByteChannel channel;
	protected CharSequence header;

	public static CpioHeader read( ReadableByteChannel channel) throws IOException {
		ByteBuffer descriptor = Util.fill( channel, CPIO_HEADER);
		header = Charset.forName( "US-ASCII").decode( descriptor).toString();
		System.out.println( "Header:\n" + header);
		return new CpioHeader();
	}

	public static void write( WritableByteChannel channel, CpioHeader header) throws IOException {
		ByteBuffer descriptor = ByteBuffer.allocate( CPIO_HEADER);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "File: ").append( header).append( "\n");
		return builder.toString();
	}
}
