package org.tcoffee.client.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeyValueTest {

	@Test 
	public void testParse() { 
		KeyValue pair = KeyValue.parse(""); 
		assertEquals( null, pair );

		pair = KeyValue.parse("x"); 
		assertEquals( "x", pair.key );
		assertEquals( null, pair.value );

		pair = KeyValue.parse("y="); 
		assertEquals( "y", pair.key );
		assertEquals( null, pair.value );

		pair = KeyValue.parse("z=99"); 
		assertEquals( "z", pair.key );
		assertEquals( "99", pair.value );

		pair = KeyValue.parse("w = 88"); 
		assertEquals( "w", pair.key );
		assertEquals( "88", pair.value );
		
	}
}
