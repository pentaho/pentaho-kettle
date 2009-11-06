package org.pentaho.di.core.vfs;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import junit.framework.TestCase;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;

public class VfsCoreTest extends TestCase {

	private static final String tmpDir = System.getProperty("java.io.tmpdir");
	private static final String content = "Just a small line of text\n";

	public void testWriteReadFile() throws Exception {
		// Write a text
		//
		FileObject tempFile = KettleVFS.createTempFile("prefix", "suffix", tmpDir);
		OutputStream outputStream = KettleVFS.getOutputStream(tempFile, false);
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		writer.write(content);
		writer.close();
		outputStream.close();
		
		// Read it back...
		//
		InputStream inputStream = KettleVFS.getInputStream(tempFile);
		StringBuffer buffer= new StringBuffer();
		int c;
		while ( (c=inputStream.read())>=0 ) {
			buffer.append((char)c);
		}
		inputStream.close();
		
		assertEquals(content, buffer.toString());
		
		// Now open the data as a regular file...
		//
		String url = tempFile.getName().getURI();
		String textFileContent = KettleVFS.getTextFileContent(url, Const.XML_ENCODING);
		
		// Now delete the file...
		//
		tempFile.delete();
		
		assertEquals(false, tempFile.exists());
		
		assertEquals(content, textFileContent);
	}
}
