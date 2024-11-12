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


package org.pentaho.di.engine.api.reporting;

import java.io.Serializable;

/**
 * Created by hudak on 1/6/17.
 */
public enum Status implements Serializable {
  RUNNING( false ),
  PAUSED( false ),
  STOPPED( true ),
  FAILED( true ),
  FINISHED( true );

  private static final long serialVersionUID = -938695168387846889L;
  final boolean finalState;

  Status( Boolean finalState ) {
    this.finalState = finalState;
  }

  public boolean isFinal() {
    return finalState;
  }
}
