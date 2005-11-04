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
		builder.setOs( LINUX);
		builder.setSummary( "A test RPM.");
		builder.setDescription( "A test RPM with several files.");
		builder.setBuildHost( "localhost");
		builder.setLicense( "MIT");
		builder.setGroup( "Miscellaneous");
		builder.setVendor( "FreeCompany RPM Repository http://yum.freecompany.org/");
		builder.setPackager( "Craig Blake <craigwblake@mac.com>");
		builder.setUrl( "http://www.freecompany.org/test/");
		builder.setProvides( "test");

		builder.addFile( "/tmp/AbstractHeader.java", new File( args[1]));
		builder.addFile( "/tmp/Entry.java", new File( args[2]));

		File file = new File( args[ 0]);
		file.delete();
		builder.build( new RandomAccessFile( file, "rw").getChannel());
	}
}
