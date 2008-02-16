package org.freecompany.redline.ant;

import junit.framework.TestCase;

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
}
