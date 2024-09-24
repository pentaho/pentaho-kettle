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

package org.pentaho.di.engine.api;

public class RowException extends Exception {

  private static final long serialVersionUID = -7989905162629837994L;

  public RowException( String msg ) {
    super( msg );
  }

  public RowException( String msg, Throwable inner ) {
    super( msg, inner );
  }

}
