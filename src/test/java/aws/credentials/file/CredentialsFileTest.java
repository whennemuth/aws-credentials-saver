package aws.credentials.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import aws.credentials.AbstractMappedContentTest;
import aws.credentials.file.CredentialsFile.Type;
import aws.credentials.profile.NamedProfile;
import aws.credentials.profile.NamedProfileElement;

class CredentialsFileTest extends AbstractMappedContentTest {

	CredentialsFile credfile;
	
	@SuppressWarnings("unused")
	@Override
	protected void setupMore() {
		if(true) return;
		System.out.println(String.format("%s.setupMore() not implemented", this.getClass().getSimpleName()));
	}

	/**
	 * Load in a sample credentials file and test that each named profile is represented properly in the loaded object.
	 */
	@Test
	void Test1() {
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		assertEquals(3, credfile.getNamedProfiles().size());
		NamedProfile[] namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);
		assertTrue(namedProfiles[0].isDefault());
		assertEquals("my.sample.profile", namedProfiles[1].getName());
		assertEquals("my.duplicate.profile", namedProfiles[2].getName());
	}

	/**
	 * Perform the same test as in Test1, but Load in a credentials file with named profiles that are peppered with various junk lines. 
	 */
	@Test
	void Test2() {
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		assertEquals(2, credfile.getNamedProfiles().size());
		NamedProfile[] namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);
		assertTrue(namedProfiles[0].isDefault());
		assertEquals("my.sample.profile", namedProfiles[1].getName());
	}

	/**
	 * Remove a named profile from a sample credentials file and test that the credentials file shrank down to what is expected.
	 */
	@Test
	void Test3() {
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		assertEquals(3, credfile.getNamedProfiles().size());
		credfile.remove("myprofile2");
		NamedProfile[] namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);
		assertEquals(2, namedProfiles.length);
		assertEquals("myprofile1", namedProfiles[0].getName());
		assertEquals("myprofile3", namedProfiles[1].getName());
		assertEquals(getText("b"), credfile.toString().replaceAll("[\\r\\n]+", "\n"));
	}
	
	/**
	 * Take a sample credentials file and replace one of its named profiles with a new one.
	 * Test that the credentials file has the content that is expected.
	 * Repeat the test for each named profile in the credentials file.
	 */
	@Test
	void Test4() {
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		assertEquals(3, credfile.getNamedProfiles().size());

		NamedProfile newProfile = new NamedProfile(Arrays.asList(new String[] {
			"[myprofile1]",
			"aws_access_key_id = newAccessKeyId",
			"aws_secret_access_key = newSecretAccessKey",
			"aws_session_token = newSessionToken",
			"region = us-east-1",
			"junk",
			"output = json"
		}), Type.CREDENTIALS);
		
		NamedProfile[] namedProfiles = null;
		
		credfile.replace(newProfile);
		namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);			
		assertEquals(3, namedProfiles.length);
		assertEquals("myprofile1", namedProfiles[0].getName());
		assertEquals("myprofile2", namedProfiles[1].getName());
		assertEquals("myprofile3", namedProfiles[2].getName());
		assertEquals("newAccessKeyId", namedProfiles[0].getMember(NamedProfileElement.AWS_ACCESS_KEY_ID).getValue());
		assertEquals("newSecretAccessKey", namedProfiles[0].getMember(NamedProfileElement.AWS_SECRET_ACCESS_KEY).getValue());
		assertEquals("newSessionToken", namedProfiles[0].getMember(NamedProfileElement.AWS_SESSION_TOKEN).getValue());
		assertEquals("us-east-1", namedProfiles[0].getMember(NamedProfileElement.REGION).getValue());
		assertEquals("json", namedProfiles[0].getMember(NamedProfileElement.OUTPUT).getValue());
					
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		newProfile.setProfileName("myprofile2");
		credfile.replace(newProfile);
		namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);			
		assertEquals(3, namedProfiles.length);
		assertEquals("myprofile1", namedProfiles[0].getName());
		assertEquals("myprofile2", namedProfiles[1].getName());
		assertEquals("myprofile3", namedProfiles[2].getName());
		assertEquals("newAccessKeyId", namedProfiles[1].getMember(NamedProfileElement.AWS_ACCESS_KEY_ID).getValue());
		assertEquals("newSecretAccessKey", namedProfiles[1].getMember(NamedProfileElement.AWS_SECRET_ACCESS_KEY).getValue());
		assertEquals("newSessionToken", namedProfiles[1].getMember(NamedProfileElement.AWS_SESSION_TOKEN).getValue());
		assertEquals("us-east-1", namedProfiles[1].getMember(NamedProfileElement.REGION).getValue());
		assertEquals("json", namedProfiles[1].getMember(NamedProfileElement.OUTPUT).getValue());
		
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		newProfile.setProfileName("myprofile3");
		credfile.replace(newProfile);
		namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);			
		assertEquals(3, namedProfiles.length);
		assertEquals("myprofile1", namedProfiles[0].getName());
		assertEquals("myprofile2", namedProfiles[1].getName());
		assertEquals("myprofile3", namedProfiles[2].getName());
		assertEquals("newAccessKeyId", namedProfiles[2].getMember(NamedProfileElement.AWS_ACCESS_KEY_ID).getValue());
		assertEquals("newSecretAccessKey", namedProfiles[2].getMember(NamedProfileElement.AWS_SECRET_ACCESS_KEY).getValue());
		assertEquals("newSessionToken", namedProfiles[2].getMember(NamedProfileElement.AWS_SESSION_TOKEN).getValue());
		assertEquals("us-east-1", namedProfiles[2].getMember(NamedProfileElement.REGION).getValue());
		assertEquals("json", namedProfiles[2].getMember(NamedProfileElement.OUTPUT).getValue());
		
		credfile = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		newProfile.setProfileName("myprofile4");
		credfile.replace(newProfile);
		namedProfiles = (NamedProfile[]) credfile.getNamedProfiles().toArray(new NamedProfile[credfile.getNamedProfiles().size()]);			
		assertEquals(4, namedProfiles.length);
		assertEquals("myprofile1", namedProfiles[0].getName());
		assertEquals("myprofile2", namedProfiles[1].getName());
		assertEquals("myprofile3", namedProfiles[2].getName());
		assertEquals("myprofile4", namedProfiles[3].getName());
		assertEquals("newAccessKeyId", namedProfiles[3].getMember(NamedProfileElement.AWS_ACCESS_KEY_ID).getValue());
		assertEquals("newSecretAccessKey", namedProfiles[3].getMember(NamedProfileElement.AWS_SECRET_ACCESS_KEY).getValue());
		assertEquals("newSessionToken", namedProfiles[3].getMember(NamedProfileElement.AWS_SESSION_TOKEN).getValue());
		assertEquals("us-east-1", namedProfiles[3].getMember(NamedProfileElement.REGION).getValue());
		assertEquals("json", namedProfiles[3].getMember(NamedProfileElement.OUTPUT).getValue());
	}
	
	/**
	 * When persisting an update of one credentials file from another, any updated named profile should be "updated", not "swapped".
	 * This means that any updated named profile should retain the region or output members if they were missing from the updating named profile.
	 */
	@Test
	void Test5() {
		CredentialsFile target = new CredentialsFile(getText("a"), Type.CREDENTIALS);
		CredentialsFile source = new CredentialsFile(getText("b"), Type.CREDENTIALS);
		CredentialsFile updated = null;
		
		String targetProfileName = target.getNamedProfiles().toArray(new NamedProfile[target.getNamedProfiles().size()])[0].getName();
		target.update(targetProfileName, source.getDefault());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedOutputStream buff = new BufferedOutputStream(baos);
		target.persist(buff);
		try {
			buff.flush();
		} 
		catch (IOException e) {
			fail("Not expecting an exception here.", e);
		}
		
		String rawText = new String(baos.toByteArray());
		updated = new CredentialsFile(rawText, Type.CREDENTIALS);
				
		assertEquals(1, updated.getNamedProfiles().size());
		NamedProfile np = updated.getFirst();
		assertEquals("accessKeyId2", np.getKeyId());
		assertEquals("secretAccessKey2", np.getSecretKey());
		assertEquals("sessionToken2", np.getSessionToken());
		assertEquals("us-east-1", np.getRegion());
		assertEquals("text", np.getOutput());
	}
}
