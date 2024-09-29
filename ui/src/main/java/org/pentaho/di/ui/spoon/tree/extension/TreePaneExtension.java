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


package org.pentaho.di.ui.spoon.tree.extension;

import org.eclipse.swt.widgets.Composite;

public interface TreePaneExtension {

  /**
   * Controller to open/close the pane
   */
  public interface ExpandController {
    void show();
    void hide();
  }

  /**
   * @param main
   *          Container where to create the extension pane. Implementors should keep a reference to it or its contents.
   *          It has not default layout.
   * @param expander
   *          Callback to show or hide the area.
   * @return if extension will start enabled
   */
  boolean createPane( Composite main, ExpandController expander );
}
