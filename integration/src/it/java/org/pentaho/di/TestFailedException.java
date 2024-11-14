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


package org.pentaho.di;

public class TestFailedException extends Exception {

  private static final long serialVersionUID = 8585395841938180974L;

  TestFailedException( String message ) {
    super( message );
  }
}
