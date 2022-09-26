package aws.credentials.credential;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import aws.credentials.credential.Output;

class OutputTest {

	private Output output;
	
	@Test
	void Test1() {
		output = new Output("output = json");
		assertEquals("json", output.getValue());
		assertTrue(output.isRecognized());
		assertTrue(output.isValid());
	}
	
	@Test
	void Test2() {
		output = new Output("AWS_SESSION_TOKEN = ASIA3GU5SOU7YZ5KURO4");
		assertEquals("ASIA3GU5SOU7YZ5KURO4", output.getValue());
		assertFalse(output.isRecognized());
		assertFalse(output.isValid());
	}
	
	@Test
	void Test3() {
		output = new Output("output = ");
		assertNull(output.getValue());
		assertTrue(output.isRecognized());
		assertFalse(output.isValid());
	}


}
