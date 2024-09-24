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
 * A listener that will signal when the filename of an object changes.
 *
 * @author Matt Casters (mcasters@pentaho.org)
 *
 */
public interface FilenameChangedListener {
  /**
   * The method that is executed when the filename of an object changes
   *
   * @param object
   *          The object for which there is a filename change
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   */
  public void filenameChanged( Object object, String oldFilename, String newFilename );
}
