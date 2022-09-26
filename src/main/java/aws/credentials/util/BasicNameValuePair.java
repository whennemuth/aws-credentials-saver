package aws.credentials.util;

/**
 * Simple class to break up two values separated by an equals sign into its trimmed name/value parts.
 * 
 * @author wrh
 *
 */
public class BasicNameValuePair implements NameValuePair {
	
	protected String rawText;
	protected String name;
	protected String value;
	protected String delimiter;

	public BasicNameValuePair(String rawText) {
		super();
		this.rawText = rawText;
		initialize();
	}
	
	public BasicNameValuePair(String rawText, String delimiter) {
		this.rawText = rawText;
		this.delimiter = delimiter;
		initialize();
	}
	
	private void initialize() {
		if(rawText.contains(getDelimeter())) {
			name = rawText.substring(0, rawText.indexOf(getDelimeter())).trim();
			if(name.isBlank()) {
				name = null;
			}
			else {
				value = rawText.substring(rawText.indexOf(getDelimeter())+1).trim();
				if(value.isBlank()) {
					value = null;
				}
			}
		}				
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean is(String name) {
		if(getName().equals(name)) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isPair() {
		if(getName() == null)
			return false;
		if(getValue() == null)
			return false;
		return true;
	}
	
	@Override
	public String getRawText() {
		return rawText;
	}

	@Override
	public String getDelimeter() {
		return delimiter == null ? defaultDelimiter : delimiter;
	}
	
	@Override
	public String asString() {
		return rawText;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NameValuePair [rawText=").append(rawText).append(", name=").append(name).append(", value=")
				.append(value).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof BasicNameValuePair))
			return false;
		BasicNameValuePair other = (BasicNameValuePair) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
