package org.freecompany.redline;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import java.security.*;

public abstract class ChannelWrapper {

	public static class Key< T> {}

	protected interface Consumer< T> {
		void consume( ByteBuffer buffer);
		T finish() throws Exception;
	}

	protected Map< Key< ?>, Consumer< ?>> consumers = new HashMap< Key< ?>, Consumer< ?>>();

	public Key< Integer> start( final WritableByteChannel output) {
		final Key< Integer> object = new Key< Integer>();
		consumers.put( object, new Consumer< Integer>() {
			int count;
			public void consume( final ByteBuffer buffer) {
				try {
					count += output.write( buffer);
				} catch ( IOException e) {
					throw new RuntimeException( e);
				}
			}
			public Integer finish() { return count; }
		});
		return object;
	}

	/**
	 * Initializes a byte counter on this channel.
	 */
	public Key< Integer> start() throws Exception {
		final Key< Integer> object = new Key< Integer>();
		consumers.put( object, new Consumer< Integer>() {
			int count;
			public void consume( final ByteBuffer buffer) { count += buffer.remaining(); }
			public Integer finish() throws Exception { return count; }
		});
		return object;
	}

	/**
	 * Initialize a signature on this channel.
	 */
	public Key< byte[]> start( final PrivateKey key) throws Exception {
		final Signature signature = Signature.getInstance( key.getAlgorithm());
		signature.initSign( key);
		final Key< byte[]> object = new Key< byte[]>();
		consumers.put( object, new Consumer< byte[]>() {
			public void consume( final ByteBuffer buffer) {
				try {
					signature.update( buffer);
				} catch ( Exception e) {
					throw new RuntimeException( e);
				}
			}
			public byte[] finish() throws Exception {
				return signature.sign();
			}
		});
		return object;
	}

	/**
	 * Initialize a digest on this channel.
	 */
	public Key< byte[]> start( final String algorithm) throws Exception {
		final MessageDigest digest = MessageDigest.getInstance( algorithm);
		final Key< byte[]> object = new Key< byte[]>();
		consumers.put( object, new Consumer() {
			public void consume( final ByteBuffer buffer) {
				try {
					digest.update( buffer);
				} catch ( Exception e) {
					throw new RuntimeException( e);
				}
			}
			public byte[] finish() throws Exception {
				return digest.digest();
			}
		});
		return object;
	}

	public < T> T finish( final Key< T> object) throws Exception {
		return ( T) consumers.remove( object).finish();
	}

	public void close() throws IOException {
		if ( !consumers.isEmpty()) throw new IOException( "There are '" + consumers.size() + "' unfinished operations.");
	}
}
