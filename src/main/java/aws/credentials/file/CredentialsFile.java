package aws.credentials.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import aws.credentials.profile.NamedProfile;
import aws.credentials.profile.NamedProfileHeader;
import aws.credentials.util.Utils;

/**
 * This class represents a configuration file where named profiles can be stored, such as ~/.aws/config or ~/.aws/credentials.
 * It concerns itself with the named profiles only.
 * 
 * @author wrh
 *
 */
public class CredentialsFile implements Watchable {
	
	private List<String> rawTextLines = new ArrayList<String>();
	private Path credentialsFile;
	private LinkedHashSet<NamedProfile> namedProfiles = new LinkedHashSet<NamedProfile>();

	public CredentialsFile(String rawText) {
		this.rawTextLines.addAll(Utils.readLines(rawText));
		initialize();
	}
	
	public CredentialsFile(Path credentialsFile) {
		this.credentialsFile = credentialsFile;
		this.rawTextLines.addAll(Utils.readLines(this.credentialsFile));
		initialize();
	}
	
	public CredentialsFile(InputStream in) {
		this.rawTextLines.addAll(Utils.readLines(in));
		initialize();
	}
	
	private void initialize() {
		List<String> namedProfileLines = new ArrayList<String>();
		for (Iterator<String> iterator = rawTextLines.iterator(); iterator.hasNext();) {
			String line = (String) iterator.next();
			if( ! line.isBlank()) {
				if(new NamedProfileHeader(line).isHeader()) {
					if( ! namedProfileLines.isEmpty()) {
						addNamedProfile(namedProfileLines);
						namedProfileLines.clear();
					}
				}
				else if(namedProfileLines.isEmpty()) {
					// A non-header line was encountered before the first named profile header in the file.
					// The line must be a comment or something not part of a named profile, so skip it.
					continue;
				}
				namedProfileLines.add(line);
			}
			if( ! iterator.hasNext() && namedProfileLines.isEmpty() == false) {
				addNamedProfile(namedProfileLines);
			}
		}
	}
	
	public boolean addNamedProfile(List<String> namedProfileLines) {
		return addNamedProfile(new NamedProfile(namedProfileLines));
	}
	
	public boolean addNamedProfile(NamedProfile np) {
		if( ! namedProfiles.add(np)) {
			System.out.println(String.format("The named profile %s was encountered more than once!", np.getName()));
			return false;
		}
		return true;
	}
	
	
	public Path getPath() {
		return credentialsFile;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Iterator<NamedProfile> iterator = namedProfiles.iterator(); iterator.hasNext();) {
			NamedProfile namedProfile = (NamedProfile) iterator.next();
			builder.append(namedProfile.rawString());
			if(iterator.hasNext()) {
				builder.append("\n\n");
			}
		}
		return builder.toString();
	}

	public Set<NamedProfile> getNamedProfiles() {
		return namedProfiles;
	}

	public NamedProfile get(String profileName) {
		for (Iterator<NamedProfile> iterator = namedProfiles.iterator(); iterator.hasNext();) {
			NamedProfile np = (NamedProfile) iterator.next();
			if(np.getName() != null && np.getName().equals(profileName)) {
				return np;
			}
		}
		return null;
	}

	public NamedProfile getDefault() {
		for (Iterator<NamedProfile> iterator = namedProfiles.iterator(); iterator.hasNext();) {
			NamedProfile np = (NamedProfile) iterator.next();
			if(np.isDefault()) {
				return np;
			}
		}
		return null;
	}
	
	public NamedProfile getFirst() {
		return namedProfiles.iterator().next();
	}
	
	public boolean has(String profileName) {
		return get(profileName) != null;
	}

	public boolean hasDefault() {
		return getDefault() != null;
	}
	
	public boolean hasOnlyDefault() {
		return hasDefault() && getNamedProfiles().size() == 1;
	}
	
	public void remove(String profileName) {
		for (Iterator<NamedProfile> iterator = namedProfiles.iterator(); iterator.hasNext();) {
			NamedProfile np = (NamedProfile) iterator.next();
			if(np.getName().equals(profileName)) {
				iterator.remove();
				break;
			}
		}
	}
	
	public void replace(NamedProfile namedProfile) {
		replace(namedProfile.getName(), namedProfile);
	}

	public void replace(String profileName, NamedProfile namedProfile) {
		change(profileName, namedProfile, true);
	}
	
	public void update(NamedProfile namedProfile) {
		replace(namedProfile.getName(), namedProfile);
	}

	public void update(String profileName, NamedProfile namedProfile) {
		change(profileName, namedProfile, false);
	}	

	private void change(String profileName, NamedProfile namedProfile, boolean replace) {
		for (Iterator<NamedProfile> iterator = namedProfiles.iterator(); iterator.hasNext();) {
			NamedProfile np = (NamedProfile) iterator.next();
			if(np.isDefault()) {
				if(profileName == null) {
					change(np, namedProfile, replace);
					return;
				}
			}
			else if(np.getName().equals(profileName)) {
				change(np, namedProfile, replace);				
				return;
			}
		}
		// No matching profile was found to replace, so just add it.
		namedProfiles.add(namedProfile);
	}
	
	private void change(NamedProfile oldprof, NamedProfile newprof, boolean replace) {
		if(replace) {
			oldprof.swapWith(newprof);
		}
		else {
			oldprof.updateWith(newprof);
		}				

	}

	public void persist(OutputStream out) {
		Utils.readWriteLines(new ByteArrayInputStream(this.toString().getBytes()), out);
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
		return credentialsFile.register(watcher, events, modifiers);
	}

	@Override
	public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
		return credentialsFile.register(watcher, events);
	}

}
