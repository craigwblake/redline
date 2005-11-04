package org.freecompany.redline;

import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

import static org.freecompany.redline.ChannelWrapper.Key;

public class IncludeFiles {
	
	protected int total;
	protected final ByteBuffer buffer = ByteBuffer.allocate( 1024);
	protected final Map< CpioHeader, File> targets = new HashMap< CpioHeader, File>();
	protected final Map< CpioHeader, File> sources = new HashMap< CpioHeader, File>();
	protected final Map< CpioHeader, String> md5s = new HashMap< CpioHeader, String>();
	protected final LinkedList< CpioHeader> headers = new LinkedList< CpioHeader>();

	public synchronized void addFile( final File target, final File source) throws Exception {
		final CpioHeader header = new CpioHeader( source);
		headers.add( header);
		targets.put( header, target);
		sources.put( header, source);
		final ReadableChannelWrapper input = new ReadableChannelWrapper( new FileInputStream( source).getChannel());
		final Key< Integer> size = input.start();
		final Key< byte[]> key = input.start( "MD5");
		while ( input.read( buffer) != -1) buffer.rewind();
		md5s.put( header, Util.hex( input.finish( key)));
		total += input.finish( size);
		input.close();
	}

	public Iterable< CpioHeader> headers() { return headers; }
	public File target( final CpioHeader header) { return targets.get( header); }
	public File source( final CpioHeader header) { return sources.get( header); }

	public int getTotalSize() { return total; }

	public String[] getDirNames() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = targets.get( headers.get( x)).getParent() + "/";
		return array;
	}

	public String[] getBaseNames() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = targets.get( headers.get( x)).getName();
		return array;
	}

	public int[] getSizes() {
		int[] array = new int[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( int) sources.get( headers.get( x)).length();
		return array;
	}

	public short[] getModes() {
		short[] array = new short[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( short) headers.get( x).getMode();
		return array;
	}

	public short[] getRdevs() {
		short[] array = new short[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( short) headers.get( x).getRdevMajor();
		return array;
	}

	public int[] getMtimes() {
		int[] array = new int[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( int) headers.get( x).getMtime();
		return array;
	}

	public String[] getMD5s() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = md5s.get( headers.get( x));
		return array;
	}

	public String[] getLinkTos() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "";
		return array;
	}

	public int[] getFlags() {
		return new int[ headers.size()];
	}

	public String[] getUsers() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "";
		return array;
	}

	public String[] getGroups() {
		String[] array = new String[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = "";
		return array;
	}

	public int[] getVerifyFlags() {
		return new int[ headers.size()];
	}

	public int[] getDevices() {
		int[] array = new int[ headers.size()];
		for ( int x = 0; x < array.length; x++) array[ x] = ( int) headers.get( x).getDevMajor();
		return array;
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
		for ( int x = 0; x < array.length; x++) array[ x] = "";
		return array;
	}
}
