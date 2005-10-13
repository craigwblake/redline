package org.freecompany.redline;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * A 96 byte RPM file lead section, which specifies information such as the name
 * of the package.  This section is obsolete and now used primarily to identify
 * the file type.
 */
public class Lead {

	public enum Type {
		BINARY( 0, "Binary"), SOURCE( 1, "Source");

		private static Map< Integer, Type> values = new ConcurrentHashMap< Integer, Type>();
		static {
			for ( Type type : Type.values()) values.put( type.getCode(), type);
		}
		
		private int code;
		private String name;
		
		private Type( int code, String name) {
			this.code = code;
			this.name = name;
		}
		
		public int getCode() { return code; }
		public static Type getType( int code) { return values.get( code); }
		public String toString() { return name; }
	}

	public enum Architecture {
		I386( 1, "i386");

		private static Map< Integer, Architecture> values = new ConcurrentHashMap< Integer, Architecture>();
		static {
			for ( Architecture architecture : Architecture.values()) values.put( architecture.getCode(), architecture);
		}

		private int code;
		private String name;

		private Architecture( int code, String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() { return code; }
		public static Architecture getArchitecture( int code) { return values.get( code); }
		public String toString() { return name; }
	}

	public enum Os {
		LINUX( 1, "Linux");

		private static Map< Integer, Os> values = new ConcurrentHashMap< Integer, Os>();
		static {
			for ( Os os : Os.values()) values.put( os.getCode(), os);
		}

		private int code;
		private String name;

		private Os( int code, String name) {
			this.code = code;
			this.name = name;
		}

		public int getCode() { return code; }
		public static Os getOs( int code) { return values.get( code); }
		public String toString() { return name; }
	}

	protected int major;
	protected int minor;
	protected Type type;
	protected Architecture architecture;
	protected Os os;
	protected CharSequence name;

	public void setMajor( int major) {
		this.major = major;
	}

	public int getMajor() {
		return major;
	}

	public void setMinor( int minor) {
		this.minor = minor;
	}

	public int getMinor() {
		return minor;
	}

	public void setType( Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setArchitecture( Architecture architecture) {
		this.architecture = architecture;
	}

	public Architecture getArchitecture() {
		return architecture;
	}

	public void setOs( Os os) {
		this.os = os;
	}

	public Os getOs() {
		return os;
	}

	public void setName( CharSequence name) {
		this.name = name;
	}

	public CharSequence getName() {
		return name;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "Version: ").append( major).append( ".").append( minor).append( "\n");
		builder.append( "Type: ").append( type).append( "\n");
		builder.append( "Architecture: ").append( architecture).append( "\n");
		builder.append( "Operating System: ").append( os).append( "\n");
		builder.append( "Name: ").append( name).append( "\n");
		return builder.toString();
	}

	public void read( final ReadableByteChannel channel) throws IOException {
		ByteBuffer lead = ByteBuffer.allocate( 96);
		channel.read( lead);
		lead.flip();

		if ( lead.getInt() != 0xEDABEEDB) throw new IOException( "Malformed file, magic number is incorrect.");
		setMajor( lead.get());
		setMinor( lead.get());
		setType( Lead.Type.getType( lead.getShort()));
		setArchitecture( Architecture.getArchitecture( lead.getShort()));

		byte[] name = new byte[ 66];
		lead.get( name);
		setName( new String( name));
		setOs( Lead.Os.getOs( lead.getShort()));
		if ( lead.getShort() != 5) throw new IOException( "Unspported signature version.");
	}
}
