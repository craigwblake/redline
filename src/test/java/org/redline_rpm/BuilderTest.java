package org.redline_rpm;

import org.junit.Test;
import org.redline_rpm.Scanner;
import org.redline_rpm.header.Format;
import org.redline_rpm.header.Header.HeaderTag;
import org.redline_rpm.header.Header;
import org.redline_rpm.payload.Directive;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.redline_rpm.ScannerTest.channelWrapper;
import static org.redline_rpm.header.Architecture.NOARCH;
import static org.redline_rpm.header.Flags.EQUAL;
import static org.redline_rpm.header.Flags.GREATER;
import static org.redline_rpm.header.Flags.LESS;
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
    public void testFiles() throws Exception {
        Builder builder = new Builder();
        builder.setPackage("filestest", "1.0", "1");
        builder.setBuildHost( "localhost" );
        builder.setLicense( "GPL" );
        builder.setPlatform( NOARCH, LINUX );
        builder.setType( BINARY );
        builder.addFile( "/etc", new File("src/test/resources/prein.sh"), 0755, 0755,
                new Directive(Directive.RPMFILE_CONFIG | Directive.RPMFILE_DOC | Directive.RPMFILE_NOREPLACE),
                "jabberwocky", "vorpal");

        builder.build( new File( getTargetDir()));

        Format format = new Scanner().run(channelWrapper("target" + File.separator + "filestest-1.0-1.noarch.rpm"));

        assertArrayEquals(new String[] { "jabberwocky" },
                (String[])format.getHeader().getEntry(Header.HeaderTag.FILEUSERNAME).getValues());
        assertArrayEquals(new String[] { "vorpal" },
                (String[])format.getHeader().getEntry(Header.HeaderTag.FILEGROUPNAME).getValues());
        assertArrayEquals(new int[] { Directive.RPMFILE_CONFIG | Directive.RPMFILE_DOC | Directive.RPMFILE_NOREPLACE },
                (int[])format.getHeader().getEntry(Header.HeaderTag.FILEFLAGS).getValues());
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

    @Test
    public void testCapabilities() throws Exception {
        Builder builder = new Builder();
        builder.setPackage("testCapabilities", "1.0", "1");
        builder.setBuildHost("localhost");
        builder.setLicense("GPL");
        builder.setPlatform(NOARCH, LINUX);
        builder.setType(BINARY);
        builder.addDependency("httpd", 0, "");
        builder.addProvides("frobnicator", "");
        builder.addProvides("barnacle", "3.89");
        builder.addConflicts("fooberry", GREATER | EQUAL, "1a");
        builder.addObsoletes("testCappypkg", 0, "");
        builder.build( new File( getTargetDir()));

        Format format = new Scanner().run(channelWrapper("target" + File.separator + "testCapabilities-1.0-1.noarch.rpm"));

        String[] require = (String[])format.getHeader().getEntry(HeaderTag.REQUIRENAME).getValues();
        int[] requireflags = (int[])format.getHeader().getEntry(HeaderTag.REQUIREFLAGS).getValues();
        String[] requireversion = (String[])format.getHeader().getEntry(HeaderTag.REQUIREVERSION).getValues();
        assertArrayEquals(new String[] { "httpd" }, Arrays.copyOfRange(require, require.length - 1, require.length));
        assertArrayEquals(new    int[] { 0       }, Arrays.copyOfRange(requireflags, requireflags.length - 1, require.length));
        assertArrayEquals(new String[] { ""      }, Arrays.copyOfRange(requireversion, requireversion.length - 1, require.length));

        String[] provide = (String[])format.getHeader().getEntry(HeaderTag.PROVIDENAME).getValues();
        int[] provideflags = (int[])format.getHeader().getEntry(HeaderTag.PROVIDEFLAGS).getValues();
        String[] provideversion = (String[])format.getHeader().getEntry(HeaderTag.PROVIDEVERSION).getValues();
        assertArrayEquals(new String[] { "testCapabilities", "frobnicator", "barnacle" }, provide);
        assertArrayEquals(new    int[] { EQUAL,              0,             EQUAL      }, provideflags);
        assertArrayEquals(new String[] { "0:1.0-1",          "",            "3.89"     }, provideversion);

        String[] conflict = (String[])format.getHeader().getEntry(HeaderTag.CONFLICTNAME).getValues();
        int[] conflictflags = (int[])format.getHeader().getEntry(HeaderTag.CONFLICTFLAGS).getValues();
        String[] conflictversion = (String[])format.getHeader().getEntry(HeaderTag.CONFLICTVERSION).getValues();
        assertArrayEquals(new String[] { "fooberry"      }, conflict);
        assertArrayEquals(new    int[] { GREATER | EQUAL }, conflictflags);
        assertArrayEquals(new String[] { "1a"            }, conflictversion);

        String[] obsolete = (String[])format.getHeader().getEntry(HeaderTag.OBSOLETENAME).getValues();
        int[] obsoleteflags = (int[])format.getHeader().getEntry(HeaderTag.OBSOLETEFLAGS).getValues();
        String[] obsoleteversion = (String[])format.getHeader().getEntry(HeaderTag.OBSOLETEVERSION).getValues();
        assertArrayEquals(new String[] { "testCappypkg" }, obsolete);
        assertArrayEquals(new    int[] { 0              }, obsoleteflags);
        assertArrayEquals(new String[] { ""             }, obsoleteversion);
    }

    @Test
    public void testMultipleCapabilities() throws Exception {
        Builder builder = new Builder();
        builder.setPackage("testMultipleCapabilities", "1.0", "1");
        builder.setBuildHost("localhost");
        builder.setLicense("GPL");
        builder.setPlatform(NOARCH, LINUX);
        builder.setType(BINARY);
        builder.addDependency("httpd", GREATER | EQUAL, "1.0");
        builder.addDependency("httpd", LESS, "2.0");
        builder.build( new File( getTargetDir()));

        Format format = new Scanner().run(channelWrapper("target" + File.separator + "testMultipleCapabilities-1.0-1.noarch.rpm"));

        String[] require = (String[])format.getHeader().getEntry(HeaderTag.REQUIRENAME).getValues();
        int[] requireflags = (int[])format.getHeader().getEntry(HeaderTag.REQUIREFLAGS).getValues();
        String[] requireversion = (String[])format.getHeader().getEntry(HeaderTag.REQUIREVERSION).getValues();
        assertArrayEquals(new String[] { "httpd"         }, Arrays.copyOfRange(require, require.length - 2, require.length - 1));
        assertArrayEquals(new    int[] { GREATER | EQUAL }, Arrays.copyOfRange(requireflags, requireflags.length - 2, require.length - 1));
        assertArrayEquals(new String[] { "1.0"           }, Arrays.copyOfRange(requireversion, requireversion.length - 2, require.length - 1));
        assertArrayEquals(new String[] { "httpd"         }, Arrays.copyOfRange(require, require.length - 1, require.length));
        assertArrayEquals(new    int[] { LESS            }, Arrays.copyOfRange(requireflags, requireflags.length - 1, require.length));
        assertArrayEquals(new String[] { "2.0"           }, Arrays.copyOfRange(requireversion, requireversion.length - 1, require.length));
    }
    
    @Test
    public void testProvideOverride() throws Exception {
        Builder builder = new Builder();
        builder.setPackage("testProvideOverride", "1.0", "1");
        builder.setBuildHost("localhost");
        builder.setLicense("GPL");
        builder.setPlatform(NOARCH, LINUX);
        builder.setType(BINARY);
        
        // add a provide of the same name as the one created by setPackage().
        // only one provide entry should appear in the RPM.
        builder.addProvides("testProvideOverride", "1.0");

        builder.build( new File( getTargetDir()));

        Format format = new Scanner().run(channelWrapper("target" + File.separator + "testProvideOverride-1.0-1.noarch.rpm"));

        String[] provide = (String[])format.getHeader().getEntry(HeaderTag.PROVIDENAME).getValues();
        int[] provideflags = (int[])format.getHeader().getEntry(HeaderTag.PROVIDEFLAGS).getValues();
        String[] provideversion = (String[])format.getHeader().getEntry(HeaderTag.PROVIDEVERSION).getValues();

        assertEquals( 1, provide.length);
        assertArrayEquals(new String[] { "testProvideOverride"}, Arrays.copyOfRange(provide, 0, provide.length));
        assertArrayEquals(new    int[] { EQUAL                }, Arrays.copyOfRange(provideflags, 0, provide.length));
        assertArrayEquals(new String[] { "1.0"                }, Arrays.copyOfRange(provideversion, 0, provide.length));
    }

    @Test
    public void testAddHeaderEntry() {
    	Builder builder = new Builder();
    	builder.addHeaderEntry(Header.HeaderTag.CHANGELOGTIME, 1);
    	try {
        	builder.addHeaderEntry(Header.HeaderTag.CHANGELOGNAME, 1);
        	fail("ClassCastException expected on setting header int value where String expected.");
    	} catch (ClassCastException e) { /* exception expected*/ }
    	
    	try {
        	builder.addHeaderEntry(Header.HeaderTag.CHANGELOGTIME, "Mon Jan 01 2016");
        	fail("ClassCastException expected on setting header String value where int expected.");
    	} catch (ClassCastException e) { /* exception expected*/ }
    	try {
        	builder.addHeaderEntry(Header.HeaderTag.CHANGELOGTIME, 1L);
        	fail("ClassCastException expected on setting header long value where int expected.");
    	} catch (ClassCastException e) { /* exception expected*/ }
    	try {
    		short s = (short) 1;
        	builder.addHeaderEntry(Header.HeaderTag.CHANGELOGTIME, s);
        	fail("ClassCastException expected on setting header short value where int expected.");
    	} catch (ClassCastException e) { /* exception expected*/ }
    	try {
    		Character c = 'c';
        	builder.addHeaderEntry(Header.HeaderTag.CHANGELOGTIME, c);
        	fail("ClassCastException expected on setting header char value where int expected.");
    	} catch (ClassCastException e) { /* exception expected*/ }

    }
}
