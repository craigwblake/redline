package org.freecompany.redline.ant;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;

import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.Scanner;
import org.freecompany.redline.header.AbstractHeader;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.header.Header;

public class RedlineTaskTest extends TestCase {

	public void testRestrict() throws Exception {
		Depends one = new Depends();
		one.setName( "one");
		one.setVersion( "1.0");
		
		Depends two = new Depends();
		two.setName( "two");
		two.setVersion( "1.0");

		RedlineTask task = new RedlineTask();
		task.addDepends( one);
		task.addDepends( two);

		assertEquals( 2, task.depends.size());
		assertEquals( "one", task.depends.get( 0).getName());
		assertEquals( "two", task.depends.get( 1).getName());

		task.restrict( "one");

		assertEquals( 1, task.depends.size());
		assertEquals( "two", task.depends.get( 0).getName());
	}

    public void testScripts() throws Exception {
        Project project = new Project();
        project.setCoreLoader(getClass().getClassLoader());
        project.init();

        RedlineTask task = new RedlineTask();
        task.setProject(project);
        
        task.setDestination(new File("target/"));
        task.setName("rpmtest");
        task.setVersion("1.0");
        task.setRelease("1");
        task.setGroup("Application/Office");
        task.setPreInstallScript(new File("source/test/prein.sh"));
        task.setPostInstallScript(new File("source/test/postin.sh"));
        task.setPreUninstallScript(new File("source/test/preun.sh"));
        task.setPostUninstallScript(new File("source/test/postun.sh"));

        task.execute();
        
        Scanner scanner = new Scanner();
        Format format = scanner.run(new ReadableChannelWrapper(Channels.newChannel(new FileInputStream("target/rpmtest-1.0-1.noarch.rpm"))));

        assertHeaderEquals("#!/bin/sh\n\necho Hello Pre Install!\n", format, Header.HeaderTag.PREINSCRIPT);
        assertHeaderEquals("#!/bin/sh\n\necho Hello Post Install!\n", format, Header.HeaderTag.POSTINSCRIPT);
        assertHeaderEquals("#!/bin/sh\n\necho Hello Pre Uninstall!\n", format, Header.HeaderTag.PREUNSCRIPT);
        assertHeaderEquals("#!/bin/sh\n\necho Hello Post Uninstall!\n", format, Header.HeaderTag.POSTUNSCRIPT);
        
        assertHeaderEquals("/bin/sh", format, Header.HeaderTag.PREINPROG);
        assertHeaderEquals("/bin/sh", format, Header.HeaderTag.POSTINPROG);
        assertHeaderEquals("/bin/sh", format, Header.HeaderTag.PREUNPROG);
        assertHeaderEquals("/bin/sh", format, Header.HeaderTag.POSTUNPROG);
    }

    private void assertHeaderEquals(String expected, Format format, AbstractHeader.Tag tag) {
        assertNotNull("null format", format);
        AbstractHeader.Entry entry = format.getHeader().getEntry(tag);
        assertNotNull("Entry not found : " + tag.getName(), entry);
        assertEquals("Entry type : " + tag.getName(), 6, entry.getType());

        String[] values = (String[]) entry.getValues();
        assertNotNull("null values", values);
        assertEquals("Entry size : " + tag.getName(), 1, values.length);

        assertEquals("Entry value : " + tag.getName(), expected, values[0]);       
    }
}
