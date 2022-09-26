package aws.credentials.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import aws.credentials.profile.NamedProfileHeader;

class NamedProfileHeaderTest {
	
	private NamedProfileHeader nph;
	
	@Test
	void Test1() {
		// Bad format
		nph = new NamedProfileHeader("myprofile");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("my profile");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[myprofile]");
		assertTrue(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("profile myprofile]");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile myprofile");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[[profile myprofile]]");
		assertFalse(nph.isHeader());
		
		// Bad characters
		nph = new NamedProfileHeader("[profile my@profile]");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my&profile]");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my#profile]");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my*profile]");
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my?profile]");
		assertFalse(nph.isHeader());
		
		
		// Good format, good characters		
		nph = new NamedProfileHeader("[profile myprofile]");
		assertTrue(nph.isHeader());
		assertEquals("myprofile", nph.getProfileName());

		nph = new NamedProfileHeader("  [profile  myprofile]  ");
		assertTrue(nph.isHeader());
		assertEquals("myprofile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[profile my-profile]");
		assertTrue(nph.isHeader());
		assertEquals("my-profile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[profile my_profile]");
		assertTrue(nph.isHeader());
		assertEquals("my_profile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[profile my.profile]");
		assertTrue(nph.isHeader());
		assertEquals("my.profile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[default]");
		assertTrue(nph.isHeader());
		assertTrue(nph.isDefault());
		assertNull(nph.getProfileName());
		
		nph = new NamedProfileHeader(" [default] ");
		assertTrue(nph.isHeader());
		assertTrue(nph.isDefault());
		assertNull(nph.getProfileName());
	}

}
