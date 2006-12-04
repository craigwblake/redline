package org.freecompany.redline;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public class WritableChannelWrapper extends ChannelWrapper implements WritableByteChannel {

	protected WritableByteChannel channel;

	public WritableChannelWrapper( final WritableByteChannel channel) {
		this.channel = channel;
	}

	public int write( final ByteBuffer buffer) throws IOException {
		for ( Consumer consumer : consumers.values()) consumer.consume( buffer.duplicate());
		return channel.write( buffer);
	}

	public void close() throws IOException {
		channel.close();
		super.close();
	}

	public boolean isOpen() {
		return channel.isOpen();
	}
}
