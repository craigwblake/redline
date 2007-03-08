package org.freecompany.redline.header;

import java.nio.*;
import java.nio.charset.*;
import junit.framework.*;

import static org.freecompany.redline.header.AbstractHeader.*;

public class AbstractHeaderTest extends TestCase {

	@SuppressWarnings( "unchecked")
	public void testCharSingle() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 1);
		buffer.put(( byte) 1);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< byte[]> entry = ( Entry< byte[]>) header.createEntry( 1);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 1);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testCharMultiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 2);
		buffer.put(( byte) 1);
		buffer.put(( byte) 2);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< byte[]> entry = ( Entry< byte[]>) header.createEntry( 1);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);
		assertEquals( 2, entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 2);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt8Single() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 1);
		buffer.put(( byte) 1);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< byte[]> entry = ( Entry< byte[]>) header.createEntry( 2);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 1);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt8Multiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 2);
		buffer.put(( byte) 1);
		buffer.put(( byte) 2);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< byte[]> entry = ( Entry< byte[]>) header.createEntry( 2);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);
		assertEquals( 2, entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 2);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt16Single() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 2);
		buffer.putShort(( short) 1);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< short[]> entry = ( Entry< short[]>) header.createEntry( 3);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 2);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt16Multiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 4);
		buffer.putShort(( short) 1);
		buffer.putShort(( short) 2);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< short[]> entry = ( Entry< short[]>) header.createEntry( 3);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);
		assertEquals( 2, entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 4);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt32Single() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 4);
		buffer.putInt( 1);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< int[]> entry = ( Entry< int[]>) header.createEntry( 4);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 4);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt32Multiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 8);
		buffer.putInt( 1);
		buffer.putInt( 2);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< int[]> entry = ( Entry< int[]>) header.createEntry( 4);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);
		assertEquals( 2, entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 8);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt64Single() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 8);
		buffer.putLong( 1);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< long[]> entry = ( Entry< long[]>) header.createEntry( 5);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 8);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testInt64Multiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.putLong( 1);
		buffer.putLong( 2);
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< long[]> entry = ( Entry< long[]>) header.createEntry( 5);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( 1, entry.getValues()[ 0]);
		assertEquals( 2, entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 16);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testStringSingle() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 8);
		buffer.put( Charset.forName( "US-ASCII").encode( "1234567\000"));
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< String[]> entry = ( Entry< String[]>) header.createEntry( 6);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( "1234567", entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 8);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testStringMultiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.put( Charset.forName( "US-ASCII").encode( "1234567\0007654321\000"));
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< String[]> entry = ( Entry< String[]>) header.createEntry( 6);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( "1234567", entry.getValues()[ 0]);
		assertEquals( "7654321", entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 16);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testBinary() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 8);
		buffer.put( new String( "12345678").getBytes());
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< byte[]> entry = ( Entry< byte[]>) header.createEntry( 7);
		entry.setCount( 8);
		entry.read( buffer);
		assertTrue( ByteBuffer.wrap( "12345678".getBytes()).equals( ByteBuffer.wrap( entry.getValues())));

		ByteBuffer data = ByteBuffer.allocate( 8);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testStringArraySingle() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 8);
		buffer.put( Charset.forName( "US-ASCII").encode( "1234567\000"));
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< String[]> entry = ( Entry< String[]>) header.createEntry( 8);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( "1234567", entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 8);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testStringArrayMultiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.put( Charset.forName( "US-ASCII").encode( "1234567\000"));
		buffer.put( Charset.forName( "US-ASCII").encode( "7654321\000"));
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< String[]> entry = ( Entry< String[]>) header.createEntry( 8);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( "1234567", entry.getValues()[ 0]);
		assertEquals( "7654321", entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 16);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testI18NStringSingle() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 8);
		buffer.put( Charset.forName( "US-ASCII").encode( "1234567\000"));
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< String[]> entry = ( Entry< String[]>) header.createEntry( 9);
		entry.setCount( 1);
		entry.read( buffer);
		assertEquals( "1234567", entry.getValues()[ 0]);

		ByteBuffer data = ByteBuffer.allocate( 8);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	@SuppressWarnings( "unchecked")
	public void testI18NStringMultiple() throws Exception {
		ByteBuffer buffer = ByteBuffer.allocate( 16);
		buffer.put( Charset.forName( "US-ASCII").encode( "1234567\000"));
		buffer.put( Charset.forName( "US-ASCII").encode( "7654321\000"));
		buffer.flip();
		
		TestHeader header = new TestHeader();
		Entry< String[]> entry = ( Entry< String[]>) header.createEntry( 9);
		entry.setCount( 2);
		entry.read( buffer);
		assertEquals( "1234567", entry.getValues()[ 0]);
		assertEquals( "7654321", entry.getValues()[ 1]);

		ByteBuffer data = ByteBuffer.allocate( 16);
		entry.write( data);

		data.flip();
		buffer.flip();
		assertTrue( buffer.equals( data));
	}

	public class TestHeader extends AbstractHeader {
		protected boolean pad() { return false; }
	}

	public AbstractHeaderTest( String name) {
		super( name);
	}

	public static Test suite() {
		return new TestSuite( AbstractHeaderTest.class);
	}

	public static void main( String[] args) {
		junit.textui.TestRunner.run( suite());
	}
}
