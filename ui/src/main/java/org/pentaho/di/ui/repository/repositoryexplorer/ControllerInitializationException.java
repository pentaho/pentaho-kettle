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



package org.pentaho.di.ui.repository.repositoryexplorer;

public class ControllerInitializationException extends Exception {
  private static final long serialVersionUID = -2151267602926525247L;

  public ControllerInitializationException() {
    super();
  }

  public ControllerInitializationException( final String message ) {
    super( message );
  }

  public ControllerInitializationException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public ControllerInitializationException( final Throwable reas ) {
    super( reas );
  }
}
