package org.freecompany.redline;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import org.freecompany.redline.header.Architecture;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Os;
import org.freecompany.redline.header.RpmType;
import org.freecompany.redline.payload.Contents;
import org.freecompany.redline.payload.CpioHeader;
import org.freecompany.redline.payload.Directive;

import static org.freecompany.redline.ChannelWrapper.*;
import static org.freecompany.redline.header.AbstractHeader.*;
import static org.freecompany.redline.header.Flags.*;
import static org.freecompany.redline.header.Signature.SignatureTag.*;
import static org.freecompany.redline.header.Header.HeaderTag.*;

/**
 * The normal entry point to the API used for
 * building and RPM. The API provides methods to
 * configure and add contents to a new RPM. The
 * current version of the RPM format (3.0) requires
 * numerous headers to be set for an RPM to be
 * valid. All of the required fields are either
 * set automatically or exposed through setters in
 * this builder class. Any required fields are
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
	protected final Map< String, Integer> flags = new LinkedHashMap< String, Integer>();

	@SuppressWarnings( "unchecked")
	protected final Entry< byte[]> signature = ( Entry< byte[]>) format.getSignature().addEntry( SIGNATURES, 16);
	
	@SuppressWarnings( "unchecked")
	protected final Entry< byte[]> immutable = ( Entry< byte[]>) format.getHeader().addEntry( HEADERIMMUTABLE, 16);

	protected Contents contents = new Contents();

	/**
	 * Initializes the builder and sets some required fields to known values.
	 */
	public Builder() {
		format.getHeader().createEntry( HEADERI18NTABLE, "C");
		//format.getHeader().createEntry( BUILDTIME, ( int) ( System.currentTimeMillis() / 1000));
		format.getHeader().createEntry( BUILDTIME, 0);
		format.getHeader().createEntry( RPMVERSION, "4.4.2");
		format.getHeader().createEntry( PAYLOADFORMAT, "cpio");
		format.getHeader().createEntry( PAYLOADCOMPRESSOR, "gzip");

		addDependencyLess( "rpmlib(VersionedDependencies)", "3.0.3-1");
		addDependencyLess( "rpmlib(CompressedFileNames)", "3.0.4-1");
		addDependencyLess( "rpmlib(PayloadFilesHavePrefix)", "4.0-1");
	}

	/**
	 * Adds a dependency to the RPM package. This dependecy version will be marked as the maximum
	 * allowed, and the package will require the named dependency with this version or lower at
	 * install time.
	 *
	 * @param name the name of the dependency.
	 * @param version the version identifier.
	 */
	public void addDependencyLess( final CharSequence name, final CharSequence version) {
		addDependency( name, version, LESS | EQUAL);
	}

	/**
	 * Adds a dependency to the RPM package. This dependecy version will be marked as the minimum
	 * allowed, and the package will require the named dependency with this version or higher at
	 * install time.
	 *
	 * @param name the name of the dependency.
	 * @param version the version identifier.
	 */
	public void addDependencyMore( final CharSequence name, final CharSequence version) {
		addDependency( name, version, GREATER | EQUAL);
	}

	/**
	 * Adds a dependency to the RPM package. This dependecy version will be marked as the exact
	 * requirement, and the package will require the named dependency with exactly this version at
	 * install time.
	 *
	 * @param name the name of the dependency.
	 * @param version the version identifier.
	 */
	protected void addDependency( final CharSequence name, final CharSequence version, final int flag) {
		dependencies.put( name.toString(), version);
		flags.put( name.toString(), flag);
	}
	
	/**
	 * <b>Required Field</b>. Sets the package information, such as the rpm name, the version, and the release number.
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
	 * <b>Required Field</b>. Sets the type of the RPM to be either binary or source.
	 *
	 * @param type the type of RPM to generate.
	 */
	public void setType( final RpmType type) {
		format.getLead().setType( type);
	}

	/**
	 * <b>Required Field</b>. Sets the platform related headers for the resulting RPM. The platform is specified as a
	 * combination of target architecture and OS.
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
	 * <b>Required Field</b>. Sets the summary text for the file. The summary is generally a short, one line description of the
	 * function of the package, and is often shown by RPM tools.
	 *
	 * @param summary summary text.
	 */
	public void setSummary( final CharSequence summary) {
		if ( summary != null) format.getHeader().createEntry( SUMMARY, summary);
	}

	/**
	 * <b>Required Field</b>. Sets the description text for the file. The description is often a paragraph describing the
	 * package in detail.
	 *
	 * @param description description text.
	 */
	public void setDescription( final CharSequence description) {
		if ( description != null) format.getHeader().createEntry( DESCRIPTION, description);
	}

	/**
	 * <b>Required Field</b>. Sets the build host for the RPM. This is an internal field.
	 *
	 * @param host hostname of the build machine.
	 */
	public void setBuildHost( final CharSequence host) {
		if ( host != null) format.getHeader().createEntry( BUILDHOST, host);
	}

	/**
	 * <b>Required Field</b>. Lists the license under which this software is distributed. This field may be
	 * displayed by RPM tools.
	 *
	 * @param license the chosen distribution license.
	 */
	public void setLicense( final CharSequence license) {
		if ( license != null) format.getHeader().createEntry( LICENSE, license);
	}

	/**
	 * <b>Required Field</b>. Software group to which this package belongs.	The group describes what sort of
	 * function the software package provides.
	 *
	 * @param group target group.
	 */
	public void setGroup( final CharSequence group) {
		if ( group != null) format.getHeader().createEntry( GROUP, group);
	}

	/**
	 * <b>Required Field</b>. Distribution tag listing the ditributable package.
	 *
	 * @param distribution the distribution.
	 */
	public void setDistribution( final CharSequence distribution) {
		if ( distribution != null) format.getHeader().createEntry( DISTRIBUTION, distribution);
	}
	/**
	 * <b>Required Field</b>. Vendor tag listing the organization providing this software package.
	 *
	 * @param vendor software vendor.
	 */
	public void setVendor( final CharSequence vendor) {
		if ( vendor != null) format.getHeader().createEntry( VENDOR, vendor);
	}

	/**
	 * <b>Required Field</b>. Build packager, usually the username of the account building this RPM.
	 *
	 * @param packager packager name.
	 */
	public void setPackager( final CharSequence packager) {
		if ( packager != null) format.getHeader().createEntry( PACKAGER, packager);
	}

	/**
	 * <b>Required Field</b>. Website URL for this package, usually a project site.
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
		if ( provides != null) format.getHeader().createEntry( PROVIDENAME, provides);
	}

	/**
	 * Sets the group of contents to include in this RPM. Note that this method causes the existing
	 * file set to be overwritten and therefore should be called before adding any other contents via
	 * the {@link #addFile()} methods.
	 *
	 * @param contents the set of contents to use in constructing this RPM.
	 */
	public void setFiles( final Contents contents) {
		this.contents = contents;
	}
	
	/**
	 * Adds a source rpm.
	 *
	 * @param rpm name of rpm source file
	 */
	public void setSourceRpm( final String rpm) {
		if ( rpm != null) format.getHeader().createEntry( SOURCERPM, rpm);
	}
	
	/**
	 * Sets the package prefix directories to allow any files installed under
	 * them to be relocatable.
	 *
	 * @param prefixes Path prefixes which may be relocated
	 */
	public void setPrefixes( final String... prefixes) {
		if ( prefixes != null) format.getHeader().createEntry( PREFIXES, prefixes);
	}

	/**
	 * Declares a script to be run as part of the RPM pre-installation. The
	 * script will be run using the interpretter declared with the
	 * {@link #setPreInstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */ 
	public void setPreInstallScript( final String script) {
		if ( script != null) format.getHeader().createEntry( PREINSCRIPT, script);
	}
	
	/**
	 * Declares the interpretter to be used when invoking the RPM
	 * pre-installation script that can be set with the
	 * {@link #setPreInstallScript(String)} method.
	 *
	 * @param program Path to the interpretter
	 */
	public void setPreInstallProgram( final String program) {
		if ( program != null) format.getHeader().createEntry( PREINPROG, program);
	}
	
	/**
	 * Declares a script to be run as part of the RPM post-installation. The
	 * script will be run using the interpretter declared with the
	 * {@link #setPostInstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPostInstallScript( final String script) {
		if ( script != null) format.getHeader().createEntry( POSTINSCRIPT, script);
	}
	
	/**
	 * Declares the interpretter to be used when invoking the RPM
	 * post-installation script that can be set with the
	 * {@link #setPreInstallScript(String)} method.
	 *
	 * @param program Path to the interpretter
	 */
	public void setPostInstallProgram( final String program) {
		if ( program != null) format.getHeader().createEntry( POSTINPROG, program);
	}

	/**
	 * Declares a script to be run as part of the RPM pre-uninstallation. The
	 * script will be run using the interpretter declared with the
	 * {@link #setPreUninstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPreUninstallScript( final String script) {
		if ( script != null) format.getHeader().createEntry( PREUNSCRIPT, script);
	}

	/**
	 * Declares the interpretter to be used when invoking the RPM
	 * pre-uninstallation script that can be set with the
	 * {@link #setPreUninstallScript(String)} method.
	 *
	 * @param program Path to the interpretter
	 */
	public void setPreUninstallProgram( final String program) {
		if ( program != null) format.getHeader().createEntry( PREUNPROG, program);
	}
	
	/**
	 * Declares a script to be run as part of the RPM post-uninstallation. The
	 * script will be run using the interpretter declared with the
	 * {@link #setPostUninstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPostUninstallScript( final String script) {
		if ( script != null) format.getHeader().createEntry( POSTUNSCRIPT, script);
	}
	
	/**
	 * Declares the interpretter to be used when invoking the RPM
	 * post-uninstallation script that can be set with the
	 * {@link #setPostUninstallScript(String)} method.
	 *
	 * @param program Path to the interpretter
	 */
	public void setPostUninstallProgram( final String program) {
		if ( program != null) format.getHeader().createEntry( POSTUNPROG, program);
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
	public void addFile( final String path, final File source, final int mode) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source, mode);
	}
	
	/**
	 * Add the specified file to the repository payload in order.
	 * The required header entries will automatically be generated
	 * to record the directory names and file names, as well as their
	 * digests.
	 *
	 * @param path the absolute path at which this file will be installed.
	 * @param source the file content to include in this rpm.
	 * @param mode the mode of the target file in standard three octet notation
	 * @param directive directive indicating special handling for this file.
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 */
	public void addFile( final String path, final File source, final int mode, final Directive directive, final String uname, final String gname) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source, mode, directive, uname, gname);
	}
	
	/**
	 * Add the specified file to the repository payload in order.
	 * The required header entries will automatically be generated
	 * to record the directory names and file names, as well as their
	 * digests.
	 *
	 * @param path the absolute path at which this file will be installed.
	 * @param source the file content to include in this rpm.
	 * @param mode the mode of the target file in standard three octet notation
	 * @param directive directive indicating special handling for this file.
	 */
	public void addFile( final String path, final File source, final int mode, final Directive directive) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source, mode, directive);
	}
	
	/**
	 * Adds the file to the repository with the default mode of <code>644</code>.
	 *
	 * @param path the absolute path at which this file will be installed.
	 * @param file the file content to include in this rpm.
	 */
	public void addFile( final String path, final File source) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source);
	}
	
	/**
	 * Adds the directory to the repository with the default mode of <code>644</code>.
	 *
	 * @param path the absolute path at which this file will be installed.
	 * @param file the file content to include in this rpm.
	 */
	public void addDirectory( final String path) throws NoSuchAlgorithmException, IOException {
		contents.addDirectory( path);
	}

	/**
	 * Adds the directory to the repository.
	 *
	 * @param path the absolute path to add as a directory.
	 * @param permissions the mode of the directory in standard three octet notation.
	 * @param directive directive indicating special handling for this file.
	 * @param uname user owner of the directory
	 * @param gname group owner of the directory
	 */
	public void addDirectory( final String path, final int permissions, final Directive directive, final String uname, final String gname) throws NoSuchAlgorithmException, IOException {
		contents.addDirectory( path, permissions, directive, uname, gname);
	}
	
	/**
	 * Adds the directory to the repository.
	 *
	 * @param path the absolute path to add as a directory.
	 * @param permissions the mode of the directory in standard three octet notation.
	 * @param directive directive indicating special handling for this file.
	 * @param uname user owner of the directory
	 * @param gname group owner of the directory
	 * @param addParents whether to add parent directories to the rpm 
	 */
	public void addDirectory( final String path, final int permissions, final Directive directive, final String uname, final String gname, final boolean addParents) throws NoSuchAlgorithmException, IOException {
		contents.addDirectory( path, permissions, directive, uname, gname, addParents);
	}
	
	/**
	 * Adds the directory to the repository with the default mode of <code>644</code>.
	 *
	 * @param path the absolute path to add as a directory.
	 * @param directive directive indicating special handling for this file.
	 */
	public void addDirectory( final String path, final Directive directive) throws NoSuchAlgorithmException, IOException {
		contents.addDirectory( path, directive);
	}

	/**
	 * Adds a symbolic link to the repository.
	 *
	 * @param path the absolute path at which this link will be installed.
	 * @param target the path of the file this link will point to.
	 */
	public void addLink( final String path, final String target) throws NoSuchAlgorithmException, IOException {
		contents.addLink( path, target);
	}

	/**
	 * Adds a symbolic link to the repository.
	 *
	 * @param path the absolute path at which this link will be installed.
	 * @param target the path of the file this link will point to.
	 */
	public void addLink( final String path, final String target, int permissions) throws NoSuchAlgorithmException, IOException {
		contents.addLink( path, target, permissions);
	}

	/**
	 * Add a key to generate a new signature for the header and payload portions of the
	 * rpm file. Supported algorithms are "MD5withRSA" and "SHAwithDSA".
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
	public String build( final File directory) throws NoSuchAlgorithmException, IOException {
		final String rpm = format.getLead().getName() + "." + format.getLead().getArch().toString().toLowerCase() + ".rpm";
		final File file = new File( directory, rpm);
		if ( file.exists()) file.delete();
		build( new RandomAccessFile( file, "rw").getChannel());
		return rpm;
	}

	/**
	 * Generates the rpm file to the provided file channel. This file channel must support memory mapping
	 * and therefore should be created from a {@link RandomAccessFile}, otherwise an {@link IOException} will be
	 * generated.
	 *
	 * @param original the {@link FileChannel} to which the resulting RPM will be written.
	 */
	@SuppressWarnings( "unchecked")
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

		format.getHeader().createEntry( EPOCH, 0);
		format.getHeader().createEntry( REQUIRENAME, dependencies.keySet().toArray( new String[ dependencies.size()]));
		format.getHeader().createEntry( REQUIREVERSION, dependencies.values().toArray( new String[ dependencies.size()]));
		format.getHeader().createEntry( REQUIREFLAGS, convert( flags.values().toArray( new Integer[ flags.size()])));

		format.getHeader().createEntry( SIZE, contents.getTotalSize());
		format.getHeader().createEntry( DIRNAMES, contents.getDirNames());
		format.getHeader().createEntry( DIRINDEXES, contents.getDirIndexes());
		format.getHeader().createEntry( BASENAMES, contents.getBaseNames());
		format.getHeader().createEntry( FILEMD5S, contents.getMD5s());
		format.getHeader().createEntry( FILESIZES, contents.getSizes());
		format.getHeader().createEntry( FILEMODES, contents.getModes());
		format.getHeader().createEntry( FILERDEVS, contents.getRdevs());
		format.getHeader().createEntry( FILEMTIMES, contents.getMtimes());
		format.getHeader().createEntry( FILELINKTOS, contents.getLinkTos());
		format.getHeader().createEntry( FILEFLAGS, contents.getFlags());
		format.getHeader().createEntry( FILEUSERNAME, contents.getUsers());
		format.getHeader().createEntry( FILEGROUPNAME, contents.getGroups());
		format.getHeader().createEntry( FILEVERIFYFLAGS, contents.getVerifyFlags());
		format.getHeader().createEntry( FILEDEVICES, contents.getDevices());
		format.getHeader().createEntry( FILEINODES, contents.getInodes());
		format.getHeader().createEntry( FILELANGS, contents.getLangs());
		//format.getHeader().createEntry( FILEDEPENDSX, contents.getDependsX());
		//format.getHeader().createEntry( FILEDEPENDSN, contents.getDependsN());
		//format.getHeader().createEntry( FILECOLORS, contents.getColors());
		//format.getHeader().createEntry( FILECLASS, contents.getClasses());
		format.getHeader().createEntry( FILECONTEXTS, contents.getContexts());

		format.getHeader().createEntry( PAYLOADFLAGS, new String[] { "9"});

		final Entry< int[]> sigsize = ( Entry< int[]>) format.getSignature().addEntry( LEGACY_SIGSIZE, 1);
		final Entry< int[]> payload = ( Entry< int[]>) format.getSignature().addEntry( PAYLOADSIZE, 1);
		final Entry< byte[]> md5 = ( Entry< byte[]>) format.getSignature().addEntry( LEGACY_MD5, 16);
		final Entry< String[]> sha = ( Entry< String[]>) format.getSignature().addEntry( SHA1HEADER, 1);
		sha.setSize( SHASIZE);

		format.getLead().write( original);
		signature.setValues( getSignature( format.getSignature().count()));
		Util.empty( output, ByteBuffer.allocate( format.getSignature().write( original)));

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
		int headerPadding = format.getHeader().write( output);
		sha.setValues( new String[] { Util.hex( output.finish( shakey))});
		//Util.empty( output, ByteBuffer.allocate( headerPadding));

		final GZIPOutputStream zip = new GZIPOutputStream( Channels.newOutputStream( output));
		final WritableChannelWrapper compressor = new WritableChannelWrapper( Channels.newChannel( zip));
		final Key< Integer> payloadkey = compressor.start();

		int total = 0;
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);
		for ( CpioHeader header : contents.headers()) {
			final String path = header.getName();
			if ( path.startsWith( "/")) header.setName( "." + path);
			total = header.write( compressor, total);
			
			final Object object = contents.getSource( header);
			if ( object instanceof File) {
				final FileChannel in = new FileInputStream(( File) object).getChannel();
				while ( in.read(( ByteBuffer) buffer.rewind()) > 0) {
					total += compressor.write(( ByteBuffer) buffer.flip());
					buffer.compact();
				}
				total += header.skip( compressor, total);
				in.close();
			} else if ( object instanceof CharSequence) {
				final CharSequence target = ( CharSequence) object;
				total += compressor.write( ByteBuffer.wrap( String.valueOf( target).getBytes()));
				total += header.skip( compressor, target.length());
			}
		}
		
		final CpioHeader trailer = new CpioHeader();
		trailer.setLast();
		total = trailer.write( compressor, total);
		trailer.skip( compressor, total);

		int length = compressor.finish( payloadkey);
		int pad = Util.difference( length, 3);
		Util.empty( compressor, ByteBuffer.allocate( pad));
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

	/**
	 * Returns the special header expected by RPM for
	 * a particular header.
	 */
	protected byte[] getSpecial( final int tag, final int count) {
		final ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.putInt( tag);
		buffer.putInt( 0x00000007);
		buffer.putInt( count * -16);
		buffer.putInt( 0x00000010);
		return buffer.array();
	}

	/**
	 * Converts an array of Integer objects into an equivalent
	 * array of int primitives.
	 */
	protected int[] convert( final Integer[] ints) {
		int[] array = new int[ ints.length];
		int count = 0;
		for ( int i : ints) array[ count++] = i;
		return array;
	}
}
