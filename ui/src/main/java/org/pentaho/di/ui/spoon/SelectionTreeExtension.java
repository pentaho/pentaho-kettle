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
