/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.widget.tree;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bmorrise on 6/27/18.
 */
public class TreeNode {
  private String label;
  private Image image;
  private List<TreeNode> children = new ArrayList<>();
  private Font font;
  private Color foreground;
  private Color background;
  private boolean expanded = false;
  private boolean hidden = false;
  private Map<String, Object> data = new HashMap<>();
  private int index = -1;

  public TreeNode() {
  }

  public TreeNode( String label, Image image, boolean expanded ) {
    this.label = label;
    this.image = image;
    this.expanded = expanded;
  }

  public List<TreeNode> getChildren() {
    return children;
  }

  public void addChild( TreeNode treeNode ) {
    children.add( treeNode );
  }

  public boolean hasChildren() {
    return children != null && children.size() > 0;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel( String label ) {
    this.label = label;
  }

  public Image getImage() {
    return image;
  }

  public void setImage( Image image ) {
    this.image = image;
  }

  public Font getFont() {
    return font;
  }

  public void setFont( Font font ) {
    this.font = font;
  }

  public Color getForeground() {
    return foreground;
  }

  public void setForeground( Color foreground ) {
    this.foreground = foreground;
  }

  public Color getBackground() {
    return background;
  }

  public void setBackground( Color background ) {
    this.background = background;
  }

  public boolean isExpanded() {
    return expanded;
  }

  public void setExpanded( boolean expanded ) {
    this.expanded = expanded;
  }

  public void removeAll() {
    this.children.clear();
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden( boolean hidden ) {
    this.hidden = hidden;
  }

  public void setData( String key, Object value ) {
    data.put( key, value );
  }

  public Map<String, Object> getData() {
    return data;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex( int index ) {
    this.index = index;
  }
}
