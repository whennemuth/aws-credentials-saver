package aws.credentials.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Watchable;

import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import aws.credentials.util.Utils;

/**
 * Simple file watcher test case. Tests watching of file creation and modification events.
 * 
 * @author wrh
 *
 */
class FileWatcherTest {
	
	FileSystem fs;
	Path watchable;
	FileWatcher watcher;
	Thread watcherThread;
	String lastMessage;
	
	private void startWatcher(String filepath, FileEventHandler handler) {
		Configuration config = Configuration.unix().toBuilder()
			       .setAttributeViews("basic", "owner", "posix", "unix")
			       .setWorkingDirectory("/")
			       .setRoots("/")
			       .build();
				
		fs = Jimfs.newFileSystem(config);
		
		try {
			Files.createDirectories(fs.getPath(filepath).getParent());
			watchable = fs.getPath(filepath);
			watcher = new FileWatcher(watchable, handler, fs);
		} 
		catch (IOException e) {
			fail("Not expecting exception while creating FileWatcher: ", e);
		}
		
		try {
			watcherThread = new Thread(watcher);
			watcherThread.start();
		}
		catch (Exception e) {
			fail("Not expecting exception while starting FileWatcher: ", e);
		}		
	}
	
	private void stopWatcher() {
		if(watcherThread != null) {
			watcher.setStop();
		}
	}

	private FileEventHandler getEventHandler() {
		return new FileEventHandler() {
			@Override public void onUpdate(Watchable watchable) {
				System.out.println("Update handled.");
				lastMessage = "updated";
			};
			@Override public void onCreate(Watchable watchable) {
				System.out.println("Create handled.");
				lastMessage = "created";
			}
			@Override
			public void onDelete(Watchable watchable) {
				System.out.println("Delete handled.");
				lastMessage = "deleted";
			};
		};
	}
	
	/**
	 * Before the result of an event can be asserted, it is necessary to wait for it to happen.
	 * This function will wait up to a specified timeout period.
	 * @param timeoutMils
	 * @return
	 */
	private boolean isEventHandledBeforeTimeout(final int timeoutMils) {
		int elapsed = 0;
		int interval = 500;
		for(;;) {
			if(elapsed >= timeoutMils) {
				return false;
			}
			try {
				Thread.sleep(interval);
				elapsed += interval;
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			if(lastMessage != null) {
				System.out.format("Assertable in %s milliseconds\n", String.valueOf(elapsed));
				return true;				
			}
		}
	}
	
	/**
	 * Assert that the variable set by a triggered event handler has the expected value (indicates expected event type).
	 * @param expected
	 */
	private void assertEventResult(String expected) {
		final int timeout = 10000; // 10 seconds
		if(isEventHandledBeforeTimeout(timeout)) {
			assertEquals(expected, lastMessage);
			lastMessage = null;
		}
		else {
			fail(String.format("No event was triggered before %s millisecond timeout", String.valueOf(timeout)));
		}		
	}

	/**
	 * Watch a mock file and assert that its creation and modification each triggers the appropriate event.
	 */
	@Test
	void test() {
		
		try {			
			startWatcher("/tmp/newCredentials", getEventHandler());
			assertNull(lastMessage);
			
			// Create the file.
			Utils.writeStringToFile("Apples\nOranges\nPears", watchable);
			assertEventResult("created");
			
			// Modify the file.
			Utils.writeStringToFile("Pears\nApples\nOranges", watchable);
			assertEventResult("updated");
			
		} 
		catch(Exception e) { 
			fail(e);
		}
		finally {
			stopWatcher();
		}
	}

}
