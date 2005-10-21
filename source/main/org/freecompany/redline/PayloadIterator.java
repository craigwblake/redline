package org.freecompany.redline.payload;

import org.freecompany.redline.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

public class Payload {

	protected static final int CPIO_HEADER = 76;

	public enum Permission { ON, OFF };

	protected ByteChannel channel;
	protected CharSequence header;

	public void read( ReadableByteChannel channel) throws IOException {
		ByteBuffer descriptor = Util.fill( channel, CPIO_HEADER);
		header = Charset.forName( "US-ASCII").decode( descriptor).toString();
		System.out.println( "Header:\n" + header);
	}

	public void write( WritableByteChannel channel) throws IOException {
		ByteBuffer descriptor = ByteBuffer.allocate( CPIO_HEADER);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "File: ").append( header).append( "\n");
		return builder.toString();
	}
}
