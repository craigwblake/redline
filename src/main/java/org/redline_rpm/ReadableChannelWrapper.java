package org.redline_rpm;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Wrapper for observing data read from a NIO channel. This wrapper is
 * used for operations that must be notified of incoming IO data.
 */
public class ReadableChannelWrapper extends ChannelWrapper implements ReadableByteChannel {

	protected ReadableByteChannel channel;

	public ReadableChannelWrapper( final ReadableByteChannel channel) {
		this.channel = channel;
	}

	/**
	 * Reads data from the channel and passes it to the consumer.  This method 
	 * does not mutate the acutal data in the provided buffer, but makes it's
	 * own copy to pass to the consumer.
	 *
	 * @param buffer the buffer to read into
	 * @return the number of bytes read from the underlying channel
	 * @throws IOException if an IO error occurrs
	 */
	public int read( final ByteBuffer buffer) throws IOException {
		final int read = channel.read( buffer);
		for ( Consumer< ?> consumer : consumers.values()) consumer.consume(( ByteBuffer) buffer.duplicate().flip());
		return read;
	}

	/**
	 * Close the underlying read channel and complete any operations in the
	 * consumer.
	 *
	 * @throws IOException if an IO error occurrs
	 */
	public void close() throws IOException {
		channel.close();
		super.close();
	}

	/**
	 * Boolean flag indicating whether the channel is open or closed.
	 *
	 * @return true if the channel is open, false if not
	 */
	public boolean isOpen() {
		return channel.isOpen();
	}
}
