package org.freecompany.redline.payload;

import org.freecompany.redline.*;
import org.freecompany.util.text.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

/**
 * This class provides a means to read file content from the compressed CPIO stream
 * that is the body of an RPM distributable.  Iterative calls to to read header will
 * result in a header description being returned which includes a count of how many bytes
 * to read from the channel for the file content.
 */
public class CpioHeader {
	
	protected static final int CPIO_HEADER = 110;
	protected static final CharSequence MAGIC = "070701";

	protected Charset charset = Charset.forName( "US-ASCII");

	protected int magic;
	protected int inode;
	protected int mode;
	protected int uid;
	protected int gid;
	protected int nlink;
	protected long mtime;
	protected int filesize;
	protected int devMinor;
	protected int devMajor;
	protected int rdevMinor;
	protected int rdevMajor;
	protected int checksum;
	protected CharSequence name;

	/**
	 * Test to see if this is the last header, and is therefore the end of the
	 * archive.  Uses the CPIO magic trailer value to denote the last header of
	 * the stream.
	 */
	public boolean isLast() {
		return Comparison.equals( "TRAILER!!!", name);
	}
	
	public int getFileSize() {
		return filesize;
	}

	protected ByteBuffer writeSix( CharSequence data) {
		return charset.encode( pad( data, 6));
	}

	protected ByteBuffer writeEight( int data) {
		return charset.encode( pad( Integer.toHexString( data), 8));
	}

	protected String readSix( CharBuffer buffer) {
		return readBytes( buffer, 6);
	}

	protected int readEight( CharBuffer buffer) {
		return Integer.parseInt( readBytes( buffer, 8), 16);
	}

	protected String readBytes( CharBuffer buffer, int length) {
		if ( buffer.length() < length) throw new IllegalStateException( "Insufficent capacity buffer.");
		char[] chars = new char[ length];
		buffer.get( chars);
		//Util.dump( chars);
		//System.out.println();
		return new String( chars);
	}

	protected String pad( CharSequence sequence, int length) {
		while ( sequence.length() < length) sequence = "0" + sequence;
		return sequence.toString();
	}

	public void read( final ReadableByteChannel channel) throws IOException {
		ByteBuffer descriptor = Util.fill( channel, CPIO_HEADER);
		CharBuffer buffer = charset.decode( descriptor);

		String magic = readSix( buffer);
		if ( !Comparison.equals( MAGIC, magic)) throw new IllegalStateException( "Invalid magic number '" + magic + "'.");
		inode = readEight( buffer);
		mode = readEight( buffer);
		uid = readEight( buffer);
		gid = readEight( buffer);
		nlink = readEight( buffer);
		mtime = 1000L * readEight( buffer);
		filesize = readEight( buffer);
		devMajor = readEight( buffer);
		devMinor = readEight( buffer);
		rdevMajor = readEight( buffer);
		rdevMinor = readEight( buffer);
		int namesize = readEight( buffer);
		checksum = readEight( buffer);

		name = charset.decode(( ByteBuffer) Util.fill( channel, Util.round( namesize, 1)).limit( namesize - 1));
	}

	/**
	 * Writed the content for the CPIO header, including the name immediately following.  The name data is reounded
	 * to the nearest 2 byte boundary as CPIO requires by appending a null when needed.
	 */
	public void write( final WritableByteChannel channel) throws IOException {
		int length = name.length() + 1;
		ByteBuffer descriptor = ByteBuffer.allocate( CPIO_HEADER);
		descriptor.put( writeSix( MAGIC));
		descriptor.put( writeEight( inode));
		descriptor.put( writeEight( mode));
		descriptor.put( writeEight( uid));
		descriptor.put( writeEight( gid));
		descriptor.put( writeEight( nlink));
		descriptor.put( writeEight(( int) ( mtime / 1000)));
		descriptor.put( writeEight( filesize));
		descriptor.put( writeEight( devMajor));
		descriptor.put( writeEight( devMinor));
		descriptor.put( writeEight( rdevMajor));
		descriptor.put( writeEight( rdevMinor));
		descriptor.put( writeEight( length));
		descriptor.put( writeEight( checksum));
		
		descriptor.flip();
		Util.empty( channel, descriptor);
		Util.empty( channel, charset.encode( CharBuffer.wrap( name)));
		Util.empty( channel, ByteBuffer.wrap( Util.round( length, 1) != length ? new byte[] { 0, 0} : new byte[] { 0}));
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Inode: ").append( inode).append( "\n");
		builder.append( "Mode: ").append( mode).append( "\n");
		builder.append( "UID: ").append( uid).append( "\n");
		builder.append( "GID: ").append( gid).append( "\n");
		builder.append( "Nlink: ").append( nlink).append( "\n");
		builder.append( "MTime: ").append( new Date( mtime)).append( "\n");
		builder.append( "FileSize: ").append( filesize).append( "\n");
		builder.append( "DevMinor: ").append( devMinor).append( "\n");
		builder.append( "DevMajor: ").append( devMajor).append( "\n");
		builder.append( "RDevMinor: ").append( rdevMinor).append( "\n");
		builder.append( "RDevMajor: ").append( rdevMajor).append( "\n");
		builder.append( "NameSize: ").append( name.length() + 1).append( "\n");
		builder.append( "Name: ").append( name).append( "\n");
		return builder.toString();
	}
}
