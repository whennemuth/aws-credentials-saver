package aws.credentials.file;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import aws.credentials.profile.NamedProfile;

/**
 * This class replaces a specified named profile in one credentials file with the corresponding named profile in another credentials file.
 * 
 * @author wrh
 *
 */
public class CredentialsFileModifier {

	private CredentialsFile oldcreds;
	private CredentialsFile newcreds;
	
	public CredentialsFileModifier(FileSystem fs, String source, String target) {
		this(fs, fs.getPath(source), fs.getPath(target));
	}
	
	public CredentialsFileModifier(FileSystem fs, Path newcreds, Path oldcreds) {
		this.oldcreds = new CredentialsFile(oldcreds);
		this.newcreds = new CredentialsFile(newcreds);
	}

	/**
	 * Replace the specified named profile in a "target" credentials file with the same named profile
	 * from a "source" credentials file. If no such named profile existed in the target file, then append it.
	 * 
	 * If no such named profile exists in the source file and it has more than one profile in it, then abort.
	 * Otherwise, update the named profile in the target with the source if the source is a default profile.
	 * Otherwise, append the named profile to the target with the source.
	 *  
	 * @param profileName
	 * @throws IOException 
	 */
	public void updateNamedProfile(String profileName) throws IOException {
		
		if(newcreds.has(profileName) && oldcreds.has(profileName)) {
			oldcreds.update(newcreds.get(profileName));
		}
		else if(oldcreds.has(profileName) && newcreds.hasOnlyDefault()) {
			oldcreds.update(profileName, newcreds.getDefault());
		}
		else if(newcreds.hasOnlyDefault()) {
			NamedProfile np = newcreds.getDefault();
			np.setProfileName(profileName);
			oldcreds.addNamedProfile(np);
		}
		else if(newcreds.has(profileName)) {
			oldcreds.addNamedProfile(newcreds.get(profileName));
		}
		
		oldcreds.persist(
			Files.newOutputStream(
					oldcreds.getPath(), 
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING)
		);
	}
}
