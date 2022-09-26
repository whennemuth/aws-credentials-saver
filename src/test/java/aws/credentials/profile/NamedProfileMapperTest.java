package aws.credentials.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;

import org.junit.jupiter.api.Test;

import aws.credentials.util.Utils;

class NamedProfileMapperTest {

	@Test
	void test1() {
		InputStream in = Utils.getClassPathResourceInputStream("mock-content/NamedProfileMap.properties");
		NamedProfileMapper mapper = new NamedProfileMapper(in);
		assertEquals("infnprd", mapper.getProfileName("770203350335"));
		assertEquals("infprd", mapper.getProfileName("115619461932"));
		assertEquals("legacy", mapper.getProfileName("730096353738"));
		assertEquals("vdsnprd", mapper.getProfileName("253997709890"));
		assertEquals("vdsprd", mapper.getProfileName("586030668166"));
		assertNull(mapper.getProfileName("bogus-account-nbr"));
	}

}
