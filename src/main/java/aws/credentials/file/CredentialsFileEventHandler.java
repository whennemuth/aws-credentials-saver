package aws.credentials.file;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Watchable;

import aws.credentials.profile.NamedProfile;
import aws.credentials.profile.NamedProfileElement;
import aws.credentials.profile.NamedProfileMapper;
import aws.credentials.util.CredentialsProvider;

/**
 * An instance of this class will modify a named profile in a specified credentials file with 
 * the details of a default profile in another specified credentials file.
 *  
 * @author wrh
 *
 */
public class CredentialsFileEventHandler implements FileEventHandler {

	private FileSystem fs;
	private Path targetCredFilePath;
	private NamedProfileMapper mapper;
	
	public CredentialsFileEventHandler(FileSystem fs, Path targetCredFilePath, NamedProfileMapper mapper) {
		this.fs = fs;
		this.targetCredFilePath = targetCredFilePath;
		this.mapper = mapper;
	}

	/**
	 * Perform the credentials file update
	 * 
	 * @param sourceCredFilePath
	 * @throws IOException
	 */
	private void handle(Watchable sourceCredFilePath) throws IOException {
		
		CredentialsFile newcreds = new CredentialsFile((Path) sourceCredFilePath);
		
		if( ! newcreds.hasDefault()) {
			System.err.format(""
					+ "Cannot update profile!\n"
					+ "Expected a default profile in the newly downloaded credentials file:\n"
					+ "   \"%s\"\n"
					+ "No default profile found.\n", newcreds.getPath());
			return;
		}
		
		NamedProfile np = newcreds.getDefault();
		CredentialsProvider provider = CredentialsProvider.getInstance()
				.setKeyId(np.getKeyId())
				.setSecretKey(np.getSecretKey())
				.setSessionToken(np.getSessionToken())
				.setRegion(np.hasMember(NamedProfileElement.REGION) ? np.getRegion() : "us-east-1");
		
		String accountId = provider.getAccountId();		
		if(accountId == null) {
			System.err.println("Could not get account id of new credentials download!");
			return;
		}
		
		String profileName = mapper.getProfileName(accountId);
		if(profileName == null) {
			System.err.format("Could not match a profile name for account id \"%s\"\n", accountId);
			return;
		}
		
		CredentialsFileModifier modifier = new CredentialsFileModifier(fs, newcreds.getPath(), targetCredFilePath);
		
		modifier.updateNamedProfile(profileName);
		
		System.out.format("Profile %s has been updated!\n", profileName);
	}
	
	/**
	 * The source credentials file has been updated.
	 */
	@Override public void onUpdate(Watchable source) throws IOException {
		handle(source);
	}

	/**
	 * The source credentials file has been created.
	 */
	@Override public void onCreate(Watchable source) throws IOException {
		handle(source);
	}

	/**
	 * The source credentials file has been deleted.
	 */
	@Override
	public void onDelete(Watchable watchable) {
		System.out.format("\"%s\" has been deleted\n", watchable);
	}

}
