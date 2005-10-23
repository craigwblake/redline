package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

public class Copier {

	public static void main( String[] args) throws Exception {
		new Copier().run( Channels.newChannel( System.in), new RandomAccessFile( args[ 0], "rw").getChannel());
	}

	public void run( ReadableByteChannel in, FileChannel out) throws Exception {
		Format format = new Scanner().run( in);
		format.write( out);
		in = Channels.newChannel( new GZIPInputStream( Channels.newInputStream( in))); 
		WritableByteChannel zipped = Channels.newChannel( new GZIPOutputStream( Channels.newOutputStream( out)));
		
		ByteBuffer buffer = ByteBuffer.allocate( 4096);
		CpioHeader header;
		do {
			header = new CpioHeader();
			header.read( in);
			header.write( zipped);
			int remaining = Util.round( header.getFileSize(), 3);
			while ( remaining > 0) {
				if ( remaining < buffer.capacity()) buffer.limit( remaining);
				remaining -= in.read( buffer);
				buffer.flip();
				while ( buffer.hasRemaining()) zipped.write( buffer);
				buffer.clear();
				if ( buffer.limit() != buffer.capacity()) buffer.reset();
			}
		} while ( !header.isLast());
		Util.empty( zipped, ByteBuffer.wrap( new byte[] { 0, 0}));
		zipped.close();
		out.close();
		in.close();
	}
}
