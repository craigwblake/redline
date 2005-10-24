package org.freecompany.redline.header;

import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;

public class Format {

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

	public void write( final FileChannel channel) throws IOException {
		lead.write( channel);
		signature.write( channel);
		header.write( channel);
	}

	public String toString() {
		return lead.toString() + signature + header;
	}
}
