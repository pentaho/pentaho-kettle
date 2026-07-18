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



package org.pentaho.di.workarounds;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;


/**
 * This interface represents a resource that contains data elements with variables, relative paths etc
 * that have to be resolved before this resource could be used.
 */
public interface ResolvableResource {

  /**
   * Resolves resource data elements variables and temporary references.
   */
  @Deprecated
  default void resolve() {
    resolve( DefaultBowl.getInstance() );
  }

  void resolve( Bowl bowl );
}
