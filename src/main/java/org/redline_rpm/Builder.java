package org.redline_rpm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import org.bouncycastle.openpgp.PGPPrivateKey;
import org.redline_rpm.changelog.ChangelogHandler;
import org.redline_rpm.changelog.ChangelogParseException;
import org.redline_rpm.header.Architecture;
import org.redline_rpm.header.Format;
import org.redline_rpm.header.Os;
import org.redline_rpm.header.RpmType;
import org.redline_rpm.payload.Contents;
import org.redline_rpm.payload.CpioHeader;
import org.redline_rpm.payload.Directive;

import static org.redline_rpm.ChannelWrapper.*;
import static org.redline_rpm.header.AbstractHeader.*;
import static org.redline_rpm.header.Flags.*;
import static org.redline_rpm.header.Signature.SignatureTag.*;
import static org.redline_rpm.header.Header.HeaderTag.*;

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
	private static final int SHA256_SIZE = 65;
	private static final int MD5SIZE = 32;

	private static final String DEFAULTSCRIPTPROG = "/bin/sh";

	private static final char[] ILLEGAL_CHARS_VARIABLE = new char[] { '-', '/' };
	private static final char[] ILLEGAL_CHARS_NAME = new char[] { '/', ' ', '\t', '\n', '\r' };

	protected final Format format = new Format();
	protected final Set< PrivateKey> signatures = new HashSet< PrivateKey>();

	protected final List< Dependency> requires = new LinkedList< Dependency>();
	protected final List< Dependency> obsoletes = new LinkedList< Dependency>();
	protected final List< Dependency> conflicts = new LinkedList< Dependency>();
	protected final Map< String, Dependency> provides = new LinkedHashMap< String, Dependency>();

	protected final List< String> triggerscripts = new LinkedList< String>();
	protected final List< String> triggerscriptprogs = new LinkedList< String>();

	protected final List< String> triggernames = new LinkedList< String>();
	protected final List< String> triggerversions = new LinkedList< String>();
	protected final List< Integer> triggerflags = new LinkedList< Integer>();
	protected final List< Integer> triggerindexes = new LinkedList< Integer>();

	private int triggerCounter = 0;

	@SuppressWarnings( "unchecked")
	protected final Entry< byte[]> signature = ( Entry< byte[]>) format.getSignature().addEntry( SIGNATURES, 16);

	@SuppressWarnings( "unchecked")
	protected final Entry< byte[]> immutable = ( Entry< byte[]>) format.getHeader().addEntry( HEADERIMMUTABLE, 16);

	protected Contents contents = new Contents();
    protected File privateKeyRingFile;
    protected String privateKeyId;
    protected String privateKeyPassphrase;
    protected PGPPrivateKey privateKey;

	/**
	 * Initializes the builder and sets some required fields to known values.
	 */
	public Builder() {
		format.getHeader().createEntry( HEADERI18NTABLE, "C");
		format.getHeader().createEntry( BUILDTIME, ( int) ( System.currentTimeMillis() / 1000));
		format.getHeader().createEntry( RPMVERSION, "4.4.2");
		format.getHeader().createEntry( PAYLOADFORMAT, "cpio");
		format.getHeader().createEntry( PAYLOADCOMPRESSOR, "gzip");

		addDependencyLess( "rpmlib(VersionedDependencies)", "3.0.3-1");
		addDependencyLess( "rpmlib(CompressedFileNames)", "3.0.4-1");
		addDependencyLess( "rpmlib(PayloadFilesHavePrefix)", "4.0-1");
	}
	
	public void addBuiltinDirectory(String builtinDirectory) {
		contents.addLocalBuiltinDirectory(builtinDirectory);
	}

	public void addObsoletes(final String name, final int comparison, final String version) {
		obsoletes.add(new Dependency(name, version, comparison));
	}
	
	public void addObsoletesLess (final CharSequence name, final CharSequence version) {
		int flag = LESS | EQUAL;
		addObsoletes(name, version, flag);
	}
	
	public void addObsoletesMore (final CharSequence name, final CharSequence version) {
		int flag = GREATER | EQUAL;
		addObsoletes(name, version, flag);
	}
	
	protected void addObsoletes( final CharSequence name, final CharSequence version, final int flag) {
		obsoletes.add(new Dependency(name.toString(), version.toString(), flag));
	}
	
	public void addConflicts(final String name, final int comparison, final String version) {
		conflicts.add(new Dependency(name, version, comparison));
	}
	
	public void addConflictsLess(final CharSequence name, final CharSequence version) {
		int flag = LESS | EQUAL;
		addConflicts(name, version, flag);
	}
	
	public void addConflictsMore(final CharSequence name, final CharSequence version) {
		int flag = GREATER | EQUAL;
		addConflicts(name, version, flag);
	}
	
	protected void addConflicts(final CharSequence name, final CharSequence version, final int flag) {
		conflicts.add(new Dependency(name.toString(), version.toString(), flag));
	}
	
	public void addProvides(final String name, final String version) {
		provides.put(name, new Dependency(name, version, version.length() > 0 ? EQUAL : 0));
	}

	protected void addProvides(final CharSequence name, final CharSequence version, final int flag) {
		provides.put(name.toString(), new Dependency(name.toString(), version.toString(), flag));
	}
	
	/**
	 * Adds a dependency to the RPM package. This dependency version will be marked as the exact
	 * requirement, and the package will require the named dependency with exactly this version at
	 * install time.
	 *
	 * @param name the name of the dependency.
	 * @param comparison the comparison flag.
	 * @param version the version identifier.
	 */
	public void addDependency( final String name, final int comparison, final String version ) {
		requires.add(new Dependency(name, version, comparison));
	}

	/**
	 * Adds a dependency to the RPM package. This dependency version will be marked as the maximum
	 * allowed, and the package will require the named dependency with this version or lower at
	 * install time.
	 *
	 * @param name the name of the dependency.
	 * @param version the version identifier.
	 */
	public void addDependencyLess( final CharSequence name, final CharSequence version) {
		int flag = LESS | EQUAL;
		if (name.toString().startsWith("rpmlib(")){
			flag = flag | RPMLIB; 
		}
		addDependency( name, version, flag);
	}

	/**
	 * Adds a dependency to the RPM package. This dependency version will be marked as the minimum
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
	 * Adds a dependency to the RPM package. This dependency version will be marked as the exact
	 * requirement, and the package will require the named dependency with exactly this version at
	 * install time.
	 *
	 * @param name the name of the dependency.
	 * @param version the version identifier.
	 * @param flag the file flags
	 */
	protected void addDependency( final CharSequence name, final CharSequence version, final int flag) {
		requires.add(new Dependency(name.toString(), version.toString(), flag));
	}
	
	/**
     * Adds a header entry value to the header. For example use this to set the source RPM package
     * name on your RPM
     * @param tag the header tag to set
     * @param value the value to set the header entry with
     */
	public void addHeaderEntry( final Tag tag, final String value) {
	    format.getHeader().createEntry(tag, value);
	}
	
	/**
     * Adds a header entry byte (8-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
	 * @throws ClassCastException - if the type required by tag.type() is not byte[]
     */
	public void addHeaderEntry( final Tag tag, final byte value) {
	    format.getHeader().createEntry(tag, new byte[] {value});
	}
	
	/**
     * Adds a header entry char (8-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
	 * @throws ClassCastException - if the type required by tag.type() is not byte[]
     */
	public void addHeaderEntry( final Tag tag, final char value) {
	    format.getHeader().createEntry(tag, new byte[] {(byte) value});
	}

	
	/**
     * Adds a header entry short (16-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
	 * @throws ClassCastException - if the type required by tag.type() is not short[]
     */
	public void addHeaderEntry( final Tag tag, final short value) {
	    format.getHeader().createEntry(tag,  new short[] {value});
	}
	
	/**
     * Adds a header entry int (32-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
 	 * @throws ClassCastException - if the type required by tag.type() is not int[]
    */
	public void addHeaderEntry( final Tag tag, final int value) {
	    format.getHeader().createEntry(tag, new int[] {value});
	}
	
	/**
     * Adds a header entry long (64-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
 	 * @throws ClassCastException - if the type required by tag.type() is not long[]
     */
	public void addHeaderEntry( final Tag tag, final long value) {
	    format.getHeader().createEntry(tag,  new long[] {value});
	}
	
	/**
     * Adds a header entry byte array (8-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
 	 * @throws ClassCastException - if the type required by tag.type() is not byte[]
     */
	public void addHeaderEntry( final Tag tag, final byte[] value) {
	    format.getHeader().createEntry(tag, value);
	}
	
	/**
     * Adds a header entry short array (16-bit) value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
 	 * @throws ClassCastException - if the type required by tag.type() is not short[]
     */
	public void addHeaderEntry( final Tag tag, final short[] value) {
	    format.getHeader().createEntry(tag, value);
	}
	
	/**
     * Adds a header entry int (32-bit) array value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
 	 * @throws ClassCastException - if the type required by tag.type() is not int[]
     */
	public void addHeaderEntry( final Tag tag, final int[] value) {
	    format.getHeader().createEntry(tag, value);
	}
	
	/**
     * Adds a header entry long (64-bit) array value to the header. 
     * @param tag the header tag to set
     * @param value the value to set the header entry with
 	 * @throws ClassCastException - if the type required by tag.type() is not long[]
     */
	public void addHeaderEntry( final Tag tag, final long[] value) {
	    format.getHeader().createEntry(tag, value);
	}
	
	/**
	 * @param illegalChars the illegal characters to check for.
	 * @param variable the character sequence to check for illegal characters.
	 * @param variableName the name to include in IllegalArgumentException
	 * @throws IllegalArgumentException if passed in character sequence contains dashes.
	 */
	private void checkVariableContainsIllegalChars(final char[] illegalChars, final CharSequence variable, final String variableName) {
		for (int i = 0; i < variable.length(); i++) {
			char currChar = variable.charAt(i);
			for (char illegalChar : illegalChars) {
				if (currChar == illegalChar) {
					throw new IllegalArgumentException(variableName + " with value: '" + variable + "' contains illegal character " + currChar);
				}
			}
		}
	}

	/**
	 * <b>Required Field</b>. Sets the package information, such as the rpm name, the version, and the release number.
	 * 
	 * @param name the name of the RPM package.
	 * @param version the version of the new package.
	 * @param release the release number, specified after the version, of the new RPM.
	 * @param epoch the epoch number of the new RPM
	 * @throws IllegalArgumentException if version or release contain
	 *         dashes, as they are explicitly disallowed by RPM file format.
	 */
	public void setPackage( final CharSequence name, final CharSequence version, final CharSequence release, final int epoch) {
		checkVariableContainsIllegalChars(ILLEGAL_CHARS_NAME, name, "name");
		checkVariableContainsIllegalChars(ILLEGAL_CHARS_VARIABLE, version, "version");
		checkVariableContainsIllegalChars(ILLEGAL_CHARS_VARIABLE, release, "release");
		format.getLead().setName( name + "-" + version + "-" + release);
		format.getHeader().createEntry( NAME, name);
		format.getHeader().createEntry( VERSION, version);
		format.getHeader().createEntry( RELEASE, release);
		format.getHeader().createEntry( EPOCH, epoch);
		this.provides.clear();
		addProvides(String.valueOf(name), "" + epoch + ":" + version + "-" + release);
	}

	public void setPackage( final CharSequence name, final CharSequence version, final CharSequence release) {
		setPackage(name, version, release, 0);
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
	 * @param arch the target architecture.
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
     * <b>Required Field</b>. Sets the platform related headers for the resulting RPM. The platform is specified as a
     * combination of target architecture and OS. 
     *
     * @param arch the target architecture.
     * @param osName the non-standard target operating system.
     */
    public void setPlatform( final Architecture arch, final CharSequence osName) {
            format.getLead().setArch( arch);
            format.getLead().setOs( Os.UNKNOWN);
            
            final CharSequence archName = arch.toString().toLowerCase();
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
	 * <b>Required Field</b>. Distribution tag listing the distributable package.
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
	 * @param url the URL
	 */
	public void setUrl( CharSequence url) {
		if ( url != null) format.getHeader().createEntry( URL, url);
	}

	/**
	 * Declares a dependency that this package exports, and that other packages can use to
	 * provide library functions.  Note that this method causes the existing provides set to be
	 * overwritten and therefore should be called before adding any other contents via
	 * the <code>addProvides()</code> methods.
	 * 
	 * You should use <code>addProvides()</code> instead.
	 *
	 * @param provides dependency provided by this package.
	 */
	public void setProvides( final CharSequence provides) {
		if ( provides != null) {
			this.provides.clear();
			addProvides( provides, "", EQUAL);
		}
	}

	/**
	 * Sets the group of contents to include in this RPM. Note that this method causes the existing
	 * file set to be overwritten and therefore should be called before adding any other contents via
	 * the <code>addFile()</code> methods.
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
		if ( prefixes != null && 0 < prefixes.length) format.getHeader().createEntry( PREFIXES, prefixes);
	}

    /**
     * Return the content of the specified script file as a String.
     *
     * @param file the script file to be read
     */
    private String readScript( File file) throws IOException {
        if ( file == null) return null;

        StringBuilder script = new StringBuilder();
        BufferedReader in = new BufferedReader( new FileReader(file));

        try {
            String line;
            while (( line = in.readLine()) != null) {
                script.append( line);
                script.append( "\n");
            }
        } finally {
            in.close();
        }

        return script.toString();
    }

    /**
     * Returns the program use to run the specified script (guessed by parsing 
     * the shebang at the beginning of the script)
     * 
     * @param script
     */
    private String readProgram( String script) {
        String program = null;
        
        if ( script != null) {
            Pattern pattern = Pattern.compile( "^#!(/.*)");
            Matcher matcher = pattern.matcher( script);
            if ( matcher.find()) {
                program = matcher.group( 1);
            }            
        }
                
        return program;
    }

    /**
	 * Declares a script to be run as part of the RPM pre-transaction. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPreTransProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */ 
	public void setPreTransScript( final String script) {
		setPreTransProgram(readProgram(script));
		if ( script != null) format.getHeader().createEntry( PRETRANSSCRIPT, script);
	}

	/**
	 * Declares a script file to be run as part of the RPM pre-transaction. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPreTransProgram(String)} method.
	 *
	 * @param file Script to run (i.e. shell commands)
	 * @throws IOException there was an IO error
	 */
	public void setPreTransScript( final File file) throws IOException {
		setPreTransScript(readScript(file));
	}

	/**
	 * Declares the interpreter to be used when invoking the RPM
	 * pre-transaction script that can be set with the
	 * {@link #setPreTransScript(String)} method.
	 *
	 * @param program Path to the interpreter
	 */
	public void setPreTransProgram( final String program) {
		if ( null == program) {
			format.getHeader().createEntry( PRETRANSPROG, DEFAULTSCRIPTPROG);
		} else if ( 0 == program.length()){
			format.getHeader().createEntry( PRETRANSPROG, DEFAULTSCRIPTPROG);
		} else {
			format.getHeader().createEntry( PRETRANSPROG, program);
		}
	}
    
	/**
	 * Declares a script to be run as part of the RPM pre-installation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPreInstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */ 
	public void setPreInstallScript( final String script) {
        setPreInstallProgram(readProgram(script));
        if ( script != null) format.getHeader().createEntry( PREINSCRIPT, script);
	}
	
	/**
	 * Declares a script file to be run as part of the RPM pre-installation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPreInstallProgram(String)} method.
	 *
	 * @param file Script to run (i.e. shell commands)
	 * @throws IOException there was an IO error
	 */
	public void setPreInstallScript( final File file) throws IOException {
		setPreInstallScript(readScript(file));
	}

    /**
	 * Declares the interpreter to be used when invoking the RPM
	 * pre-installation script that can be set with the
	 * {@link #setPreInstallScript(String)} method.
	 *
	 * @param program Path to the interpretter
	 */
	public void setPreInstallProgram( final String program) {
		if ( null == program) {
			format.getHeader().createEntry( PREINPROG, DEFAULTSCRIPTPROG);
		} else if ( 0 == program.length()){
			format.getHeader().createEntry( PREINPROG, DEFAULTSCRIPTPROG);
		} else {
			format.getHeader().createEntry( PREINPROG, program);
		}
	}
	
	/**
	 * Declares a script to be run as part of the RPM post-installation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPostInstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPostInstallScript( final String script) {
        setPostInstallProgram(readProgram(script));
        if ( script != null) format.getHeader().createEntry( POSTINSCRIPT, script);
	}
	
	/**
	 * Declares a script file to be run as part of the RPM post-installation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPostInstallProgram(String)} method.
	 *
	 * @param file Script to run (i.e. shell commands)
	 * @throws IOException there was an IO error
	 */
	public void setPostInstallScript( final File file) throws IOException {
        setPostInstallScript(readScript(file));
	}

    /**
	 * Declares the interpreter to be used when invoking the RPM
	 * post-installation script that can be set with the
	 * {@link #setPostInstallScript(String)} method.
	 *
	 * @param program Path to the interpreter
	 */
	public void setPostInstallProgram( final String program) {
		if ( null == program) {
			format.getHeader().createEntry( POSTINPROG, DEFAULTSCRIPTPROG);
		} else if ( 0 == program.length()){
			format.getHeader().createEntry( POSTINPROG, DEFAULTSCRIPTPROG);
		} else {
			format.getHeader().createEntry( POSTINPROG, program);
		}
	}

	/**
	 * Declares a script to be run as part of the RPM pre-uninstallation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPreUninstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPreUninstallScript( final String script) {
        setPreUninstallProgram(readProgram(script));
        if ( script != null) format.getHeader().createEntry( PREUNSCRIPT, script);
	}

	/**
	 * Declares a script file to be run as part of the RPM pre-uninstallation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPreUninstallProgram(String)} method.
	 *
	 * @param file Script to run (i.e. shell commands)
	 * @throws IOException there was an IO error
	 */
	public void setPreUninstallScript( final File file) throws IOException {
        setPreUninstallScript(readScript(file));
	}

    /**
	 * Declares the interpreter to be used when invoking the RPM
	 * pre-uninstallation script that can be set with the
	 * {@link #setPreUninstallScript(String)} method.
	 *
	 * @param program Path to the interpreter
	 */
	public void setPreUninstallProgram( final String program) {
		if ( null == program) {
			format.getHeader().createEntry( PREUNPROG, DEFAULTSCRIPTPROG);
		} else if ( 0 == program.length()){
			format.getHeader().createEntry( PREUNPROG, DEFAULTSCRIPTPROG);
		} else {
			format.getHeader().createEntry( PREUNPROG, program);
		}
	}

	/**
	 * Declares a script to be run as part of the RPM post-uninstallation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPostUninstallProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPostUninstallScript( final String script) {
        setPostUninstallProgram(readProgram(script));
        if ( script != null) format.getHeader().createEntry( POSTUNSCRIPT, script);
	}

	/**
	 * Declares a script file to be run as part of the RPM post-uninstallation. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPostUninstallProgram(String)} method.
	 *
	 * @param file Script contents to run (i.e. shell commands)
	 * @throws IOException there was an IO error
	 */
	public void setPostUninstallScript( final File file) throws IOException {
        setPostUninstallScript(readScript(file));
	}

    /**
	 * Declares the interpreter to be used when invoking the RPM
	 * post-uninstallation script that can be set with the
	 * {@link #setPostUninstallScript(String)} method.
	 *
	 * @param program Path to the interpreter
	 */
	public void setPostUninstallProgram( final String program) {
		if ( null == program) {
			format.getHeader().createEntry( POSTUNPROG, DEFAULTSCRIPTPROG);
		} else if ( 0 == program.length()){
			format.getHeader().createEntry( POSTUNPROG, DEFAULTSCRIPTPROG);
		} else {
			format.getHeader().createEntry( POSTUNPROG, program);
		}
	}

	/**
	 * Declares a script to be run as part of the RPM post-transaction. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPostTransProgram(String)} method.
	 *
	 * @param script Script contents to run (i.e. shell commands)
	 */
	public void setPostTransScript( final String script) {
		setPostTransProgram(readProgram(script));
		if ( script != null) format.getHeader().createEntry( POSTTRANSSCRIPT, script);
	}

	/**
	 * Declares a script file to be run as part of the RPM post-transaction. The
	 * script will be run using the interpreter declared with the
	 * {@link #setPostTransProgram(String)} method.
	 *
	 * @param file Script contents to run (i.e. shell commands)
	 * @throws IOException there was an IO error
	 */
	public void setPostTransScript( final File file) throws IOException {
		setPostTransScript(readScript(file));
	}

	/**
	 * Declares the interpreter to be used when invoking the RPM
	 * post-transaction script that can be set with the
	 * {@link #setPostTransScript(String)} method.
	 *
	 * @param program Path to the interpreter
	 */
	public void setPostTransProgram( final String program) {
		if ( null == program) {
			format.getHeader().createEntry( POSTTRANSPROG, DEFAULTSCRIPTPROG);
		} else if ( 0 == program.length()){
			format.getHeader().createEntry( POSTTRANSPROG, DEFAULTSCRIPTPROG);
		} else {
			format.getHeader().createEntry( POSTTRANSPROG, program);
		}
	}

	/**
	 * Adds a trigger to the RPM package.
	 *
	 * @param script the script to add.
	 * @param prog the interpreter with which to run the script.
	 * @param depends the map of rpms and versions that will trigger the script
	 * @param flag the trigger type (SCRIPT_TRIGGERPREIN, SCRIPT_TRIGGERIN, SCRIPT_TRIGGERUN, or SCRIPT_TRIGGERPOSTUN)
	 * @throws IOException there was an IO error
	 */
	public void addTrigger( final File script, final String prog, final Map< String, IntString> depends, final int flag) throws IOException {
		triggerscripts.add(readScript(script));
		if ( null == prog) {
			triggerscriptprogs.add(DEFAULTSCRIPTPROG);
		} else if ( 0 == prog.length()){
			triggerscriptprogs.add(DEFAULTSCRIPTPROG);
		} else {
			triggerscriptprogs.add(prog);
		}
		for ( Map.Entry< String, IntString> depend : depends.entrySet()) {
			triggernames.add( depend.getKey());
			triggerflags.add( depend.getValue().getInt() | flag);
			triggerversions.add( depend.getValue().getString());
			triggerindexes.add ( triggerCounter);
		}
		triggerCounter++;
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
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
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
	 * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addFile( final String path, final File source, final int mode, final int dirmode) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source, mode, dirmode);
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
     * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
     * @param uname user owner for the given file
     * @param gname group owner for the given file
     * @throws NoSuchAlgorithmException the algorithm isn't supported
     * @throws IOException there was an IO error
     */
    public void addFile( final String path, final File source, final int mode, final int dirmode, final String uname, final String gname) throws NoSuchAlgorithmException, IOException {
        contents.addFile( path, source, mode, null, uname, gname, dirmode);
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
     * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
     * @param directive directive indicating special handling for this file.
     * @param uname user owner for the given file
     * @param gname group owner for the given file
     * @throws NoSuchAlgorithmException the algorithm isn't supported
     * @throws IOException there was an IO error
     */
    public void addFile( final String path, final File source, final int mode, final int dirmode, final Directive directive, final String uname, final String gname) throws NoSuchAlgorithmException, IOException {
        contents.addFile( path, source, mode, directive, uname, gname, dirmode);
    }

    /**
     * Add the specified file to the repository payload in order.
     * The required header entries will automatically be generated
     * to record the directory names and file names, as well as their
     * digests.
     *
     * @param path the absolute path at which this file will be installed.
     * @param source the file content to include in this rpm.
     * @param mode the mode of the target file in standard three octet notation, or -1 for default.
     * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
     * @param directive directive indicating special handling for this file.
     * @param uname user owner for the given file, or null for default user.
     * @param gname group owner for the given file, or null for default group.
     * @param addParents whether to create parent directories for the file, defaults to true for other methods.
     * @throws NoSuchAlgorithmException the algorithm isn't supported
     * @throws IOException there was an IO error
     */
    public void addFile( final String path, final File source, final int mode, final int dirmode, final Directive directive, final String uname, final String gname, final boolean addParents) throws NoSuchAlgorithmException, IOException {
        contents.addFile( path, source, mode, directive, uname, gname, dirmode, addParents);
    }

    /**
     * Add the specified file to the repository payload in order.
     * The required header entries will automatically be generated
     * to record the directory names and file names, as well as their
     * digests.
     *
     * @param path the absolute path at which this file will be installed.
     * @param source the file content to include in this rpm.
     * @param mode the mode of the target file in standard three octet notation, or -1 for default.
     * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
     * @param directive directive indicating special handling for this file.
     * @param uname user owner for the given file, or null for default user.
     * @param gname group owner for the given file, or null for default group.
     * @param addParents whether to create parent directories for the file, defaults to true for other methods.
     * @param verifyFlags verify flags
     * @throws NoSuchAlgorithmException the algorithm isn't supported
     * @throws IOException there was an IO error
     */
    public void addFile( final String path, final File source, final int mode, final int dirmode, final Directive directive, final String uname, final String gname, final boolean addParents, final int verifyFlags) throws NoSuchAlgorithmException, IOException {
        contents.addFile( path, source, mode, directive, uname, gname, dirmode, addParents, verifyFlags);
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
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
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
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addFile( final String path, final File source, final int mode, final Directive directive) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source, mode, directive);
	}

	/**
	 * Adds the file to the repository with the default mode of <code>644</code>.
	 *
	 * @param path the absolute path at which this file will be installed.
	 * @param source the file content to include in this rpm.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addFile( final String path, final File source) throws NoSuchAlgorithmException, IOException {
		contents.addFile( path, source);
	}

	/**
	 * Add the specified file to the repository payload in order by URL.
	 * The required header entries will automatically be generated
	 * to record the directory names and file names, as well as their
	 * digests.
	 *
	 * @param path the absolute path at which this file will be installed.
	 * @param source the file content to include in this rpm.
	 * @param mode the mode of the target file in standard three octet notation
	 *  @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addURL( final String path, final URL source, final int mode, final int dirmode) throws NoSuchAlgorithmException, IOException {
		contents.addURL( path, source, mode, null, null, null, dirmode);
	}

	/**
     * Add the specified file to the repository payload in order by URL.
     * The required header entries will automatically be generated
     * to record the directory names and file names, as well as their
     * digests.
     *
     * @param path the absolute path at which this file will be installed.
     * @param source the file content to include in this rpm.
     * @param mode the mode of the target file in standard three octet notation
     * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
     * @param username ownership of added file
     * @param group ownership of added file
     * @throws NoSuchAlgorithmException the algorithm isn't supported
     * @throws IOException there was an IO error
     */
    public void addURL( final String path, final URL source, final int mode, final int dirmode, final String username, final String group) throws NoSuchAlgorithmException, IOException {
        contents.addURL( path, source, mode, null, username, group, dirmode);
    }

    /**
     * Add the specified file to the repository payload in order by URL.
     * The required header entries will automatically be generated
     * to record the directory names and file names, as well as their
     * digests.
     *
     * @param path the absolute path at which this file will be installed.
     * @param source the file content to include in this rpm.
     * @param mode the mode of the target file in standard three octet notation
     * @param dirmode the mode of the parent directories in standard three octet notation, or -1 for default.
     * @param directive directive indicating special handling for this file.
     * @param username ownership of added file
     * @param group ownership of added file
     * @throws NoSuchAlgorithmException the algorithm isn't supported
     * @throws IOException there was an IO error
     */
    public void addURL( final String path, final URL source, final int mode, final int dirmode, final Directive directive, final String username, final String group) throws NoSuchAlgorithmException, IOException {
        contents.addURL( path, source, mode, directive, username, group, dirmode);
    }

	/**
	 * Adds the directory to the repository with the default mode of <code>644</code>.
	 *
	 * @param path the absolute path to add as a directory.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
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
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
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
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addDirectory( final String path, final int permissions, final Directive directive, final String uname, final String gname, final boolean addParents) throws NoSuchAlgorithmException, IOException {
		contents.addDirectory( path, permissions, directive, uname, gname, addParents);
	}
	
	/**
	 * Adds the directory to the repository with the default mode of <code>644</code>.
	 *
	 * @param path the absolute path to add as a directory.
	 * @param directive directive indicating special handling for this file.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addDirectory( final String path, final Directive directive) throws NoSuchAlgorithmException, IOException {
		contents.addDirectory( path, directive);
	}

	/**
	 * Adds a symbolic link to the repository.
	 *
	 * @param path the absolute path at which this link will be installed.
	 * @param target the path of the file this link will point to.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addLink( final String path, final String target) throws NoSuchAlgorithmException, IOException {
		contents.addLink( path, target);
	}

	/**
	 * Adds a symbolic link to the repository.
	 *
	 * @param path the absolute path at which this link will be installed.
	 * @param target the path of the file this link will point to.
	 * @param permissions the permissions flags
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addLink( final String path, final String target, int permissions) throws NoSuchAlgorithmException, IOException {
		contents.addLink( path, target, permissions);
	}

	/**
	 * Adds a symbolic link to the repository.
	 *
	 * @param path the absolute path at which this link will be installed.
	 * @param target the path of the file this link will point to.
	 * @param permissions the permissions flags
	 * @param username user owner of the link
	 * @param groupname group owner of the link
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public void addLink( final String path, final String target, int permissions, String username, String groupname) throws NoSuchAlgorithmException, IOException {
		contents.addLink( path, target, permissions, username, groupname);
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
	 * Adds the supplied Changelog file as a Changelog to the header
	 * @param changelogFile File containing the Changelog information
	 * @throws IOException if file does not exist or cannot be read
	 * @throws ChangelogParseException if file is not of the correct format.
	 */
	public void addChangelogFile(File changelogFile) throws IOException, ChangelogParseException {
		new ChangelogHandler(this.format.getHeader()).addChangeLog(changelogFile);
	}
	

    /**
     * Sets the PGP key ring file used for header and header + payload signature.
     * Alternatively you can set the private key directly with {@link #setPrivateKey(org.bouncycastle.openpgp.PGPPrivateKey)}
     * @param privateKeyRingFile the private key ring file
     */
    public void setPrivateKeyRingFile( File privateKeyRingFile ) {
        this.privateKeyRingFile = privateKeyRingFile;
    }

    /**
     * Selects a private key from the current {@link #setPrivateKeyRingFile(java.io.File) private key ring file}.
     * If no key is specified, the first signing key will be selected.
     * @param privateKeyId hex key id
     */
    public void setPrivateKeyId( String privateKeyId ) {
        this.privateKeyId = privateKeyId;
    }

    /**
     * Passphrase for the private key
     * @param privateKeyPassphrase the private key pass phrase
     */
    public void setPrivateKeyPassphrase( String privateKeyPassphrase ) {
        this.privateKeyPassphrase = privateKeyPassphrase;
    }

    /**
     * Sets the private key for header and payload signing directly. Alternatively, you can set
     * {@link #setPrivateKeyRingFile(java.io.File) key ring file}, {@link #setPrivateKeyId(String) key id}
     * and {@link #setPrivateKeyPassphrase(String) passphrase} directly. Setting the private key has more
     * priorisation than providing key ring file.
     * @param privateKey the PGP private key
     */
    public void setPrivateKey( PGPPrivateKey privateKey ) {
        this.privateKey = privateKey;
    }

    /**
	 * Generates an RPM with a standard name consisting of the RPM package name, version, release,
	 * and type in the given directory.
	 *
	 * @param directory the destination directory for the new RPM file.
	 * @return the name of the rpm
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public String build( final File directory) throws NoSuchAlgorithmException, IOException {
		final String rpm = format.getLead().getName() + "." + format.getLead().getArch().toString().toLowerCase() + ".rpm";
		final File file = new File( directory, rpm);
		if ( file.exists()) file.delete();
		RandomAccessFile raFile = new RandomAccessFile( file, "rw");
		build(raFile.getChannel());
		raFile.close();
		return rpm;
	}

	/**
	 * Generates the rpm file to the provided file channel. This file channel must support memory mapping
	 * and therefore should be created from a {@link RandomAccessFile}, otherwise an {@link IOException} will be
	 * generated.
	 *
	 * @param original the {@link FileChannel} to which the resulting RPM will be written.
	 * @throws NoSuchAlgorithmException the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	@SuppressWarnings( "unchecked")
	public void build( final FileChannel original) throws NoSuchAlgorithmException, IOException {
		final WritableChannelWrapper output = new WritableChannelWrapper( original);

		format.getHeader().createEntry( REQUIRENAME, Dependency.getArrayOfNames(requires));
		format.getHeader().createEntry( REQUIREVERSION, Dependency.getArrayOfVersions(requires));
		format.getHeader().createEntry( REQUIREFLAGS, convert(Dependency.getArrayOfFlags(requires)));

		if (0 < obsoletes.size())
		{
			format.getHeader().createEntry( OBSOLETENAME, Dependency.getArrayOfNames(obsoletes));
			format.getHeader().createEntry( OBSOLETEVERSION, Dependency.getArrayOfVersions(obsoletes));
			format.getHeader().createEntry( OBSOLETEFLAGS, convert(Dependency.getArrayOfFlags(obsoletes)));
		}

		if (0 < conflicts.size())
		{
			format.getHeader().createEntry( CONFLICTNAME, Dependency.getArrayOfNames(conflicts));
			format.getHeader().createEntry(CONFLICTVERSION, Dependency.getArrayOfVersions(conflicts));
			format.getHeader().createEntry( CONFLICTFLAGS, convert(Dependency.getArrayOfFlags(conflicts)));
		}

		if (0 < provides.size())
		{
			format.getHeader().createEntry( PROVIDENAME, Dependency.getArrayOfNames(provides));
			format.getHeader().createEntry(PROVIDEVERSION, Dependency.getArrayOfVersions(provides));
			format.getHeader().createEntry( PROVIDEFLAGS, convert(Dependency.getArrayOfFlags(provides)));
		}

		format.getHeader().createEntry( SIZE, contents.getTotalSize());

		if (0 < contents.size()) {
			format.getHeader().createEntry(DIRNAMES, contents.getDirNames());
			format.getHeader().createEntry(DIRINDEXES, contents.getDirIndexes());
			format.getHeader().createEntry(BASENAMES, contents.getBaseNames());
		}


		if ( 0 < triggerCounter) {
			format.getHeader().createEntry( TRIGGERSCRIPTS, triggerscripts.toArray( new String[ triggerscripts.size()]));
			format.getHeader().createEntry( TRIGGERNAME, triggernames.toArray( new String[ triggernames.size()]));
			format.getHeader().createEntry( TRIGGERVERSION, triggerversions.toArray( new String[ triggerversions.size()]));
			format.getHeader().createEntry( TRIGGERFLAGS, convert( triggerflags.toArray( new Integer[ triggerflags.size()])));
			format.getHeader().createEntry( TRIGGERINDEX, convert( triggerindexes.toArray( new Integer[ triggerindexes.size()])));
			format.getHeader().createEntry( TRIGGERSCRIPTPROG, triggerscriptprogs.toArray( new String[ triggerscriptprogs.size()]));
		}

		if (0 < contents.size()) {
			String[] checksums = contents.getFileChecksums();
			format.getHeader().createEntry(FILEDIGESTALGO, 8);
			format.getHeader().createEntry(PAYLOADDIGESTALGO, 8);
			format.getHeader().createEntry(FILEDIGESTS, checksums);
			format.getHeader().createEntry(FILESIZES, contents.getSizes());
			format.getHeader().createEntry(FILEMODES, contents.getModes());
			format.getHeader().createEntry(FILERDEVS, contents.getRdevs());
			format.getHeader().createEntry(FILEMTIMES, contents.getMtimes());
			format.getHeader().createEntry(FILELINKTOS, contents.getLinkTos());
			format.getHeader().createEntry(FILEFLAGS, contents.getFlags());
			format.getHeader().createEntry(FILEUSERNAME, contents.getUsers());
			format.getHeader().createEntry(FILEGROUPNAME, contents.getGroups());
			format.getHeader().createEntry(FILEVERIFYFLAGS, contents.getVerifyFlags());
			format.getHeader().createEntry(FILEDEVICES, contents.getDevices());
			format.getHeader().createEntry(FILEINODES, contents.getInodes());
			format.getHeader().createEntry(FILELANGS, contents.getLangs());
			format.getHeader().createEntry(FILECONTEXTS, contents.getContexts());
		}

		format.getHeader().createEntry( PAYLOADFLAGS, new String[] { "9"});
		final Entry<String[]> payloadDigest = ( Entry< String[]>) format.getHeader().addEntry( PAYLOADDIGEST, 1);
		final Entry<String[]> payloadDigestAlt = ( Entry< String[]>) format.getHeader().addEntry( PAYLOADDIGESTALT, 1);

		final Entry< int[]> sigsize = ( Entry< int[]>) format.getSignature().addEntry( LEGACY_SIGSIZE, 1);
		final Entry< int[]> payload = ( Entry< int[]>) format.getSignature().addEntry( PAYLOADSIZE, 1);
		final Entry< byte[]> md5 = ( Entry< byte[]>) format.getSignature().addEntry( LEGACY_MD5, 16);
		final Entry< String[]> sha = ( Entry< String[]>) format.getSignature().addEntry( SHA1HEADER, 1);
		final Entry<String[]> sha256 = ( Entry< String[]>) format.getSignature().addEntry( SHA256HEADER, 1);
		sha.setSize( SHASIZE);
		sha256.setSize(SHA256_SIZE);
		payloadDigest.setSize(SHA256_SIZE);
		payloadDigestAlt.setSize(SHA256_SIZE);

        SignatureGenerator signatureGenerator = createSignatureGenerator();
        signatureGenerator.prepare( format.getSignature() );

		format.getLead().write( original);
		signature.setValues( getSignature( format.getSignature().count()));
		Util.empty( output, ByteBuffer.allocate( format.getSignature().write( original)));

		final Key< Integer> sigsizekey = output.start();
		final Key< byte[]> shakey = output.start( "SHA");
		final Key< byte[]> md5key = output.start( "MD5");
		final Key< byte[]> sha256key = output.start( "SHA-256");
        signatureGenerator.startBeforeHeader( output );
		immutable.setValues(getImmutable( format.getHeader().count()));
		String[] payloadDigestValue =  new String[] { Util.hex(calcPayloadDigest()) };
		payloadDigest.setValues( payloadDigestValue );
		payloadDigestAlt.setValues( payloadDigestValue );
		format.getHeader().write( output);
		sha.setValues( new String[] { Util.hex( output.finish( shakey))});
		sha256.setValues( new String[] { Util.hex( output.finish( sha256key) ) });
		signatureGenerator.finishAfterHeader( output );
		int payloadLength = processPayload(Channels.newOutputStream(output));
		payload.setValues( new int[] { payloadLength });
		md5.setValues( output.finish( md5key));
		sigsize.setValues( new int[] { output.finish( sigsizekey)});
        signatureGenerator.finishAfterPayload( output );
        format.getSignature().writePending( original);
	}

	private int processPayload(OutputStream output) throws IOException {
		final GZIPOutputStream zip = new GZIPOutputStream(output);
		final WritableChannelWrapper compressor = new WritableChannelWrapper( Channels.newChannel( zip));
		final Key< Integer> payloadkey = compressor.start();

		int total = 0;
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);
		for ( CpioHeader header : contents.headers()) {
			if ( ( header.getFlags() & Directive.RPMFILE_GHOST ) == Directive.RPMFILE_GHOST ) {
				continue;
			}
			final String path = header.getName();
			if ( path.startsWith( "/")) header.setName( "." + path);
			total = header.write( compressor, total);

			final Object object = contents.getSource( header);
			if ( object instanceof File) {
				FileInputStream fin = new FileInputStream(( File) object);
				final FileChannel in = fin.getChannel();
				while ( in.read(( ByteBuffer) buffer.rewind()) > 0) {
					total += compressor.write(( ByteBuffer) buffer.flip());
					buffer.compact();
				}
				total += header.skip( compressor, total);
				fin.close();
			} else if ( object instanceof URL) {
				final ReadableByteChannel in = Channels.newChannel((( URL) object).openConnection().getInputStream());
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

		zip.finish();
		return length;
	}

	private byte[] calcPayloadDigest() throws IOException {
		final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance( "SHA-256");
		} catch ( Exception e) {
			throw new RuntimeException( e);
		}
		DigestOutputStream digestOutputStream = new DigestOutputStream(nullOutputStream(), digest);
		processPayload(digestOutputStream);
		return digest.digest();
	}

    protected SignatureGenerator createSignatureGenerator() {
        if (privateKey != null) {
           return new SignatureGenerator( privateKey );
        }
        return new SignatureGenerator( privateKeyRingFile, privateKeyId, privateKeyPassphrase);
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
	 * @param tag the tag to get
	 * @param count the number to get
	 * @return the header bytes
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
	 * @param ints the array of Integer objects
	 * @return the primitive ints array
	 */
	protected int[] convert( final Integer[] ints) {
		int[] array = new int[ ints.length];
		int count = 0;
		for ( int i : ints) array[ count++] = i;
		return array;
	}

	public static OutputStream nullOutputStream() {
		return new OutputStream() {
			private volatile boolean closed;

			private void ensureOpen() throws IOException {
				if (closed) {
					throw new IOException("Stream closed");
				}
			}

			@Override
			public void write(int b) throws IOException {
				ensureOpen();
			}

			@Override
			public void write(byte b[], int off, int len) throws IOException {
				ensureOpen();
			}

			@Override
			public void close() {
				closed = true;
			}
		};
	}
}
