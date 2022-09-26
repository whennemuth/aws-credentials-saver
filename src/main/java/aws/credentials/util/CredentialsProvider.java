package aws.credentials.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

/**
 * An instance of this class can be used as a credentials provider for awssdk methods.
 * A set of raw credentials are provided as a NamedProfile and built into an AwsCredentials method to be returned by the providers resolveCredentials method.
 * Also, a method is provided to determine if the credentials work, using the sts getCallerIdentity method to get the account Id.
 * 
 * @author wrh
 *
 */
public class CredentialsProvider implements AwsCredentialsProvider {

	public static final Region DEFAULT_REGION = Region.US_EAST_1;
	private String keyId;
	private String secretKey;
	private String sessionToken;
	private String region;
	private String output;
	private String accountId;
	
	private CredentialsProvider() {
		super();
	}
	
	private boolean isTemporaryCredentials() {
		return sessionToken != null;
	}

	@Override
	public AwsCredentials resolveCredentials() {
		if(isTemporaryCredentials()) {
			return AwsSessionCredentials.create(keyId, secretKey, sessionToken);
		}
		else {
			return AwsBasicCredentials.create(keyId, secretKey);
		}
	}

	public Region getRegion() {
		if(region == null)
			return DEFAULT_REGION;
		return Region.of(region);
	}
	public CredentialsProvider setRegion(String region) {
		this.region = region;
		return this;
	}	
	public String getOutput() {
		return output;
	} 
	public String getKeyId() {
		return keyId;
	}
	public CredentialsProvider setKeyId(String keyId) {
		this.keyId = keyId;
		return this;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public CredentialsProvider setSecretKey(String secretKey) {
		this.secretKey = secretKey;
		return this;
	}
	public String getSessionToken() {
		return sessionToken;
	}
	public CredentialsProvider setSessionToken(String sessionToken) {
		this.sessionToken = sessionToken;
		return this;
	}
	public CredentialsProvider setOutput(String output) {
		this.output = output;
		return this;
	}

	public String getAccountId() {
		if(this.accountId == null) {			
			try {
				StsClient client = StsClient.builder()
						.region(getRegion())
						.credentialsProvider(this)
						.httpClient(ApacheHttpClient.builder().build())
						.build();
				
				GetCallerIdentityResponse response = client.getCallerIdentity();			
				accountId = response.account();
			}
			catch (Exception e) {
				e.printStackTrace();
			}			
		}
		return this.accountId;
	}
	
	public static CredentialsProvider getInstance() {
		return new CredentialsProvider();
	}

	/**
	 * Cheap test harness.
	 * @param args
	 */
	public static void main(String[] args) {
		Path home = Paths.get(System.getProperty("user.home"));
		
		Path credfilePath = home.resolve("Downloads/credentials");
		if(Files.isRegularFile(credfilePath)) {
			System.out.println("Found credentials file: " + credfilePath);
			CredentialsProvider provider = CredentialsProvider.getInstance()
					.setRegion(args[0])
					.setKeyId(args[1])
					.setSecretKey(args[2])
					.setSessionToken(args[3]);

			System.out.println("AccountId: " + String.valueOf(provider.getAccountId()));
		}
		else {
			System.out.println("Could not find credentials file: " + credfilePath);
		}
	}
}
