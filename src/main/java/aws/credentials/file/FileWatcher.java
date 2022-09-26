package aws.credentials.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import aws.credentials.util.Utils;

/**
 * An object that waits for creation of or modification to a specified file to occur.
 * A provided event handler is invoked when this happens.
 * 
 * @author wrh
 *
 */
public class FileWatcher implements Runnable {
	
	private final WatchService watcher;
	private final Path watchable;
	private final Path watchableDir;
	private final FileEventHandler handler;
	private WatchKey initialKey;
	private boolean stop;
	
	/**
	 * Instantiate FileWatcher, having the directory of a watchable file in the default file system registered with a WatchService.
	 * @param watchable
	 * @param handler
	 * @throws IOException
	 */
	public FileWatcher(Path watchable, FileEventHandler handler) throws IOException {
		this(watchable, handler, FileSystems.getDefault());
	}

	/**
	 * Instantiate FileWatcher, having the directory of a watchable file in the specified file system registered with a WatchService.
	 * @param watchable
	 * @param handler
	 * @param fs
	 * @throws IOException
	 */
	public FileWatcher(Path watchable, FileEventHandler handler, FileSystem fs) throws IOException {
		this.watchable = watchable;
		this.watcher = fs.newWatchService();
		this.watchableDir = watchable.getParent();
		this.handler = handler;
		if(Files.isDirectory(watchableDir)) {
			initialKey = watchableDir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
		}
		else {
			throw new IOException(String.format("Path \"%s\" is invalid", watchable));
		}
	}

	public void setStop() {
		this.stop = true;
	}
	
	/**
	 * Identify what kind of event occurred and handle it accordingly.
	 * @param handler
	 * @param key
	 * @throws IOException 
	 */
	private void handleEvent(WatchKey key) throws IOException {
        for (WatchEvent<?> event : key.pollEvents()) {
        	if (event.kind() == OVERFLOW) {
        		System.out.println("Overflow event occurred. Unlikely - how did this happen?");
                continue;
            }
        	
        	if(event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
        		Path relative = (Path) event.context();
        		Path file = watchableDir.resolve(relative);
        		if(file.equals(watchable)) {
	        		System.out.format("%s: %s: %s\n", Utils.getTimestamp(), event.kind().name(), file);
	        		if(event.kind() == ENTRY_CREATE) {
	        			handler.onCreate(file);
	        		}
	        		else {
	        			handler.onUpdate(file);
	        		}        			
        		}
        		else {
        			// System.out.format("\"%s\" is not being watched, skipping.\n", file);
        			continue;
        		}
        	}
        }
	}

	/**
	 * Start watching the file for events, invoking the supplied handler when they occur.
	 * @param handler
	 * @throws IOException
	 */
	private void startWatching() {
		for (;;) {
			if(stop) {
				System.out.println("A stop has been set. Stopping watcher...");
				break;
			}
			
            // Wait for key to be signalled (the take method does the waiting).
            WatchKey key;
            try {
                key = watcher.take();
                // System.out.format("Event for \"%s\" detected.\n", watchable);
            } 
            catch (InterruptedException x) {
            	x.printStackTrace(System.err);
                break;
            }
			
            if( ! key.equals(initialKey)) {
            	System.err.println("WatchKey not recognized! Stopping watcher...");
            	break;
            }
            
            try {
				handleEvent(key);
			} 
            catch (Exception e) {
				e.printStackTrace();
				setStop();
				continue;
			}
            
            if( ! key.reset()) {
            	System.out.format("The path: \"%s\" is no longer valid. Stopping watcher...\n", watchable);
            	break;
            }
		}
		System.out.println("Watcher stopped.");
	}

	@Override
	public void run() {
		System.out.println(String.format("Watcher has started. Watching %s...", watchable));
		startWatching();		
	}

}
