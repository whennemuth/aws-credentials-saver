package aws.credentials.profile;

import aws.credentials.util.BasicNameValuePair;
import aws.credentials.util.NameValuePair;

/**
 * Simple class to break up two values separated by an equals sign into its trimmed name/value parts.
 * Has decorating functionality specific to what part of a named profile the pair represents is implemented by subclasses.
 * 
 * @author wrh
 *
 */
public abstract class NameValuePairProfileDecorator implements NameValuePair {
	
	protected NameValuePair nvp;
	
	public NameValuePairProfileDecorator(NameValuePair nvp) {
		this.nvp = nvp;
	}
	
	public abstract boolean isRecognized();
	
	public abstract NamedProfileElement getType();
	
	@Override
	public String getName() {
		return nvp.getName();
	}
	
	@Override
	public String getValue() {
		return nvp.getValue();
	}
	
	@Override
	public boolean is(String name) {
		if(isValid()) {
			if(getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isPair() {
		return nvp.isPair();
	}

	@Override
	public String getRawText() {
		return nvp.getRawText();
	}

	@Override
	public String getDelimeter() {
		return nvp.getDelimeter();
	}
	
	public boolean isValid() {
		if(getValue() == null || getValue().isBlank()) return false;
		return isRecognized();
	}

	public boolean invalid() {
		return ! isValid();
	}
	
	@Override
	public String asString() {
		return nvp.asString();
	}

	@Override
	public String toString() {
		return nvp.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nvp == null) ? 0 : nvp.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NameValuePairProfileDecorator))
			return false;
		NameValuePairProfileDecorator other = (NameValuePairProfileDecorator) obj;
		if (nvp == null) {
			if (other.nvp != null)
				return false;
		} else if (!nvp.equals(other.nvp))
			return false;
		
		if (getType() == null) {
			if (other.getType() != null)
				return false;
		} else if (!getType().equals(other.getType()))
			return false;
		
		return true;
	}

	public static NameValuePairProfileDecorator getBasicInstance(String raw) {
		return new NameValuePairProfileDecorator(new BasicNameValuePair(raw)) {
			@Override public boolean isRecognized() {
				return false;
			}
			@Override public NamedProfileElement getType() {
 				return null;
			}};
	}
}
