package org.freecompany.redline;

import java.io.File;

import static org.freecompany.redline.header.RpmType.BINARY;
import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;

public class Test {

	public static void main( String[] args) throws Exception {

		// Set required fields for the RPM package.
		final Builder builder = new Builder();
		builder.setPackage( "test", "0.0.1", "1");
		builder.setType( BINARY);
		builder.setPlatform( NOARCH, LINUX);
		builder.setSummary( "Test RPM");
		builder.setDescription( "A test RPM with a few packaged files.");
		builder.setBuildHost( "localhost");
		builder.setLicense( "MIT");
		builder.setGroup( "Miscellaneous");
		builder.setDistribution( "FreeCompany");
		builder.setVendor( "FreeCompany RPM Repository http://yum.freecompany.org/");
		builder.setPackager( "Jane Doe");
		builder.setUrl( "http://www.freecompany.org/test/");
		builder.setProvides( "test");

		// Adds one file passed as an argument to the package.
		if ( args.length == 2) builder.addFile( args[ 1], new File( args[ 0]));

		// This generates a RPM file in the current directory named by the package and type settings.
		builder.build( new File( "."));
	}
}
