package aws.credentials.credential;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import aws.credentials.credential.AwsAccessKeyId;

class AwsAccessKeyIdTest {

	private AwsAccessKeyId id;
	
	@Test
	void Test1() {
		id = new AwsAccessKeyId("aws_access_key_id = ASIA3GU5SOU7YZ5KURO4");
		assertEquals("ASIA3GU5SOU7YZ5KURO4", id.getValue());
		assertTrue(id.isRecognized());
		assertTrue(id.isValid());
	}
	
	@Test
	void Test2() {
		id = new AwsAccessKeyId("AWS_SESSION_TOKEN = ASIA3GU5SOU7YZ5KURO4");
		assertEquals("ASIA3GU5SOU7YZ5KURO4", id.getValue());
		assertFalse(id.isRecognized());
		assertFalse(id.isValid());
	}
	
	@Test
	void Test3() {
		id = new AwsAccessKeyId("aws_access_key_id = ");
		assertNull(id.getValue());
		assertTrue(id.isRecognized());
		assertFalse(id.isValid());
	}
}
