package org.freecompany.redline;

import org.freecompany.redline.header.*;
import org.freecompany.redline.payload.*;
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

public class Scanner {

	public static void main( String[] args) throws Exception {
		ReadableByteChannel in = Channels.newChannel( System.in);
		Rpm rpm = new Scanner().run( );
		System.out.println( rpm);
		CpioHeader header;
		while (( header = CpioHeader.read( in)) != null) System.out.println( header);
	}

	public Rpm run( ReadableByteChannel in) throws IOException {
		Rpm rpm = new Rpm();
		rpm.read( in);
		return rpm;
	}
}
