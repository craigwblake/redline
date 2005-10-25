package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;
import java.util.zip.*;

import static java.security.KeyStore.PrivateKeyEntry;
import static java.security.KeyStore.PasswordProtection;
import static org.freecompany.redline.header.AbstractHeader.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;

public class Copier {

	public static void main( String[] args) throws Exception {
		PrivateKey key = null;
		if ( args.length > 2) {
			System.out.print( "Please enter keystore password: ");
			String password = new BufferedReader( new InputStreamReader( System.in)).readLine();
			KeyStore keystore = KeyStore.getInstance( KeyStore.getDefaultType());
			FileInputStream input = new FileInputStream( args[ 2]);
			keystore.load( input, password.toCharArray());
			input.close();

			key = (( PrivateKeyEntry) keystore.getEntry( "freecompany", new PasswordProtection( password.toCharArray()))).getPrivateKey();
		}
		
		new Copier().run( new FileInputStream( args[ 0]).getChannel(), new RandomAccessFile( args[ 1], "rw").getChannel(), key);
	}

	public void run( ReadableByteChannel in, FileChannel out, PrivateKey key) throws Exception {
		Format format = new Scanner().run( in);
		
		Entry< byte[]> dsaEntry = ( Entry< byte[]>) format.getSignature().getEntry( DSAHEADER);
		Entry< byte[]> gpgEntry = ( Entry< byte[]>) format.getSignature().getEntry( LEGACY_GPG);
		Entry< String[]> shaEntry = ( Entry< String[]>) format.getSignature().getEntry( SHA1HEADER);
		format.getSignature().removeEntry( gpgEntry);
		if ( key == null) {
			format.getSignature().removeEntry( dsaEntry);
			dsaEntry = null;
			format.getSignature().removeEntry( shaEntry);
			shaEntry = null;
		} else {
			if ( dsaEntry != null) {
				dsaEntry.setSize( dsaEntry.size());
				dsaEntry.setValues( null);
			}
			if ( shaEntry != null) {
				shaEntry.setSize( shaEntry.size());
				shaEntry.setValues( null);
			}
		}

		format.write( out);
		
		System.out.println( "Pending: " + format.getSignature().getPending());
		
		in = Channels.newChannel( new GZIPInputStream( Channels.newInputStream( in))); 
		WritableByteChannel zipped = Channels.newChannel( new GZIPOutputStream( Channels.newOutputStream( out)));
		SigningChannel dsa = dsaEntry == null ? null : new SigningChannel( zipped, key, "DSA");
		SigningChannel sha = shaEntry == null ? null : new SigningChannel( dsa == null ? zipped : dsa, key, "SHA1withRSA");
		
		WritableByteChannel output = sha != null ? sha : dsa != null ? dsa : zipped;
		ByteBuffer buffer = ByteBuffer.allocate( 4096);
		CpioHeader header;
		do {
			header = new CpioHeader();
			header.read( in);
			header.write( output);
			int remaining = Util.round( header.getFileSize(), 3);
			while ( remaining > 0) {
				if ( remaining < buffer.capacity()) buffer.limit( remaining);
				remaining -= in.read( buffer);
				buffer.flip();
				while ( buffer.hasRemaining()) output.write( buffer);
				buffer.clear();
				if ( buffer.limit() != buffer.capacity()) buffer.reset();
			}
		} while ( !header.isLast());
		Util.empty( output, ByteBuffer.wrap( new byte[] { 0, 0}));
		
		if ( dsaEntry != null) dsaEntry.setValues( dsa.sign());
		if ( shaEntry != null) {
			byte[] signature = sha.sign();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream printer = new PrintStream( baos);
			for ( byte b : signature) printer.format( "%02x", b);
			printer.flush();
			shaEntry.setValues( new String[]{ baos.toString()});
		}

		//System.out.println( "Writing out SHA signature: " + shaEntry.getValues()[ 0]);

		format.getSignature().writePending();
		
		zipped.close();
		out.close();
		in.close();
	}
}
