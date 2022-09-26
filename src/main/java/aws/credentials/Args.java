package aws.credentials;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import aws.credentials.util.BasicNameValuePair;
import aws.credentials.util.NameValuePair;
import aws.credentials.util.NameValuePairCaselessDecorator;

/**
 * Class for an creating an object that represents the parameters that are passed into the main method as a string array of name/value pairs.
 * These name/value pairs become properties of this object and are validated (missing or bad paths).
 * 
 * @author wrh
 *
 */
class Args {
	private Path sourceCredsPath;
	private Path targetCredsPath;
	private Path propertiesPath;
	private StringBuilder msg = new StringBuilder();
	
	public Args(String[] args, FileSystem fs) {
		for(String arg : args) {
			NameValuePair nvp = new NameValuePairCaselessDecorator(new BasicNameValuePair(arg));
			if(nvp.isPair()) {
				if(nvp.is("source")) {
					this.sourceCredsPath = fs.getPath(nvp.getValue());
				}
				else if(nvp.is("target")) {
					this.targetCredsPath = fs.getPath(nvp.getValue());
				}
				else if(nvp.is("map")) {
					this.propertiesPath = fs.getPath(nvp.getValue());
				}
			}
		}
	}
	
	public Path getSourceCredsPath() {
		return sourceCredsPath;
	}
	public Path getTargetCredsPath() {
		if(targetCredsPath == null) {
			Path home = Paths.get(System.getProperty("user.home"));			
			return home.resolve(".aws/credentials");				
		}
		return targetCredsPath;
	}
	public Path getPropertiesPath() {
		return propertiesPath;
	}
	private boolean isValidPath(Path p) {
		if(p == null)
			return false;
		if(p.getParent() == null)
			return false;
		if( ! Files.isDirectory(p.getParent()))
			return false;
		return true;
	}
	private boolean isNullOrValidPath(Path p) {
		return p == null || isValidPath(p);
	}
	private boolean isNullOrInvalidFile(Path p) {
		return p == null || Files.isRegularFile(p) == false;
	}
	public boolean isValid() {
		if( ! isNullOrValidPath(sourceCredsPath))
			msg.append("  - \"").append(String.valueOf(sourceCredsPath)).append("\" is an invalid source credentials path\n");
		if( ! isNullOrValidPath(getTargetCredsPath()))
			msg.append("  - \"").append(String.valueOf(getTargetCredsPath())).append("\" is an invalid target credentials path\n");
		if(isNullOrInvalidFile(propertiesPath))
			msg.append("  - \"").append(String.valueOf(propertiesPath)).append("\" is an invalid map file\n");
		return msg.length() == 0;
	}
	public void printUsage() {
		System.out.println("Invalid/Missing args:\n"
				+ msg.toString() + ""
				+ "USAGE:\n"
				+ "java -jar AwsCredentials.jar \\\n"
				+ "   source=path/to/the/source/credentials/file \\\n"
				+ "   target=path/to/the/target/credentials/file \\\n"
				+ "   map=path/to/the/aws_account_number/to/profile_name/mapping/properties/file \n"
				+ "[Note: target is optional and will default to ~/.aws/credentials\n");
	}
}