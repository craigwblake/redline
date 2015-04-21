package org.redline_rpm;

import org.junit.Test;
import org.redline_rpm.Scanner;
import org.redline_rpm.header.Format;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.redline_rpm.ScannerTest.channelWrapper;
import static org.redline_rpm.header.Architecture.NOARCH;
import static org.redline_rpm.header.Os.LINUX;
import static org.redline_rpm.header.RpmType.BINARY;

public class BuilderTest extends TestBase {

    @Test
    public void testLongNameTruncation() throws Exception {
        Builder builder = new Builder();
        builder.setPackage( "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxa", "1.0", "1" );
        builder.setBuildHost( "localhost" );
        builder.setLicense( "GPL" );
        builder.setPlatform( NOARCH, LINUX );
        builder.setType( BINARY );
        builder.build( new File( getTargetDir()));

        Format format = new Scanner().run(channelWrapper("target" + File.separator + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxa-1.0-1.noarch.rpm"));

		assertEquals("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx", format.getLead().getName());
    }

    @Test
    public void testBuildWithoutSignature() throws Exception {
        Builder builder = new Builder();
        builder.setPackage("test", "1.0", "1");
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

    @Test
    public void testBuildWithEpoch() throws Exception {
        Builder builder = new Builder();
        builder.setPackage( "testEpoch", "1.0", "1", 1 );
        builder.setBuildHost( "localhost" );
        builder.setLicense( "GPL" );
        builder.setPlatform( NOARCH, LINUX );
        builder.setType( BINARY );
        builder.build( new File( getTargetDir()));
    }

    @Test
    public void testBuildMetapackage() throws Exception {
        Builder builder = new Builder();
        builder.setPackage( "testMetapkg", "1.0", "1", 1 );
        builder.setBuildHost( "localhost" );
        builder.setLicense( "GPL" );
        builder.setPlatform( NOARCH, LINUX );
        builder.setType( BINARY );
        builder.addDependencyMore("glibc", "2.17");
        builder.build( new File( getTargetDir()));
    }
}
