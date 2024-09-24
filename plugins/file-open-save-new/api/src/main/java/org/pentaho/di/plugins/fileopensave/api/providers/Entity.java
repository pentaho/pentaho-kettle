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

package org.pentaho.di.plugins.fileopensave.api.providers;

import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;

import java.util.Date;

/**
 * Created by bmorrise on 2/14/19.
 */
public interface Entity {
  String getName();
  String getPath();
  String getParent();
  String getType();
  String getRoot();
  Date getDate();
  boolean isCanEdit();
  boolean isCanDelete();
  EntityType getEntityType();

  /**
   * Gets the decoded version of name of an element.
   * Defaults to {@link #getName()}.
   *
   * @return A non-empty name of an element.
   */
  default String getNameDecoded() {
    return getName();
  }
}
