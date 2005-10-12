package org.freecompany.redline;

import java.io.*;
import java.util.*;

public class Rpm {

	protected Lead lead = new Lead();
	protected Header signature = new Header();
	protected Header header = new Header();
	protected Payload payload = new Payload();

	public void setMajor( int major) {
		lead.setMajor( major);
	}

	public int getMajor() {
		return lead.getMajor();
	}

	public void setMinor( int minor) {
		lead.setMinor( minor);
	}

	public int getminor() {
		return lead.getMinor();
	}

	public void addSignature( Tag tag, Object object) {
		signature.put( tag, object);
	}

	public Map< Tag, Object> getSignatures() {
		return signature.getEntries();
	}

	public void addHeader( Tag tag, Object object) {
		header.put( tag, object);
	}

	public Map< Tag, Object> getHeaders() {
		return header.getEntries();
	}

	public void addPayload( File file) {
		payload.add( file);
	}
	
	public List< File> getPayloads() {
		return payload.getFiles();
	}
}
