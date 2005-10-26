package org.freecompany.redline;

import java.io.*;
import java.security.*;

import static org.freecompany.redline.header.RpmType.*;
import static org.freecompany.redline.header.Architecture.*;
import static org.freecompany.redline.header.Os.*;

public class Test {

	public static void main( String[] args) throws Exception {

		Builder builder = new Builder();
		builder.setPackage( "test", "1.1.2", "1");
		builder.setType( BINARY);
		builder.setArch( NOARCH);
		builder.setOs( UNKNOWN);
		builder.setSummary( "A test RPM.");
		builder.setDescription( "A test RPM with several files.");
		builder.setBuildHost( "localhost");
		builder.setLicense( "MIT");
		builder.setGroup( "Miscellaneous");

		builder.addFile( "/tmp/AbstractHeader.java", new File( args[1]));
		builder.addFile( "/tmp/Entry.java", new File( args[2]));

		builder.build( new RandomAccessFile( new File( args[ 0]), "rw").getChannel());
	}
}
