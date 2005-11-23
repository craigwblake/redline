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

	public static final int FIFO = 1;
	public static final int CDEV = 2;
	public static final int DIR = 4;
	public static final int BDEV = 6;
	public static final int FILE = 8;
	public static final int SYMLINK = 10;
	public static final int SOCKET = 12;
	
	protected static final int CPIO_HEADER = 110;
	protected static final CharSequence MAGIC = "070701";
	protected static final CharSequence TRAILER = "TRAILER!!!";

	protected Charset charset = Charset.forName( "US-ASCII");

	protected int inode;
	protected int type;
	protected int permission;
	protected int uid;
	protected int gid;
	protected int nlink;
	protected long mtime;
	protected int filesize;
	protected int devMinor = 1;
	protected int devMajor = 9;
	protected int rdevMinor;
	protected int rdevMajor;
	protected int checksum;
	protected CharSequence name;

	public CpioHeader() {
	}

	public CpioHeader( final File file) {
		mtime = file.lastModified();
		filesize = ( int ) file.length();
		name = file.getName();
		setPermission( 0644);
		if ( file.isDirectory()) setType( DIR);
		else setType( FILE);
	}

	public int getType() { return type; }
	public int getPermission() { return permission; }
	public int getRdevMajor() { return rdevMajor; }
	public int getRdevMinor() { return rdevMinor; }
	public int getDevMajor() { return devMajor; }
	public int getDevMinor() { return devMinor; }
	public int getMtime() { return ( int) ( mtime / 1000L) ; }
	public int getInode() { return inode; }

	public int getMode() { return ( type << 12) | permission; }

	public void setPermission( int permission) { this.permission = permission; }
	public void setType( int type) { this.type = type; }

	/**
	 * Test to see if this is the last header, and is therefore the end of the
	 * archive.  Uses the CPIO magic trailer value to denote the last header of
	 * the stream.
	 */
	public boolean isLast() {
		return Comparison.equals( TRAILER, name);
	}

	public void setLast() {
		name = TRAILER;
	}

	public void setName( CharSequence name) {
		this.name = name;
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

	protected CharSequence readSix( CharBuffer buffer) {
		return readChars( buffer, 6);
	}

	protected int readEight( CharBuffer buffer) {
		return Integer.parseInt( readChars( buffer, 8).toString(), 16);
	}

	protected CharSequence readChars( CharBuffer buffer, int length) {
		if ( buffer.remaining() < length) throw new IllegalStateException( "Buffer has '" + buffer.remaining() + "' bytes but '" + length + "' are needed.");
		try {
			return buffer.subSequence( 0, length);
		} finally {
			buffer.position( buffer.position() + length);
		}
	}

	protected String pad( CharSequence sequence, final int length) {
		while ( sequence.length() < length) sequence = "0" + sequence;
		return sequence.toString();
	}

	protected int skip( final ReadableByteChannel channel, final int total) throws IOException {
		int skipped = Util.difference( total, 3);
		//System.out.println( "Skipping '" + skipped + "' bytes from stream at position '" + total + "'.");
		Util.fill( channel, skipped);
		return skipped;
	}

	public int skip( final WritableByteChannel channel, int total) throws IOException {
		int skipped = Util.difference( total, 3);
		Util.empty( channel, ByteBuffer.allocate( skipped));
		//System.out.println( "Skipping '" + skipped + "' bytes from stream at position '" + total + "'.");
		return skipped;
	}

	public int read( final ReadableByteChannel channel, int total) throws IOException {
		total += skip( channel, total);
		ByteBuffer descriptor = Util.fill( channel, CPIO_HEADER);
		CharBuffer buffer = charset.decode( descriptor);

		final CharSequence magic = readSix( buffer);
		if ( !Comparison.equals( MAGIC, magic)) throw new IllegalStateException( "Invalid magic number '" + magic + "' of length '" + magic.length() + "'.");
		inode = readEight( buffer);
		
		final int mode = readEight( buffer);
		permission = mode & 07777;
		type = mode >>> 12;
		
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
		total += CPIO_HEADER;

		name = charset.decode( Util.fill( channel, namesize - 1));
		Util.fill( channel, 1);
		total += namesize;
		total += skip( channel, total);
		return total;
	}

	/**
	 * Writed the content for the CPIO header, including the name immediately following.  The name data is rounded
	 * to the nearest 2 byte boundary as CPIO requires by appending a null when needed.
	 */
	public int write( final WritableByteChannel channel, int total) throws IOException {
		int length = name.length() + 1;
		ByteBuffer descriptor = ByteBuffer.allocate( CPIO_HEADER);
		descriptor.put( writeSix( MAGIC));
		descriptor.put( writeEight( inode));
		descriptor.put( writeEight( getMode()));
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

		total += CPIO_HEADER + length;
		Util.empty( channel, descriptor);
		Util.empty( channel, charset.encode( CharBuffer.wrap( name)));
		Util.empty( channel, ByteBuffer.allocate( 1));
		return total + skip( channel, total);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Inode: ").append( inode).append( "\n");
		builder.append( "Permission: ").append( Integer.toString( permission, 8)).append( "\n");
		builder.append( "Type: ").append( type).append( "\n");
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
