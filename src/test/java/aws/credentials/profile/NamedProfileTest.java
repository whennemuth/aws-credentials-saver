package aws.credentials.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import aws.credentials.AbstractMappedContentTest;
import aws.credentials.file.CredentialsFile.Type;

class NamedProfileTest extends AbstractMappedContentTest {
	
	NamedProfile np;

	@SuppressWarnings("unused")
	@Override
	protected void setupMore() {
		if(true) return;
		System.out.println(String.format("%s.setupMore() not implemented", this.getClass().getSimpleName()));
	}

	@Test
	void TestNoText() {
		// Load nothing and test for null and that no errors occur.
		assertNull(getText("a"));
	}
	
	@Test
	void Test1() {
		// Load a simple 3 member default profile and test each expected member is set.
		String raw = getText("a");
		assertEquals("[default]", raw.substring(0, 9));
		np = new NamedProfile(raw, Type.CREDENTIALS);
		assertNull(np.getName());
		assertTrue(np.isDefault());
		assertNotNull(np.getKeyId());
		assertNotNull(np.getSecretKey());
		assertNotNull(np.getSessionToken());
		assertNull(np.getRegion());
		assertNull(np.getOutput());
		assertTrue(np.isValid());
		assertTrue(np.isValidAndTemporary());
		assertEquals(raw, np.rawString());
		
		// Another named profile loaded with the same content, only with the lines in a different order should still be equal to the first.
		// Only the raw text should be unequal.
		NamedProfile np2 = new NamedProfile(getText("b"), Type.CREDENTIALS);
		assertEquals(np, np2);
		assertNotEquals(np.rawString(), np2.rawString());
		assertEquals(np.toString(), np2.toString())
;	}
	
	@Test
	void Test2() {
		// Load a named profile with some junk, non-recognizable, and blank members and test they are effectively ignored.
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		assertEquals("myprofile", np.getName());
		assertFalse(np.isDefault());
		assertTrue(np.isValid());
		assertFalse(np.isValidAndTemporary());
		
		// Load another named profile that is based on the first, but with blank lines. The blank lines should not introduce any inequality.
		assertEquals(getText("b"), np.rawString());
		assertEquals(np, new NamedProfile(getText("b"), Type.CREDENTIALS));
	}
	
	/**
	 * Perform tests for updating and swapping one profile with another, where both profiles have the same members, but with different values.
	 */
	@Test
	void Test3() {
		// Update one default profile with another. 
		// 1) The resulting profiles should be equal since the target profile had no fields that the source profile didn't and the name is the same.
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		NamedProfile newprofile = new NamedProfile(getText("b"), Type.CREDENTIALS);
		np.updateWith(newprofile);
		assertTrue(newprofile.isDefault() && np.isDefault());
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
		// 2) With different names, the resulting content should be the same, except for having retained the difference in name.
		np = new NamedProfile(getText("a").replaceFirst("default", "OldProfile"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("b").replaceFirst("default", "NewProfile"), Type.CREDENTIALS);
		np.updateWith(newprofile);
		assertNotEquals(newprofile.getName(), np.getName());
		assertNotEquals(newprofile, np);
		assertNotEquals(newprofile.toString(), np.toString());
		assertNotEquals(newprofile.rawString(), np.rawString());
		
		// Swap one default profile with another. Swapping should always result in the target being equal to the source.
		// 1) Both having the same name
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("b"), Type.CREDENTIALS);
		np.swapWith(newprofile);
		assertTrue(newprofile.isDefault() && np.isDefault());
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile, np);
		// 2) Having different names
		np = new NamedProfile(getText("a").replaceFirst("default", "OldProfile"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("b").replaceFirst("default", "NewProfile"), Type.CREDENTIALS);
		np.swapWith(newprofile);
		assertEquals(newprofile.getName(), np.getName());
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
	}
	
	/**
	 * Perform tests for updating and swapping one profile with another where the profiles have different members with different values.
	 */
	@Test
	void Test4() {
		// Source and Target profiles both have key, secret, session_token, region & output fields, but extra arbitrary fields of with different names
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		NamedProfile newprofile = new NamedProfile(getText("b"), Type.CREDENTIALS);
		np.updateWith(newprofile);
		assertNotEquals(newprofile.rawString(), np.rawString());
		assertNotEquals(newprofile.toString(), np.toString());
		assertNotEquals(newprofile.getName(), np.getName());
		assertEquals(newprofile.getRegion(), np.getRegion());
		assertEquals(newprofile.getKeyId(), np.getKeyId());
		assertEquals(newprofile.getSecretKey(), np.getSecretKey());
		assertEquals(newprofile.getSessionToken(), np.getSessionToken());
		assertEquals("OldProfile", np.getName());
		assertEquals("us-east-1", np.getRegion());
		assertEquals("json", np.getOutput());
		
		// Target has region and output fields, but the source does not (the target should NOT lose its region and output fields)
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("c"), Type.CREDENTIALS);
		np.updateWith(newprofile);
		assertNotEquals(newprofile.rawString(), np.rawString());
		assertNotEquals(newprofile.toString(), np.toString());
		assertNotEquals(newprofile.getName(), np.getName());
		assertEquals(newprofile.getKeyId(), np.getKeyId());
		assertEquals(newprofile.getSecretKey(), np.getSecretKey());
		assertEquals(newprofile.getSessionToken(), np.getSessionToken());
		assertEquals("OldProfile", np.getName());
		assertEquals("us-west-2", np.getRegion());
		assertEquals("text", np.getOutput());		
		
		// Target has a session token field, but the source does not (the target SHOULD lose its session token field)
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("d"), Type.CREDENTIALS);
		np.updateWith(newprofile);
		assertNotEquals(newprofile.rawString(), np.rawString());
		assertNotEquals(newprofile.toString(), np.toString());
		assertNotEquals(newprofile.getName(), np.getName());
		assertEquals(newprofile.getKeyId(), np.getKeyId());
		assertEquals(newprofile.getSecretKey(), np.getSecretKey());
		assertNull(np.getSessionToken());
		assertEquals("OldProfile", np.getName());
		assertEquals("us-west-2", np.getRegion());
		assertEquals("text", np.getOutput());
		
		// Swaps should always result in a complete identity.
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("b"), Type.CREDENTIALS);
		np.swapWith(newprofile);
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
		
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("c"), Type.CREDENTIALS);
		np.swapWith(newprofile);
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
		
		np = new NamedProfile(getText("a"), Type.CREDENTIALS);
		newprofile = new NamedProfile(getText("d"), Type.CREDENTIALS);
		np.swapWith(newprofile);
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
	}

}
