package org.freecompany.redline;

import java.io.IOException;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class Util extends org.freecompany.util.text.Util {

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

	public static int difference( int start, int boundary) {
		return (( boundary + 1) - ( start & boundary)) & boundary;
	}

	public static int round( int start, int boundary) {
		return ( start + boundary) & ~boundary;
	}

	public static void pad( ByteBuffer buffer, int boundary) {
		buffer.position( round( buffer.position(), boundary));
	}
}
