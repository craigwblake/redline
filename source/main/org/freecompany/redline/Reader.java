package org.freecompany.redline;

import java.io.*;
import java.nio.channels.*;

public class Reader {

	public static void main( String[] args) throws Exception {
		Rpm rpm = read( new FileInputStream( args[ 0]).getChannel());
		System.out.println( rpm);
	}

	public static Rpm read( final ReadableByteChannel channel) throws IOException {
		final Rpm rpm = new Rpm();
		rpm.getLead().read( channel);
		rpm.getSignature().read( channel);
		System.out.println( "Read sig\n" + rpm.getSignature());
		rpm.getHeader().read( channel);
		//rpm.getPayload().read( channel);
		return rpm;
	}
}
