package aws.credentials.credential;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import aws.credentials.credential.AwsSessionToken;

class AwsSessionTokenTest {
	
	private AwsSessionToken token;
	private static final String validValue1 = 
			"FwoGZXIvYXdzEL7//////////wEaDP062c4CFY7uRg2XaiLLAqWVycUY5" +
			"x7NwHBKf23vE6QVEPc3nNqxWrZKULO/Dp+g6hMBG2ie737TBruAsOQM+g" +
			"JkpIQNJBQz/cTOuh4fbeoCcGMjtlnToJDAqpND9SqFlWtVHQ+jQKyUjqB" +
			"GIez8EXsoPjSdL0aOY49TyUO5M8O+0ZP0K6LE1dTmeiapo20KjVvtCRz7" +
			"pD4KT7381A+i0AxAUBW0fOtw01PF668G4v0D4prkhS4FKxXDXAnYZ30Yx" +
			"NdJKQR4osqjD+/BC0M7DNbHwuMlmK5MMEHyhvYIyX0aIzblaeHJPiBImR" +
			"l8TaBDKmGBCkYUetfgr+vj85XtLferyKRJpkIum61lixyxjFk2Pml6WDt" +
			"qsxzj4dTXJhHKWMZTtVPHx5yZX+vAWU25iw/LDUAbxXgGLo30sW5IbTwu" +
			"hvrfxLLmtItvmFwjE5Aj9GJEgGdnMSjqdfkoyN/okQYyM//i+9Uirlj+L" +
			"wl5+hFvwsaBmUYpSDMJLnbRAnVs4nt591aM2y6JGHanYLgrsw055NGQug==";

	@Test
	void Test1() {
		
		token = new AwsSessionToken(String.format("aws_session_token = %s", validValue1));
		assertEquals(validValue1, token.getValue());
		assertTrue(token.isRecognized());
		assertTrue(token.isValid());
	}
	
	@Test
	void Test2() {
		token = new AwsSessionToken("AWS_SECRET_ACCESS_KEY = ASIA3GU5SOU7YZ5KURO4");
		assertEquals("ASIA3GU5SOU7YZ5KURO4", token.getValue());
		assertFalse(token.isRecognized());
		assertFalse(token.isValid());
	}
	
	@Test
	void Test3() {
		token = new AwsSessionToken("aws_session_token = ");
		assertNull(token.getValue());
		assertTrue(token.isRecognized());
		assertFalse(token.isValid());
	}

}
