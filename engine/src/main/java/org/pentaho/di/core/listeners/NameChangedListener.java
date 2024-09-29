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
 * A listener that will signal when the name of an object changes.
 *
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public interface NameChangedListener {
  /**
   * The method that is executed when the name of an object changes
   *
   * @param object
   *          The object for which there is a name change
   * @param oldName
   *          the old name
   * @param newName
   *          the new name
   */
  public void nameChanged( Object object, String oldName, String newName );
}
