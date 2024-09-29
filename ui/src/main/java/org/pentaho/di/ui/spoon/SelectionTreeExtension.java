/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.ui.core.gui.GUIResource;

public class SelectionTreeExtension {

  private TreeItem tiRootName = null;
  private AbstractMeta meta = null;
  private GUIResource guiResource = null;
  private Object selection = null;
  private String action = null;

  public SelectionTreeExtension( Object selection, String action ) {
    this.selection = selection;
    this.action = action;
  }

  public SelectionTreeExtension( TreeItem tiRootName, AbstractMeta meta, GUIResource guiResource, String action ) {
    this.tiRootName = tiRootName;
    this.meta = meta;
    this.guiResource = guiResource;
    this.action = action;
  }

  public TreeItem getTiRootName() {
    return this.tiRootName;
  }

  public AbstractMeta getMeta() {
    return this.meta;
  }

  public GUIResource getGuiResource() {
    return this.guiResource;
  }

  public String getAction() {
    return this.action;
  }

  public Object getSelection() {
    return this.selection;
  }
}
