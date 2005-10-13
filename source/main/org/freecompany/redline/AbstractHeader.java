package org.freecompany.redline;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public abstract class AbstractHeader< T> {

	/**
	 * This map iterates in insertion order which is critical in teh parsing of the RPM
	 * headers.  Make sure is stays this way.
	 */
	protected Map< Index< T>, Object> entries = new LinkedHashMap< Index< T>, Object>();

	protected abstract T getTag( int code);

	public void put( Index< T> index, Object object) {
		entries.put( index, object);
	}

	public Map< Index< T>, Object> getEntries() {
		return entries;
	}

	public void read( final ReadableByteChannel channel) throws IOException {
		ByteBuffer header = ByteBuffer.allocate( 16);
		channel.read( header);
		header.flip();

		if ( header.getInt() != 0x8EADE801) throw new IOException( "Malformed index, magic number is incorrect.");
		header.getInt();
	   	ByteBuffer indexes = ByteBuffer.allocate( 16 * header.getInt());
		ByteBuffer data = ByteBuffer.allocate( header.getInt());
		
		channel.read( indexes);
		indexes.flip();
		readIndex( indexes);

		channel.read( data);
		data.flip();
		System.out.println( "Creating data buffer size '" + data.remaining() + "'.");
		for ( Index< T> index : entries.keySet()) readData( data, index);
	}

	protected void readIndex( final ByteBuffer buffer) throws IOException {
		while ( buffer.hasRemaining()) {
			Index< T> index = new Index< T>();
			index.setTag( getTag( buffer.getInt()));
			index.setType( Type.getType( buffer.getInt()));
			index.setOffset( buffer.getInt());
			index.setCount( buffer.getInt());
			System.out.println( "Index at '" + index.getOffset() + "' has '" + index.getCount() + "' entries of '" + index.getType() + "'.");
			entries.put( index, null);
		}
	}

	protected void readData( final ByteBuffer buffer, final Index< T> index) throws IOException {
		System.out.println( "Reading index '" + index.getTag() + "' with '" + index.getCount() + "' entries of type '" + index.getType() + "' at offset '" + index.getOffset() + "'.");
		switch ( index.getType()) {
			case NULL:
				break;
			case CHAR:
			case INT8:
				if ( index.getCount() == 1) put( index, buffer.get( index.getOffset()));
				else {
					byte[] values = new byte[ index.getCount()];
					for ( int x = 0; x < index.getCount(); x++) values[ x] = buffer.get( index.getOffset() + x);
					put( index, values);
				}
				break;
			case INT16:
				if ( index.getCount() == 1) put( index, buffer.getChar( index.getOffset()));
				else {
					char[] values = new char[ index.getCount()];
					for ( int x = 0; x < index.getCount(); x++) values[ x] = buffer.getChar( index.getOffset() + x * 2);
					put( index, values);
				}
				break;
			case INT32:
				if ( index.getCount() == 1) put( index, buffer.getInt( index.getOffset()));
				else {
					int[] values = new int[ index.getCount()];
					for ( int x = 0; x < index.getCount(); x++) values[ x] = buffer.getInt( index.getOffset() + x * 4);
					put( index, values);
				}
				break;
			case INT64:
				if ( index.getCount() == 1) put( index, buffer.getLong( index.getOffset()));
				else {
					long[] values = new long[ index.getCount()];
					for ( int x = 0; x < index.getCount(); x++) values[ x] = buffer.getLong( index.getOffset() + x * 8);
					put( index, values);
				}
				break;
			case STRING:
				put( index, readString( index, buffer));
				System.out.println( "Read '" + entries.get( index) + "'.");
				break;
			case BINARY:
				byte[] values = new byte[ index.getCount()];
				buffer.position( index.getOffset());
				buffer.get( values);
				put( index, values);
				break;
			case STRING_ARRAY:
				String[] strings = new String[ index.getCount()];
				for ( int x = 0; x < index.getCount(); x++) {
					strings[ x] = readString( index, buffer);
				}
				put( index, strings);
				break;
			case I18NSTRING:
				break;
		}
	}

	protected String readString( final Index index, final ByteBuffer buffer) {
		StringBuilder builder = new StringBuilder();
		buffer.position( index.getOffset());
		byte character;
		while (( character = buffer.get()) != 0) builder.append(( char) character);
		return builder.toString();
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Index count: ").append( entries.size()).append( "\n");
		for ( Index< T> index : entries.keySet()) {
			builder.append( "Tag: ").append( index.getTag());
			builder.append( ", type: ").append( index.getType());
			builder.append( ",  data: ").append( entries.get( index)).append( "\n");
		}
		return builder.toString();
	}
}
