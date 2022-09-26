package aws.credentials.profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

import aws.credentials.util.NameValuePair;
import aws.credentials.util.Utils;

/**
 * This class represents a named profile consisting of a group of lines of text as you would
 * find in a ~/.aws/config or ~/.aws/credentials file.
 * 
 * @author wrh
 *
 */
public class NamedProfile {

	private List<String> rawTextLines = new ArrayList<String>();
	private NamedProfileHeader header;
	private Set<NameValuePairProfileDecorator> members = new HashSet<NameValuePairProfileDecorator>();
	private List<String> other = new ArrayList<String>();

	public NamedProfile(String rawText) {
		if(rawText != null) {
			this.rawTextLines.addAll(Utils.readLines(rawText));
		}
		initialize();
	}
	
	public NamedProfile(List<String> rawTextLines) {
		if(rawTextLines != null) {
			this.rawTextLines.addAll(rawTextLines);
		}
		initialize();
	}
	
	/**
	 * Update this named profile with all of the name/value pairs of another named profile, AND change to the new name too.
	 * @param np
	 */
	public void swapWith(NamedProfile np) {
		this.rawTextLines.clear();
		this.rawTextLines.addAll(np.rawTextLines);
		initialize();
		// In case the profile name for np was reset since its raw text lines were loaded (Is the only non-readonly field).
		this.setProfileName(np.getName());
	}
	
	/**
	 * Update this named profile with all of the name/value pairs of another named profile, BUT leave the name of the profile unchanged.
	 * @param np
	 */
	public void updateWith(NamedProfile np) {
		this.rawTextLines.clear();
		this.rawTextLines.addAll(np.rawTextLines);
		initialize(header.toString());
	}
	
	private void initialize() {
		initialize(null);
	}
		
	private void initialize(String newHeader) {
		members.clear();
		other.clear();
		for (ListIterator<String> iterator = rawTextLines.listIterator(); iterator.hasNext();) {
			String line = (String) iterator.next();			
			NameValuePairProfileDecorator nvp;
			NamedProfileHeader headerTest = new NamedProfileHeader(line);
			if(headerTest.isHeader()) {
				if(newHeader == null) {
					if(header == null) {
						header = headerTest;
					}
					else {
						iterator.set(header.toString());
					}
				}
				else {
					NamedProfileHeader nph = new NamedProfileHeader(newHeader);
					header = nph;
					iterator.set(nph.toString());
				}
			}
			else {
				boolean matched = false;
				for(String shortname : new String[] { "id", "key", "token", "region", "output" }) {				
					nvp = NamedProfileElement.getInstance(shortname, line);
					if(nvp != null && nvp.isValid()) {
						members.add(nvp);
						matched = true;
						break;
					}										
				}
				if( ! matched) {
					other.add(line);
				}
			}
		}
	}
	
	/**
	 * Set the profile name field with another value. 
	 * Also modify the raw text behind this named profile so that the line corresponding to the name also reflects the new value.
	 * @param profileName
	 */
	public void setProfileName(String profileName) {
		if(profileName == null || NamedProfileHeader.isDefault(profileName)) {
			makeDefault();
		}
		else {
			this.header = new NamedProfileHeader(String.format("[Profile %s]", profileName));
		}
		for (ListIterator<String> iterator = rawTextLines.listIterator(); iterator.hasNext();) {
			String line = iterator.next();
			if(new NamedProfileHeader(line).isHeader()) {
				iterator.set(this.header.toString());
				break;
			}			
		}
	}
	
	public void makeDefault() {
		this.header = new NamedProfileHeader("[default]");
	}
	
	/**
	 * The profile has at a minimum its name, key, and id.
	 * @return
	 */
	public boolean isValid() {
		if(header == null) return false;
		if(! hasMember(NamedProfileElement.AWS_ACCESS_KEY_ID)) return false;
		if(! hasMember(NamedProfileElement.AWS_SECRET_ACCESS_KEY)) return false;
		return true;
	}
	
	/**
	 * The profile has at a minimum its name, key, id and session token.
	 * 
	 * @return
	 */
	public boolean isValidAndTemporary() {
		if( ! isValid()) return false;
		if(! hasMember(NamedProfileElement.AWS_SESSION_TOKEN)) return false;
		return true;
	}
	
	public boolean hasMember(NamedProfileElement element) {
		return getMember(element) != null;
	}
	
	public NameValuePair getMember(NamedProfileElement element) {
		for(NameValuePairProfileDecorator nvp : members) {
			if(element.equals(nvp.getType())) {
				return nvp;
			}
		}
		return null;
	}
	public String getMemberValue(NamedProfileElement element) {
		NameValuePair member = getMember(element);
		if(member == null) {
			return null;
		}
		return member.getValue();
	}

	public String getOtherValue(String name) {
		for(String raw : other) {
			NameValuePair nvp = NameValuePairProfileDecorator.getBasicInstance(raw);
			if(nvp.is(name)) {
				return nvp.getValue();
			}
		}
		return null;
	}
	
	public String getName() {
		if(header != null) {
			return header.getProfileName();
		}		
		return null;
	}
	
	public String getSecretKey() {
		return getMemberValue(NamedProfileElement.AWS_SECRET_ACCESS_KEY);
	}
	public String getKeyId() {
		return getMemberValue(NamedProfileElement.AWS_ACCESS_KEY_ID);
	}
	public String getRegion() {
		return getMemberValue(NamedProfileElement.REGION);
	}
	public String getOutput() {
		return getMemberValue(NamedProfileElement.OUTPUT);
	}
	public String getSessionToken() {
		return getMemberValue(NamedProfileElement.AWS_SESSION_TOKEN);
	}
	
	public boolean isDefault() {
		if(header != null) {
			return header.isDefault();
		}
		return false;
	}

	/**
	 * Get a string representation of the named profile most closely representing the "raw" input it is based on.
	 * Items appear will include "junk" or comment lines and all will appear in the original order.
	 * 
	 * @return
	 */
	public String rawString() {
		if(rawTextLines.isEmpty()) return null;
		String retval = rawTextLines.stream().collect(Collectors.joining("\n")).trim();
		return retval;
	}

	/**
	 * Get a string representation of the named profile which only includes its recognized members (no comments or unrecognized name=value pairs).
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(header).append("\n");
		for(NameValuePair nvp : members) {
			sb.append(nvp.asString()).append("\n");
		}
//		for(String line : other) {
//			sb.append(line).append("\n");
//		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((header == null) ? 0 : header.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		return result;
	}
	
	private boolean equalsBasic(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NamedProfile))
			return false;
		return true;
	}
	
	private boolean overlaps(Object obj) {
		if( ! equalsBasic(obj)) return false;
		NamedProfile other = (NamedProfile) obj;
		for (Iterator<NameValuePairProfileDecorator> iterator = members.iterator(); iterator.hasNext();) {
			NameValuePairProfileDecorator nvp = (NameValuePairProfileDecorator) iterator.next();
			if( ! other.members.contains(nvp)) return false;
			if( ! other.getMember(nvp.getType()).equals(nvp)) return false;
		}
		return true;		
	}

	@Override
	public boolean equals(Object obj) {
		if( ! equalsBasic(obj)) return false;
		NamedProfile other = (NamedProfile) obj;
		if (header == null) {
			if (other.header != null)
				return false;
		} else if (!header.equals(other.header))
			return false;		
		if (members.size() != other.members.size())
			return false;
		if ( ! overlaps(obj)) return false;
		return true;
	}
}
