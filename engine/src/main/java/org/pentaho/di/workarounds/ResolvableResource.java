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


package org.pentaho.di.workarounds;


/**
 * This interface represents a resource that contains data elements with variables, relative paths etc
 * that have to be resolved before this resource could be used.
 */
public interface ResolvableResource {

  /**
   * Resolves resource data elements variables and temporary references.
   */
  void resolve();

}
