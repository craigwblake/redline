package org.freecompany.redline.header;

import org.freecompany.redline.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

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
	protected final Map< Entry, Integer> pending = new LinkedHashMap< Entry, Integer>();

	/**
	 * Reads the entire header contents for this channel and returns the number of entries
	 * found.
	 */
	public int read( ReadableByteChannel in) throws IOException {
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

		int count = 0;
		while ( index.remaining() >= ENTRY_SIZE) {
			readEntry( index.getInt(), index.getInt(), index.getInt(), index.getInt(), data);
			count++;
		}
		return count;
	}

	/**
	 * Writes this header section to the provided file at the current position.
	 */
	public void write( WritableByteChannel out) throws IOException {
		final ByteBuffer header = getHeader();
		final ByteBuffer index = getIndex();
		final ByteBuffer data = getData( index);

		data.flip();
		int pad = Util.round( data.remaining(), 7) - data.remaining();
		header.putInt( data.remaining() +  pad);
		Util.empty( out, ( ByteBuffer) header.flip());
		Util.empty( out, ( ByteBuffer) index.flip());
		Util.empty( out, data);
		Util.empty( out, ByteBuffer.allocate( pad));
	}

	public int count() {
		return entries.size();
	}

	/**
	 * Memory maps the portion of the destination file that will contain the header structure
	 * header and advances the file channels position.  The resulting buffer will be prefilled with
	 * the necesssary magic data and the correct index count, but will require an integer value to
	 * be written with the total data section size once data writing is complete.
	 * <p/>
	 * This method must be invoked before mapping the index or data sections.
	 */
	protected ByteBuffer getHeader() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate( HEADER_HEADER_SIZE);
		buffer.putInt( MAGIC_WORD);
		buffer.putInt( 0);
		buffer.putInt( count());
		return buffer;
	}

	/**
	 * Memory maps the portion of the destination file that will contain the index structure
	 * header and advances the file channels position.  The resulting buffer will be ready for
	 * writing of the entry indexes.
	 * <p/>
	 * This method must be invoked before mapping the data section, but after mapping the header.
	 */
	protected ByteBuffer getIndex() throws IOException {
		return ByteBuffer.allocate( count() * ENTRY_SIZE);
	}

	/**
	 * Writes the data section of the file, starting at the current position which must be immediately
	 * after the header section.  Each entry writes its corresponding index into the provided index buffer
	 * and then writes its data to the file channel.
	 * <p/>
	 * @return the total number of bytes written to the data section of the file.
	 */
	protected ByteBuffer getData( final ByteBuffer index) throws IOException {
		int offset = 0;
		final List< ByteBuffer> buffers = new LinkedList< ByteBuffer>();
		final Iterator< Integer> i = entries.keySet().iterator();
		
		index.position( 16);
		final Entry first = entries.get( i.next());
		Entry entry = null;
		try {
			while ( i.hasNext()) {
				entry = entries.get( i.next());
				offset = writeData( buffers, index, entry, offset);
			}
			index.position( 0);
			offset = writeData( buffers, index, first, offset);
			index.position( index.limit());
		} catch ( Throwable t) {
			throw new RuntimeException( "Error while writing '" + entry + "'.", t);
		}
		ByteBuffer data = ByteBuffer.allocate( offset);
		for ( ByteBuffer buffer : buffers) data.put( buffer);
		return data;
	}

	protected int writeData( final Collection< ByteBuffer> buffers, final ByteBuffer index, final Entry entry, int offset) {
		final int shift = entry.getOffset( offset) - offset;
		if ( shift > 0) buffers.add( ByteBuffer.allocate( shift));
		offset += shift;
		
		final int size = entry.size();
		final ByteBuffer buffer = ByteBuffer.allocate( size);
		entry.index( index, offset);
		if ( entry.ready()) {
			entry.write( buffer);
			buffer.flip();
		}
		else pending.put( entry, offset);
		buffers.add( buffer);
		return offset + size;
	}

	public void writePending( final FileChannel channel) {
		for ( Entry< Object> entry : pending.keySet()) {
			try {
				ByteBuffer data = ByteBuffer.allocate( entry.size());
				entry.write( data);
				channel.position( Lead.LEAD_SIZE + HEADER_HEADER_SIZE + count() * ENTRY_SIZE + pending.get( entry));
				Util.empty( channel, ( ByteBuffer) data.flip());
			} catch ( Throwable t) {
				throw new RuntimeException( "Error writing pending entry '" + entry.getTag() + "'.", t);
			}
		}
	}

	public Map< Entry, Integer> getPending() {
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
	public < T> Entry< T> createEntry( Tag tag, int type, T values) {
		Entry< T> entry = createEntry( tag.getCode(), type, values.getClass().isArray() ? Array.getLength( values) : 1);
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
		return createEntry( tag.getCode(), tag.getType(), count);
	}

	public Entry readEntry( final int tag, final int type, final int offset, final int count, final ByteBuffer data) {
		final Entry entry = createEntry( tag, type, count);
		final ByteBuffer buffer = data.duplicate();
		buffer.position( offset);
		entry.read( buffer);
		entry.setOffset( offset);
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
		void setOffset( int offset);
		void setValues( T values);
		T getValues();
		int getTag();
		int getType();
		int getOffset( int offset);
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
		protected int offset;
		protected T values;

		public void setTag( Tag tag) { this.tag = tag.getCode(); }
		public void setTag( int tag) { this.tag = tag; }
		public void setSize( int size) { this.size = size; }
		public void setCount( int count) { this.count = count; }
		public void setOffset( int offset) { this.offset = offset; }
		public void setValues( T values) { this.values = values; }

		public T getValues() { return values; }
		public int getTag() { return tag; }

		public int getOffset( int offset) { return offset; }//Util.round( offset, 1); }

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
			StringBuilder builder = new StringBuilder();
			if ( tags.containsKey( tag)) builder.append( tags.get( tag).getName());
			else builder.append( super.toString());
			builder.append( "[tag=").append( tag);
			builder.append( ",type=").append( getType());
			builder.append( ",count=").append( count);
			builder.append( ",size=").append( size());
			builder.append( ",offset=").append( offset);
			builder.append( "]");
			return builder.toString();
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
		public int size() { return count * ( Byte.SIZE / 8); }
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
		public int size() { return count * ( Byte.SIZE / 8); }
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
		public int getOffset( int offset) { return Util.round( offset, 1); }
		public int getType() { return 3; }
		public int size() { return count * ( Short.SIZE / 8); }
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
		public int getOffset( int offset) { return Util.round( offset, 3); }
		public int getType() { return 4; }
		public int size() { return count * ( Integer.SIZE / 8); }
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
		public int getOffset( int offset) { return Util.round( offset, 7); }
		public int getType() { return 5; }
		public int size() { return count * ( Long.SIZE / 8); }
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

	class StringArrayEntry extends StringEntry {
		public int getType() { return 8; }
	}

	class I18NStringEntry extends StringEntry {
		public int getType() { return 9; }
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Start Header ( ").append( getClass()).append( ")").append( "\n");
		for ( int tag : entries.keySet()) builder.append( entries.get( tag)).append( "\n");
		return builder.toString();
	}
}
