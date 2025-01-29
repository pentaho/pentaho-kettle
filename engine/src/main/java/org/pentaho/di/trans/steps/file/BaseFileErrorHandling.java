/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.file;

import org.pentaho.di.core.injection.Injection;

/**
 * Block for error handling settings.
 */
public class BaseFileErrorHandling implements Cloneable {

  /** Ignore error : turn into warnings */
  @Injection( name = "IGNORE_ERRORS" )
  public boolean errorIgnored;

  /** File error field name. */
  @Injection( name = "FILE_ERROR_FIELD" )
  public String fileErrorField;

  /** File error text field name. */
  @Injection( name = "FILE_ERROR_MESSAGE_FIELD" )
  public String fileErrorMessageField;

  @Injection( name = "SKIP_BAD_FILES" )
  public boolean skipBadFiles;

  /** The directory that will contain warning files */
  @Injection( name = "WARNING_FILES_TARGET_DIR" )
  public String warningFilesDestinationDirectory;

  /** The extension of warning files */
  @Injection( name = "WARNING_FILES_EXTENTION" )
  public String warningFilesExtension;

  /** The directory that will contain error files */
  @Injection( name = "ERROR_FILES_TARGET_DIR" )
  public String errorFilesDestinationDirectory;

  /** The extension of error files */
  @Injection( name = "ERROR_FILES_EXTENTION" )
  public String errorFilesExtension;

  /** The directory that will contain line number files */
  @Injection( name = "LINE_NR_FILES_TARGET_DIR" )
  public String lineNumberFilesDestinationDirectory;

  /** The extension of line number files */
  @Injection( name = "LINE_NR_FILES_EXTENTION" )
  public String lineNumberFilesExtension;

  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch ( CloneNotSupportedException ex ) {
      throw new IllegalArgumentException( "Clone not supported for " + this.getClass().getName() );
    }
  }
}
