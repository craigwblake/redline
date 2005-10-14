package org.freecompany.redline;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public abstract class AbstractHeader {

	public interface Tag {
		int getCode();
		String getName();
	}

	private static final int HEADER_HEADER_SIZE = 16;
	private static final int ENTRY_SIZE = 16;

	private static final int MAGIC_WORD = 0x8EADE801;

	protected static final Map< Integer, Tag> TAGS = new HashMap< Integer, Tag>();

	private final List< Entry> entries = new ArrayList< Entry>();
	private ByteBuffer index;
	private ByteBuffer data;

	public void read( ReadableByteChannel in) throws IOException {
		ByteBuffer header = Util.fill( in, HEADER_HEADER_SIZE);

		int magic = header.getInt();

		// TODO: Determine if this hack to fix mangled headers for some RPMs is really needed.
		if ( magic == 0) {
			header.compact();
			Util.fill( in, header);
			magic = header.getInt();
		}
		Util.check( MAGIC_WORD, magic);
		header.getInt();
		index = Util.fill( in, header.getInt() * ENTRY_SIZE);
		data = Util.fill( in, header.getInt());

		while ( index.remaining() >= 16) createEntry( index.getInt(), index.getInt(), index.getInt(), index.getInt());
	}

	public void write( WritableByteChannel out) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate( HEADER_HEADER_SIZE);
		buffer.putInt( MAGIC_WORD);
		buffer.putInt( 0);
		int size = entries.size() * ENTRY_SIZE;
		buffer.putInt( size);

		index = ByteBuffer.allocate( size);
		data = ByteBuffer.allocate( 10000);
		for ( Entry entry : entries) entry.write();
		buffer.putInt( data.position());
		
		Util.empty( out, ( ByteBuffer) buffer.flip());
		Util.empty( out, ( ByteBuffer) index.flip());
		Util.empty( out, ( ByteBuffer) data.flip());
	}

	public Iterable< Entry> entries() {
		return entries;
	}

	public void createEntry( int tag, int type, int offset, int count) {
		ByteBuffer buffer = data.duplicate();
		buffer.position( offset);
		
		Entry entry = createEntry( type);
		entry.setTag( tag);
		entry.setCount( count);
		entry.read( buffer);
		entries.add( entry);
	}

	protected Entry createEntry( int type) {
		switch ( type) {
			case 0:
				return new NullEntry();
			case 1:
				return new CharEntry();
			case 2:
				return new Int8Entry();
			case 3:
				return new Int8Entry();
			case 4:
				return new Int8Entry();
			case 5:
				return new Int8Entry();
			case 6:
				return new StringEntry();
			case 7:
				return new BinEntry();
			case 8:
				return new StringArrayEntry();
			case 9:
				return new I18NStringEntry();
		}
		throw new IllegalStateException( "Unknown entry type '" + type + "'.");
	}
	
	public abstract class Entry< T> {
		protected int tag;
		protected int count;
		protected T values;

		public void setTag( Tag tag) { this.tag = tag.getCode(); }
		public void setTag( int tag) { this.tag = tag; }
		public void setCount( int count) { this.count = count; }
		public void setValues( T values) { this.values = values; }

		public abstract void read( final ByteBuffer buffer);
		public abstract void write();

		public String toString() {
			return ( TAGS.containsKey( tag) ? TAGS.get( tag).getName() : super.toString()) + "[tag=" + tag + ",offset=" + data.position() + ",count=" + count + "]";
		}
	}

	class NullEntry extends Entry {
		public void read( final ByteBuffer buffer) {}
		public void write() {
			index.putInt( tag).putInt( 0).putInt( data.position()).putInt( 0);
		}
	}

	class CharEntry extends Entry< char[]> {
		public void read( final ByteBuffer buffer) {
			char[] values = new char[ count];
			for ( int x = 0; x < count; x++) values[ x] = ( char) buffer.get();
			setValues( values);
		}
		public void write() {
			index.putInt( tag).putInt( 1).putInt( data.position()).putInt( values.length);
			for ( char c : values) data.put(( byte) c);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( char c : values) builder.append( c);
			builder.append( "\n\t");
			return builder.toString();
		}
	}

	class Int8Entry extends Entry< byte[]> {
		public void read( final ByteBuffer buffer) {
			byte[] values = new byte[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.get();
			setValues( values);
		}
		public void write() {
			index.putInt( tag).putInt( 2).putInt( data.position()).putInt( values.length);
			for ( byte b : values) data.put( b);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( byte b : values) builder.append( b).append( ", ");
			return builder.toString();
		}
	}

	class Int16Entry extends Entry< short[]> {
		public void read( final ByteBuffer buffer) {
			short[] values = new short[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.getShort();
			setValues( values);
		}
		public void write() {
			Util.pad( data, 0x1);
			index.putInt( tag).putInt( 3).putInt( data.position()).putInt( values.length);
			for ( short s : values) data.putShort( s);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( short s : values) builder.append( s).append( ", ");
			return builder.toString();
		}
	}

	class Int32Entry extends Entry< int[]> {
		public void read( final ByteBuffer buffer) {
			int[] values = new int[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.getInt();
			setValues( values);
		}
		public void write() {
			Util.pad( data, 0x3);
			index.putInt( tag).putInt( 4).putInt( data.position()).putInt( values.length);
			for ( int i : values) data.putInt( i);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( int i : values) builder.append( i).append( ", ");
			return builder.toString();
		}
	}

	class Int64Entry extends Entry< long[]> {
		public void read( final ByteBuffer buffer) {
			long[] values = new long[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.getLong();
			setValues( values);
		}
		public void write() {
			Util.pad( data, 0x7);
			index.putInt( tag).putInt( 5).putInt( data.position()).putInt( values.length);
			for ( long l : values) data.putLong( l);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( long l : values) builder.append( l).append( ", ");
			return builder.toString();
		}
	}

	/**
	 * According to early documentation it should be illegal for this type of
	 * entry to store more than one string value, but other recent documents
	 * indicate that this may not longer be the case.
	 */
	class StringEntry extends Entry< String[]> {
		public void read( final ByteBuffer buffer) {
			String[] values = new String[ count];
			for ( int x = 0; x < count; x++) {
				StringBuilder string = new StringBuilder();
				byte b;
				while (( b = buffer.get()) != 0) string.append(( char) b);
				values[ x] = string.toString();
			}
			setValues( values);
		}
		public void write() {
			index.putInt( tag).putInt( 6).putInt( data.position()).putInt( values.length);
			for ( String s : values) data.put( Charset.forName( "US-ASCII").encode( s)).put(( byte) 0);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( String s : values) {
				builder.append( "\n\t");
				builder.append( s);
			}
			return builder.toString();
		}
	}

	class BinEntry extends Entry< byte[]> {
		public void read( final ByteBuffer buffer) {
			byte[] values = new byte[ count];
			buffer.get( values);
			setValues( values);
		}
		public void write() {
			index.putInt( tag).putInt( 6).putInt( data.position()).putInt( values.length);
			data.put( values);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n");
			Util.dump( values, builder);
			return builder.toString();
		}
	}

	class StringArrayEntry extends Entry< String[]> {
		public void read( final ByteBuffer buffer) {
			String[] values = new String[ count];
			for ( int x = 0; x < count; x++) {
				StringBuilder string = new StringBuilder();
				byte b;
				while (( b = buffer.get()) != 0) string.append(( char) b);
				values[ x] = string.toString();
			}
			setValues( values);
		}
		public void write() {
			index.putInt( tag).putInt( 6).putInt( data.position()).putInt( values.length);
			for ( String s : values) data.put( Charset.forName( "US-ASCII").encode( s)).put(( byte) 0);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( String s : values) {
				builder.append( "\n\t");
				builder.append( s);
			}
			return builder.toString();
		}
	}

	class I18NStringEntry extends Entry< String[]> {
		public void read( final ByteBuffer buffer) {
			String[] values = new String[ count];
			for ( int x = 0; x < count; x++) {
				StringBuilder string = new StringBuilder();
				byte b;
				while (( b = buffer.get()) != 0) string.append(( char) b);
				values[ x] = string.toString();
			}
			setValues( values);
		}
		public void write() {
			index.putInt( tag).putInt( 6).putInt( data.position()).putInt( values.length);
			for ( String s : values) data.put( Charset.forName( "US-ASCII").encode( s)).put(( byte) 0);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( String s : values) {
				builder.append( "\n\t");
				builder.append( s);
			}
			return builder.toString();
		}
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		for ( Entry entry : entries()) builder.append( entry).append( "\n");
		return builder.toString();
	}
}
