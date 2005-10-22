package org.freecompany.redline.header;

import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;

public class Format {

	protected Signature dsa = Signature.getInstance( "DSA");
	protected Signature sha = Signature.getInstance( "SHA1");
	protected Lead lead = new Lead();
	protected Signature signature = new Signature();
	protected Header header = new Header();

	public Lead getLead() {
		return lead;
	}

	public Signature getSignature() {
		return signature;
	}

	public Header getHeader() {
		return header;
	}

	public void read( final ReadableByteChannel channel) throws IOException {
		lead.read( channel);
		signature.read( channel);
		header.read( channel);
	}

	public void write( final WritableByteChannel channel) throws IOException {
		lead.write( channel);
		signature.write( channel);
		header.write( channel);
	}

	public WritableByteChannel wrapForsignatures( final WritableByteChannel channel) throws IOException {
		return new WritableByteChannel() {
			public int write( final ByteBuffer buffer) throws IOException {
				dsa.update( buffer.duplicate());
				sha.update( buffer.duplicate());
			}
		};
	}

	public String toString() {
		return lead.toString() + header + signature;
	}
}
