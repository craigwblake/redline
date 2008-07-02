package org.freecompany.redline;

import java.io.IOException;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * General utilities needed to read and write
 * RPM files. Some of these utilities are available
 * elsewhere but reproduced here to minimize runtime
 * dependencies.
 */
public class Util extends org.freecompany.util.text.Util {

	private Util() {}

	/**
	 * Converts path characters from their native
	 * format to the "forward-slash" format expected
	 * within RPM files.
	 */
	public static String normalizePath( final String path) {
		return path.replace( '\\', '/');
	}

	/**
	 * Creates a new buffer and fills it with bytes from the
	 * provided channel. The amount of data to read is specified
	 * in the arguments.
	 *
	 * @param in the channel to read from
	 * @param size the number of bytes to read into a new buffer
	 * @return a new buffer containing the bytes read
	 * @throws IOException if an IO error occurs
	 */
	public static ByteBuffer fill( ReadableByteChannel in, int size) throws IOException {
		return fill( in, ByteBuffer.allocate( size));
	}

	/**
	 * Fills the provided buffer it with bytes from the
	 * provided channel. The amount of data to read is
	 * dependant on the available space in the provided
	 * buffer.
	 *
	 * @param in the channel to read from
	 * @param buffer the buffer to read into
	 * @return the provided buffer
	 * @throws IOException if an IO error occurs
	 */
	public static ByteBuffer fill( ReadableByteChannel in, ByteBuffer buffer) throws IOException {
		while ( buffer.hasRemaining()) if ( in.read( buffer) == -1) throw new BufferUnderflowException();
		buffer.flip();
		return buffer;
	}

	/**
	 * Empties the contents of the given buffer into the
	 * writable channel provided. The buffer will be copied
	 * to the channel in it's entirety.
	 *
	 * @param out the channel to write to
	 * @param buffer the buffer to write out to the channel
	 * @throws IOException if an IO error occurs
	 */
	public static void empty( WritableByteChannel out, ByteBuffer buffer) throws IOException {
		while ( buffer.hasRemaining()) out.write( buffer);
	}

	/**
	 * Checks that two integers are the same, while generating
	 * a formatted error message if they are not. The error
	 * message will indicate the hex value of the integers if
	 * they do not match.
	 *
	 * @param expected the expected value
	 * @param actual the actual value
	 * @throws IOException if the two values do not match
	 */
	public static void check( int expected, int actual) throws IOException {
		if ( expected != actual) throw new IOException( "check expected " + Integer.toHexString( 0xff & expected) + ", found " + Integer.toHexString( 0xff & actual));
	}

	/**
	 * Checks that two bytes are the same, while generating
	 * a formatted error message if they are not. The error
	 * message will indicate the hex value of the bytes if
	 * they do not match.
	 *
	 * @param expected the expected value
	 * @param actual the actual value
	 * @throws IOException if the two values do not match
	 */
	public static void check( byte expected, byte actual) throws IOException {
		if ( expected != actual) throw new IOException( "check expected " + Integer.toHexString( 0xff & expected) + ", found " + Integer.toHexString( 0xff & actual));
	}

	public static int difference( int start, int boundary) {
		return (( boundary + 1) - ( start & boundary)) & boundary;
	}

	public static int round( int start, int boundary) {
		return ( start + boundary) & ~boundary;
	}

	/**
	 * Pads the given buffer up to the indicated boundary.
	 * The RPM file format requires that headers be aligned
	 * with various boundaries, this method pads output
	 * to match the requirements.
	 *
	 * @param buffer the buffer to pad zeros into
	 * @param boundary the boundary to which we need to pad
	 */
	public static void pad( ByteBuffer buffer, int boundary) {
		buffer.position( round( buffer.position(), boundary));
	}
}
