package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.zip.*;

import static org.freecompany.redline.header.AbstractHeader.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;

public class Copier {

	public static void main( String[] args) throws Exception {
		new Copier().run( Channels.newChannel( System.in), new RandomAccessFile( args[ 0], "rw").getChannel());
	}

	public void run( ReadableByteChannel in, FileChannel out) throws Exception {
		Format format = new Scanner().run( in);
		
		Entry dsaEntry = format.getSignature().getEntry( DSAHEADER);
		dsaEntry.setSize( dsaEntry.size());
		dsaEntry.setValues( null);
		Entry shaEntry = format.getSignature().getEntry( SHA1HEADER);
		shaEntry.setSize( shaEntry.size());
		shaEntry.setValues( null);

		format.write( out);
		
		System.out.println( "Pending: " + format.getSignature().getPending());
		
		in = Channels.newChannel( new GZIPInputStream( Channels.newInputStream( in))); 
		WritableByteChannel zipped = Channels.newChannel( new GZIPOutputStream( Channels.newOutputStream( out)));
		SigningChannel dsa = new SigningChannel( zipped, "DSA");
		SigningChannel sha = new SigningChannel( dsa, "SHA1withDSA");
		
		ByteBuffer buffer = ByteBuffer.allocate( 4096);
		CpioHeader header;
		do {
			header = new CpioHeader();
			header.read( in);
			header.write( sha);
			int remaining = Util.round( header.getFileSize(), 3);
			while ( remaining > 0) {
				if ( remaining < buffer.capacity()) buffer.limit( remaining);
				remaining -= in.read( buffer);
				buffer.flip();
				while ( buffer.hasRemaining()) sha.write( buffer);
				buffer.clear();
				if ( buffer.limit() != buffer.capacity()) buffer.reset();
			}
		} while ( !header.isLast());
		Util.empty( sha, ByteBuffer.wrap( new byte[] { 0, 0}));
		
		dsaEntry.setValues( dsa.sign());
		shaEntry.setValues( new String( sha.sign()));

		format.getSignature().writePending();
		
		zipped.close();
		out.close();
		in.close();
	}
}
