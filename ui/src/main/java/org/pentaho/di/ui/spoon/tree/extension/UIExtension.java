/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.di.ui.spoon.tree.extension;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface to allow the Plugins to add UI extensions
 */
public interface UIExtension {

  /**
   * Plugins can use this method to build the UI elements in the Composite parameter
   *
   * @param main Composite
   */
  void buildExtension( Composite main );
}
