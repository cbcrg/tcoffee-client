package org.tcoffee.client.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class VersionTest {
	
	@Test 
	public void testToString( ) { 
		Version v1 = new Version("1.0.2");

		assertEquals( "1.0.2", v1.toString() );
	}
	
	@Test 
	public void testHashCode() { 
		Version v = new Version("1");
		Version w = new Version("1");
		assertTrue( v.hashCode() == w.hashCode() );

		v = new Version("1.2.3");
		w = new Version("1.2.3");
		assertTrue( v.hashCode() == w.hashCode() );
	
		v = new Version("1.2.3");
		v = new Version("1.2.2");
		assertFalse( v.hashCode() == w.hashCode() );
	
	}

	@Test
	public void testEquals( ) { 
		Version v1 = new Version("1.0.2");
		Version v2 = new Version("1.0.2");
		
		assertTrue( v1.equals(v2) );
		assertTrue( v2.equals(v1) );
		
		assertFalse( v1.equals(null) );
		assertFalse( v1.equals(new Version("1")) );
		
	}
	
	@Test
	public void testCompareTo() { 

		assertEquals(  0, new Integer(1).compareTo(new Integer(1)) );
		assertEquals( -1, new Integer(1).compareTo(new Integer(2)) );
		assertEquals(  1, new Integer(1).compareTo(new Integer(0)) );
		
		assertEquals(  0, new Version("1").compareTo(new Version("1")) );
		assertEquals( -1, new Version("1").compareTo(new Version("2")) );
		assertEquals(  1, new Version("1").compareTo(new Version("0")) );

		assertEquals(  0, new Version("1").compareTo(new Version("1")) );
		assertEquals( -1, new Version("1").compareTo(new Version("1.1")) );
		assertEquals( -1, new Version("1").compareTo(new Version("1.0")) );
		assertEquals(  1, new Version("1.1").compareTo(new Version("1")) );		
		
		assertEquals(  0, new Version("1.0.3").compareTo(new Version("1.0.3")) );
		assertEquals( -1, new Version("1.0.1").compareTo(new Version("1.0.99")) );
		assertEquals( -1, new Version("1.2").compareTo(new Version("1.2.1")) );
		assertEquals(  1, new Version("1.2").compareTo(new Version("1.1.99")) );
		assertEquals(  1, new Version("2").compareTo(new Version("1.99.99")) );

	}
	
	
	@Test
	public void testLessThan() { 
		Version version = new Version("2.1.1");
		

		assertTrue( version.lessThen(new Version("2.1.2")) );
		assertTrue( version.lessThen(new Version("3.1.1")) );
		assertTrue( version.lessThen(new Version("2.1.99")) );

		assertFalse( version.lessThen(new Version("2.1.1")) );
		assertFalse( version.lessThen(new Version("2.0.0")) );
	
	}

	@Test
	public void testLessThanEquals() { 
		Version version = new Version("2.1.1");
		

		assertTrue( version.lessThenEquals(new Version("2.1.1")) );
		assertTrue( version.lessThenEquals(new Version("2.1.2")) );
		assertTrue( version.lessThenEquals(new Version("3.1.1")) );
		assertTrue( version.lessThenEquals(new Version("2.1.99")) );
		
		assertFalse( version.lessThenEquals(new Version("2.0.0")) );
	
	}
	
	@Test
	public void testGreaterThan() { 
		Version version = new Version("2.1.1");
		

		assertTrue( version.greaterThan(new Version("2")) );
		assertTrue( version.greaterThan(new Version("2.1")) );
		assertTrue( version.greaterThan(new Version("2.1.0")) );
		assertTrue( version.greaterThan(new Version("2.0.9999")) );

		assertFalse( version.greaterThan(new Version("2.1.1")) );
		assertFalse( version.greaterThan(new Version("3")) );
	
	}	


	@Test
	public void testGreaterThanEquals() { 
		Version version = new Version("2.1.1");
		

		assertTrue( version.greaterThanEquals(new Version("2")) );
		assertTrue( version.greaterThanEquals(new Version("2.1")) );
		assertTrue( version.greaterThanEquals(new Version("2.1.0")) );
		assertTrue( version.greaterThanEquals(new Version("2.0.9999")) );
		assertTrue( version.greaterThanEquals(new Version("2.1.1")) );

		assertFalse( version.greaterThanEquals(new Version("3")) );
	
	}	
}
