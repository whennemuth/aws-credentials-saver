package aws.credentials.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import aws.credentials.AbstractMappedContentTest;

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
		np = new NamedProfile(raw);
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
		NamedProfile np2 = new NamedProfile(getText("b"));
		assertEquals(np, np2);
		assertNotEquals(np.rawString(), np2.rawString());
		assertEquals(np.toString(), np2.toString())
;	}
	
	@Test
	void Test2() {
		// Load a named profile with some junk, non-recognizable, and blank members and test they are effectively ignored.
		np = new NamedProfile(getText("a"));
		assertEquals("myprofile", np.getName());
		assertFalse(np.isDefault());
		assertTrue(np.isValid());
		assertFalse(np.isValidAndTemporary());
		
		// Load another named profile that is based on the first, but with blank lines. The blank lines should not introduce any inequality.
		assertEquals(getText("b"), np.rawString());
		assertEquals(np, new NamedProfile(getText("b")));
	}
	
	@Test
	void Test3() {
		// Update one default profile with another. 
		// 1) The resulting profiles should be equal since the target profile had no fields that the source profile didn't and the name is the same.
		np = new NamedProfile(getText("a"));
		NamedProfile newprofile = new NamedProfile(getText("b"));
		np.updateWith(newprofile);
		assertTrue(newprofile.isDefault() && np.isDefault());
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
		// 2) With different names, the resulting content should be the same, except for having retained the difference in name.
		np = new NamedProfile(getText("a").replaceFirst("default", "Profile OldProfile"));
		newprofile = new NamedProfile(getText("b").replaceFirst("default", "Profile NewProfile"));
		np.updateWith(newprofile);
		assertNotEquals(newprofile.getName(), np.getName());
		assertNotEquals(newprofile, np);
		assertNotEquals(newprofile.toString(), np.toString());
		assertNotEquals(newprofile.rawString(), np.rawString());
		
		// Swap one default profile with another. Swapping should always result in the target being equal to the source.
		// 1) Both having the same name
		np = new NamedProfile(getText("a"));
		newprofile = new NamedProfile(getText("b"));
		np.swapWith(newprofile);
		assertTrue(newprofile.isDefault() && np.isDefault());
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile, np);
		// 2) Having different names
		np = new NamedProfile(getText("a").replaceFirst("default", "Profile OldProfile"));
		newprofile = new NamedProfile(getText("b").replaceFirst("default", "Profile NewProfile"));
		np.swapWith(newprofile);
		assertEquals(newprofile.getName(), np.getName());
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
}
	
	@Test
	void Test4() {
		np = new NamedProfile(getText("a"));
		NamedProfile newprofile = new NamedProfile(getText("b"));
		np.updateWith(newprofile);
		assertNotEquals(newprofile.rawString(), np.rawString());
		assertNotEquals(newprofile.toString(), np.toString());
		assertNotEquals(newprofile.getName(), np.getName());
		assertEquals(newprofile.getRegion(), np.getRegion());
		assertEquals(newprofile.getKeyId(), np.getKeyId());
		assertEquals(newprofile.getSecretKey(), np.getSecretKey());
		assertEquals(newprofile.getSessionToken(), np.getSessionToken());
		assertEquals(np.getName(), "OldProfile");
		assertEquals(np.getRegion(), "us-east-1");
		assertEquals(np.getOutput(), "json");
		
		np = new NamedProfile(getText("a"));
		newprofile = new NamedProfile(getText("b"));
		np.swapWith(newprofile);
		assertEquals(newprofile.rawString(), np.rawString());
		assertEquals(newprofile.toString(), np.toString());
		assertEquals(newprofile, np);
	}

}
