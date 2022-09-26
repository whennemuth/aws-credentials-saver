package aws.credentials.util;

public interface NameValuePair {

	String getName();

	String getValue();

	boolean is(String name);
	
	boolean isPair();

	String getRawText();

	String asString();

	String getDelimeter();
	
	final String defaultDelimiter = "=";

}