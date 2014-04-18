package org.redline_rpm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Wrapper around a writable channel that allows
 * for observing written data.
 */
public class WritableChannelWrapper extends ChannelWrapper implements WritableByteChannel {

	protected WritableByteChannel channel;

	public WritableChannelWrapper( final WritableByteChannel channel) {
		this.channel = channel;
	}

	/**
	 * Writes data to the wrapped channel, while passing an
	 * exact copy to the registered consumers.
	 *
	 * @param buffer the buffer to write to the wrapped channel
	 * @return the number of bytes written
	 * @throws IOException if an IO error occurs
	 */
	public int write( final ByteBuffer buffer) throws IOException {
		for ( Consumer consumer : consumers.values()) consumer.consume( buffer.duplicate());
		return channel.write( buffer);
	}

	/**
	 * Closes the underlying channel and completes
	 * any outstanding operations in the consumers.
	 *
	 * @throws IOException if an IO error occurs
	 */
	public void close() throws IOException {
		channel.close();
		super.close();
	}

	/**
	 * Flag indicating whether the underlying channel
	 * is open.
	 *
	 * @return true if it is open, false otherwise
	 */
	public boolean isOpen() {
		return channel.isOpen();
	}
}
