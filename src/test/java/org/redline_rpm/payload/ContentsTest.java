package org.redline_rpm.payload;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ContentsTest extends TestCase {

	public void testListParents() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		new Contents().listParents(list, new File("/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/one/two/three", list.get( 0));
		assertEquals( "/one/two", list.get( 1));
		assertEquals( "/one", list.get( 2));
	}

	public void testListParentsBuiltin() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		new Contents().listParents(list, new File("/bin/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/bin/one/two/three", list.get( 0));
		assertEquals( "/bin/one/two", list.get( 1));
		assertEquals( "/bin/one", list.get( 2));
	}
	
	public void testListParentsNewBuiltin() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		Contents.addBuiltinDirectory("/home");
		new Contents().listParents( list, new File( "/home/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/home/one/two/three", list.get( 0));
		assertEquals( "/home/one/two", list.get( 1));
		assertEquals( "/home/one", list.get( 2));
	}
	
	public void testListParentsNewLocalBuiltin() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		Contents contents = new Contents();
		contents.addLocalBuiltinDirectory("/home");
		contents.listParents( list, new File( "/home/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/home/one/two/three", list.get( 0));
		assertEquals( "/home/one/two", list.get( 1));
		assertEquals( "/home/one", list.get( 2));
	}

    public void testAddFileSetsDirModeOnHeader() throws FileNotFoundException {
        Contents contents = new Contents();
        contents.addFile("/test/file.txt", new File("/test/source"), 0777, null, "testuser", "testgroup", 0111);
        Iterable<CpioHeader> headers = contents.headers();
        Map<String, Integer> filemodes = new HashMap<String, Integer>();
        for (CpioHeader header : headers) {
            filemodes.put(header.getName(), header.getPermissions());
        }
        assertThat(filemodes.get("/test"), is(73));
        assertThat(filemodes.get("/test/file.txt"), is(511));
    }
}
