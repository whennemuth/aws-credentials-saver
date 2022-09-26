package aws.credentials.profile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Convenience wrapper for Properties object.
 * @author wrh
 *
 */
public class NamedProfileMapper extends Properties {

	private static final long serialVersionUID = -4508123572799684936L;

	public NamedProfileMapper(String propertiesFile) {
		this(Paths.get(propertiesFile));
	}
	
	public NamedProfileMapper(Path propertiesFile) {
		try (InputStream in = new FileInputStream(propertiesFile.toAbsolutePath().toString())) {
			initialize(in);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public NamedProfileMapper(InputStream in) {
		initialize(in);
	}
	
	private void initialize(InputStream in) {
		try {
			this.load(in);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}	
	}

	public String getProfileName(String accountId) {
		return this.getProperty(accountId);
	}

	/**
	 * Cheap tester.
	 * @param args
	 */
	public static void main(String[] args) {
		// Standard windows uri
		String propfile = "C:\\whennemuth\\workspaces\\bu_workspace\\AwsCredential\\src\\test\\resources\\mock-content\\NamedProfileMap.properties";
		NamedProfileMapper mapper = new NamedProfileMapper(propfile);
		System.out.println(mapper.getProfileName("730096353738"));
		
		// This also works:
		propfile = "/whennemuth/workspaces/bu_workspace/AwsCredential/src/test/resources/mock-content/NamedProfileMap.properties";
		mapper = new NamedProfileMapper(propfile);
		System.out.println(mapper.getProfileName("770203350335"));
	}
}
