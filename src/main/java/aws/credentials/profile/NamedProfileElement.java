package aws.credentials.profile;

import aws.credentials.credential.AwsAccessKeyId;
import aws.credentials.credential.AwsSecretAccessKey;
import aws.credentials.credential.AwsSessionToken;
import aws.credentials.credential.Output;
import aws.credentials.credential.Region;

public enum NamedProfileElement {
	AWS_ACCESS_KEY_ID("id", AwsAccessKeyId.class),
	AWS_SECRET_ACCESS_KEY("key", AwsSecretAccessKey.class),
	AWS_SESSION_TOKEN("token", AwsSessionToken.class),
	REGION("region", Region.class),
	OUTPUT("output", Output.class);
	
	private String shortName;
	private Class <? extends NameValuePairProfileDecorator> clazz;
	
	private NamedProfileElement(String shortName, Class <? extends NameValuePairProfileDecorator> clazz) {
		this.shortName = shortName;
		this.clazz = clazz;
	}
	
	public boolean isRecognized(String s) {
		return this.name().equalsIgnoreCase(s);
	}
	
	public static NamedProfileElement fromShortName(String shortName) {
		for(NamedProfileElement e : NamedProfileElement.values()) {
			if(e.shortName.equalsIgnoreCase(shortName)) {
				return e;
			}
		}
		return null;
	}

	public Class <? extends NameValuePairProfileDecorator> getClazz() {
		return clazz;
	}
	
	public NameValuePairProfileDecorator instanceOf(String rawText) {
		try {
			if(rawText != null && rawText.isBlank() == false) {
				return (NameValuePairProfileDecorator) getClazz().getConstructor(new Class[]{String.class}).newInstance(rawText);
			}
		} 
		catch (Exception e) {
			return null;
		}
		return null;
	}
	
	public static NameValuePairProfileDecorator getInstance(String shortname, String rawText) {
		return NamedProfileElement.fromShortName(shortname).instanceOf(rawText);
	}
}
