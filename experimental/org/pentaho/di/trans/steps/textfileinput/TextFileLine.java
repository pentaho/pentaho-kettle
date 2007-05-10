package org.pentaho.di.trans.steps.textfileinput;

import org.apache.commons.vfs.FileObject;

public class TextFileLine {
	String line;

	long lineNumber;

	FileObject file;

	public TextFileLine(String line, long lineNumber, FileObject file) {
		super();
		this.line = line;
		this.lineNumber = lineNumber;
		this.file = file;
	}
}
