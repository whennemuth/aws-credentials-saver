package aws.credentials.credential;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import aws.credentials.credential.Region;

class RegionTest {

	private Region region;
	
	@Test
	void Test1() {
		region = new Region("region = us-east-1");
		assertEquals("us-east-1", region.getValue());
		assertTrue(region.isRecognized());
		assertTrue(region.isValid());
	}
	
	@Test
	void Test2() {
		region = new Region("AWS_SESSION_TOKEN = ASIA3GU5SOU7YZ5KURO4");
		assertEquals("ASIA3GU5SOU7YZ5KURO4", region.getValue());
		assertFalse(region.isRecognized());
		assertFalse(region.isValid());
	}
	
	@Test
	void Test3() {
		region = new Region("region = ");
		assertNull(region.getValue());
		assertTrue(region.isRecognized());
		assertFalse(region.isValid());
	}

}
