package org.freecompany.redline;

import java.io.*;
import java.nio.channels.*;

public class Rpm {

	protected Lead lead = new Lead();
	protected Header header = new Header();
	protected Signature signature = new Signature();

	public Lead getLead() {
		return lead;
	}

	public Header getHeader() {
		return header;
	}

	public Signature getSignature() {
		return signature;
	}

	public void read( final ReadableByteChannel channel) throws IOException {
		lead.read( channel);
		header.read( channel);
		signature.read( channel);
	}

	public void write( final WritableByteChannel channel) throws IOException {
		lead.write( channel);
		header.write( channel);
		signature.write( channel);
	}

	public String toString() {
		return lead.toString() + header + signature;
	}
}
