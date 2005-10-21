package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class Scanner {

	public static void main( String[] args) throws Exception {
		Rpm rpm = new Scanner().run( Channels.newChannel( System.in));
		System.out.println( rpm);
		for ( Payload payload : rpm.getPayloads()) System.out.println( payload);
	}

	public Rpm run( ReadableByteChannel in) throws IOException {
		Rpm rpm = new Rpm();
		rpm.read( in);
		return rpm;
	}
}
