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

package org.pentaho.di.engine.api.reporting;

import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Transformation;

import java.io.Serializable;

public class SubTransCreation implements Serializable {
  private static final long serialVersionUID = 6249199047103429616L;
  private ExecutionContext context;
  private Transformation transformation;

  public ExecutionContext getContext() {
    return context;
  }

  public void setContext( ExecutionContext context ) {
    this.context = context;
  }

  public Transformation getTransformation() {
    return transformation;
  }

  public void setTransformation( Transformation transformation ) {
    this.transformation = transformation;
  }
}
