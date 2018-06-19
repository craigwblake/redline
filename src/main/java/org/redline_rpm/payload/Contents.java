package org.redline_rpm.payload;

import org.redline_rpm.ChannelWrapper.Key;
import org.redline_rpm.ReadableChannelWrapper;
import org.redline_rpm.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import static java.util.Arrays.asList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Logger.getLogger;
import static org.redline_rpm.Util.normalizePath;
import static org.redline_rpm.payload.CpioHeader.DEFAULT_DIRECTORY_PERMISSION;
import static org.redline_rpm.payload.CpioHeader.DEFAULT_FILE_PERMISSION;
import static org.redline_rpm.payload.CpioHeader.DEFAULT_GROUP;
import static org.redline_rpm.payload.CpioHeader.DEFAULT_USERNAME;
import static org.redline_rpm.payload.CpioHeader.DIR;
import static org.redline_rpm.payload.CpioHeader.FILE;
import static org.redline_rpm.payload.CpioHeader.SYMLINK;

/**
 * The contents of an RPM archive. These entries define the files and links that
 * the RPM contains as well as headers those files require. Note that the RPM format
 * requires that files in the archive be naturally ordered.
 */
public class Contents {

	private static final Set< String> BUILTIN = new HashSet< String>();
	private static final Set< String> DOC_DIRS = new HashSet< String>();
	static {
		BUILTIN.add( "/");
		BUILTIN.add( "/bin");
		BUILTIN.add( "/dev");
		BUILTIN.add( "/etc");
		BUILTIN.add( "/etc/bash_completion.d");
		BUILTIN.add( "/etc/cron.d");
		BUILTIN.add( "/etc/cron.daily");
		BUILTIN.add( "/etc/cron.hourly");
		BUILTIN.add( "/etc/cron.monthly");
		BUILTIN.add( "/etc/cron.weekly");
		BUILTIN.add( "/etc/default");
		BUILTIN.add( "/etc/init.d");
		BUILTIN.add( "/etc/logrotate.d");
		BUILTIN.add( "/lib");
		BUILTIN.add( "/usr");
		BUILTIN.add( "/usr/bin");
		BUILTIN.add( "/usr/lib");
		BUILTIN.add( "/usr/lib64");
		BUILTIN.add( "/usr/local");
		BUILTIN.add( "/usr/local/bin");
		BUILTIN.add( "/usr/local/lib");
		BUILTIN.add( "/usr/sbin");
		BUILTIN.add( "/usr/share");
		BUILTIN.add( "/usr/share/applications");
		BUILTIN.add( "/root");
		BUILTIN.add( "/sbin");
		BUILTIN.add( "/opt");
		BUILTIN.add( "/run");
		BUILTIN.add( "/srv");
		BUILTIN.add( "/tmp");
		BUILTIN.add( "/var");
		BUILTIN.add( "/var/cache");
		BUILTIN.add( "/var/lib");
		BUILTIN.add( "/var/log");
		BUILTIN.add( "/var/run");
		BUILTIN.add( "/var/spool");
		DOC_DIRS.add("/usr/doc");
		DOC_DIRS.add("/usr/man");
		DOC_DIRS.add("/usr/X11R6/man");
		DOC_DIRS.add("/usr/share/doc");
		DOC_DIRS.add("/usr/share/man");
		DOC_DIRS.add("/usr/share/info");
	}

	private Logger logger = getLogger( Contents.class.getName());
	private int inode = 1;

	protected final Set< CpioHeader> headers = new TreeSet< CpioHeader>( new HeaderComparator());
	protected final Set< String> files = new HashSet< String>();
	protected final Map< CpioHeader, Object> sources = new HashMap< CpioHeader, Object>();
	protected final Set< String> builtins = new HashSet< String>();
	
	public Contents()
	{
		builtins.addAll(BUILTIN);
	}

	/**
	 * Adds a directory entry to the archive with the default permissions of 644.
	 *
	 * @param path the destination path for the installed file.
	 * @param target the target string
	 */
	public synchronized void addLink( final String path, final String target) {
		addLink( path, target, -1);
	}

	/**
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param target the target string
	 * @param permissions the permissions flags.
	 */
	public synchronized void addLink( String path, final String target, int permissions) {
		addLink( path, target, permissions, null, null);
	}

	/**
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param target the target string
	 * @param permissions the permissions flags.
   	 * @param uname user owner for the given link
	 * @param gname group owner for the given link
	 */
	public synchronized void addLink( String path, final String target, int permissions, final String uname, final String gname) {
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( FINE, "Adding link ''{0}''.", path);
		CpioHeader header = new CpioHeader( path);
		header.setType( SYMLINK);
		header.setFileSize( target.length());
		header.setMtime( System.currentTimeMillis());
		header.setUname( getDefaultIfMissing( uname, DEFAULT_USERNAME));
		header.setGname( getDefaultIfMissing( gname, DEFAULT_GROUP));
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, target);
	}

	private String getDefaultIfMissing( String value, String defaultValue) {
		return value == null || value.isEmpty() ? defaultValue : value;
	}


	/**
	 * Adds a directory entry to the archive with the default permissions of 644.
	 *
	 * @param path the destination path for the installed file.
	 */
	public synchronized void addDirectory( final String path) {
		addDirectory( path, -1);
	}
	
	/**
	 * Adds a directory entry to the archive with the default permissions of 644.
	 *
	 * @param path the destination path for the installed file.
	 * @param directive directive indicating special handling for this directory.
	 */
	public synchronized void addDirectory( final String path, final Directive directive) {
		addDirectory( path, -1, directive, null, null);
	}
	
	/**
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param permissions the permissions flags.
	 */
	public synchronized void addDirectory( final String path, final int permissions) {
		addDirectory(path, permissions, null, null, null);
	}

	/**
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param permissions the permissions flags.
	 * @param directive directive indicating special handling for this directory.
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 */
	public synchronized void addDirectory( final String path, final int permissions, final Directive directive, final String uname, final String gname) {
		addDirectory(path, permissions, directive, uname, gname, true);
	}

	/**
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param permissions the permissions flags.
	 * @param directive directive indicating special handling for this directory.
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 * @param addParents whether to add parent directories to the rpm
	 */
	public synchronized void addDirectory( final String path, final int permissions, final Directive directive, final String uname, final String gname, boolean addParents) {
		if ( files.contains( path)) return;

		if ( addParents) addParents( new File( path), permissions, uname, gname);
		files.add( path);
		logger.log( FINE, "Adding directory ''{0}''.", path);
		CpioHeader header = new CpioHeader( path);
		header.setType( DIR);
		header.setInode( inode++);
		if ( null == uname) {
			header.setUname(DEFAULT_USERNAME);
		} else if (0 == uname.length()) {
			header.setUname(DEFAULT_USERNAME);
		} else {
			header.setUname(uname);
		}
		if ( null == gname) {
			header.setGname(DEFAULT_GROUP);
		} else if (0 == gname.length()) {
			header.setGname(DEFAULT_GROUP);
		} else {
			header.setGname(gname);
		}
		header.setMtime( System.currentTimeMillis());
		if ( -1 == permissions) {
			header.setPermissions( DEFAULT_DIRECTORY_PERMISSION);
		} else {
			header.setPermissions( permissions);
		}
		headers.add( header);
		sources.put( header, "");
		if ( directive != null) header.setFlags( directive.flag());
	}

	/**
	 * Adds a file entry to the archive with the default permissions of 644.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public void addFile( final String path, final File source) throws FileNotFoundException {
		addFile( path, source, -1);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public void addFile( final String path, final File source, int permissions) throws FileNotFoundException {
		addFile(path, source, permissions, null, null, null);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags.
	 * @param dirmode permission flags for parent directories, use -1 to leave as default.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public void addFile( final String path, final File source, int permissions, int dirmode) throws FileNotFoundException {
		addFile(path, source, permissions, null, null, null, dirmode);
	}
	
	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags.
	 * @param directive directive indicating special handling for this file.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public void addFile( final String path, final File source, int permissions, final Directive directive) throws FileNotFoundException {
		addFile(path, source, permissions, directive, null, null);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags.
	 * @param directive directive indicating special handling for this file.
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public void addFile( final String path, final File source, final int permissions, final Directive directive, final String uname, final String gname) throws FileNotFoundException {
		addFile( path, source, permissions, directive, uname, gname, -1);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags.
	 * @param directive directive indicating special handling for this file.
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 * @param dirmode permission flags for parent directories, use -1 to leave as default.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public synchronized void addFile( final String path, final File source, final int permissions, final Directive directive, final String uname, final String gname, final int dirmode) throws FileNotFoundException {
		addFile( path, source, permissions, directive, uname, gname, dirmode, true);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags, use -1 to leave as default.
	 * @param directive directive indicating special handling for this file, use null to ignore.
	 * @param uname user owner for the given file, use null for default user.
	 * @param gname group owner for the given file, use null for default group.
	 * @param dirmode permission flags for parent directories, use -1 to leave as default.
	 * @param addParents whether to create parent directories for the file, defaults to true for other methods.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public synchronized void addFile( final String path, final File source, final int permissions, final Directive directive, final String uname, final String gname, final int dirmode, final boolean addParents) throws FileNotFoundException {
		addFile( path, source, permissions, directive, uname, gname, dirmode, addParents, -1);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags, use -1 to leave as default.
	 * @param directive directive indicating special handling for this file, use null to ignore.
	 * @param uname user owner for the given file, use null for default user.
	 * @param gname group owner for the given file, use null for default group.
	 * @param dirmode permission flags for parent directories, use -1 to leave as default.
	 * @param addParents whether to create parent directories for the file, defaults to true for other methods.
	 * @param verifyFlags verify flags
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public synchronized void addFile( final String path, final File source, final int permissions, final Directive directive, final String uname, final String gname, final int dirmode, final boolean addParents, final int verifyFlags) throws FileNotFoundException {
		if ( files.contains( path)) return;

		if ( addParents) addParents( new File( path), dirmode, uname, gname);
		files.add( path);
		logger.log( FINE, "Adding file ''{0}''.", path);
		CpioHeader header;
		if ( directive != null && (( directive.flag() & Directive.RPMFILE_GHOST ) == Directive.RPMFILE_GHOST ))
			header = new CpioHeader( path);
		else
			header = new CpioHeader( path, source);
		header.setType( FILE);
		header.setInode( inode++);
		if ( null == uname) {
			header.setUname(DEFAULT_USERNAME);
		} else if (0 == uname.length()) {
			header.setUname(DEFAULT_USERNAME);
		} else {
			header.setUname(uname);
		}
		if ( null == gname) {
			header.setGname(DEFAULT_GROUP);
		} else if (0 == gname.length()) {
			header.setGname(DEFAULT_GROUP);
		} else {
			header.setGname(gname);
		}
		if ( -1 == permissions) {
			header.setPermissions(DEFAULT_FILE_PERMISSION);
		} else {
			header.setPermissions( permissions);
		}
		header.setVerifyFlags(verifyFlags);
		headers.add( header);
		sources.put( header, source);

		if ( directive != null) header.setFlags( directive.flag());
	}

	/**
	 * Adds a URL entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the URL with the data to be added
	 * @param permissions the permissions flags.
	 * @param directive directive indicating special handling for this file.
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 * @param dirmode permission flags for parent directories, use -1 to leave as default.
	 * @throws java.io.FileNotFoundException file wasn't found
	 */
	public synchronized void addURL( final String path, final URL source, final int permissions, final Directive directive, final String uname, final String gname, final int dirmode) throws FileNotFoundException {
		if ( files.contains( path)) return;

		addParents( new File( path), dirmode, uname, gname);
		files.add( path);
		logger.log( FINE, "Adding file ''{0}''.", path);
		CpioHeader header = new CpioHeader( path, source);
		header.setType( FILE);
		header.setInode( inode++);
		if (uname != null) header.setUname(uname);
		if (gname != null) header.setGname(gname);
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, source);
		
		if ( directive != null) header.setFlags( directive.flag());
	}

	/**
	 * Adds entries for parent directories of this file, so that they may be cleaned up when 
	 * removing the package.
	 * @param file the file to add parent directories of
	 * @param permissions the permissions flags
	 * @param uname user owner for the given file
	 * @param gname group owner for the given file
	 */
	protected synchronized void addParents( final File file, final int permissions, final String uname, final String gname ) {
		final ArrayList< String> parents = new ArrayList< String>();
		listParents( parents, file);
		for ( String parent : parents) addDirectory( parent, permissions, null, uname, gname);
	}

	/**
	 * Add additional directory that is assumed to already exist on system where the RPM will be installed
	 * (e.g. /etc) and should not have an entry in the RPM.
	 * 
	 * The directory will be added to all instance of Contents created after this method is called.
	 *
	 * @param directory the directory to add
	 */
	public static synchronized void addBuiltinDirectory( final String directory) {
		BUILTIN.add(directory);
	}
	
	/**
	 * Add additional directory that is assumed to already exist on system where the RPM will be installed
	 * (e.g. /etc) and should not have an entry in the RPM.
	 * 
	 * The builtin will only be added to this instance of Contents.
	 *
	 * @param directory the directory to add
	 */
	public synchronized void addLocalBuiltinDirectory( final String directory) {
		builtins.add(directory);
	}

	/**
	 * Retrieve the size of this archive in number of files. This count includes both directory entries and
	 * soft links.
	 * @return the number of files in this archive
	 */
	public int size() { return headers.size(); }

	/**
	 * Retrieve the archive headers. The returned {@link Iterable} will iterate in the correct order for
	 * the final archive.
	 * @return the headers
	 */
	public Iterable< CpioHeader> headers() { return headers; }

	/**
	 * Retrieves the content for this archive entry, which may be a {@link File} if the entry is a regular file or
	 * a {@link CharSequence} containing the name of the target path if the entry is a link. This is the value to
	 * be written to the archive as the body of the entry.
	 * @param header the header to get the content from
	 * @return the content
	 */
	public Object getSource( CpioHeader header) { return sources.get( header); }

	/**
	 * Accumulated size of all files included in the archive.
	 * @return the size of all files included in the archive
	 */
	public int getTotalSize() {
		int total = 0;
		try {
			for ( Object object : sources.values()) {
				if ( object instanceof File) total += (( File) object).length();
				else if ( object instanceof URL) total += (( URL) object).openConnection().getContentLength();
			}
		} catch ( IOException e) {
			throw new RuntimeException( e);
		}
		return total;
	}

	/**
	 * Gets the dirnames headers values.
	 * @return the dirnames headers values
	 */
	public String[] getDirNames() {
		final Set< String> set = new LinkedHashSet< String>();
		for ( CpioHeader header : headers) {
			String path = new File( header.getName()).getParent();
			if ( path == null) continue;

			String parent = normalizePath( path);
			if ( !parent.endsWith( "/")) parent += "/";
			set.add( parent);
		}
		return set.toArray( new String[ set.size()]);
	}

	/**
	 * Gets the dirindexes headers values.
	 * @return the dirindexes
	 */
	// TODO: Fix this (as part of general refactoring) to be much better.
	public int[] getDirIndexes() {
		final List< String> dirs = asList( getDirNames());
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			String path = new File( header.getName()).getParent();
			if ( path == null) continue;

			String parent = normalizePath( path);
			if ( !parent.endsWith( "/")) parent += "/";
			array[ x++] = dirs.indexOf( parent);
		}
		return array;
	}

	/**
	 * Gets the basenames header values.
	 * @return the basename header values
	 */
	public String[] getBaseNames() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = normalizePath( new File( header.getName()).getName());
		return array;
	}

	/**
	 * Gets the sizes header values.
	 * @return the sizes header values
	 */
	public int[] getSizes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		try {
			for ( CpioHeader header : headers) {
				Object object = sources.get( header);
				if ( object instanceof File) array[ x] = ( int) (( File) object).length();
				else if ( object instanceof URL) array[ x] = (( URL) object).openConnection().getContentLength();
				else if ( header.getType() == DIR) array[ x] = 4096;
				else if ( header.getType() == SYMLINK) array[ x] = (( String) object).length();
				++x;
			}
		} catch ( IOException e) {
			throw new RuntimeException( e);
		}
		return array;
	}

	/**
	 * Gets the modes header values.
	 * @return the modes header values
	 */
	public short[] getModes() {
		short[] array = new short[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( short) header.getMode();
		return array;
	}

	/**
	 * Gets the rdevs header values.
	 * @return the rdevs header values
	 */
	public short[] getRdevs() {
		short[] array = new short[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( short) (( header.getRdevMajor() << 8) + header.getRdevMinor());
		return array;
	}

	/**
	 * Gets the mtimes header values.
	 * @return the mtimes header values
	 */
	public int[] getMtimes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			array[ x++] = header.getMtime();
		}
		return array;
	}

	/**
	 * Caclulates an MD5 hash for each file in the archive.
	 * @return the MD5 hashes
	 * @throws NoSuchAlgorithmException if the algorithm isn't supported
	 * @throws IOException there was an IO error
	 */
	public String[] getMD5s() throws NoSuchAlgorithmException, IOException {
		/**
		 * This could be more efficiently handled during the output phase using a filtering channel,
		 * but would require placeholder values in the archive and some state. This is left for a
		 * later refactoring.
		 */
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);
		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			Object object = sources.get( header);
			String value = "";
			if ( object instanceof File) {
				FileInputStream fileInput = new FileInputStream(( File) object);
				final ReadableChannelWrapper input = new ReadableChannelWrapper( fileInput.getChannel());
				final Key< byte[]> key = input.start( "MD5");
				while ( input.read( buffer) != -1) buffer.rewind();
				value = Util.hex(input.finish(key));
				input.close();
				fileInput.close();
			} else if ( object instanceof URL) {
				final ReadableChannelWrapper input = new ReadableChannelWrapper( Channels.newChannel((( URL) object).openConnection().getInputStream()));
				final Key< byte[]> key = input.start( "MD5");
				while ( input.read( buffer) != -1) buffer.rewind();
				value = Util.hex(input.finish(key));
				input.close();
			}
			array[ x++] = value;
		}
		return array;
	}

	/**
	 * Gets the linktos header values.
	 * @return the linktos header values
	 */
	public String[] getLinkTos() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			Object object = sources.get( header);
			String value = "";
			if ( object instanceof String) value = String.valueOf( object);
			array[ x++] = value;
		}
		return array;
	}

	/**
	 * Gets the flags header values.
	 * @return the flags header values
	 */
	public int[] getFlags() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = header.getFlags();
		return array;
	}

	/**
	 * Gets the users header values.
	 * @return the users header values
	 */
	public String[] getUsers() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for (CpioHeader header : headers) {
			array[ x++] = header.getUname() == null ? "root" : header.getUname();
		}
		return array;
	}

	/**
	 * Gets the groups header values.
	 * @return the groups header values
	 */
	public String[] getGroups() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for (CpioHeader header : headers) {
			array[ x++] = header.getGname() == null ? "root" : header.getGname();
		}
		return array;
	}

	/**
	 * Gets the colors header values.
	 * @return the colors header values
	 */
	public int[] getColors() {
		return new int[ headers.size()];
	}

	/**
	 * Gets the verifyflags header values.
	 * @return the verifyflags header values
	 */
	public int[] getVerifyFlags() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for (CpioHeader header : headers) {
			array[ x++] = header.getVerifyFlags();
		}
		return array;
	}

	/**
	 * Gets the classes header values.
	 * @return the classes header values
	 */
	public int[] getClasses() {
		int[] array = new int[ headers.size()];
		Arrays.fill( array, 1);
		return array;
	}

	/**
	 * Gets the devices header values.
	 * @return the devices header values
	 */
	public int[] getDevices() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( header.getDevMajor() << 8) + header.getDevMinor();
		return array;
	}

	/**
	 * Gets the inodes header values.
	 * @return the iNodes header values
	 */
	public int[] getInodes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = header.getInode();
		return array;
	}

	/**
	 * Gets the langs header values.
	 * @return the langs header values
	 */
	public String[] getLangs() {
		String[] array = new String[ headers.size()];
		Arrays.fill( array, "");
		return array;
	}

	/**
	 * Gets the dependsx header values.
	 * @return the dependsx header values
	 */
	public int[] getDependsX() {
		return new int[ headers.size()];
	}

	/**
	 * Gets the dependsn header values.
	 * @return the dependsn header values
	 */
	public int[] getDependsN() {
		return new int[ headers.size()];
	}

	/**
	 * Gets the contexts header values.
	 * @return the contexts header values
	 */
	public String[] getContexts() {
		String[] array = new String[ headers.size()];
		Arrays.fill( array, "<<none>>");
		return array;
	}

	/**
	 * Generates a list of parent paths given a starting path.
	 * @param parents the list to add the parents to
	 * @param file the file to search for parents of
	 */
	protected void listParents( final List< String> parents, final File file) {
		final File parent = file.getParentFile();
		if ( parent == null) return;
		
		final String path = normalizePath( parent.getPath());
		if ( builtins.contains( path)) return;

		parents.add( path);
		listParents( parents, parent);
	}

	/**
	 * Comparator that orders files in the CPIO archive by their file name
	 * as present in th header.
	 */
	private static class HeaderComparator implements Comparator< CpioHeader> {
		public int compare( final CpioHeader one, final CpioHeader two) {
			return one.getName().compareTo( two.getName());
		}
		public boolean equals( final CpioHeader one, final CpioHeader two) {
			return one.getName().equals( two.getName());
		}
	}
}
