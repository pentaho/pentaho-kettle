/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020-2023 by Hitachi Vantara : http://www.pentaho.com
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
}
