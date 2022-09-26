package aws.credentials.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {

	public static String stackTraceToString(Throwable e) {
		if(e == null)
			return null;
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		pw.flush();
		String trace = sw.getBuffer().toString();
		return trace;
	}
	
	public static boolean isEmpty(String val) {
		return (val == null || val.trim().length() == 0);
	}
	
	public static boolean isEmpty(Object val) {
		try {
			return (val == null || val.toString().length() == 0);
		} catch (Exception e) {
			return String.valueOf(val).length() == 0;
		}
	}
	
	public static boolean anyEmpty(String... vals) {
		for(int i=0; i<vals.length; i++) {
			if(isEmpty(vals[i])) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isNumeric(String val) {
		if(isEmpty(val))
			return false;
		return val.matches("\\d+");
	}
	
	public static String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss aaa");
		return sdf.format(new Date());
	}

	
	/**
	 * Get the directory containing the jar file whose code is currently running.
	 * 
	 * @return
	 * @throws Exception
	 */
	public static File getRootDirectory() throws Exception {
		String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		File f = new File(decodedPath);
		if(f.isFile() && f.getName().endsWith(".jar")) {
			return f.getParentFile();
		}
		return f;
    }
	
	/**
	 * Get the content of a file as a string.
	 * @param in
	 * @return
	 */
	public static String getStringFromInputStream(InputStream in) {
		if(in == null) {
			return null;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));			
			String inputLine;
			StringWriter sb = new StringWriter();
			PrintWriter pw = new PrintWriter(new BufferedWriter(sb));
			while ((inputLine = br.readLine()) != null) {
				pw.println(inputLine);
			}
			pw.flush();
			return sb.toString();
		} 
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		finally {
			if(br != null) {
				try {
					br.close();
				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	public static List<String> readLines(String rawText) {
		if(rawText != null) {
			return readLines(new ByteArrayInputStream(rawText.getBytes()));
		}
		return new ArrayList<String>();
	}
	
	public static List<String> readLines(Path p) {
		if(p != null && Files.isRegularFile(p)) {
			try {
				return readLines(Files.newInputStream(p));
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new ArrayList<String>();
	}
	
	public static List<String> readLines(InputStream in) {
		List<String> lines = new ArrayList<String>();
		if(in != null) {
			BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(in));
				String line = null;							
				while ((line = br.readLine()) != null) {
					line = line.trim();
					if( ! line.isEmpty()) {
						lines.add(line.trim());
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if(br != null) {
					try {
						br.close();
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}							
		}
		return lines;
	}
	
	public static void readWriteLines(InputStream in, OutputStream out) {
		if(in != null && out != null) {
			BufferedReader br = null;
			PrintWriter writer = new PrintWriter(new BufferedOutputStream(out));
			try {
				br = new BufferedReader(new InputStreamReader(in));
				String line = null;							
				while ((line = br.readLine()) != null) {
					writer.println(line);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if(br != null) {
					try {
						br.close();
						writer.flush();
						writer.close();
					} 
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}							
		}
	}
	
	public static File getClassPathResource(String resource) {
		URL url = Utils.class.getClassLoader().getResource(resource);
		if(url == null) {
			return null;
		}
		return new File(url.getFile());
	}

	public static InputStream getClassPathResourceInputStream(String resource) {
		File r = getClassPathResource(resource);
		if(r != null && r.isFile()) {
			return Utils.class.getClassLoader().getResourceAsStream(resource);
		}
		return null;
	}
	
	public static void writeInputStreamToFile(InputStream in, Path path) {
		try {
			OutputStream out = Files.newOutputStream(path);
			readWriteLines(in, out);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeClassPathResourceToFile(String resource, Path path) {
		writeInputStreamToFile(getClassPathResourceInputStream(resource), path);
	}
	
	public static void writeStringToFile(String s, Path path) {
		writeInputStreamToFile(new ByteArrayInputStream(s.getBytes()), path);
	}
	
	public static String getClassPathResourceContent(String resource) {
		return getStringFromInputStream(getClassPathResourceInputStream(resource));
	}
	
	public static boolean trimIgnoreCaseEqual(String s1, String s2) {
		if(s1 == null || s2 == null)
			return false;
		return s1.trim().equalsIgnoreCase(s2.trim());
	}
	
	
	public static boolean trimIgnoreCaseUnemptyEqual(String s1, String s2) {
		if(s1 == null || s2 == null)
			return false;
		if(s1.trim().isEmpty())
			return false;
		if(s2.trim().isEmpty())
			return false;
		return s1.trim().equalsIgnoreCase(s2.trim());
	}
}
