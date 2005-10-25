package org.freecompany.redline;

import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.RpmType;
import org.freecompany.redline.header.Architecture;
import org.freecompany.redline.header.Os;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;
import java.security.*;

import static org.freecompany.redline.header.AbstractHeader.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;
import static org.freecompany.redline.header.Header.HeaderTag.*;

public class Builder {

	private static final int GPGSIZE = 65;
	private static final int DSASIZE = 65;
	private static final int SHASIZE = 41;
	private static final int MD5SIZE = 32;

	protected Format format = new Format();
	protected Set< PrivateKey> signatures = new HashSet< PrivateKey>();
	protected Set< String> dirnames = new HashSet< String>();
	protected Set< String> filenames = new HashSet< String>();
	protected Map< String, File> files = new HashMap< String, File>();

	public Builder() {
		format.getHeader().createEntry( BUILDTIME, ( int) ( System.currentTimeMillis() / 1000));
		format.getHeader().createEntry( RPMVERSION, "4.4.2");
		format.getHeader().createEntry( PAYLOADFORMAT, "cpio");
		format.getHeader().createEntry( PAYLOADCOMPRESSOR, "gzip");
	}

	public void setPackage( CharSequence name, CharSequence version, CharSequence release) {
		format.getLead().setName( name + "-" + version + "-" + release);
		format.getHeader().createEntry( NAME, name);
		format.getHeader().createEntry( VERSION, version);
		format.getHeader().createEntry( RELEASE, release);
	}
	
	public void setType( RpmType type) {
		format.getLead().setType( type);
	}

	public void setArch( Architecture arch) {
		format.getLead().setArch( arch);
		format.getHeader().createEntry( ARCH, arch.toString().toLowerCase());
	}

	public void setOs( Os os) {
		format.getLead().setOs( os);
		format.getHeader().createEntry( OS, os.toString().toLowerCase());
	}

	public void setSummary( CharSequence summary) {
		format.getHeader().createEntry( SUMMARY, summary);
	}

	public void setDescription( CharSequence description) {
		format.getHeader().createEntry( DESCRIPTION, description);
	}

	public void setBuildHost( CharSequence host) {
		format.getHeader().createEntry( BUILDHOST, host);
	}

	public void setLicense( CharSequence license) {
		format.getHeader().createEntry( LICENSE, license);
	}

	public void setGroup( CharSequence group) {
		format.getHeader().createEntry( GROUP, group);
	}

	/**
	 * Add the specified files to the repository payload in the provided
	 * order.  The required header entries will automatically be generated
	 * to record the directory names and file names, as well as their
	 * digests.
	 *
	 * @param target the absolute path at which to install this file.
	 * @param file the file content to include in this rpm.
	 */
	public void addFile( CharSequence target, File source) {
		File file = new File( target.toString());
		dirnames.add( file.getParent());
		filenames.add( file.getName());
		files.put( file.getAbsolutePath(), source);
	}

	/**
	 * Add a key to generate a new signature for the header and payload portions of the
	 * rpm file.
	 */
	public void addSignature( final PrivateKey key) {
		signatures.add( key);
	}

	/**
	 * Generates the rpm file to the provided writable channel.
	 */
	public void build( final FileChannel original) throws Exception {
		format.getHeader().createEntry( DIRNAMES, dirnames.toArray( new String[ dirnames.size()]));
		format.getHeader().createEntry( BASENAMES, filenames.toArray( new String[ filenames.size()]));

		final Map< PrivateKey, Entry< byte[]>> map = new HashMap< PrivateKey, Entry< byte[]>>();
		for ( PrivateKey key : signatures) {
			if ( "MD5withRSA".equals( key.getAlgorithm())) map.put( key, ( Entry< byte[]>) format.getSignature().addEntry( GPG, GPGSIZE));
			else if ( "SHA1withDSA".equals( key.getAlgorithm())) map.put( key, ( Entry< byte[]>) format.getSignature().addEntry( DSAHEADER, DSASIZE));
			else throw new IOException( "Unknown key type '" + key.getAlgorithm() + "'.");
		}

		// Since the RPM wants the MD5 to be signed to we have to run through each file first.  Yech.
		final String[] md5s = new String[ filenames.size()];
		int count = 0;
		for ( String path : files.keySet()) {
			final File file = files.get( path);
			final ReadableByteChannel input = md5Digest( new FileInputStream( file).getChannel(), md5s, count++);
			ByteBuffer buffer = ByteBuffer.allocate( 1024);
			while ( input.read( buffer) != -1) buffer.rewind();
			input.close();
		}
		final Entry< String[]> md5 = format.getHeader().createEntry( FILEMD5S, md5s);

		final Entry< String[]> sha = ( Entry< String[]>) format.getSignature().addEntry( SHA1HEADER, 1);
		sha.setSize( SHASIZE);

		format.getLead().write( original);
		format.getSignature().write( original);

		/**
		 * Wrapping the
		 */
		WritableByteChannel channel = new WritableByteChannel() {
			public int write( final ByteBuffer buffer) throws IOException { return original.write( buffer); }
			public boolean isOpen() { return original.isOpen(); }
			public void close() throws IOException {
				format.getSignature().writePending();
				format.getHeader().writePending();
				original.close();
			}
		};

		for ( PrivateKey key : map.keySet()) {
			final Signature signature = Signature.getInstance( key.getAlgorithm());
			channel = sign( channel, map.get( key), signature);
		}
		
		channel = shaDigest( channel, sha);
		format.getHeader().write( original);

		final ByteBuffer buffer = ByteBuffer.allocate( 4096);
		final WritableByteChannel zipped = Channels.newChannel( new GZIPOutputStream( Channels.newOutputStream( channel)));
		for ( String path : files.keySet()) {
			final File file = files.get( path);
			final CpioHeader header = new CpioHeader( file);
			header.setName( path);
			header.write( zipped);
			
			FileChannel in = new FileInputStream( file).getChannel();
			while ( in.read( buffer) != -1) {
				buffer.flip();
				while ( buffer.hasRemaining()) zipped.write( buffer);
				buffer.clear();
			}
			Util.empty( zipped, ByteBuffer.wrap( new byte[ Util.round( header.getFileSize(), 3) - ( int) file.length()]));
			in.close();
		}
		
		final CpioHeader trailer = new CpioHeader();
		trailer.setLast();
		trailer.write( zipped);
		Util.empty( zipped, ByteBuffer.wrap( new byte[] { 0, 0}));
		zipped.close();
	}

	protected WritableByteChannel sign( final WritableByteChannel channel, final Entry< byte[]> entry, final Signature signature) {
		return new WritableByteChannel() {
			public boolean isOpen() { return channel.isOpen(); }
			public int write( final ByteBuffer buffer) throws IOException {
				try {
					signature.update( buffer.duplicate());
				} catch ( SignatureException e) { throw new RuntimeException( e); }
				return channel.write( buffer);
			}
			public void close() throws IOException {
				try {
					entry.setValues( signature.sign());
				} catch ( SignatureException e) { throw new RuntimeException( e); }
				channel.close();
			}
		};
	}

	protected WritableByteChannel shaDigest( final WritableByteChannel channel, final Entry< String[]> sha) throws Exception {
		final MessageDigest digest = MessageDigest.getInstance( "SHA");
		return new WritableByteChannel() {
			public boolean isOpen() { return channel.isOpen(); }
			public int write( final ByteBuffer buffer) throws IOException {
				digest.update( buffer.duplicate());
				return channel.write( buffer);
			}
			public void close() throws IOException {
				sha.setValues( new String[] { hex( digest.digest())});
				channel.close();
			}
		};
	}

	protected ReadableByteChannel md5Digest( final ReadableByteChannel channel, final String[] md5s, final int index) throws Exception {
		final MessageDigest digest = MessageDigest.getInstance( "MD5");
		return new ReadableByteChannel() {
			public boolean isOpen() { return channel.isOpen(); }
			public int read( final ByteBuffer buffer) throws IOException {
				final int read = channel.read( buffer);
				digest.update(( ByteBuffer) buffer.duplicate().flip());
				return read;
			}
			public void close() throws IOException {
				md5s[ index] = hex( digest.digest());
				channel.close();
			}
		};
	}

	protected String hex( byte[] data) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final PrintStream printer = new PrintStream( baos);
		for ( byte b : data) printer.format( "%02x", b);
		printer.flush();
		return baos.toString();
	}
}
