package org.freecompany.redline.payload;

import org.freecompany.redline.*;
import java.nio.*;
import java.nio.channels.*;

public class Payload {

	protected static final int CPIO_HEADER = 96;

	public enum Permission { ON, OFF };

	protected CharSequence name;
	protected PermissionSet user = new PermissionSet( Permission.ON, Permission.OFF, Permission.OFF);
	protected PermissionSet group = new PermissionSet( Permission.ON, Permission.OFF, Permission.OFF);
	protected PermissionSet world = new PermissionSet( Permission.ON, Permission.OFF, Permission.OFF);
	protected ByteChannel channel;

	public void read( ReadableByteChannel channel) {
		ByteBuffer descriptor = Util.fill( channel, CPIO_HEADER);
		
	}

	public void write( WritableByteChannel channel) {
		ByteBuffer descriptor = ByteBuffer.allocate( CPIO_HEADER);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( "File: ").append( name).append( "\n");
		return builder.toString();
	}

	public class PermissionSet {

		private Permission read;
		private Permission write;
		private Permission execute;

		public PermissionSet( Permission read, Permission write, Permission execute) {
			this.read = read;
			this.write = write;
			this.execute = execute;
		}
	}
}
