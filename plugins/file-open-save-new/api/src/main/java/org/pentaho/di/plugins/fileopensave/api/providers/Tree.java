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


package org.pentaho.di.plugins.fileopensave.api.providers;

import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;

import java.util.List;

/**
 * Created by bmorrise on 2/13/19.
 */
public interface Tree<T extends Entity> extends Providerable {
  String getName();
  List<T> getChildren();
  void addChild( T child );
  boolean isCanAddChildren();
  int getOrder();
  default boolean isHasChildren() {
    return true;
  }
  default boolean isCanEdit() {
    return false;
  }
  default boolean isCanDelete() {
    return false;
  }

  default EntityType getEntityType(){
    return EntityType.TREE;
  }

  /**
   * Gets the decoded version of name of an element.
   * Defaults to {@link #getName()}.
   *
   * @return A non-empty name of an element.
   */
  default String getNameDecoded() { return getName(); }
}
