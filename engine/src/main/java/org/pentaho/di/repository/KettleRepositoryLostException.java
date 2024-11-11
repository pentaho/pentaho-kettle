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


package org.pentaho.di.repository;

import org.pentaho.di.i18n.BaseMessages;

@SuppressWarnings( "serial" )
public class KettleRepositoryLostException extends RuntimeException {

  private static Class<?> PKG = KettleRepositoryLostException.class;
  private static final String MSG = BaseMessages.getString( PKG, "Repository.Lost.Error.Message" );
  private static final String PREFACE = BaseMessages.getString( PKG, "Repository.Lost.Error.Preface" );

  public KettleRepositoryLostException() {
    super( MSG );
  }

  public KettleRepositoryLostException( String message ) {
    super( message );
  }

  public KettleRepositoryLostException( Throwable cause ) {
    super( MSG, cause );
  }

  public KettleRepositoryLostException( String message, Throwable cause ) {
    super( message, cause );
  }

  public static KettleRepositoryLostException lookupStackStrace( Throwable root ) {
    while ( root != null ) {
      if ( root instanceof KettleRepositoryLostException ) {
        return (KettleRepositoryLostException) root;
      } else {
        root = root.getCause();
      }
    }

    return null;
  }

  /*
   * According to UX the verbiage to be displayed to user
   * should consist of 2 parts:
   * the first one is in Exception message
   * the second one is in the Preface.
   */
  public String getPrefaceMessage() {
    return PREFACE;
  }
}
