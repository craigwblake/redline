package org.freecompany.redline;

import org.junit.Test;

import java.io.File;

import static org.freecompany.redline.header.Architecture.NOARCH;
import static org.freecompany.redline.header.Os.LINUX;
import static org.freecompany.redline.header.RpmType.BINARY;

public class BuilderTest extends TestBase {

    @Test
    public void testBuildWithoutSignature() throws Exception {
        Builder builder = new Builder();
        builder.setPackage( "test", "1.0", "1" );
        builder.setBuildHost( "localhost" );
        builder.setLicense( "GPL" );
        builder.setPlatform( NOARCH, LINUX );
        builder.setType( BINARY );
        builder.build( new File( getTargetDir()));
    }
      
    @Test
    public void testBuildWithSignature() throws Exception {
        Builder builder = new Builder();
        builder.setPackage("signing-test", "1.0", "1");
        builder.setBuildHost( "localhost" );
        builder.setLicense( "GPL" );
        builder.setPlatform( NOARCH, LINUX );
        builder.setType( BINARY );
        builder.setPrivateKeyRingFile( new File( getFileResource( "/pgp/secring.gpg" ) ) );
        builder.setPrivateKeyPassphrase( "redline" );
        builder.build( new File( getTargetDir()));
    }
}
