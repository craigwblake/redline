package org.freecompany.redline.payload;

import org.freecompany.redline.ChannelWrapper.Key;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.Util;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import static java.util.Collections.unmodifiableSet;
import static java.util.Arrays.asList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Logger.getLogger;
import static org.freecompany.redline.payload.CpioHeader.*;
import static org.freecompany.redline.Util.normalizePath;

/**
 * The contents of an RPM archive. These entries define the files and links that
 * the RPM contains as well as headers those files require. Note that the RPM format
 * requires that files in the archive be naturally ordered.
 */
public class Contents {

	private Logger logger = getLogger( Contents.class.getName());
	private int inode = 1;

	protected final TreeSet< CpioHeader> headers = new TreeSet< CpioHeader>( new HeaderComparator());
	protected final HashSet< String> files = new HashSet< String>();
	protected final HashMap< CpioHeader, Object> sources = new HashMap< CpioHeader, Object>();

	/**
	 * Adds a directory entry to the archive with the default permissions of 644.
	 *
	 * @param path the destination path for the installed file.
	 */
	public synchronized void addLink( final String path, final String target) {
		addLink( path, target, -1);
	}

	/**
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param permissions the permissions flags.
	 */
	public synchronized void addLink( String path, final String target, int permissions) {
		path = normalizePath( path);
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( FINE, "Adding link ''{0}''.", path);
		CpioHeader header = new CpioHeader( path);
		header.setType( SYMLINK);
		header.setFileSize( target.length());
		header.setMtime( System.currentTimeMillis());
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, target);
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
	 * Adds a directory entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param permissions the permissions flags.
	 */
	public synchronized void addDirectory( String path, int permissions) {
		path = normalizePath( path);
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( FINE, "Adding directory ''{0}''.", path);
		CpioHeader header = new CpioHeader( path);
		header.setType( DIR);
		header.setInode( inode++);
		header.setMtime( System.currentTimeMillis());
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, null);
	}

	/**
	 * Adds a file entry to the archive with the default permissions of 644.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 */
	public synchronized void addFile( final String path, final File source) throws FileNotFoundException {
		addFile( path, source, -1);
	}

	/**
	 * Adds a file entry to the archive with the specified permissions.
	 *
	 * @param path the destination path for the installed file.
	 * @param source the local file to be included in the package.
	 * @param permissions the permissions flags.
	 */
	public synchronized void addFile( String path, final File source, int permissions) throws FileNotFoundException {
		path = normalizePath( path);
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( FINE, "Adding file ''{0}''.", path);
		CpioHeader header = new CpioHeader( path, source);
		header.setType( FILE);
		header.setInode( inode++);
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, source);
	}

	/**
	 * Retrieve the size of this archive in number of files. This count includes both directory entries and
	 * soft links.
	 */
	public int size() { return headers.size(); }

	/**
	 * Retrieve the archive headers. The returned {@link Iterable} will iterate in the correct order for
	 * the final archive.
	 */
	public Iterable< CpioHeader> headers() { return headers; }

	/**
	 * Retrieves the content for this archive entry, which may be a {@link File} if the entry is a regular file or
	 * a {@link CharSequence} containing the name of the target path if the entry is a link. This is the value to
	 * be written to the archive as the body of the entry.
	 */
	public Object getSource( CpioHeader header) { return sources.get( header); }

	/**
	 * Accumulated size of all files included in the archive.
	 */
	public int getTotalSize() {
		int total = 0;
		for ( Object object : sources.values()) if ( object instanceof File) total += (( File) object).length();
		return total;
	}

	public String[] getDirNames() {
		final Set< String> set = new LinkedHashSet< String>();
		for ( CpioHeader header : headers) {
			String parent = new File( header.getName().toString()).getParent();
			if ( !parent.endsWith( "/")) parent += "/";
			set.add( parent);
		}
		return set.toArray( new String[ set.size()]);
	}

	// TODO: Fix this (as part of general refactoring) to be much better.
	public int[] getDirIndexes() {
		final List< String> dirs = asList( getDirNames());
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			String parent = new File( header.getName().toString()).getParent();
			if ( !parent.endsWith( "/")) parent += "/";
			array[ x++] = dirs.indexOf( parent);
		}
		return array;
	}

	public String[] getBaseNames() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = new File( header.getName().toString()).getName();
		return array;
	}

	public int[] getSizes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			Object object = sources.get( header);
			if ( object instanceof File) array[ x] = ( int) (( File) object).length();
			++x;
		}
		return array;
	}

	public short[] getModes() {
		short[] array = new short[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( short) header.getMode();
		return array;
	}

	public short[] getRdevs() {
		short[] array = new short[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( short) (( header.getRdevMajor() << 8) + header.getRdevMinor());
		return array;
	}

	public int[] getMtimes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			array[ x++] = ( int) header.getMtime();
		}
		return array;
	}

	/**
	 * Caclulates an MD5 hash for each file in the archive.
	 */
	public String[] getMD5s() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
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
				final ReadableChannelWrapper input = new ReadableChannelWrapper( new FileInputStream(( File) object).getChannel());
				final Key< byte[]> key = input.start( "MD5");
				while ( input.read( buffer) != -1) buffer.rewind();
				value = new String( Util.hex( input.finish( key)));
				input.close();
			}
			array[ x++] = value;
		}
		return array;
	}

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

	public int[] getFlags() {
		return new int[ headers.size()];
	}

	public String[] getUsers() {
		String[] array = new String[ headers.size()];
		Arrays.fill( array, "root");
		return array;
	}

	public String[] getGroups() {
		String[] array = new String[ headers.size()];
		Arrays.fill( array, "root");
		return array;
	}

	public int[] getColors() {
		return new int[ headers.size()];
	}

	public int[] getVerifyFlags() {
		int[] array = new int[ headers.size()];
		Arrays.fill( array, -1);
		return array;
	}

	public int[] getClasses() {
		int[] array = new int[ headers.size()];
		Arrays.fill( array, 1);
		return array;
	}

	public int[] getDevices() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( header.getDevMajor() << 8) + header.getDevMinor();
		return array;
	}

	public int[] getInodes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) array[ x++] = ( int) header.getInode();
		return array;
	}

	public String[] getLangs() {
		String[] array = new String[ headers.size()];
		Arrays.fill( array, "");
		return array;
	}

	public int[] getDependsX() {
		return new int[ headers.size()];
	}

	public int[] getDependsN() {
		return new int[ headers.size()];
	}

	public String[] getContexts() {
		String[] array = new String[ headers.size()];
		Arrays.fill( array, "<<none>>");
		return array;
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
	};
}
