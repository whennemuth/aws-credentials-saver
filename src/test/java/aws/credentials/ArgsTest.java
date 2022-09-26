package aws.credentials;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import aws.credentials.util.Utils;

/**
 * Tests for various combinations and values of args that can be passed into the main method.
 * 
 * @author wrh
 *
 */
class ArgsTest {

	Args args;
	FileSystem fs;
	static final String source_path = "Users/wrh/.aws/credentials";
	static final String target_path = "Users/wrh/Downloads/credentials";
	static final String map_path = "Users/wrh/ProfileMap.properties";
	
	@BeforeEach
	void setup() {
		try {
			System.setProperty("user.home", "/Users/wrh");
			fs = Jimfs.newFileSystem(Configuration.unix().toBuilder()
		       .setAttributeViews("basic", "owner", "posix", "unix")
		       .setWorkingDirectory("/")
		       .setRoots("/")
		       .build());
			
			Path source = fs.getPath(source_path);
			Files.createDirectories(source.getParent());
			Utils.writeStringToFile("source content", source);
			
			Path target = fs.getPath(target_path);
			Files.createDirectories(target.getParent());
			Utils.writeStringToFile("target content", target);
			
			Path map = fs.getPath(map_path);
			Files.createDirectories(map.getParent());
			Utils.writeStringToFile("map content", map);
		} 
		catch (IOException e) {
			fail("Not expecting exception during setup: ", e);
		}
	}
	
	/**
	 * All 3 paths not existing should be invalid
	 */
	@Test
	void testFail1() {
		args = new Args(new String[] {
				"source=Some/bogus/path",
				"target=Another/bogus/path",
				"map=Third/bogus/path"
		}, fs);
		assertFalse(args.isValid());
		args.printUsage();
	}
	
	/**
	 * Invalid paths for target and map should be invalid
	 */
	@Test
	void testFail2() {
		args = new Args(new String[] {
				String.format("source=%s", source_path),
				"target=Another/bogus/path",
				"map=Third/bogus/path"
		}, fs);
		assertFalse(args.isValid());
		args.printUsage();
	}
	
	/**
	 * An invalid path for map should be invalid
	 */
	@Test
	void testFail3() {
		args = new Args(new String[] {
				String.format("source=%s", source_path),
				String.format("target=%s", target_path),
				"map=Third/bogus/path"
		}, fs);
		assertFalse(args.isValid());
		args.printUsage();
	}
	
	/**
	 * A missing map arg should be invalid
	 */
	@Test
	void testFail4() {
		args = new Args(new String[] {
				String.format("source=%s", source_path),
				String.format("target=%s", target_path)
		}, fs);
		assertFalse(args.isValid());
		args.printUsage();
	}

	/**
	 * A missing target arg should be invalid if the systems "user.home" property does not match an existing directory
	 */
	@Test
	void testFail5() {
		System.setProperty("user.home", "/Bogus/directory");
		args = new Args(new String[] {
				String.format("source=%s", source_path.replace("credentials", "not-created-source-file")),
				String.format("map=%s", map_path)
		}, fs);
		assertFalse(args.isValid());		
	}
	
	/**
	 * A valid path for a 3 files should be valid
	 */
	@Test
	void testOk1() {
		args = new Args(new String[] {
				String.format("source=%s", source_path),
				String.format("target=%s", target_path),
				String.format("map=%s", map_path)
		}, fs);
		assertTrue(args.isValid());
	}
	
	/**
	 * A valid path for source and target directories with non-existent source and target files should be valid as long as the map file exists.
	 */
	@Test
	void testOk2() {
		args = new Args(new String[] {
				String.format("source=%s", source_path.replace("credentials", "not-created-source-file")),
				String.format("target=%s", target_path.replace("credentials", "not-created-target-file")),
				String.format("map=%s", map_path)
		}, fs);
		assertTrue(args.isValid());
	}

	/**
	 * A missing target arg should be valid if the systems "user.home" property refers to an existing directory.
	 */	
	@Test
	void testOk3() {
		args = new Args(new String[] {
				String.format("source=%s", source_path.replace("credentials", "not-created-source-file")),
				String.format("map=%s", map_path)
		}, fs);
		assertTrue(args.isValid());		
	}
}
