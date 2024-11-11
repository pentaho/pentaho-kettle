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


package org.pentaho.di.ui.repository.repositoryexplorer.model;

public class UIObjectCreationException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public UIObjectCreationException() {
    super();
  }

  public UIObjectCreationException( final String message ) {
    super( message );
  }

  public UIObjectCreationException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public UIObjectCreationException( final Throwable reas ) {
    super( reas );
  }
}
