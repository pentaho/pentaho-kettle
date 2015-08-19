package org.pentaho.di.trans.steps.newfileinput;

import org.apache.commons.vfs2.FileObject;

public class NewFileLine {
  String line;

  long lineNumber;

  FileObject file;

  public NewFileLine( String line, long lineNumber, FileObject file ) {
    super();
    this.line = line;
    this.lineNumber = lineNumber;
    this.file = file;
  }
}
