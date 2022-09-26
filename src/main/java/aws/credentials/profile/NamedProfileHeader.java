package aws.credentials.profile;

/**
 * This class represents the bracketed name for a named profile.
 * Examples: "[profile myprofile]", "[default]"
 * 
 * @author wrh
 *
 */
public class NamedProfileHeader {

	private String header;
	private String name;
	private boolean _default;
	
	public NamedProfileHeader(String header) {
		if(header != null) {
			this.header = header.trim();
			if(this.header.matches("^\\[.*\\]$")) {
				name = this.header.substring(1, this.header.length()-1);
				if("default".equals(name)) {
					name = null;
					_default = true;
				}
				else {
					String[] parts = name.split("\\s+");
					if(parts.length > 1) {
						name = parts[1];
					}				
				}				
			}
		}
	}
	
	public boolean isHeader() {
		return isDefault() || header.matches("(?i)^\\[(profile\\x20+)?[\\w\\.\\-]+\\]$");
	}
	
	public String getProfileName() {
		return name;
	}

	public boolean isDefault() {
		return _default;
	}

	public static boolean isDefault(String profileName) {
		return new NamedProfileHeader(profileName).isDefault();
	}
	
	

	@Override
	public String toString() {
		return header;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_default ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NamedProfileHeader))
			return false;
		NamedProfileHeader other = (NamedProfileHeader) obj;
		if (_default != other._default)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
}
