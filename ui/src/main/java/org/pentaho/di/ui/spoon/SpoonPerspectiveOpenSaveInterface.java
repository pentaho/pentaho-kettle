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


package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;

/**
 * The spoon perspective implementing this interface implements its own open/save dialogs and logic.
 *
 * @author matt
 */
public interface SpoonPerspectiveOpenSaveInterface {
  /**
   * Open a file/object
   */
  public void open();

  /**
   * Import from a file (extension driven by the perspective)
   *
   * @param filename
   *          the file to read from
   */
  public void importFile( String filename );

  /**
   * Save the specified file/object
   *
   * @param meta
   *          The object to be saved.
   * @return true if the object was saved
   */
  public boolean save( EngineMetaInterface meta );

  /**
   * Export to a file
   *
   * @param meta
   *          the object to export
   * @param filename
   *          the file to write to
   */
  boolean exportFile( EngineMetaInterface meta, String filename );

}
