/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
