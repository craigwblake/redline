package org.freecompany.redline;

import org.freecompany.redline.header.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

public class DumpPayload {

	public static void main( String[] args) throws Exception {
		ReadableByteChannel in = Channels.newChannel( System.in);
		Format format = new Scanner().run( new ReadableChannelWrapper( in));
		FileChannel out = new FileOutputStream( args[ 0]).getChannel();
		
		long position = 0;
		long read;
		while (( read = out.transferFrom( in, position, 1024)) > 0) position += read;
	}
}
