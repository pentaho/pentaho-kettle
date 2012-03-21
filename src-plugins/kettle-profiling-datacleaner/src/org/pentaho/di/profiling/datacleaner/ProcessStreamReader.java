package org.pentaho.di.profiling.datacleaner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Reads an output stream from an external process.
 * Implemented as a thread.
 */
class ProcessStreamReader extends Thread {
	StringBuffer		stream;
	InputStreamReader	in;
	
	final static int BUFFER_SIZE = 1024;
	
	/**
	 *	Creates new ProcessStreamReader object.
	 *	
	 *	@param	in
	 */
	ProcessStreamReader (InputStream in) {
		super();
		
		this.in = new InputStreamReader(in);

		this.stream = new StringBuffer();
		
		super.setName("process stream reader");
	}
	
	public void run() {
		try {       
			int read;
			char[] c = new char[BUFFER_SIZE];
			
			while ((read = in.read(c, 0, BUFFER_SIZE - 1)) > 0) {
				stream.append(c, 0, read);
				if (read < BUFFER_SIZE - 1) break;
			}
		}
		catch(IOException io) {}
	}
	
	String getString() {
			return stream.toString();
	}
	
  
}
