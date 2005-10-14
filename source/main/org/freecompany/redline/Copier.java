package org.freecompany.redline;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class Copier {

	public static void main( String[] args) throws Exception {
		new Copier().run( Channels.newChannel( System.in), Channels.newChannel( System.out));
	}

	public void run( ReadableByteChannel in, WritableByteChannel out) throws Exception {
		Rpm rpm = new Scanner().run( in);
		rpm.write( out);
	}
}
