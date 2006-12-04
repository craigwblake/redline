package org.freecompany.redline;

import java.io.File;

import static org.freecompany.redline.header.RpmType.*;
import static org.freecompany.redline.header.Architecture.*;
import static org.freecompany.redline.header.Os.*;

public class Test {

	public static void main( String[] args) throws Exception {

		final Builder builder = new Builder();
		builder.setPackage( "test", "0.0.1", "1");
		builder.setType( BINARY);
		builder.setPlatform( NOARCH, LINUX);
		builder.setSummary( "A test RPM.");
		builder.setDescription( "A test RPM with several files.");
		builder.setBuildHost( "localhost");
		builder.setLicense( "MIT");
		builder.setGroup( "Miscellaneous");
		builder.setDistribution( "FreeCompany");
		builder.setVendor( "FreeCompany RPM Repository http://yum.freecompany.org/");
		builder.setPackager( "Craig Blake <craigwblake@mac.com>");
		builder.setUrl( "http://www.freecompany.org/test/");
		builder.setProvides( "test");

		for ( int x = 0; x < args.length; x++) {
			final File file = new File( args[x]);
			builder.addFile( "/tmp/" + file.getName(), file);
		}

		builder.build( new File( "."));
	}
}
