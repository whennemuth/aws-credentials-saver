package aws.credentials.file;

import java.io.IOException;
import java.nio.file.Watchable;

public interface FileEventHandler {

	void onUpdate(Watchable watchable) throws IOException;

	void onCreate(Watchable watchable) throws IOException;

	void onDelete(Watchable watchable);
}
