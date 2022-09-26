package aws.credentials.credential;

import aws.credentials.profile.NameValuePairProfileDecorator;
import aws.credentials.profile.NamedProfileElement;
import aws.credentials.util.BasicNameValuePair;

public class AwsSecretAccessKey extends NameValuePairProfileDecorator {
	
	public AwsSecretAccessKey(String rawText) {
		super(new BasicNameValuePair(rawText));
	}

	@Override
	public boolean isRecognized() {
		return getType().isRecognized(getName());
	}

	@Override
	public NamedProfileElement getType() {
		return NamedProfileElement.AWS_SECRET_ACCESS_KEY;
	}

}
