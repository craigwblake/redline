package org.freecompany.redline.payload;

import org.freecompany.redline.ChannelWrapper.*;
import org.freecompany.redline.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Collections.unmodifiableSet;
import static java.util.Arrays.asList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;
import static org.freecompany.redline.payload.CpioHeader.*;

/**
 * NOT THREADSAFE
 */
public class Contents {

	private Logger logger = getLogger( Contents.class.getName());
	private int inode = 1;

	/**
	 * The RPM format requires that files in the archive and file lists to be naturally
	 * ordered.
	 */
	protected final TreeSet< CpioHeader> headers = new TreeSet< CpioHeader>( new Comparator< CpioHeader>() {
		public int compare( final CpioHeader one, final CpioHeader two) {
			return one.getName().compareTo( two.getName());
		}
		public boolean equals( final CpioHeader one, final CpioHeader two) {
			return one.getName().equals( two.getName());
		}
	});
	protected final HashSet< String> files = new HashSet< String>();
	protected final HashMap< CpioHeader, Object> sources = new HashMap< CpioHeader, Object>();

	public void addLink( final String path, final String target) {
		addLink( path, target, -1);
	}

	public void addLink( final String path, final String target, int permissions) {
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( INFO, "Adding link ''{0}''.", path);
		CpioHeader header = new CpioHeader( path);
		header.setType( SYMLINK);
		header.setFileSize( target.length());
		header.setMtime( System.currentTimeMillis());
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, target);
	}

	public void addDirectory( final String path) {
		addDirectory( path, -1);
	}

	public void addDirectory( final String path, int permissions) {
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( INFO, "Adding directory ''{0}''.", path);
		CpioHeader header = new CpioHeader( path);
		header.setType( DIR);
		header.setInode( inode++);
		header.setMtime( System.currentTimeMillis());
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, null);
	}

	public void addFile( final String path, final File source) throws FileNotFoundException {
		addFile( path, source, -1);
	}

	public void addFile( final String path, final File source, int permissions) throws FileNotFoundException {
		if ( files.contains( path)) return;
		files.add( path);
		logger.log( INFO, "Adding file ''{0}''.", path);
		CpioHeader header = new CpioHeader( path, source);
		header.setType( FILE);
		header.setInode( inode++);
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, source);
	}

	public int size() { return headers.size(); }
	public Iterable< CpioHeader> headers() { return headers; }
	public Object getSource( CpioHeader header) { return sources.get( header); }

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
			array[ x] = dirs.indexOf( parent);
			++x;
		}
		return array;
	}

	public String[] getBaseNames() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			array[ x] = new File( header.getName().toString()).getName();
			++x;
		}
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
		for ( CpioHeader header : headers) {
			array[ x] = ( short) header.getMode();
			++x;
		}
		return array;
	}

	public short[] getRdevs() {
		short[] array = new short[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			array[ x] = ( short) (( header.getRdevMajor() << 8) + header.getRdevMinor());
			++x;
		}
		return array;
	}

	public int[] getMtimes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			array[ x] = ( int) header.getMtime();
			++x;
		}
		return array;
	}

	public String[] getMD5s() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);

		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			Object object = sources.get( header);
			if ( object instanceof File) {
				final ReadableChannelWrapper input = new ReadableChannelWrapper( new FileInputStream(( File) object).getChannel());
				final Key< byte[]> key = input.start( "MD5");
				while ( input.read( buffer) != -1) buffer.rewind();
				array[ x] = new String( Util.hex( input.finish( key)));
				input.close();
			} else array[ x] = "";
			++x;
		}
		return array;
	}

	public String[] getLinkTos() {
		String[] array = new String[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			Object object = sources.get( header);
			if ( object instanceof String) array[ x] = String.valueOf( object);
			else array[ x] = "";
			++x;
		}
		return array;
	}

	public int[] getFlags() {
		return new int[ headers.size()];
	}

	public String[] getUsers() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "root";
		return array;
	}

	public String[] getGroups() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "root";
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
		for ( CpioHeader header : headers) {
			array[ x] = ( header.getDevMajor() << 8) + header.getDevMinor();
			++x;
		}
		return array;
	}

	public int[] getInodes() {
		int[] array = new int[ headers.size()];
		int x = 0;
		for ( CpioHeader header : headers) {
			array[ x] = ( int) header.getInode();
			++x;
		}
		return array;
	}

	public String[] getLangs() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "";
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
		for ( int x = 0; x < array.length; x++) array[ x] = "<<none>>";
		return array;
	}
}
