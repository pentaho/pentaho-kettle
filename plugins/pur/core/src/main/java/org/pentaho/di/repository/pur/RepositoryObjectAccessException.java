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
package org.pentaho.di.repository.pur;

public class RepositoryObjectAccessException extends Exception implements java.io.Serializable {

  private static final long serialVersionUID = -3339087102211752867L; /* EESOURCE: UPDATE SERIALVERUID */

  public enum AccessExceptionType {
    USER_HOME_DIR
  }

  private AccessExceptionType type;

  public RepositoryObjectAccessException( String message, AccessExceptionType type ) {
    this.type = type;
  }

  public AccessExceptionType getObjectAccessType() {
    return type;
  }

  public void setObjectAccessType( AccessExceptionType type ) {
    this.type = type;
  }

}
