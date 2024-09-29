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


package org.pentaho.di.trans.steps.loadsave.initializer;

public interface InitializerInterface<T> {

  /**
   * Perform in-place modifications to the stepMeta before
   * FieldLoadSaveValidator classes are called on the stepMeta
   * 
   * @deprecated The stepMeta class should be updated so that
   * developers can instantiate the stepMeta, and immediately
   * call setter methods.  Commonly, this is used for steps
   * that define an allocate method, which pre-populate
   * empty arrays
   * 
   * @param stepMeta The stepMeta class
   */
  @Deprecated
  public void modify( T object );
}
