package org.redline_rpm.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.redline_rpm.ReadableChannelWrapper;
import org.redline_rpm.RedlineException;
import org.redline_rpm.Scanner;
import org.redline_rpm.TestBase;
import org.redline_rpm.header.AbstractHeader;
import org.redline_rpm.header.Format;
import org.redline_rpm.header.Header;
import org.redline_rpm.header.Header.HeaderTag;
import org.redline_rpm.payload.Directive;
import org.junit.Test;

import static org.redline_rpm.header.Flags.EQUAL;
import static org.redline_rpm.header.Flags.GREATER;
import static org.redline_rpm.header.Flags.LESS;
import static org.redline_rpm.header.Signature.SignatureTag.LEGACY_PGP;
import static org.redline_rpm.header.Signature.SignatureTag.RSAHEADER;
import static org.junit.Assert.*;

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

		// NB: This is no longer a bad name, long names are truncated in the header
		task.setName("ToooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooLong");
		try {
			task.execute();
		} catch (BuildException e) {
			fail();
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
	public void testBadEpoch() throws Exception {
		RedlineTask task = new RedlineTask();
		task.setName("nameRequired");
		task.setVersion("versionRequired");
		task.setGroup("groupRequired");

		// test epoch with illegal char -
		task.setEpoch("2-3");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		// test epoch with illegal char ~
		task.setEpoch("2~3");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}

		// test epoch with illegal char /
		task.setEpoch("2/3");
		try {
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}
		
		// test epoch with illegal chars abc
		task.setEpoch("abc");
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
	public void testCapabilities() throws Exception {

		File dir = ensureTargetDir();

		File filename = new File(dir, "rpmtest-1.0-1.noarch.rpm");

		RedlineTask task = createBasicTask( dir);

		for(String[] def : new String[][]{
			{"depone",   "",     "1.0"},
			{"deptwo",   "less", "2.0"},
			{"depthree", "",     ""   },
		}) {
			Depends dep = new Depends();
			dep.setName(def[0]);
			if(0 < def[1].length())
				dep.setComparison((Depends.ComparisonEnum)EnumeratedAttribute.getInstance(Depends.ComparisonEnum.class, def[1]));
			if(0 < def[2].length())
				dep.setVersion(def[2]);
			task.addDepends(dep);
		}

		for(String[] def : new String[][]{
			{"provone",   "1.1"},
			{"provtwo",   "2.1"},
			{"provthree", ""   },
		}) {
			Provides prov = new Provides();
			prov.setName(def[0]);
			if(0 < def[1].length())
				prov.setVersion(def[1]);
			task.addProvides(prov);
		}

		for(String[] def : new String[][]{
			{"conone",   "",      "1.2"},
			{"contwo",   "less",  "2.2"},
			{"conthree", "",      ""   },
		}) {
			Conflicts con = new Conflicts();
			con.setName(def[0]);
			if(0 < def[1].length())
				con.setComparison((Conflicts.ComparisonEnum)EnumeratedAttribute.getInstance(Conflicts.ComparisonEnum.class, def[1]));
			if(0 < def[2].length())
				con.setVersion(def[2]);
			task.addConflicts(con);
		}

		for(String[] def : new String[][]{
			{"obsone",   "",      "1.3"},
			{"obstwo",   "less",  "2.3"},
			{"obsthree", "",      ""   },
		}) {
			Obsoletes obs = new Obsoletes();
			obs.setName(def[0]);
			if(0 < def[1].length())
				obs.setComparison((Obsoletes.ComparisonEnum)EnumeratedAttribute.getInstance(Obsoletes.ComparisonEnum.class, def[1]));
			if(0 < def[2].length())
				obs.setVersion(def[2]);
			task.addObseletes(obs);
		}

		task.execute();

		Format format = getFormat( filename );

		String[] require = (String[])format.getHeader().getEntry(HeaderTag.REQUIRENAME).getValues();
		int[] requireflags = (int[])format.getHeader().getEntry(HeaderTag.REQUIREFLAGS).getValues();
		String[] requireversion = (String[])format.getHeader().getEntry(HeaderTag.REQUIREVERSION).getValues();
		assertArrayEquals(new String[] { "depone",      "deptwo", "depthree" }, Arrays.copyOfRange(require, require.length - 3, require.length));
		assertArrayEquals(new    int[] { EQUAL|GREATER, LESS,      0         }, Arrays.copyOfRange(requireflags, requireflags.length - 3, require.length));
		assertArrayEquals(new String[] { "1.0",         "2.0",     ""        }, Arrays.copyOfRange(requireversion, requireversion.length - 3, require.length));

		String[] provide = (String[])format.getHeader().getEntry(HeaderTag.PROVIDENAME).getValues();
		int[] provideflags = (int[])format.getHeader().getEntry(HeaderTag.PROVIDEFLAGS).getValues();
		String[] provideversion = (String[])format.getHeader().getEntry(HeaderTag.PROVIDEVERSION).getValues();
		assertArrayEquals(new String[] { "rpmtest", "provone", "provtwo", "provthree" }, provide);
		assertArrayEquals(new    int[] { EQUAL,     EQUAL,     EQUAL,     0           }, provideflags);
		assertArrayEquals(new String[] { "0:1.0-1", "1.1",     "2.1",     ""          }, provideversion);

		String[] conflict = (String[])format.getHeader().getEntry(HeaderTag.CONFLICTNAME).getValues();
		int[] conflictflags = (int[])format.getHeader().getEntry(HeaderTag.CONFLICTFLAGS).getValues();
		String[] conflictversion = (String[])format.getHeader().getEntry(HeaderTag.CONFLICTVERSION).getValues();
		assertArrayEquals(new String[] { "conone",      "contwo", "conthree" }, conflict);
		assertArrayEquals(new    int[] { EQUAL|GREATER, LESS,     0          }, conflictflags);
		assertArrayEquals(new String[] { "1.2",         "2.2",    ""         }, conflictversion);

		String[] obsolete = (String[])format.getHeader().getEntry(HeaderTag.OBSOLETENAME).getValues();
		int[] obsoleteflags = (int[])format.getHeader().getEntry(HeaderTag.OBSOLETEFLAGS).getValues();
		String[] obsoleteversion = (String[])format.getHeader().getEntry(HeaderTag.OBSOLETEVERSION).getValues();
		assertArrayEquals(new String[] { "obsone",      "obstwo", "obsthree" }, obsolete);
		assertArrayEquals(new    int[] { EQUAL|GREATER, LESS,     0          }, obsoleteflags);
		assertArrayEquals(new String[] { "1.3",         "2.3",    ""         }, obsoleteversion);
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
		assertHeaderEquals("\n\necho Hello Post Install!\n", format,
				Header.HeaderTag.POSTINSCRIPT);
		assertHeaderEquals("# comment\n\necho Hello Pre Uninstall!\n", format,
				Header.HeaderTag.PREUNSCRIPT);
		assertHeaderEquals("#!/usr/bin/perl\n\nprint \"Hello Post Uninstall!\\n\";\n", format,
				Header.HeaderTag.POSTUNSCRIPT);

		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.PREINPROG);
		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.POSTINPROG);
		assertHeaderEquals("/bin/sh", format, Header.HeaderTag.PREUNPROG);
		assertHeaderEquals("/usr/bin/perl", format, Header.HeaderTag.POSTUNPROG);

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

	private void assertHeaderEquals(String expected, Format format, AbstractHeader.Tag tag) {
		assertNotNull("null format", format);
		AbstractHeader.Entry< ?> entry = format.getHeader().getEntry(tag);
		assertNotNull("Entry not found : " + tag.getName(), entry);
		assertEquals("Entry type : " + tag.getName(), 6, entry.getType());

		String[] values = (String[]) entry.getValues();
		assertNotNull("null values", values);
		assertEquals("Entry size : " + tag.getName(), 1, values.length);

		assertEquals("Entry value : " + tag.getName(), expected, values[0]);
	}

	private void assertInt32EntryHeaderEquals(int[] expected, Format format, AbstractHeader.Tag tag) {
		assertNotNull("null format", format);
		AbstractHeader.Entry< ?> entry = format.getHeader().getEntry(tag);
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
		} catch (Exception e) {
			fail("Test failed: RedlineException should not be thrown.");
		}

		task.setName("shortpackagename");
		try {
			task.execute();
		} catch (Exception e) {
			fail("Test failed: RedlineException should not be thrown.");
		}

	}
}
