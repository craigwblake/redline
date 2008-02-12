package org.freecompany.redline.payload;

import java.io.File;
import java.util.ArrayList;
import junit.framework.TestCase;

public class ContentsTest extends TestCase {

	public void testListParentsBuiltin() throws Exception {
		ArrayList< String> list = new ArrayList< String>();
		Contents.listParents( list, new File( "/bin/one/two/three/four"));

		assertEquals( 3, list.size());
		assertEquals( "/bin/one/two/three", list.get( 0));
		assertEquals( "/bin/one/two", list.get( 1));
		assertEquals( "/bin/one", list.get( 2));
	}
}
