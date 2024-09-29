/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.textfileinput;

import org.apache.commons.vfs2.FileObject;

/**
 * @deprecated replaced by implementation in the ...steps.fileinput.text package
 */
@Deprecated
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
}
