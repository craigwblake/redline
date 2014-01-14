package org.freecompany.redline.header;

import org.freecompany.redline.Util;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;
import static org.freecompany.redline.header.RpmType.BINARY;

public class Lead {

	public static final int LEAD_SIZE = 96;
	private static final int MAGIC = 0xEDABEEDB;

	protected byte major = 3;
	protected byte minor;
	protected RpmType type = BINARY;
	protected Architecture arch = NOARCH;
	protected String name;
	protected Os os = LINUX;
	protected short sigtype = 5;

	public CharSequence getName() { return name; }
	public Architecture getArch() { return arch; }

	public void setMajor( byte major) {
		this.major = major;
	}

	public void setMinor( byte minor) {
		this.minor = minor;
	}

	public void setType( RpmType type) {
		this.type = type;
	}

	public void setArch( Architecture arch) {
		this.arch = arch;
	}

	public void setName( String name) {
		this.name = name;
	}

	public void setOs( Os os) {
		this.os = os;
	}

	public void setSigtype( short sigtype) {
		this.sigtype = sigtype;
	}

	public void read( ReadableByteChannel channel) throws IOException {
		ByteBuffer lead = Util.fill( channel, LEAD_SIZE);

		Util.check( MAGIC, lead.getInt());

		major = lead.get();
		minor = lead.get();
		type = RpmType.values()[ lead.getShort()];

		final short tmp = lead.getShort();
		if ( tmp < Architecture.values().length) arch = Architecture.values()[ tmp];

		ByteBuffer data = ByteBuffer.allocate( 66);
		lead.get( data.array());
		StringBuilder builder = new StringBuilder();
		byte b;
        while ((data.hasRemaining() && (b = data.get()) != 0)) builder.append(( char) b);
		name = builder.toString();

		// Unknown rpm tag defaults to 0xFF (see rpmtag.h)
		short o = lead.getShort();
		if ( o != 0xFF)
		    os = Os.values()[ o];
		else
		    os = Os.UNKNOWN;		
		sigtype = lead.getShort();
		if ( lead.remaining() != 16) throw new IllegalStateException( "Expected 16 remaining, found '" + lead.remaining() + "'.");
	}

	public void write( WritableByteChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate( LEAD_SIZE);
		buffer.putInt( MAGIC);
		buffer.put( major);
		buffer.put( minor);
		buffer.putShort(( short) type.ordinal());
		buffer.putShort(( short) arch.ordinal());

		byte[] data = new byte[65];
		byte[] encoded = name.getBytes( "UTF-8");
		System.arraycopy( encoded, 0, data, 0, encoded.length);
		buffer.put( data);

		// Unknown rpm tag defaults to 0xFF (see rpmtag.h)		
		buffer.putShort(( short) (os.ordinal() != 0 ? os.ordinal() : 0xFF));
		buffer.putShort( sigtype);
		buffer.position( buffer.position() + 16);
		buffer.flip();
		if ( buffer.remaining() != LEAD_SIZE) throw new IllegalStateException( "Invalid lead size generated with '" + buffer.remaining() + "' bytes.");
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
