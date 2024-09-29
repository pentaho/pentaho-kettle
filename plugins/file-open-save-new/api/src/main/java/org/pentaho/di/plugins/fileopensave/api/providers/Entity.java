/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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
