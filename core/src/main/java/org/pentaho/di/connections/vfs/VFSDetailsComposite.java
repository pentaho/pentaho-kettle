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
package org.pentaho.di.connections.vfs;

public interface VFSDetailsComposite {
  Object open();

  void close();

  /**
   * Validates the fields that the DetailsComposite if responsible for.
   * @return null if successfully validates or an error message if it does not.
   */
  default String validate() {
    return null;
  }

  /**
   * Gets the Object of SelectionAdapterFileDialogTextVar for the browse file dialog.
   * <p>
   * @param TextVar The TextVar object.
   * @return Object of SelectionAdapterFileDialogTextVar or null otherwise.
   * Defaults to {@code null}.
   */
  default Object getRootPathSelectionAdapter( Object textUiWidget ) {
    return null;
  }
}
