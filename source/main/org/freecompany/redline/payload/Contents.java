package org.freecompany.redline.payload;

import org.freecompany.redline.ChannelWrapper.*;
import org.freecompany.redline.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.freecompany.redline.payload.CpioHeader.*;

/**
 * NOT THREADSAFE
 */
public class Contents {

	protected final List< CpioHeader> headers = new LinkedList< CpioHeader>();
	protected final HashMap< CpioHeader, Object> sources = new HashMap< CpioHeader, Object>();

	public void addLink( final CharSequence path, final CharSequence target) {
		addLink( path, target, -1);
	}

	public void addLink( final CharSequence path, final CharSequence target, int permissions) {
		CpioHeader header = new CpioHeader( path);
		header.setType( SYMLINK);
		header.setFileSize( target.length());
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, target);
	}

	public void addDirectory( final CharSequence path) {
		addDirectory( path, -1);
	}

	public void addDirectory( final CharSequence path, int permissions) {
		CpioHeader header = new CpioHeader( path);
		header.setType( DIR);
		if ( permissions != -1) header.setPermissions( permissions);
		headers.add( header);
		sources.put( header, null);
	}

	public void addFile( final CharSequence path, final File source) throws FileNotFoundException {
		addFile( path, source, -1);
	}

	public void addFile( final CharSequence path, final File source, int permissions) throws FileNotFoundException {
		CpioHeader header = new CpioHeader( path, source);
		header.setType( FILE);
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
		final HashSet< String> set = new HashSet< String>();
		for ( CpioHeader header : headers) {
			String path = new File( header.getName().toString()).getParent();
			set.add( path);
		}
		return set.toArray( new String[ set.size()]);
	}

	public String[] getBaseNames() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = new File( headers.get( x).getName().toString()).getName();
		return array;
	}

	public int[] getSizes() {
		int[] array = new int[ headers.size()];
		for ( int x = 0; x < array.length; x++) {
			Object object = sources.get( headers.get( x));
			if ( object instanceof File) array[ x] = ( int) (( File) object).length();
		}
		return array;
	}

	public short[] getModes() {
		short[] array = new short[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( short) headers.get( x).getMode();
		return array;
	}

	public short[] getRdevs() {
		short[] array = new short[ headers.size()];
		for ( int x = 0; x < array.length; x++) {
			final CpioHeader header = headers.get( x);
			array[ x] = ( short) (( header.getRdevMajor() << 8) + header.getRdevMinor());
		}
		return array;
	}

	public int[] getMtimes() {
		int[] array = new int[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( int) headers.get( x).getMtime();
		return array;
	}

	public String[] getMD5s() throws FileNotFoundException, NoSuchAlgorithmException, IOException {
		final ByteBuffer buffer = ByteBuffer.allocate( 4096);

		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) {
			Object object = sources.get( headers.get( x));
			if ( object instanceof File) {
				final ReadableChannelWrapper input = new ReadableChannelWrapper( new FileInputStream(( File) object).getChannel());
				final Key< byte[]> key = input.start( "MD5");
				while ( input.read( buffer) != -1) buffer.rewind();
				array[ x] = new String( Util.hex( input.finish( key)));
				input.close();
			} else array[ x] = "";
		}
		return array;
	}

	public String[] getLinkTos() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) {
			Object object = sources.get( headers.get( x));
			if ( object instanceof String) array[ x] = String.valueOf( object);
			else array[ x] = "";
		}
		return array;
	}

	public int[] getFlags() {
		return new int[ headers.size()];
	}

	public String[] getUsers() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "unknown";
		return array;
	}

	public String[] getGroups() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "unknown";
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
		for ( int x = 0; x < array.length; x++) {
			final CpioHeader header = headers.get( x);
			array[ x] = ( header.getDevMajor() << 8) + header.getDevMinor();
		}
		return array;
	}

	public int[] getDirIndexes() {
		return new int[ headers.size()];
	}

	public int[] getInodes() {
		int[] array = new int[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( int) headers.get( x).getInode();
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
