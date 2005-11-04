package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;
import java.security.PrivateKey;

import static org.freecompany.redline.ChannelWrapper.*;
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
	protected IncludeFiles files = new IncludeFiles();

	protected Entry< byte[]> signature;
	protected Entry< byte[]> immutable;

	protected byte[] getSignature( int count) {
		return getSpecial( 0x0000003E, count);
	}

	protected byte[] getImmutable( int count) {
		return getSpecial( 0x0000003F, count);
	}

	protected byte[] getSpecial( int tag, int count) {
		final ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.putInt( tag);
		buffer.putInt( 0x00000007);
		buffer.putInt( count * -16);
		buffer.putInt( 0x00000010);
		return buffer.array();
	}

	/**
	 * Initializes the builder and sets some required fields to known values.
	 */
	public Builder() {
		signature = ( Entry< byte[]>) format.getSignature().addEntry( SIGNATURES, 16);
		immutable = ( Entry< byte[]>) format.getHeader().addEntry( HEADERIMMUTABLE, 16);
		format.getHeader().createEntry( HEADERI18NTABLE, "C");
		format.getHeader().createEntry( BUILDTIME, ( int) ( System.currentTimeMillis() / 1000));
		format.getHeader().createEntry( RPMVERSION, "4.4.2");
		format.getHeader().createEntry( PAYLOADFORMAT, "cpio");
		format.getHeader().createEntry( PAYLOADCOMPRESSOR, "gzip");
	}

	/**
	 * Sets the package information, such as the package name and the version.
	 */
	public void setPackage( CharSequence name, CharSequence version, CharSequence release) {
		format.getLead().setName( name + "-" + version + "-" + release);
		format.getHeader().createEntry( NAME, name);
		format.getHeader().createEntry( VERSION, version);
		format.getHeader().createEntry( RELEASE, release);
	}
	
	public void setType( RpmType type) {
		format.getLead().setType( type);
	}

	public void setPlatform( Architecture arch, Os os) {
		format.getLead().setArch( arch);
		format.getLead().setOs( os);
		
		final CharSequence archName = arch.toString().toLowerCase();
		final CharSequence osName = os.toString().toLowerCase();
		format.getHeader().createEntry( ARCH, archName);
		format.getHeader().createEntry( OS, osName);
		format.getHeader().createEntry( PLATFORM, archName + "-" + osName);
		format.getHeader().createEntry( RHNPLATFORM, archName);
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

	public void setVendor( CharSequence vendor) {
		format.getHeader().createEntry( VENDOR, vendor);
	}

	public void setPackager( CharSequence packager) {
		format.getHeader().createEntry( PACKAGER, packager);
	}

	public void setUrl( CharSequence url) {
		format.getHeader().createEntry( URL, url);
	}

	public void setProvides( CharSequence provides) {
		format.getHeader().createEntry( PROVIDENAME, provides);
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
	public void addFile( CharSequence target, File source) throws Exception {
		files.addFile( new File( target.toString()), source);
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
		final WritableChannelWrapper output = new WritableChannelWrapper( original);

		/*
		final Map< PrivateKey, Entry< byte[]>> map = new HashMap< PrivateKey, Entry< byte[]>>();
		for ( PrivateKey key : signatures) {
			if ( "MD5withRSA".equals( key.getAlgorithm())) map.put( key, ( Entry< byte[]>) format.getSignature().addEntry( GPG, GPGSIZE));
			else if ( "SHA1withDSA".equals( key.getAlgorithm())) map.put( key, ( Entry< byte[]>) format.getSignature().addEntry( DSAHEADER, DSASIZE));
			else throw new IOException( "Unknown key type '" + key.getAlgorithm() + "'.");
		}
		*/

		format.getHeader().createEntry( DIRNAMES, files.getDirNames());
		format.getHeader().createEntry( BASENAMES, files.getBaseNames());
		format.getHeader().createEntry( FILEMD5S, files.getMD5s());
		format.getHeader().createEntry( FILESIZES, files.getSizes());
		format.getHeader().createEntry( FILEMODES, files.getModes());
		format.getHeader().createEntry( FILERDEVS, files.getRdevs());
		format.getHeader().createEntry( FILEMTIMES, files.getMtimes());
		format.getHeader().createEntry( FILELINKTOS, files.getLinkTos());
		format.getHeader().createEntry( FILEFLAGS, files.getFlags());
		format.getHeader().createEntry( FILEUSERNAME, files.getUsers());
		format.getHeader().createEntry( FILEGROUPNAME, files.getGroups());
		format.getHeader().createEntry( FILEVERIFYFLAGS, files.getVerifyFlags());
		format.getHeader().createEntry( FILEDEVICES, files.getDevices());
		format.getHeader().createEntry( FILEINODES, files.getInodes());
		format.getHeader().createEntry( FILELANGS, files.getLangs());
		format.getHeader().createEntry( FILEDEPENDSX, files.getDependsX());
		format.getHeader().createEntry( FILEDEPENDSN, files.getDependsN());
		format.getHeader().createEntry( FILECONTEXTS, files.getContexts());

		final Entry< int[]> sigsize = ( Entry< int[]>) format.getSignature().addEntry( LEGACY_SIGSIZE, 1);
		final Entry< int[]> payload = ( Entry< int[]>) format.getSignature().addEntry( PAYLOADSIZE, 1);
		final Entry< byte[]> md5 = ( Entry< byte[]>) format.getSignature().addEntry( LEGACY_MD5, 16);
		final Entry< String[]> sha = ( Entry< String[]>) format.getSignature().addEntry( SHA1HEADER, 1);
		sha.setSize( SHASIZE);

		format.getLead().write( original);
		signature.setValues( getSignature( format.getSignature().count()));
		format.getSignature().write( original);

		/*
		for ( PrivateKey key : map.keySet()) {
			final Entry< byte[]> entry = map.get( key);
			final WritableByteChannel encrypted = new EncryptionChannel( channel) {
				public void sign( final byte[] signature) { entry.setValues( signature); }
			};
		}
		*/
		
		final Key< Integer> sigsizekey = output.start();
		final Key< byte[]> shakey = output.start( "SHA");
		final Key< byte[]> md5key = output.start( "MD5");

		immutable.setValues( getImmutable( format.getHeader().count()));
		format.getHeader().write( output);
		sha.setValues( new String[] { Util.hex( output.finish( shakey))});

		final GZIPOutputStream zip = new GZIPOutputStream( Channels.newOutputStream( output));
		final WritableChannelWrapper compressor = new WritableChannelWrapper( Channels.newChannel( zip));
		final Key< Integer> payloadkey = compressor.start();
		
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);
		for ( CpioHeader header : files.headers()) {
			final File source = files.source( header);
			final String path = "." + files.target( header).getAbsolutePath();
			header.setName( path);
			header.write( compressor);
			
			FileChannel in = new FileInputStream( source).getChannel();
			while ( in.read(( ByteBuffer) buffer.rewind()) > 0) compressor.write(( ByteBuffer) buffer.flip());
			Util.empty( compressor, ByteBuffer.wrap( new byte[ Util.round( header.getFileSize(), 3) - ( int) source.length()]));
			in.close();
		}
		
		final CpioHeader trailer = new CpioHeader();
		trailer.setLast();
		trailer.write( compressor);

		int length = compressor.finish( payloadkey);
		int pad = Util.difference( length, 3);
		Util.empty( compressor, ByteBuffer.wrap( new byte[ pad]));
		length += pad;

		payload.setValues( new int[] { length});
		zip.finish();
		
		md5.setValues( output.finish( md5key));
		sigsize.setValues( new int[] { output.finish( sigsizekey)});
		format.getSignature().writePending( original);
		output.close();
	}
}
