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

package org.pentaho.di.ui.core.widget.tree;

import java.util.Comparator;

public class LeveledTreeNode extends TreeNode {
  public enum LEVEL {
    PROJECT( "Project" ),
    GLOBAL( "Global" ),
    LOCAL( "Local" );

    // will this need to be localized?
    private final String name;

    LEVEL( String name ) {
      this.name = name;
    }
    public String getName() {
      return name;
    }
  }

  private static final String NAME_KEY = "name";
  private static final String LEVEL_KEY = "level";

  public LeveledTreeNode( String name, LEVEL level ) {
    setData( NAME_KEY, name );
    setData( LEVEL_KEY, level );
  }

  Comparator<TreeNode> COMPARATOR = Comparator.<TreeNode, String>comparing( t -> (String)t.getData().get( NAME_KEY ),
                                                                            String.CASE_INSENSITIVE_ORDER )
                                              .thenComparing( t -> (LEVEL)t.getData().get( LEVEL_KEY ));
  @Override
  public int compareTo( TreeNode other ) {
    return COMPARATOR.compare( this, other );
  }

}

