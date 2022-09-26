package aws.credentials.util;

/**
 * A decorator for a NameValuePair instance that does not exclude equality between the name portion of the pair and some other value based on case.
 * Affects the "is" and "equals" methods.
 * 
 * @author wrh
 *
 */
public class NameValuePairCaselessDecorator implements NameValuePair {

	private NameValuePair nvp;
	
	public NameValuePairCaselessDecorator(NameValuePair nvp) {
		this.nvp = nvp;
	}

	@Override
	public boolean is(String name) {
		if(getName().equalsIgnoreCase(name)) {
			return true;
		}
		return false;
	}
	
	@Override
	public String getName() {
		return nvp.getName();
	}
	@Override
	public String getValue() {
		return nvp.getValue();
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
	public String asString() {
		return nvp.asString();
	}
	@Override
	public String getDelimeter() {
		return nvp.getDelimeter();
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
		if (!(obj instanceof NameValuePairCaselessDecorator))
			return false;
		NameValuePairCaselessDecorator other = (NameValuePairCaselessDecorator) obj;
		if (nvp.getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equalsIgnoreCase(other.getName()))
			return false;
		if (getValue() == null) {
			if (other.getValue() != null)
				return false;
		} else if (!getValue().equals(other.getValue()))
			return false;
		return true;
	}

	
}
