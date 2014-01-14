package org.freecompany.redline.ant;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.RedlineException;
import org.freecompany.redline.Scanner;
import org.freecompany.redline.header.AbstractHeader;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Header;
import org.freecompany.redline.payload.Directive;
import org.junit.Test;

import static org.junit.Assert.*;

public class RedlineTaskTest extends TestCase {

	public void testBadVersionWithDashes() throws Exception {
		try {
			RedlineTask task = new RedlineTask();
			task.setName("nameRequired");
			task.setVersion("1.0-beta");
			task.setGroup("groupRequired");
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}
	}

	public void testBadReleaseWithDashes() throws Exception {
		try {
			RedlineTask task = new RedlineTask();
			task.setName("nameRequired");
			task.setVersion("versionRequired");
			task.setGroup("groupRequired");
			task.setRelease("2-3");
			task.execute();
			fail();
		} catch (IllegalArgumentException iae) {
			// Pass
		}
	}

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

	public void testScripts() throws Exception {

		File dir = new File("target");
		if (!dir.exists()) {
			assertTrue(dir.mkdir());
		}

		File filename = new File(dir, "rpmtest-1.0-1.noarch.rpm");

		Project project = new Project();
		project.setCoreLoader(getClass().getClassLoader());
		project.init();

		RedlineTask task = new RedlineTask();
		task.setProject(project);

		task.setDestination(dir);
		task.setName("rpmtest");
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

		task.execute();

		Scanner scanner = new Scanner();
		Format format = scanner.run(new ReadableChannelWrapper(Channels
				.newChannel(new FileInputStream(filename))));

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

	public void testPackageNameLength() throws RedlineException {
		File dir = new File("target");
		if (!dir.exists()) {
			assertTrue(dir.mkdir());
		}

		Project project = new Project();
		project.setCoreLoader(getClass().getClassLoader());
		project.init();

		RedlineTask task = new RedlineTask();
		task.setProject(project);

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
			fail("Test failed: RedlineException shoudl not thrown.");
		}

	}

}
