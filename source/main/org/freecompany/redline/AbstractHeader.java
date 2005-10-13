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
		Util.check( header.getInt(), MAGIC_WORD);
		header.getInt();
		index = Util.fill( in, header.getInt() * ENTRY_SIZE);
		data = Util.fill( in, header.getInt());

		Entry entry;
		while (( entry = nextEntry()) != null) entries.add( entry);
	}

	public void write( WritableByteChannel out) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate( HEADER_HEADER_SIZE);
		buffer.putInt( MAGIC_WORD);
		buffer.putInt( 0);
		//for ( Entry entry : entries) entry.writeIndex( out);
		//for ( Entry entry : entries) entry.writeData( out);
	}

	public Iterable< Entry> entries() {
		return entries;
	}

	protected Entry nextEntry() throws IOException {
		if ( index.remaining() < 16) return null;

		int tag = index.getInt();
		int type = index.getInt();
		int offset = index.getInt();
		int count = index.getInt();
		switch ( type) {
			case 0:
				return new NullEntry( tag, offset, count);
			case 1:
				return new CharEntry( tag, offset, count);
			case 2:
				return new Int8Entry( tag, offset, count);
			case 3:
				return new Int8Entry( tag, offset, count);
			case 4:
				return new Int8Entry( tag, offset, count);
			case 5:
				return new Int8Entry( tag, offset, count);
			case 6:
				return new StringEntry( tag, offset, count);
			case 7:
				return new BinEntry( tag, offset, count);
			case 8:
				return new StringArrayEntry( tag, offset, count);
			case 9:
				return new I18NStringEntry( tag, offset, count);
		}
		throw new IOException( "unknown entry type");
	}

	public abstract class Entry {
		private transient Tag tag;
		protected int code;
		protected int offset;
		protected int count;

		Entry( int code, int offset, int count) {
			this.code = code;
			tag = TAGS.get( code);
			this.offset = offset;
			this.count = count;
		}

		public String toString() {
			return ( tag != null ? tag.getName() : super.toString()) + "[tag=" + code + ",offset=" + offset + ",count=" + count + "]";
		}

		protected ByteBuffer data() {
			ByteBuffer buf = data.duplicate();
			buf.position( offset);
			return buf;
		}
	}

	class NullEntry extends Entry {
		public NullEntry( int code, int offset, int count) { super( code, offset, count); }
	}

	class CharEntry extends Entry {
		public CharEntry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			b.append( "\n\t");
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append(( char) buf.get());
			}
			return b.toString();
		}
	}

	class Int8Entry extends Entry {
		public Int8Entry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			b.append( "\n\t");
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( buf.get());
				b.append( ", ");
			}
			return b.toString();
		}
	}

	class Int16Entry extends Entry {
		public Int16Entry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			b.append( "\n\t");
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( buf.getShort());
				b.append( ", ");
			}
			return b.toString();
		}
	}

	class Int32Entry extends Entry {
		public Int32Entry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			b.append( "\n\t");
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( buf.getInt());
				b.append( ", ");
			}
			return b.toString();
		}
	}

	class Int64Entry extends Entry {
		public Int64Entry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			b.append( "\n\t");
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( buf.getLong());
				b.append( ", ");
			}
			return b.toString();
		}
	}

	class StringEntry extends Entry {
		public StringEntry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( "\n\t");
				while ( true) {
					byte bb = buf.get();
					if ( bb == 0) break;
					b.append(( char) bb);
				}
			}
			return b.toString();
		}
	}

	class BinEntry extends Entry {
		public BinEntry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			b.append( "\n");
			ByteBuffer buf = data();
			buf.limit( count);
			Util.dump( buf, b);
			return b.toString();
		}
	}

	class StringArrayEntry extends Entry {
		public StringArrayEntry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( "\n\t");
				while ( true) {
					byte bb = buf.get();
					if ( bb == 0) break;
					b.append(( char) bb);
				}
			}
			return b.toString();
		}
	}

	class I18NStringEntry extends Entry {
		public I18NStringEntry( int code, int offset, int count) { super( code, offset, count); }
		public String toString() {
			StringBuilder b = new StringBuilder( super.toString());
			ByteBuffer buf = data();
			for ( int i = 0; i < count; i++) {
				b.append( "\n\t");
				while ( true) {
					byte bb = buf.get();
					if ( bb == 0) break;
					b.append(( char) bb);
				}
			}
			return b.toString();
		}
	}
}
