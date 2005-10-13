package org.freecompany.redline;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
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
		buffer.putInt( offset);

		index = ByteBuffer.allocate( size);
		data = ByteBuffer.allocate( 10000);
		for ( Entry entry : entries) entry.write( out);
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
		protected T data;

		public void setTag( Tag tag) { this.tag = tag.getCode(); }
		public void setTag( int tag) { this.tag = tag; }
		public void setCount( int count) { this.count = count; }
		public void setData( T data) { this.data = data; }

		public abstract void read( final ByteBuffer buffer);
		public abstract void write( final ByteBuffer buffer);

		public String toString() {
			return ( TAGS.containsKey( tag) ? TAGS.get( tag).getName() : super.toString()) + "[tag=" + tag + ",offset=" + offset + ",count=" + count + "]";
		}
	}

	class NullEntry extends Entry {
		public void read( final ByteBuffer buffer) {}
		public void write( final WritableByteChannel channel) {
			index.put( tag, 0, data.position(), 0);
		}
	}

	class CharEntry extends Entry< char[]> {
		public void read( final ByteBuffer buffer) {
			char[] data = new char[ count];
			for ( int x = 0; x < count; x++) data[ x] = ( char) buffer.get();
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( char c : data) builder.append( c);
			builder.append( "\n\t");
			return builder.toString();
		}
	}

	class Int8Entry extends Entry< byte[]> {
		public void read( final ByteBuffer buffer) {
			byte[] data = new byte[ count];
			for ( int x = 0; x < count; x++) data[ x] = buffer.get();
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( byte b : data) builder.append( b).append( ", ");
			return builder.toString();
		}
	}

	class Int16Entry extends Entry< short[]> {
		public void read( final ByteBuffer buffer) {
			short[] data = new short[ count];
			for ( int x = 0; x < count; x++) data[ x] = buffer.getShort();
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( short s : data) builder.append( s).append( ", ");
			return builder.toString();
		}
	}

	class Int32Entry extends Entry< int[]> {
		public void read( final ByteBuffer buffer) {
			int[] data = new int[ count];
			for ( int x = 0; x < count; x++) data[ x] = buffer.getInt();
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( int i : data) builder.append( i).append( ", ");
			return builder.toString();
		}
	}

	class Int64Entry extends Entry< long[]> {
		public void read( final ByteBuffer buffer) {
			long[] data = new long[ count];
			for ( int x = 0; x < count; x++) data[ x] = buffer.getLong();
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( long l : data) builder.append( l).append( ", ");
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
			String[] data = new String[ count];
			for ( int x = 0; x < count; x++) {
				StringBuilder string = new StringBuilder();
				byte b;
				while (( b = buffer.get()) != 0) string.append(( char) b);
				data[ x] = string.toString();
			}
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( String s : data) {
				builder.append( "\n\t");
				builder.append( s);
			}
			return builder.toString();
		}
	}

	class BinEntry extends Entry< byte[]> {
		public void read( final ByteBuffer buffer) {
			byte[] data = new byte[ count];
			buffer.get( data);
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n");
			Util.dump( data, builder);
			return builder.toString();
		}
	}

	class StringArrayEntry extends Entry< String[]> {
		public void read( final ByteBuffer buffer) {
			String[] data = new String[ count];
			for ( int x = 0; x < count; x++) {
				StringBuilder string = new StringBuilder();
				byte b;
				while (( b = buffer.get()) != 0) string.append(( char) b);
				data[ x] = string.toString();
			}
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( String s : data) {
				builder.append( "\n\t");
				builder.append( s);
			}
			return builder.toString();
		}
	}

	class I18NStringEntry extends Entry< String[]> {
		public void read( final ByteBuffer buffer) {
			String[] data = new String[ count];
			for ( int x = 0; x < count; x++) {
				StringBuilder string = new StringBuilder();
				byte b;
				while (( b = buffer.get()) != 0) string.append(( char) b);
				data[ x] = string.toString();
			}
			setData( data);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( String s : data) {
				builder.append( "\n\t");
				builder.append( s);
			}
			return builder.toString();
		}
	}
}
