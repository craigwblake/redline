package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import static org.freecompany.redline.ChannelWrapper.*;
import static org.freecompany.redline.header.AbstractHeader.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;
import static org.freecompany.redline.header.Header.HeaderTag.*;

/**
 * The normal entry point to the API used for building and RPM.  The API provides methods to
 * configure and add files to a new RPM.  The current version of the RPM format (3.0) requires
 * numerous headers to be set for an RPM to be valid.  All of the required fields are either
 * set automatically or exposed through setters in this builder class.  Any required fields are
 * marked in their respective method API documentation.
 */
public class Builder {

	private static final int GPGSIZE = 65;
	private static final int DSASIZE = 65;
	private static final int SHASIZE = 41;
	private static final int MD5SIZE = 32;

	protected final Format format = new Format();
	protected final Set< PrivateKey> signatures = new HashSet< PrivateKey>();
	protected final Map< String, CharSequence> dependencies = new LinkedHashMap< String, CharSequence>();

	protected final Entry< byte[]> signature = ( Entry< byte[]>) format.getSignature().addEntry( SIGNATURES, 16);
	protected final Entry< byte[]> immutable = ( Entry< byte[]>) format.getHeader().addEntry( HEADERIMMUTABLE, 16);

	protected IncludeFiles files = new IncludeFiles();

	/**
	 * Initializes the builder and sets some required fields to known values.
	 */
	public Builder() {
		format.getHeader().createEntry( HEADERI18NTABLE, "C");
		format.getHeader().createEntry( BUILDTIME, ( int) ( System.currentTimeMillis() / 1000));
		format.getHeader().createEntry( RPMVERSION, "4.4.2");
		format.getHeader().createEntry( PAYLOADFORMAT, "cpio");
		format.getHeader().createEntry( PAYLOADCOMPRESSOR, "gzip");

		addDependency( "rpmlib(CompressedFileNames)", "3.0.4-1");
		addDependency( "rpmlib(PayloadFilesHavePrefix)", "4.0-1");
	}

	public void addDependency( CharSequence name, CharSequence value) {
		dependencies.put( name.toString(), value);
	}

	/**
	 * Sets the package information, such as the rpm name, the version, and the release number.
	 * </p>
	 * This field is required.
	 * 
	 * @param name the name of the RPM package.
	 * @param version the version of the new package.
	 * @param release the release number, specified after the version, of the new RPM.
	 */
	public void setPackage( final CharSequence name, final CharSequence version, final CharSequence release) {
		format.getLead().setName( name + "-" + version + "-" + release);
		format.getHeader().createEntry( NAME, name);
		format.getHeader().createEntry( VERSION, version);
		format.getHeader().createEntry( RELEASE, release);
		format.getHeader().createEntry( PROVIDEVERSION, 8, new String[] { "0:" + version + "-" + release});
		format.getHeader().createEntry( PROVIDEFLAGS, new int[] { 8});
	}
	
	/**
	 * Sets the type of the RPM to be either binary or source.
	 * </p>
	 * This field is required.
	 *
	 * @param type the type of RPM to generate.
	 */
	public void setType( final RpmType type) {
		format.getLead().setType( type);
	}

	/**
	 * Sets the platform related headers for the resulting RPM.  The platform is specified as a
	 * combination of target architecture and OS.
	 * <p/>
	 * This field is required.
	 *
	 * @param arch the target architectur.
	 * @param os the target operating system.
	 */
	public void setPlatform( final Architecture arch, final Os os) {
		format.getLead().setArch( arch);
		format.getLead().setOs( os);
		
		final CharSequence archName = arch.toString().toLowerCase();
		final CharSequence osName = os.toString().toLowerCase();
		format.getHeader().createEntry( ARCH, archName);
		format.getHeader().createEntry( OS, osName);
		format.getHeader().createEntry( PLATFORM, archName + "-" + osName);
		format.getHeader().createEntry( RHNPLATFORM, archName);
	}

	/**
	 * Sets the summary text for the file.  The summary is generally a short, one line description of the
	 * function of the package, and is often shown by RPM tools.
	 * <p/>
	 * This field is required.
	 *
	 * @param summary summary text.
	 */
	public void setSummary( final CharSequence summary) {
		format.getHeader().createEntry( SUMMARY, summary);
	}

	/**
	 * Sets the description text for the file.  The description is often a paragraph describing the
	 * package in detail.
	 * <p/>
	 * This field is required.
	 *
	 * @param description description text.
	 */
	public void setDescription( final CharSequence description) {
		format.getHeader().createEntry( DESCRIPTION, description);
	}

	/**
	 * Sets the build host for the RPM.  This is an internal field.
	 * <p/>
	 * This field is required.
	 *
	 * @param host hostname of the build machine.
	 */
	public void setBuildHost( final CharSequence host) {
		format.getHeader().createEntry( BUILDHOST, host);
	}

	/**
	 * Lists the license under which this software is distributed.  This field may be
	 * displayed by RPM tools.
	 * <p/>
	 * This field is required.
	 *
	 * @param license the chosen distribution license.
	 */
	public void setLicense( final CharSequence license) {
		format.getHeader().createEntry( LICENSE, license);
	}

	/**
	 * Software group to which this package belongs.  The group describes what sort of
	 * function the software package provides.
	 * <p/>
	 * This is a required field.
	 *
	 * @param group target group.
	 */
	public void setGroup( final CharSequence group) {
		format.getHeader().createEntry( GROUP, group);
	}

	/**
	 * Vendor tag listing the organization providing this software package.
	 * <p/>
	 * This is a required field.
	 *
	 * @param vendor software vendor.
	 */
	public void setVendor( final CharSequence vendor) {
		if ( vendor != null) format.getHeader().createEntry( VENDOR, vendor);
	}

	/**
	 * Build packager, usually the username of the account building this RPM.
	 * <p/>
	 * This is a required field.
	 *
	 * @param packager packager name.
	 */
	public void setPackager( final CharSequence packager) {
		format.getHeader().createEntry( PACKAGER, packager);
	}

	/**
	 * Website URL for this package, usually a project site.
	 * <p/>
	 * This is a required field.
	 *
	 * @param url 
	 */
	public void setUrl( CharSequence url) {
		if ( url != null) format.getHeader().createEntry( URL, url);
	}

	/**
	 * Declares a dependency that this package exports, and that other packages can use to
	 * provide library functions.
	 *
	 * @param dependency provided by this package.
	 */
	public void setProvides( final CharSequence provides) {
		format.getHeader().createEntry( PROVIDENAME, provides);
	}

	/**
	 * Sets the group of files to include in this RPM.  Note that this method causes the existing
	 * file set to be overwritten and therefore should be called before adding any other files via
	 * the {@link #addFile()} methods.
	 *
	 * @param files the set of files to use in constructing this RPM.
	 */
	public void setFiles( final IncludeFiles files) {
		this.files = files;
	}
	
	/**
	 * Add the specified file to the repository payload in order.
	 * The required header entries will automatically be generated
	 * to record the directory names and file names, as well as their
	 * digests.
	 *
	 * @param target the absolute path at which this file will be installed.
	 * @param file the file content to include in this rpm.
	 * @param mode the mode of the target file in standard three octet notation
	 */
	public void addFile( final CharSequence target, final File source, final int mode) throws Exception {
		files.addFile( new File( target.toString()), source, mode);
	}

	/**
	 * Addes the file to the repository with the default mode of <code>644</code>.
	 *
	 * @param target the absolute path at which this file will be installed.
	 * @param file the file content to include in this rpm.
	 */
	public void addFile( final CharSequence target, final File source) throws Exception {
		addFile( target, source, 0644);
	}

	/**
	 * Add a key to generate a new signature for the header and payload portions of the
	 * rpm file.  Supported algorithms are "MD5withRSA" and "SHAwithDSA".
	 *
	 * @param key private key to use in generating a signature.
	 */
	public void addSignature( final PrivateKey key) {
		signatures.add( key);
	}

	/**
	 * Generates an RPM with a standard name consisting of the RPM package name, version, release,
	 * and type in teh given directory.
	 *
	 * @param directory the destination directory for the new RPM file.
	 */
	public void build( final File directory) throws NoSuchAlgorithmException, IOException {
		final String rpm = format.getLead().getName() + "." + format.getLead().getArch().toString().toLowerCase() + ".rpm";
		final File file = new File( directory, rpm);
		if ( file.exists()) file.delete();
		build( new RandomAccessFile( file, "rw").getChannel());
	}

	/**
	 * Generates the rpm file to the provided file channel.  This file channel must support memory mapping
	 * and therefore should be created from a {@link RandomAccessFile}, otherwise an {@link IOException} will be
	 * generated.
	 *
	 * @param original the {@link FileChannel} to which the resulting RPM will be written.
	 */
	public void build( final FileChannel original) throws NoSuchAlgorithmException, IOException {
		final WritableChannelWrapper output = new WritableChannelWrapper( original);

		/*
		final Map< PrivateKey, Entry< byte[]>> map = new HashMap< PrivateKey, Entry< byte[]>>();
		for ( PrivateKey key : signatures) {
			if ( "MD5withRSA".equals( key.getAlgorithm())) map.put( key, ( Entry< byte[]>) format.getSignature().addEntry( GPG, GPGSIZE));
			else if ( "SHA1withDSA".equals( key.getAlgorithm())) map.put( key, ( Entry< byte[]>) format.getSignature().addEntry( DSAHEADER, DSASIZE));
			else throw new IOException( "Unknown key type '" + key.getAlgorithm() + "'.");
		}
		*/

		int[] flags = new int[ dependencies.size()];
		Arrays.fill( flags, 16777290);
		format.getHeader().createEntry( REQUIREFLAGS, flags);
		format.getHeader().createEntry( REQUIRENAME, dependencies.keySet().toArray( new String[ dependencies.size()]));
		format.getHeader().createEntry( REQUIREVERSION, dependencies.values().toArray( new String[ dependencies.size()]));

		format.getHeader().createEntry( SIZE, files.getTotalSize());
		format.getHeader().createEntry( DIRNAMES, files.getDirNames());
		format.getHeader().createEntry( DIRINDEXES, files.getDirIndexes());
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
		format.getHeader().createEntry( FILECOLORS, files.getColors());
		format.getHeader().createEntry( FILECLASS, files.getClasses());

		format.getHeader().createEntry( PAYLOADFLAGS, new String[] { "9"});

		final Entry< int[]> sigsize = ( Entry< int[]>) format.getSignature().addEntry( LEGACY_SIGSIZE, 1);
		final Entry< int[]> payload = ( Entry< int[]>) format.getSignature().addEntry( PAYLOADSIZE, 1);
		final Entry< byte[]> md5 = ( Entry< byte[]>) format.getSignature().addEntry( LEGACY_MD5, 16);
		//final Entry< String[]> sha = ( Entry< String[]>) format.getSignature().addEntry( SHA1HEADER, 1);
		//sha.setSize( SHASIZE);

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
		//final Key< byte[]> shakey = output.start( "SHA");
		final Key< byte[]> md5key = output.start( "MD5");

		immutable.setValues( getImmutable( format.getHeader().count()));
		format.getHeader().write( output);
		//sha.setValues( new String[] { Util.hex( output.finish( shakey))});

		final GZIPOutputStream zip = new GZIPOutputStream( Channels.newOutputStream( output));
		final WritableChannelWrapper compressor = new WritableChannelWrapper( Channels.newChannel( zip));
		final Key< Integer> payloadkey = compressor.start();

		int total = 0;
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);
		for ( CpioHeader header : files.headers()) {
			final File source = files.source( header);
			final String path = "." + files.target( header).getAbsolutePath();
			header.setName( path);
			total = header.write( compressor, total);
			
			FileChannel in = new FileInputStream( source).getChannel();
			while ( in.read(( ByteBuffer) buffer.rewind()) > 0) total += compressor.write(( ByteBuffer) buffer.flip());
			total += header.skip( compressor, total);
			in.close();
		}
		
		final CpioHeader trailer = new CpioHeader();
		trailer.setLast();
		total = trailer.write( compressor, total);
		trailer.skip( compressor, total);

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

	protected byte[] getSignature( final int count) {
		return getSpecial( 0x0000003E, count);
	}

	protected byte[] getImmutable( final int count) {
		return getSpecial( 0x0000003F, count);
	}

	protected byte[] getSpecial( final int tag, final int count) {
		final ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.putInt( tag);
		buffer.putInt( 0x00000007);
		buffer.putInt( count * -16);
		buffer.putInt( 0x00000010);
		return buffer.array();
	}
}
