package org.freecompany.redline.header;

import org.freecompany.redline.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

import static java.nio.channels.FileChannel.MapMode.*;

public abstract class AbstractHeader {

	public interface Tag {
		int getCode();
		int getType();
		String getName();
	}

	protected static final int HEADER_HEADER_SIZE = 16;
	protected static final int ENTRY_SIZE = 16;
	protected static final int MAGIC_WORD = 0x8EADE801;

	protected final Map< Integer, Tag> tags = new HashMap< Integer, Tag>();
	protected final Map< Integer, Entry> entries = new TreeMap< Integer, Entry>();
	protected final Map< Entry, ByteBuffer> pending = new LinkedHashMap< Entry, ByteBuffer>();

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
		final ByteBuffer index = Util.fill( in, header.getInt() * ENTRY_SIZE);
		final ByteBuffer data = Util.fill( in, header.getInt());

		while ( index.remaining() >= ENTRY_SIZE) readEntry( index.getInt(), index.getInt(), index.getInt(), index.getInt(), data);
	}

	/**
	 * Writes this header section to the provided file at the current position.
	 */
	public void write( FileChannel out) throws IOException {
		final ByteBuffer header = mapHeader( out);
		final ByteBuffer index = mapIndex( out);
		header.putInt( writeData( index, out));
	}

	/**
	 * Memory maps the portion of the destination file that will contain the header structure
	 * header and advances the file channels position.  The resulting buffer will be prefilled with
	 * the necesssary magic data and the correct index count, but will require an integer value to
	 * be written with the total data section size once data writing is complete.
	 * <p/>
	 * This method must be invoked before mapping the index or data sections.
	 */
	protected ByteBuffer mapHeader( final FileChannel out) throws IOException {
		ByteBuffer buffer = out.map( READ_WRITE, out.position(), HEADER_HEADER_SIZE);
		out.position( out.position() + HEADER_HEADER_SIZE);
		buffer.putInt( MAGIC_WORD);
		buffer.putInt( 0);
		buffer.putInt( entries.size());
		return buffer;
	}

	/**
	 * Memory maps the portion of the destination file that will contain the index structure
	 * header and advances the file channels position.  The resulting buffer will be ready for
	 * writing of the entry indexes.
	 * <p/>
	 * This method must be invoked before mapping the data section, but after mapping the header.
	 */
	protected ByteBuffer mapIndex( final FileChannel out) throws IOException {
		final int size = entries.size() * ENTRY_SIZE;
		try {
			return out.map( READ_WRITE, out.position(), size);
		} finally {
			out.position( out.position() + size);
		}
	}

	/**
	 * Writes the data section of the file, starting at the current position which must be immediately
	 * after the header section.  Each entry writes its corresponding index into the provided index buffer
	 * and then writes its data to the file channel.
	 * <p/>
	 * This method must be invoked before mapping the data section, but after mapping the header.
	 * @return the total number of bytes written to the data section of the file.
	 */
	protected int writeData( final ByteBuffer index, final FileChannel out) throws IOException {
		int offset = 0;
		final long start = out.position();
		for ( int tag : entries.keySet()) {
			final Entry entry = entries.get( tag);
			try {
				final int size = entry.size();
				final ByteBuffer data = out.map( READ_WRITE, out.position(), size);
				entry.index( index, offset);
				if ( entry.ready()) entry.write( data);
				else pending.put( entry, data);
				out.position( out.position() + size);
				offset += size;
			} catch ( Throwable t) {
				throw new RuntimeException( "Error while writing '" + entry.getTag() + "'.", t);
			}
		}
		return ( int) ( out.position() - start);
	}

	public void writePending() {
		for ( Entry< Object> entry : pending.keySet()) {
			try {
				entry.write( pending.get( entry));
			} catch ( Throwable t) {
				throw new RuntimeException( "Error writing pending entry '" + entry.getTag() + "'.", t);
			}
		}
	}

	public Map< Entry, ByteBuffer> getPending() {
		return pending;
	}

	public void removeEntry( final Entry entry) {
		entries.remove( entry.getTag());
	}

	public Entry< ?> getEntry( final Tag tag) {
		return getEntry( tag.getCode());
	}

	public Entry< ?> getEntry( final int tag) {
		return entries.get( tag);
	}

	@SuppressWarnings( "unchecked")
	public Entry< String[]> createEntry( Tag tag, CharSequence value) {
		Entry< String[]> entry = createEntry( tag.getCode(), tag.getType(), 1);
		entry.setValues( new String[] { value.toString()});
		return entry;
	}

	@SuppressWarnings( "unchecked")
	public Entry< int[]> createEntry( Tag tag, int value) {
		Entry< int[]> entry = createEntry( tag.getCode(), tag.getType(), 1);
		entry.setValues( new int[] { value});
		return entry;
	}

	@SuppressWarnings( "unchecked")
	public < T> Entry< T> createEntry( Tag tag, T values) {
		Entry< T> entry = createEntry( tag.getCode(), tag.getType(), values.getClass().isArray() ? Array.getLength( values) : 1);
		entry.setValues( values);
		return entry;
	}

	@SuppressWarnings( "unchecked")
	public < T> Entry< T> createEntry( int tag, int type, T values) {
		Entry< T> entry = createEntry( tag, type, values.getClass().isArray() ? Array.getLength( values) : 1);
		entry.setValues( values);
		return entry;
	}

	/**
	 * Adds a pending entry to this header.  This entry will have the correctly sized buffer allocated, but
	 * will not be written until the caller writes a value and then invokes {@link writePending} on this
	 * object.
	 */
	@SuppressWarnings( "unchecked")
	public Entry< ?> addEntry( Tag tag, int count) {
		Entry< ?> entry = createEntry( tag.getCode(), tag.getType(), count);
		return entry;
	}

	public Entry readEntry( final int tag, final int type, final int offset, final int count, final ByteBuffer data) {
		final Entry entry = createEntry( tag, type, count);
		final ByteBuffer buffer = data.duplicate();
		buffer.position( offset);
		entry.read( buffer);
		return entry;
	}

	public Entry createEntry( final int tag, final int type, final int count) {
		final Entry entry = createEntry( type);
		entry.setTag( tag);
		entry.setCount( count);
		entries.put( tag, entry);
		return entry;
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
				return new Int16Entry();
			case 4:
				return new Int32Entry();
			case 5:
				return new Int64Entry();
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

	public interface Entry< T> {
		void setTag( int tag);
		void setSize( int size);
		void setCount( int count);
		void setValues( T values);
		T getValues();
		int getTag();
		int getType();
		int size();
		boolean ready();
		void read( ByteBuffer buffer);
		void write( ByteBuffer buffer);
		void index( ByteBuffer buffer, int position);
	}
	
	public abstract class AbstractEntry< T> implements Entry< T> {
		protected int size;
		protected int tag;
		protected int count;
		protected T values;

		public void setTag( Tag tag) { this.tag = tag.getCode(); }
		public void setTag( int tag) { this.tag = tag; }
		public void setSize( int size) { this.size = size; }
		public void setCount( int count) { this.count = count; }
		public void setValues( T values) { this.values = values; }

		public T getValues() { return values; }
		public int getTag() { return tag; }

		/**
		 * Returns true if this entry is ready to write, indicated by the presence of
		 * a set of values.
		 */
		public boolean ready() { return values != null; }

		/**
		 * Returns the data type of this entry.
		 */
		public abstract int getType();

		/**
		 * Returns the size this entry will need in the provided data buffer to write
		 * it's contents, corrected for any trailing zeros to fill to a boundary.
		 */
		public abstract int size();

		/**
		 * Reads this entries value from the provided buffer using the set count.
		 */
		public abstract void read( final ByteBuffer buffer);

		/**
		 * Writes this entries index to the index buffer and its values to the output
		 * channel provided.
		 */
		public abstract void write( final ByteBuffer data);

		/**
		 * Writes the index entry into the provided buffer at the current position.
		 */
		public void index( final ByteBuffer index, final int position) {
			index.putInt( tag).putInt( getType()).putInt( position).putInt( count);
		}

		public String toString() {
			return ( tags.containsKey( tag) ? tags.get( tag).getName() : super.toString()) + "[tag=" + tag + ",type=" + getType() + ",count=" + count + "]";
		}
	}

	class NullEntry extends AbstractEntry {
		public int getType() { return 0; }
		public int size() { return 0; }
		public void read( final ByteBuffer buffer) {}
		public void write( final ByteBuffer data) {}
	}

	class CharEntry extends AbstractEntry< byte[]> {
		public int getType() { return 1; }
		public int size() { return count * Byte.SIZE; }
		public void read( final ByteBuffer buffer) {
			byte[] values = new byte[ count];
			for ( int x = 0; x < count; x++) values[ x] = ( byte) buffer.get();
			setValues( values);
		}
		public void write( final ByteBuffer data) {
			for ( byte c : values) data.put(( byte) c);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			for ( byte c : values) builder.append( c);
			builder.append( "\n\t");
			return builder.toString();
		}
	}

	class Int8Entry extends AbstractEntry< byte[]> {
		public int getType() { return 2; }
		public int size() { return count * Byte.SIZE; }
		public void read( final ByteBuffer buffer) {
			byte[] values = new byte[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.get();
			setValues( values);
		}
		public void write( final ByteBuffer data) {
			for ( byte b : values) data.put( b);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( byte b : values) builder.append( b).append( ", ");
			return builder.toString();
		}
	}

	class Int16Entry extends AbstractEntry< short[]> {
		public int getType() { return 3; }
		public int size() { return Util.round( count * Short.SIZE, 1); }
		public void read( final ByteBuffer buffer) {
			short[] values = new short[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.getShort();
			setValues( values);
		}
		public void write( final ByteBuffer data) {
			for ( short s : values) data.putShort( s);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( short s : values) builder.append( s).append( ", ");
			return builder.toString();
		}
	}

	class Int32Entry extends AbstractEntry< int[]> {
		public int getType() { return 4; }
		public int size() { return Util.round( count * Integer.SIZE, 3); }
		public void read( final ByteBuffer buffer) {
			int[] values = new int[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.getInt();
			setValues( values);
		}
		public void write( final ByteBuffer data) {
			for ( int i : values) data.putInt( i);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			builder.append( "\n\t");
			for ( int i : values) builder.append( i).append( ", ");
			return builder.toString();
		}
	}

	class Int64Entry extends AbstractEntry< long[]> {
		public int getType() { return 5; }
		public int size() { return Util.round( count * Long.SIZE, 7); }
		public void read( final ByteBuffer buffer) {
			long[] values = new long[ count];
			for ( int x = 0; x < count; x++) values[ x] = buffer.getLong();
			setValues( values);
		}
		public void write( final ByteBuffer data) {
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
	class StringEntry extends AbstractEntry< String[]> {
		public int getType() { return 6; }
		public int size() {
			if ( size != 0) return size;
			
			for ( String string : values) size += string.length() + 1;
			return size;
		}
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
		public void write( final ByteBuffer data) {
			for ( String s : values) data.put( Charset.forName( "US-ASCII").encode( s)).put(( byte) 0);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			if ( values != null) {
				for ( String s : values) {
					builder.append( "\n\t");
					builder.append( s);
				}
			}
			return builder.toString();
		}
	}

	class BinEntry extends AbstractEntry< byte[]> {
		public int getType() { return 7; }
		public int size() { return count; }
		public void read( final ByteBuffer buffer) {
			byte[] values = new byte[ count];
			buffer.get( values);
			setValues( values);
		}
		public void write( final ByteBuffer data) {
			data.put( values);
		}
		public String toString() {
			StringBuilder builder = new StringBuilder( super.toString());
			if ( values != null) {
				builder.append( "\n");
				Util.dump( values, builder);
			}
			return builder.toString();
		}
	}

	class StringArrayEntry extends AbstractEntry< String[]> {
		public int getType() { return 8; }
		public int size() {
			if ( size != 0) return size;
			for ( String string : values) size += string.length() + 1;
			return size;
		}
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
		public void write( final ByteBuffer data) {
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

	class I18NStringEntry extends AbstractEntry< String[]> {
		public int getType() { return 9; }
		public int size() {
			if ( size != 0) return size;
			for ( String string : values) size += string.length() + 1;
			return size;
		}
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
		public void write( final ByteBuffer data) {
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
		builder.append( "Start Header ( ").append( getClass()).append( ")").append( "\n");
		for ( int tag : entries.keySet()) builder.append( entries.get( tag)).append( "\n");
		return builder.toString();
	}
}
