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

import org.eclipse.swt.widgets.TreeItem;

public class TreeSelection {
  private Object selection;

  private Object parent;

  private Object grandParent;

  private String itemText;

  private TreeItem treeItem;

  /**
   * @param selection
   * @param parent
   * @param grandParent
   */
  public TreeSelection( TreeItem treeItem, String itemText, Object selection, Object parent, Object grandParent ) {
    this.treeItem = treeItem;
    this.itemText = itemText;
    this.selection = selection;
    this.parent = parent;
    this.grandParent = grandParent;
  }

  public TreeSelection( String itemText, Object selection, Object parent, Object grandParent ) {
    this( null, itemText, selection, parent, grandParent );
  }

  /**
   * @param selection
   * @param parent
   */
  public TreeSelection( String itemText, Object selection, Object parent ) {
    this( itemText, selection, parent, null );
  }

  /**
   * @param selection
   */
  public TreeSelection( String itemText, Object selection ) {
    this( null, itemText, selection, null, null );
  }

  /**
   * @param selection
   */
  public TreeSelection( TreeItem treeItem, String itemText, Object selection ) {
    this( treeItem, itemText, selection, null, null );
  }

  /**
   * @return the grandParent
   */
  public Object getGrandParent() {
    return grandParent;
  }

  /**
   * @param grandParent
   *          the grandParent to set
   */
  public void setGrandParent( Object grandParent ) {
    this.grandParent = grandParent;
  }

  /**
   * @return the parent
   */
  public Object getParent() {
    return parent;
  }

  /**
   * @param parent
   *          the parent to set
   */
  public void setParent( Object parent ) {
    this.parent = parent;
  }

  /**
   * @return the selection
   */
  public Object getSelection() {
    return selection;
  }

  /**
   * @param selection
   *          the selection to set
   */
  public void setSelection( Object selection ) {
    this.selection = selection;
  }

  /**
   * @return the description
   */
  public String getItemText() {
    return itemText;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setItemText( String description ) {
    this.itemText = description;
  }

  public TreeItem getTreeItem() {
    return treeItem;
  }

  public void setTreeItem( TreeItem treeItem ) {
    this.treeItem = treeItem;
  }
}
