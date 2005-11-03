package org.freecompany.redline;

import org.freecompany.redline.header.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class Util {

	private Util() {}

	public static ByteBuffer fill( ReadableByteChannel in, int size) throws IOException {
		return fill( in, ByteBuffer.allocate( size));
	}

	public static ByteBuffer fill( ReadableByteChannel in, ByteBuffer buffer) throws IOException {
		while ( buffer.hasRemaining()) if ( in.read( buffer) == -1) throw new BufferUnderflowException();
		buffer.flip();
		return buffer;
	}

	public static void empty( WritableByteChannel out, ByteBuffer buffer) throws IOException {
		while ( buffer.hasRemaining()) out.write( buffer);
	}

	public static void check( int expected, int actual) throws IOException {
		if ( expected != actual) throw new IOException( "check expected " + Integer.toHexString( 0xff & expected) + ", found " + Integer.toHexString( 0xff & actual));
	}

	public static void check( byte expected, byte actual) throws IOException {
		if ( expected != actual) throw new IOException( "check expected " + Integer.toHexString( 0xff & expected) + ", found " + Integer.toHexString( 0xff & actual));
	}

	public static int round( int start, int boundary) {
		return ( start + boundary) & ~boundary;
	}

	public static void pad( ByteBuffer buffer, int boundary) {
		buffer.position( round( buffer.position(), boundary));
	}

	public static void dump( byte[] data) {
		dump( data, System.out);
	}

	public static void dump( byte[] data, Appendable out) {
		dump( ByteBuffer.wrap( data), out);
	}

	public static void dump( char[] data) {
		dump( data, System.out);
	}

	public static void dump( char[] data, Appendable out) {
		dump( Charset.forName( "US-ASCII").encode( CharBuffer.wrap( data)), out);
	}

	public static void dump( ByteBuffer buf, Appendable out) {
		Formatter fmt = new Formatter( out);

		int pos = buf.position();
		fmt.format( "%8x:", pos & ~0xf);
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < ( pos & 0xf); i++) {
			fmt.format( "   ");
			builder.append( ' ');
		}
		while ( buf.hasRemaining()) {
			byte b = buf.get();
			fmt.format( " %2x", b);
			if ( ' ' <= b && b < 0x7f) {
				builder.append(( char) b);
			} else {
				builder.append( ' ');
			}
			if ( buf.hasRemaining() && ( buf.position() & 0xf) == 0) {
				fmt.format( " %s\n%8x:", builder, buf.position());
				builder.setLength( 0);
			}
		}
		buf.position( pos);
	}
}
