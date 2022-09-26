package aws.credentials;

import java.nio.file.FileSystems;

import aws.credentials.file.CredentialsFileEventHandler;
import aws.credentials.file.FileEventHandler;
import aws.credentials.file.FileWatcher;
import aws.credentials.profile.NamedProfileMapper;

/**
 * This is the main method for this application. Provided are three args:
 *   1) The path of the properties file that maps aws account numbers to profile names.
 *   2) The path of the credentials file whose content changes from downloads.
 *   3) [Optional] The path of the credentials file that contains the named profiles to be modified by the new downloaded content.
 *      Defaults to ~/.aws/credentials
 * Execution should be continuous and only exit in the event of an exception, or the process thread is terminated manually.
 * @author wrh
 *
 */
public class Main {

	static FileWatcher watcher;
	static Thread watcherThread;
	static FileEventHandler handler;
	
	public static void main(String[] argArray) {

		Args args = new Args(argArray, FileSystems.getDefault());
		
		if( ! args.isValid()) {
			args.printUsage();
			return;
		}
				
		try {
			handler = new CredentialsFileEventHandler(
					FileSystems.getDefault(), 
					args.getTargetCredsPath(), 
					new NamedProfileMapper(args.getPropertiesPath()));
			
			watcher = new FileWatcher(
					args.getSourceCredsPath(), 
					handler, 
					FileSystems.getDefault());
			
			watcherThread = new Thread(watcher);
			
			watcherThread.start();				
		} 
		catch (Exception e) {
			e.printStackTrace();
			if(watcher != null) {
				watcher.setStop();
			}
		}
	}

}
