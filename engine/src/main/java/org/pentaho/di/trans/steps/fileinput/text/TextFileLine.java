/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.fileinput.text;

import org.apache.commons.vfs2.FileObject;

public class TextFileLine {
  String line;

  long lineNumber;

  FileObject file;

  public TextFileLine( String line, long lineNumber, FileObject file ) {
    super();
    this.line = line;
    this.lineNumber = lineNumber;
    this.file = file;
  }

  public String getLine() {
    return line;
  }

  public void setLine( String line ) {
    this.line = line;
  }

  public long getLineNumber() {
    return lineNumber;
  }

  public FileObject getFile() {
    return file;
  }

  public void setFile( FileObject file ) {
    this.file = file;
  }
}
