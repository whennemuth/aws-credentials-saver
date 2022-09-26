package aws.credentials;


import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import aws.credentials.util.Utils;

/**
 * Subclasses can retrieve mock data from the classpath based on naming convention 
 * and invoke additional custom setup functionality through implementation of an abstract method.
 * @author wrh
 *
 */
public abstract class AbstractMappedContentTest {

	// To Use TestInfo for parameterized tests see: https://www.baeldung.com/junit-get-name-of-currently-executing-test
	TestInfo testInfo;
	Map<String, String> rawText = new TreeMap<String, String>();
	static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz"; 
	static final String PKG = "mock-content";

	public AbstractMappedContentTest() {
		super();
	}
	
	protected abstract void setupMore();

	public static String getClassPathResourceContent(String path) {
		String content = Utils.getClassPathResourceContent(path);
		if(content != null) {
			content = content.replaceAll("[\\r\\n]+", "\n").trim();
		}
		return content;
	}
	
	/**
	 * Facilitate a test content retrieval that relies on a certain file naming convention:
	 * For a given @Test annotated test method B in test class A, look for files on the classpath:
	 * wrh/[name of A]+[name of B]+[a-z].txt.
	 * If any are found, make their content available to the test in the variable rawtext.
	 * 
	 * @param testInfo
	 * @throws Exception
	 */
	@BeforeEach
	protected void setUp(TestInfo testInfo) throws Exception {
		this.testInfo = testInfo;
		String suffix = "";
		int index = 0;
		while(true) {
			try {
				suffix = ALPHABET.substring(index, index+1);
				String TestClassName = testInfo.getTestClass().get().getSimpleName().replaceFirst("(?i)test$", "");
				String TestMethodName = testInfo.getTestMethod().get().getName();
				
				String text = getClassPathResourceContent(String.format("%s/%s%s%s.txt", PKG, TestClassName, TestMethodName, suffix));
				rawText.put(suffix, text);
				if(text == null) {
					// Allow for the first test content file not having a suffix of "a" if it is the only one for the corresponding method.
					if("a".equalsIgnoreCase(suffix)) {
						text = getClassPathResourceContent(String.format("%s/%s%s.txt", PKG, TestClassName, TestMethodName));
						if(text == null) {
							break;
						}
					}
					break;
				}				
			}
			finally {
				index += 1;
			}

		}
				
		setupMore();
	}

	protected String getText(String suffix) {
		return rawText.get(suffix);
	}
}