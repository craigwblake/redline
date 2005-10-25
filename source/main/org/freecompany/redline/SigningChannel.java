package org.freecompany.redline;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.security.*;

public class SigningChannel implements WritableByteChannel {

	protected final Signature signature;
	protected final WritableByteChannel channel;

	public SigningChannel( final WritableByteChannel channel, final PrivateKey key, final String protocol) throws NoSuchAlgorithmException, InvalidKeyException {
		this.channel = channel;
		signature = Signature.getInstance( protocol);
		signature.initSign( key);
	}

	public int write( final ByteBuffer buffer) throws IOException {
		try {
			signature.update( buffer.duplicate());
		} catch ( SignatureException e) {
			throw new RuntimeException( e);
		}
		return channel.write( buffer);
	}

	public void close() throws IOException {
		channel.close();
	}

	public boolean isOpen() {
		return channel.isOpen();
	}

	public byte[] sign() throws SignatureException {
		return signature.sign();
	}
}
