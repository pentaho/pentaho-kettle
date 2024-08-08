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

package org.pentaho.di.ui.spoon.delegates;
import org.pentaho.di.ui.core.widget.tree.LeveledTreeNode;
import java.util.Objects;

/**
 * Data class that holds information about a selected node in the tree for specific types of objects.
 *
 */
public class SpoonTreeLeveledSelection {

  private final String type;
  private final String name;
  private final LeveledTreeNode.LEVEL level;

  public SpoonTreeLeveledSelection( String type, String name, LeveledTreeNode.LEVEL level ) {
    this.type = Objects.requireNonNull( type );
    this.name = Objects.requireNonNull( name );
    this.level = Objects.requireNonNull( level );
  }


  public String getType() {
    return this.type;
  }

  public String getName() {
    return this.name;
  }

  public LeveledTreeNode.LEVEL getLevel() {
    return this.level;
  }

}
