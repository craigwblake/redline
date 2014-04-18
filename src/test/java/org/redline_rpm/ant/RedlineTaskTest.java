package org.redline_rpm.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.redline_rpm.ReadableChannelWrapper;
import org.redline_rpm.RedlineException;
import org.redline_rpm.Scanner;
import org.redline_rpm.TestBase;
import org.redline_rpm.header.AbstractHeader;
import org.redline_rpm.header.Format;
import org.redline_rpm.header.Header;
import org.redline_rpm.payload.Directive;
import org.junit.Test;

import static org.redline_rpm.header.Signature.SignatureTag.LEGACY_PGP;
import static org.redline_rpm.header.Signature.SignatureTag.RSAHEADER;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

public class RedlineTaskTest extends TestBase {

    @Test
	public void testBadName() throws Exception {
        	File dir = ensureTargetDir();

		RedlineTask task = new RedlineTask();
		task.setDestination(dir);
		task.setVersion("1.0");
		task.setGroup("groupRequired");

		task.setName("test");
		task.execute();

		task.setName("ToooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooLong");
		try {
			task.execute();
			fail();
		} catch (BuildException e) {
			// Pass
		}

		task.setName("test/invalid");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		task.setName("test invalid");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		task.setName("test\tinvalid");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}
	}

    @Test
	public void testBadVersion() throws Exception {
		RedlineTask task = new RedlineTask();
		task.setName("nameRequired");
		task.setGroup("groupRequired");
		// test version with illegal char -
		task.setVersion("1.0-beta");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		// test version with illegal char ~
		task.setVersion("1.0~beta");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		// test version with illegal char /
		task.setVersion("1.0/beta");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}
	}

    @Test
	public void testBadRelease() throws Exception {
		RedlineTask task = new RedlineTask();
		task.setName("nameRequired");
		task.setVersion("versionRequired");
		task.setGroup("groupRequired");

		// test release with illegal char -
		task.setRelease("2-3");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		// test release with illegal char ~
		task.setRelease("2~3");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		// test release with illegal char /
		task.setRelease("2/3");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}
	}

    @Test
	public void testRestrict() throws Exception {
		Depends one = new Depends();
		one.setName("one");
		one.setVersion("1.0");

		Depends two = new Depends();
		two.setName("two");
		two.setVersion("1.0");

		RedlineTask task = new RedlineTask();
		task.addDepends(one);
		task.addDepends(two);

		assertEquals(2, task.depends.size());
		assertEquals("one", task.depends.get(0).getName());
		assertEquals("two", task.depends.get(1).getName());

		task.restrict("one");

		assertEquals(1, task.depends.size());
		assertEquals("two", task.depends.get(0).getName());
	}

    @Test
	public void testScripts() throws Exception {

        File dir = ensureTargetDir();

		File filename = new File(dir, "rpmtest-1.0-1.noarch.rpm");

        RedlineTask task = createBasicTask( dir );
		task.setPreInstallScript(new File("src/test/resources/prein.sh"));
		task.setPostInstallScript(new File("src/test/resources/postin.sh"));
		task.setPreUninstallScript(new File("src/test/resources/preun.sh"));
		task.setPostUninstallScript(new File("src/test/resources/postun.sh"));

		RpmFileSet fs = new RpmFileSet();
		fs.setPrefix("/etc");
		fs.setFile(new File("src/test/resources/prein.sh"));
		fs.setConfig(true);
		fs.setNoReplace(true);
		fs.setDoc(true);

		task.addRpmfileset(fs);

		task.execute();

        Format format = getFormat( filename );

		assertHeaderEquals("#!/bin/sh\n\necho Hello Pre Install!\n", format,
				Header.HeaderTag.PREINSCRIPT);
		assertHeaderEquals("#!/bin/sh\n\necho Hello Post Install!\n", format,
				Header.HeaderTag.POSTINSCRIPT);
		assertHeaderEquals("#!/bin/sh\n\necho Hello Pre Uninstall!\n", format,
				Header.HeaderTag.PREUNSCRIPT);
		assertHeaderEquals("#!/bin/sh\n\necho Hello Post Uninstall!\n", format,
				Header.HeaderTag.POSTUNSCRIPT);

		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.PREINPROG);
		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.POSTINPROG);
		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.PREUNPROG);
		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.POSTUNPROG);

		int expectedFlags = Directive.RPMFILE_CONFIG | Directive.RPMFILE_DOC
				| Directive.RPMFILE_NOREPLACE;
		assertInt32EntryHeaderEquals(new int[] { expectedFlags }, format,
				Header.HeaderTag.FILEFLAGS);
	}

    @Test
    public void testSigning() throws Exception {

        File dir = ensureTargetDir();

        File filename = new File(dir, "rpmtest-1.0-1.noarch.rpm");

        RedlineTask task = createBasicTask( dir);
        task.setPrivateKeyRingFile( new File( getFileResource( "/pgp/secring.gpg")));
        task.setPrivateKeyPassphrase( "redline");
        task.execute();

        Format format = getFormat( filename);
        assertNotNull( format.getSignature().getEntry( RSAHEADER));
        assertNotNull( format.getSignature().getEntry( LEGACY_PGP));
    }

    private Format getFormat( File filename ) throws IOException {
        Scanner scanner = new Scanner();
        return scanner.run(new ReadableChannelWrapper( Channels
                .newChannel( new FileInputStream( filename ) )));
    }

    private RedlineTask createBasicTask( File dir ) {
        RedlineTask task = new RedlineTask();
        task.setProject( createProject() );

        task.setDestination(dir);
        task.setName("rpmtest");
        task.setVersion("1.0");
        task.setRelease("1");
        task.setGroup("Application/Office");
        return task;
    }

    private Project createProject() {
        Project project = new Project();
        project.setCoreLoader(getClass().getClassLoader());
        project.init();
        return project;
    }

    private File ensureTargetDir() {
        File dir = new File("target");
        if (!dir.exists()) {
            assertTrue(dir.mkdir());
        }
        return dir;
    }

	private void assertHeaderEquals(String expected, Format format,
			AbstractHeader.Tag tag) {
		assertNotNull("null format", format);
		AbstractHeader.Entry entry = format.getHeader().getEntry(tag);
		assertNotNull("Entry not found : " + tag.getName(), entry);
		assertEquals("Entry type : " + tag.getName(), 6, entry.getType());

		String[] values = (String[]) entry.getValues();
		assertNotNull("null values", values);
		assertEquals("Entry size : " + tag.getName(), 1, values.length);

		assertEquals("Entry value : " + tag.getName(), expected, values[0]);
	}

	private void assertInt32EntryHeaderEquals(int[] expected, Format format,
			AbstractHeader.Tag tag) {
		assertNotNull("null format", format);
		AbstractHeader.Entry entry = format.getHeader().getEntry(tag);
		assertNotNull("Entry not found : " + tag.getName(), entry);
		assertEquals("Entry type : " + tag.getName(), 4, entry.getType());

		int[] values = (int[]) entry.getValues();
		assertNotNull("null values", values);
		assertEquals("Entry size : " + tag.getName(), 1, values.length);

		assertArrayEquals("Entry value : " + tag.getName(), expected, values);
	}

    @Test
	public void testPackageNameLength() throws RedlineException {
        File dir = ensureTargetDir();

        RedlineTask task = new RedlineTask();
		task.setProject( createProject() );

		task.setDestination(dir);
		task.setName("thisfilenameislongdddddddddddddddddfddddddddddddddddddddddddddddddd");
		task.setVersion("1.0");
		task.setRelease("1");
		task.setGroup("Application/Office");
		task.setPreInstallScript(new File("src/test/resources/prein.sh"));
		task.setPostInstallScript(new File("src/test/resources/postin.sh"));
		task.setPreUninstallScript(new File("src/test/resources/preun.sh"));
		task.setPostUninstallScript(new File("src/test/resources/postun.sh"));

		RpmFileSet fs = new RpmFileSet();
		fs.setPrefix("/etc");
		fs.setFile(new File("src/test/resources/prein.sh"));
		fs.setConfig(true);
		fs.setNoReplace(true);
		fs.setDoc(true);

		task.addRpmfileset(fs);

		try {
			task.execute();
			fail("Test failed: Expected RedlineException not thrown.");
		} catch (Exception e) {
		}

		task.setName("shortpackagename");
		try {
			task.execute();
		} catch (Exception e) {
			fail("Test failed: RedlineException should not thrown.");
		}

	}

}
