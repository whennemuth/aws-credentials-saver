package aws.credentials.credential;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import aws.credentials.credential.AwsSecretAccessKey;

class AwsSecretAccessKeyTest {

	private AwsSecretAccessKey id;
	
	@Test
	void Test1() {
		id = new AwsSecretAccessKey("aws_secret_access_key = 00erNR1hH46EKr6WDqbnTFSlNYVugZ+7Zji/y4zL");
		assertEquals("00erNR1hH46EKr6WDqbnTFSlNYVugZ+7Zji/y4zL", id.getValue());
		assertTrue(id.isRecognized());
		assertTrue(id.isValid());
	}
	
	@Test
	void Test2() {
		id = new AwsSecretAccessKey("AWS_SESSION_TOKEN = ASIA3GU5SOU7YZ5KURO4");
		assertEquals("ASIA3GU5SOU7YZ5KURO4", id.getValue());
		assertFalse(id.isRecognized());
		assertFalse(id.isValid());
	}
	
	@Test
	void Test3() {
		id = new AwsSecretAccessKey("aws_secret_access_key = ");
		assertNull(id.getValue());
		assertTrue(id.isRecognized());
		assertFalse(id.isValid());
	}

}
