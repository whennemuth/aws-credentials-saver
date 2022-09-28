package aws.credentials.profile;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import aws.credentials.file.CredentialsFile.Type;
import aws.credentials.profile.NamedProfileHeader;

class NamedProfileHeaderTest {
	
	private NamedProfileHeader nph;
	
	@Test
	void Test1() {
		// Bad format
		nph = new NamedProfileHeader("myprofile", Type.CREDENTIALS);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("my profile", Type.CREDENTIALS);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[myprofile]", Type.CREDENTIALS);
		assertTrue(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("profile myprofile]", Type.CONFIG);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile myprofile", Type.CONFIG);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[[profile myprofile]]", Type.CONFIG);
		assertFalse(nph.isHeader());
		
		// Bad characters
		nph = new NamedProfileHeader("[profile my@profile]", Type.CONFIG);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my&profile]", Type.CONFIG);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my#profile]", Type.CREDENTIALS);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my*profile]", Type.CONFIG);
		assertFalse(nph.isHeader());
		assertFalse(nph.isDefault());
		
		nph = new NamedProfileHeader("[profile my?profile]", Type.CONFIG);
		assertFalse(nph.isHeader());
		
		
		// Good format, good characters		
		nph = new NamedProfileHeader("[profile myprofile]", Type.CONFIG);
		assertTrue(nph.isHeader());
		assertEquals("myprofile", nph.getProfileName());

		nph = new NamedProfileHeader("[profile myprofile]", Type.CREDENTIALS);
		assertTrue(nph.isHeader());
		assertEquals("myprofile", nph.getProfileName());

		nph = new NamedProfileHeader("  [profile  myprofile]  ", Type.CREDENTIALS);
		assertTrue(nph.isHeader());
		assertEquals("myprofile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[profile my-profile]", Type.CONFIG);
		assertTrue(nph.isHeader());
		assertEquals("my-profile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[profile my_profile]", Type.CONFIG);
		assertTrue(nph.isHeader());
		assertEquals("my_profile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[profile my.profile]", Type.CONFIG);
		assertTrue(nph.isHeader());
		assertEquals("my.profile", nph.getProfileName());
		
		nph = new NamedProfileHeader("[default]", Type.CREDENTIALS);
		assertTrue(nph.isHeader());
		assertTrue(nph.isDefault());
		assertNull(nph.getProfileName());
		
		nph = new NamedProfileHeader("[default]", Type.CONFIG);
		assertTrue(nph.isHeader());
		assertTrue(nph.isDefault());
		assertNull(nph.getProfileName());
		
		nph = new NamedProfileHeader(" [default] ", Type.CREDENTIALS);
		assertTrue(nph.isHeader());
		assertTrue(nph.isDefault());
		assertNull(nph.getProfileName());
	}

}
