package aws.credentials.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class NameValuePairTest {

	NameValuePair pair;
	
	@Test
	void Test1() {
		pair = new BasicNameValuePair("hello = goodbye");
		assertEquals("hello", pair.getName());
		assertEquals("goodbye", pair.getValue());
	}
	
	@Test
	void Test2() {
		pair = new BasicNameValuePair("  hello=goodbye	");
		assertEquals("hello", pair.getName());
		assertEquals("goodbye", pair.getValue());
	}
	
	@Test
	void Test3() {
		pair = new BasicNameValuePair("  hello | goodbye	", "|");
		assertEquals("hello", pair.getName());
		assertEquals("goodbye", pair.getValue());
	}
	
	@Test
	void Test4() {
		pair = new BasicNameValuePair("bogus");
		assertNull(pair.getName());
		assertNull(pair.getValue());
	}
}
