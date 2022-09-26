package aws.credentials.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import aws.credentials.AbstractMappedContentTest;
import aws.credentials.file.CredentialsFileModifier;
import aws.credentials.profile.NamedProfile;
import aws.credentials.util.Utils;
import aws.credentials.file.CredentialsFile;

class CredentialsFileModiferTest extends AbstractMappedContentTest {

//	private static final String SOURCE_DIR = "/tmp/newCredentials";
//	private static final String TARGET_DIR = "/root/.aws/credentials";

	private static final String SOURCE = "/tmp/newCredentials";
	private static final String TARGET = "/root/.aws/credentials";
	
	CredentialsFileModifier modifier;
	FileSystem fs;

	/**
	 * Extend test setup to write the classpath resource sample content out to a File in a mocked file system.
	 * The mocked file system will represent the workstation of a developer who wants to use the aws cli with named profiles. 
	 */
	@Override
	protected void setupMore() {
		Configuration config = Configuration.unix().toBuilder()
	       .setAttributeViews("basic", "owner", "posix", "unix")
	       .setWorkingDirectory("/")
	       .setRoots("/")
	       .build();
		
		fs = Jimfs.newFileSystem(config);
		
		try {
			Files.createDirectories(fs.getPath(SOURCE).getParent());
			Files.createDirectories(fs.getPath(TARGET).getParent());
			// This is a mock for the credentials file
			Utils.writeStringToFile(getText("a"), fs.getPath(SOURCE));
			// This is a mock for an independent file containing a set of credentials
			Utils.writeStringToFile(getText("b"), fs.getPath(TARGET));
//			Utils.writeClassPathResourceToFile("mock-content/ConfigModifierTest1b.txt", fs.getPath(TARGET));
		} 
		catch (IOException e) {
			fail("Not expecting exception during setup: ", e);
		}
	}
	
	private void assertUnchangedProfile(CredentialsFile cf, int profileNbr) {
		NamedProfile np = cf.get("myprofile" + String.valueOf(profileNbr));
		assertNotNull(np);
		assertEquals("secretAccessKey" + String.valueOf(profileNbr), np.getSecretKey());
		assertEquals("accessKeyId" + String.valueOf(profileNbr), np.getKeyId());
		if(profileNbr != 2) {
			assertEquals("sessionToken" + String.valueOf(profileNbr), np.getSessionToken());		
		}
	}
	
	private void assertChangedProfile(CredentialsFile cf, String profileName) {
		NamedProfile np = cf.get(profileName);
		assertNotNull(np);
		assertEquals("kScL7FoUb5giRpQ58LhRvd3WIhqtQzuVXO8inYXB", np.getSecretKey());
		assertEquals("ASIA3GU5SOU72FMV2SHP", np.getKeyId());
		assertTrue(String.valueOf(np.getSessionToken()).startsWith("FwoGZXIvYXdzEAQaDPxbLaMLkx4jQtiMkCLLAkVt"));			
	}
	
	private void assertAddedProfile(CredentialsFile cf, String profileName) {
		assertChangedProfile(cf, profileName);
	}
	
	/**
	 * Assert that existing named profile specified by name in the target credentials file 
	 * has the new expected values as the result of an update from the source credentials file.
	 * @param profileName
	 */
	private void assertUpdate(String profileName) {
		modifier = new CredentialsFileModifier(fs, SOURCE, TARGET);
		try {
			modifier.updateNamedProfile(profileName);
		} 
		catch (IOException e) {
			fail("Not expecting exception here: ", e);
		}				
		CredentialsFile cf = new CredentialsFile(fs.getPath(TARGET));
		assertEquals(3, cf.getNamedProfiles().size());		
		assertUnchangedProfile(cf, 1);		
		assertChangedProfile(cf, profileName);		
		assertUnchangedProfile(cf, 3);
	}

	/**
	 * Assert that a profile update resulted in the updating profile getting appended to the target instead of modifying an existing named profile.
	 * @param profileName
	 */
	private void assertInsertAndUpdate(String profileName) {		
		modifier = new CredentialsFileModifier(fs, SOURCE, TARGET);
		try {
			modifier.updateNamedProfile("unknown_profile");
		} 
		catch (IOException e) {
			fail("Not expecting exception here: ", e);
		}
		CredentialsFile cf = new CredentialsFile(fs.getPath(TARGET));
		assertEquals(4, cf.getNamedProfiles().size());
		assertUnchangedProfile(cf, 1);
		assertChangedProfile(cf, profileName);
		assertUnchangedProfile(cf, 3);
		assertAddedProfile(cf, "unknown_profile");			
	}
	
	/**
	 * Assert that a profile update resulted in no effect with respect to appending.
	 * @param profileName
	 */
	private void assertUpdateNoInsert(String profileName) {
		modifier = new CredentialsFileModifier(fs, SOURCE, TARGET);
		try {
			modifier.updateNamedProfile("unknown_profile");
		} 
		catch (IOException e) {
			fail("Not expecting exception here: ", e);
		}
		CredentialsFile cf = new CredentialsFile(fs.getPath(TARGET));
		assertEquals(3, cf.getNamedProfiles().size());
		assertUnchangedProfile(cf, 1);
		assertChangedProfile(cf, profileName);
		assertUnchangedProfile(cf, 3);
	}
	
	private void assertInsertNoUpdate(String profileName) {
		modifier = new CredentialsFileModifier(fs, SOURCE, TARGET);
		try {
			modifier.updateNamedProfile("unknown_profile");
		} 
		catch (IOException e) {
			fail("Not expecting exception here: ", e);
		}
		CredentialsFile cf = new CredentialsFile(fs.getPath(TARGET));
		assertEquals(4, cf.getNamedProfiles().size());
		assertUnchangedProfile(cf, 1);
		assertUnchangedProfile(cf, 2);
		assertUnchangedProfile(cf, 3);
		assertAddedProfile(cf, "unknown_profile");			
	}
	
	/**
	 * Assert no change to the original target credentials file.
	 * @param profileName
	 */
	private void assertNoUpdateOrInsert(String profileName) {
		modifier = new CredentialsFileModifier(fs, SOURCE, TARGET);
		try {
			modifier.updateNamedProfile(profileName);
		} 
		catch (IOException e) {
			fail("Not expecting exception here: ", e);
		}				
		CredentialsFile cf = new CredentialsFile(fs.getPath(TARGET));
		assertEquals(3, cf.getNamedProfiles().size());		
		assertUnchangedProfile(cf, 1);		
		assertUnchangedProfile(cf, 2);		
		assertUnchangedProfile(cf, 3);
	}
	
	/**
	 * Scenario: A source credentials file with a single DEFAULT profile is updating another target credentials file, 
	 * once for an profile that exists in the target, and once for one that doesn't
	 */
	@Test
	void Test1() {
		assertUpdate("myprofile2");
		assertInsertAndUpdate("myprofile2");
	}
	
	/**
	 * Scenario: A source credentials file with a single NAMED profile is updating another target credentials file, 
	 * once for an profile that exists in the target and source, and once for one that doesn't
	 */
	@Test
	void Test2() {
		assertUpdate("myprofile2");
		assertUpdateNoInsert("myprofile2");
	}

	
	/**
	 * Scenario: A credentials file with a single DEFAULT profile and a NAMED profile is updating the corresponding profile in another credentials file.
	 * once for an profile that exists in the target, and once for one that doesn't.
	 */
	@Test
	void Test3() {
		assertUpdate("myprofile2");
		assertUpdateNoInsert("myprofile2");
	}

	
	/**
	 * Scenario: A credentials file with a single named profile is updating another credentials file with no corresponding named profile.
	 */
	@Test
	void Test4() {
		assertNoUpdateOrInsert("myprofile2");
		assertInsertNoUpdate("unknown_profile");
	}

	
}
