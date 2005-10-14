package org.freecompany.redline;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class Lead {

	private static final int LEAD_SIZE = 96;
	private static final int MAGIC = 0xEDABEEDB;

	protected byte major;
	protected byte minor;
	protected short type;
	protected short arch;
	protected String name;
	protected short os;
	protected short sigtype;

	public void read( ReadableByteChannel channel) throws IOException {
		ByteBuffer lead = Util.fill( channel, LEAD_SIZE);

		Util.check( MAGIC, lead.getInt());

		major = lead.get();
		minor = lead.get();
		type = lead.getShort();
		arch = lead.getShort();

		ByteBuffer data = ByteBuffer.allocate( 66);
		lead.get( data.array());
		StringBuilder builder = new StringBuilder();
		byte b;
		while (( b = data.get()) != 0) builder.append(( char) b);
		name = builder.toString();

		os = lead.getShort();
		sigtype = lead.getShort();
		if ( lead.remaining() != 16) throw new IllegalStateException( "Expected 16 remaining, found '" + lead.remaining() + "'.");
	}

	public void write( WritableByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate( LEAD_SIZE);
		buffer.putInt( MAGIC);
		buffer.put( major);
		buffer.put( minor);
		buffer.putShort( type);
		buffer.putShort( arch);

		byte[] data = new byte[ 66];
		System.arraycopy( name.getBytes(), 0, data, 0, name.length());
		buffer.put( data);

		buffer.putShort( os);
		buffer.putShort( sigtype);
		buffer.flip();
		Util.empty( channel, buffer);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Version: ").append( major).append( ".").append( minor).append( "\n");
		builder.append( "Type: ").append( type).append( "\n");
		builder.append( "Arch: ").append( arch).append( "\n");
		builder.append( "Name: ").append( name).append( "\n");
		builder.append( "OS: ").append( os).append( "\n");
		builder.append( "Sig type: ").append( sigtype).append( "\n");
		return builder.toString();
	}
}
