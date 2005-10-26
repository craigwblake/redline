package org.freecompany.redline;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;

public class EncryptionChannel implements ByteChannel {

	protected void transferred( int input, int output) {}
	protected void sign( byte[] signature) {}
	protected void digest( byte[] digest) {}
	
	protected WritableByteChannel writable;
	protected ReadableByteChannel readable;
	protected int input;
	protected int output;
	protected MessageDigest digest;
	protected Signature signature;

	public EncryptionChannel( final ByteChannel channel) {
		this.readable = channel;
		this.writable = channel;
	}

	public EncryptionChannel( final ByteChannel channel, String algorithm) throws NoSuchAlgorithmException {
		this( channel);
		setDigest( algorithm);
	}

	public EncryptionChannel( final ByteChannel channel, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException {
		this( channel);
		setPrivateKey( key);
	}

	public EncryptionChannel( final ReadableByteChannel channel) {
		this.readable = channel;
	}

	public EncryptionChannel( final ReadableByteChannel channel, String algorithm) throws NoSuchAlgorithmException {
		this( channel);
		setDigest( algorithm);
	}

	public EncryptionChannel( final WritableByteChannel channel) {
		this.writable = channel;
	}

	public EncryptionChannel( final WritableByteChannel channel, String algorithm) throws NoSuchAlgorithmException {
		this( channel);
		setDigest( algorithm);
	}

	public EncryptionChannel( final WritableByteChannel channel, PrivateKey key)  throws NoSuchAlgorithmException, InvalidKeyException {
		this( channel);
		setPrivateKey( key);
	}

	public void setDigest( final String algorithm)  throws NoSuchAlgorithmException {
		digest = MessageDigest.getInstance( algorithm);
	}

	public void setPrivateKey( final PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException {
		signature = Signature.getInstance( key.getAlgorithm());
		signature.initSign( key);
	}

	public int read( final ByteBuffer buffer) throws IOException {
		final int read = readable.read( buffer);
		input += read;
		try {
			if ( digest != null) digest.update(( ByteBuffer) buffer.duplicate().flip());
			if ( signature != null) signature.update(( ByteBuffer) buffer.duplicate().flip());
		} catch ( SignatureException e) {
			throw new RuntimeException( e);
		}
		return read;
	}

	public int write( final ByteBuffer buffer) throws IOException {
		try {
			if ( digest != null) digest.update( buffer.duplicate());
			if ( signature != null) signature.update( buffer.duplicate());
		} catch ( SignatureException e) {
			throw new RuntimeException( e);
		}
		final int wrote = writable.write( buffer);
		output += wrote;
		return wrote;
	}

	public void close() throws IOException {
		try {
			transferred( input, output);
			if ( digest != null) digest( digest.digest());
			if ( signature != null) sign( signature.sign());
		} catch ( SignatureException e) {
			throw new RuntimeException( e);
		}
		if ( readable != null) readable.close();
		if ( writable != null) writable.close();
	}

	public boolean isOpen() {
		return ( readable != null && readable.isOpen()) || ( writable != null && writable.isOpen());
	}
}
