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
	 * archive.
	 */
	public boolean isLast() {
		return Comparison.equals( "TRAILER!!!", name);
	}
	
	public int getFileSize() {
		return filesize;
	}

	protected String readBytes( CharBuffer buffer, int length) {
		if ( buffer.length() < length) throw new IllegalStateException( "Insufficent capacity buffer.");
		char[] chars = new char[ length];
		buffer.get( chars);
		Util.dump( chars);
		return new String( chars);
	}

	protected String pad( CharSequence sequence, int length) {
		while ( sequence.length() < length) sequence = "0" + sequence;
		return sequence.toString();
	}

	public void read( final ReadableByteChannel channel) throws IOException {
		ByteBuffer descriptor = Util.fill( channel, CPIO_HEADER);
		CharBuffer buffer = charset.decode( descriptor);

		String magic = readBytes( buffer, 6);
		//if ( !Comparison.equals( MAGIC, magic)) throw new IllegalStateException( "Invalid magic number '" + magic + "'.");
		inode = Integer.parseInt( readBytes( buffer, 8), 16);
		mode = Integer.parseInt( readBytes( buffer, 8), 16);
		uid = Integer.parseInt( readBytes( buffer, 8), 16);
		gid = Integer.parseInt( readBytes( buffer, 8), 16);
		nlink = Integer.parseInt( readBytes( buffer, 8), 16);
		mtime = 1000L * Integer.parseInt( readBytes( buffer, 8), 16);
		filesize = Integer.parseInt( readBytes( buffer, 8), 16);
		devMajor = Integer.parseInt( readBytes( buffer, 8), 16);
		devMinor = Integer.parseInt( readBytes( buffer, 8), 16);
		rdevMajor = Integer.parseInt( readBytes( buffer, 8), 16);
		rdevMinor = Integer.parseInt( readBytes( buffer, 8), 16);
		int namesize = Integer.parseInt( readBytes( buffer, 8), 16);
		checksum = Integer.parseInt( readBytes( buffer, 8), 16);

		name = charset.decode(( ByteBuffer) Util.fill( channel, Util.round( namesize, 1)).limit( namesize));
	}

	public void write( final WritableByteChannel channel) throws IOException {
		ByteBuffer descriptor = ByteBuffer.allocate( CPIO_HEADER);
		descriptor.put( charset.encode( pad( MAGIC, 6)));
		descriptor.put( charset.encode( pad( Integer.toHexString( inode), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( mode), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( uid), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( gid), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( nlink), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString(( int) ( mtime / 1000)), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( filesize), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( devMajor), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( devMinor), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( rdevMajor), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( rdevMinor), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( name.length()), 8)));
		descriptor.put( charset.encode( pad( Integer.toHexString( checksum), 8)));
		descriptor.put( charset.encode( CharBuffer.wrap( name)));
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
		builder.append( "NameSize: ").append( name.length()).append( "\n");
		builder.append( "Name: ").append( name).append( "\n");
		return builder.toString();
	}
}
