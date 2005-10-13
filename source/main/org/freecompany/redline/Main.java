package org.freecompany.redline;

import java.nio.channels.Channels;

public class Main {
	public static void main( String[] args) throws Exception {
		new Scanner().load( Channels.newChannel( System.in));
	}
}
