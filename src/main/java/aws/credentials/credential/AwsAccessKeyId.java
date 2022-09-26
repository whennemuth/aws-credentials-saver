package aws.credentials.credential;

import aws.credentials.profile.NameValuePairProfileDecorator;
import aws.credentials.profile.NamedProfileElement;
import aws.credentials.util.BasicNameValuePair;

public class AwsAccessKeyId extends NameValuePairProfileDecorator {
	
	public AwsAccessKeyId(String rawText) {
		super(new BasicNameValuePair(rawText));
	}

	@Override
	public boolean isRecognized() {
		return getType().isRecognized(getName());
	}

	@Override
	public NamedProfileElement getType() {
		return NamedProfileElement.AWS_ACCESS_KEY_ID;
	}

}
