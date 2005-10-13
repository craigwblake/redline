package org.freecompany.redline;

import java.io.*;
import java.util.*;

public class Rpm {

	protected Lead lead = new Lead();
	protected Signature signature = new Signature();
	protected Header header = new Header();
	protected Payload payload = new Payload();

	public Lead getLead() {
		return lead;
	}

	public Signature getSignature() {
		return signature;
	}

	public Header getHeader() {
		return header;
	}

	public Payload getPayload() {
		return payload;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append( lead);
		builder.append( signature);
		builder.append( header);
		return builder.toString();
	}
}
