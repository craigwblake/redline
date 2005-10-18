package org.freecompany.redline.header;

import org.freecompany.redline.payload.*;
import java.io.*;
import java.nio.channels.*;
import java.util.*;

public class Format {

	protected Lead lead = new Lead();
	protected Header header = new Header();
	protected Signature signature = new Signature();
	protected Map< Payload, ByteChannel> payloads = new ConcurrentHashMap< Payload, ByteChannel>();

	public Lead getLead() {
		return lead;
	}

	public Header getHeader() {
		return header;
	}

	public Signature getSignature() {
		return signature;
	}

	public Map< Payload, ByteChannel> getPayload() {
		return Collections.unmodifiableMap( payload);
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

	public String toString() {
		return lead.toString() + header + signature;
	}
}
