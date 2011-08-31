package org.freecompany.redline.payload;

import java.io.File;
import java.util.ArrayList;
import junit.framework.TestCase;

public class ContentsTest extends TestCase {

	public void testListParents() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		Contents.listParents( list, new File( "/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/one/two/three", list.get( 0));
		assertEquals( "/one/two", list.get( 1));
		assertEquals( "/one", list.get( 2));
	}

	public void testListParentsBuiltin() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		Contents.listParents( list, new File( "/bin/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/bin/one/two/three", list.get( 0));
		assertEquals( "/bin/one/two", list.get( 1));
		assertEquals( "/bin/one", list.get( 2));
	}

	public void testListParentsNewBuiltin() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		Contents.addBuiltinDirectory("/home");
		Contents.listParents( list, new File( "/home/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/home/one/two/three", list.get( 0));
		assertEquals( "/home/one/two", list.get( 1));
		assertEquals( "/home/one", list.get( 2));
	}
}
