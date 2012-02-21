package org.freecompany.redline;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;

/**
 * Wraps an IO channel so that bytes may be observed
 * during transmission. Wrappers around IO channels are
 * used for a variety of purposes, including counting
 * byte output for use in generating headers, calculating
 * a signature across output bytes, and digesting output
 * bytes using a one-way secure hash.
 */
public abstract class ChannelWrapper {

	public static class Key< T> {}

	/**
	 * Interface describing an object that consumes
	 * data from a NIO buffer.
	 */
	protected interface Consumer< T> {

		/**
		 * Consume some input from the given buffer.
		 */
		void consume( ByteBuffer buffer);

		/**
		 * Complete operationds and optionally return
		 * a value to the holder of the key.
		 */
		T finish();
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
	public Key< Integer> start() {
		final Key< Integer> object = new Key< Integer>();
		consumers.put( object, new Consumer< Integer>() {
			int count;
			public void consume( final ByteBuffer buffer) { count += buffer.remaining(); }
			public Integer finish() { return count; }
		});
		return object;
	}

	/**
	 * Initialize a signature on this channel.
	 *
	 * @param key the private key to use in signing this data stream.
	 * @throws NoSuchAlgorithmException if the key algorithm is not supported
	 * @throws InvalidKeyException if the key provided is invalid for signing
	 */
	public Key< byte[]> start( final PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException {
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
			public byte[] finish() {
				try {
					return signature.sign();
				} catch ( Exception e) {
					throw new RuntimeException( e);
				}
			}
		});
		return object;
	}

	/**
	 * Initialize a digest on this channel.
	 *
	 * @param algorithm the digest algorithm to use in computing the hash
	 * @throws NoSuchAlgorithmException if the given algorithm does not exist
	 */
	public Key< byte[]> start( final String algorithm) throws NoSuchAlgorithmException {
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
			public byte[] finish() {
				try {
					return digest.digest();
				} catch ( Exception e) {
					throw new RuntimeException( e);
				}
			}
		});
		return object;
	}

	@SuppressWarnings( "unchecked")
	public < T> T finish( final Key< T> object) {
		return ( T) consumers.remove( object).finish();
	}

	public void close() throws IOException {
		if ( !consumers.isEmpty()) throw new IOException( "There are '" + consumers.size() + "' unfinished operations.");
	}
}
