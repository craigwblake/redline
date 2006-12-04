package org.freecompany.redline;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class ReadableChannelWrapper extends ChannelWrapper implements ReadableByteChannel {

	protected ReadableByteChannel channel;

	public ReadableChannelWrapper( final ReadableByteChannel channel) {
		this.channel = channel;
	}

	public int read( final ByteBuffer buffer) throws IOException {
		final int read = channel.read( buffer);
		for ( Consumer consumer : consumers.values()) consumer.consume(( ByteBuffer) buffer.duplicate().flip());
		return read;
	}

	public void close() throws IOException {
		channel.close();
		super.close();
	}

	public boolean isOpen() {
		return channel.isOpen();
	}
}
