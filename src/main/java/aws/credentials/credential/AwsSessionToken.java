package aws.credentials.credential;

import aws.credentials.profile.NameValuePairProfileDecorator;
import aws.credentials.profile.NamedProfileElement;
import aws.credentials.util.BasicNameValuePair;

public class AwsSessionToken extends NameValuePairProfileDecorator {
	
	public AwsSessionToken(String rawText) {
		super(new BasicNameValuePair(rawText));
	}

	@Override
	public boolean isRecognized() {
		return getType().isRecognized(getName());
	}

	@Override
	public NamedProfileElement getType() {
		return NamedProfileElement.AWS_SESSION_TOKEN;
	}

}
