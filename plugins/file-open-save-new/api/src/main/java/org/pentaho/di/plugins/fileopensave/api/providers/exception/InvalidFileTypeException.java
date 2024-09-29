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


package org.pentaho.di.plugins.fileopensave.api.providers.exception;

public class InvalidFileTypeException extends FileException {
  public InvalidFileTypeException() {
    super();
  }

  public InvalidFileTypeException( Throwable cause ) {
    super( cause );
  }

  public InvalidFileTypeException( String message ) {
    super( message );
  }

  public InvalidFileTypeException( String message, Throwable cause ) {
    super( message, cause );
  }

}
