/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
