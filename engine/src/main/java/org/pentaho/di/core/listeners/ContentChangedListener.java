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


package org.pentaho.di.core.listeners;

/**
 * This listener will be called by the parent object when its content changes.
 *
 * @author matt
 *
 */
public interface ContentChangedListener {

  /**
   * This method will be called when the parent object to which this listener is added, has been changed.
   *
   * @param parentObject
   *          The changed object.
   */
  public void contentChanged( Object parentObject );

  /**
   * This method will be called when the parent object has been declared safe (or saved, persisted, ...)
   *
   * @param parentObject
   *          The safe object.
   */
  public void contentSafe( Object parentObject );
}
