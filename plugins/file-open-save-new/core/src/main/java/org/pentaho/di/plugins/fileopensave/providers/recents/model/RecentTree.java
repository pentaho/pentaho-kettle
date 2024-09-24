/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.recents.model;

import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.providers.recents.RecentFileProvider;

import java.util.ArrayList;
import java.util.List;

public class RecentTree implements Tree<RecentFile> {

  private static final int ORDER = 0;
  private String name;
  private List<RecentFile> recentFiles = new ArrayList<>();

  public RecentTree( String name ) {
    this.name = name;
  }

  @Override public String getName() {
    return name;
  }

  @Override public List<RecentFile> getChildren() {
    return recentFiles;
  }

  @Override public void addChild( RecentFile child ) {
    recentFiles.add( child );
  }

  @Override public boolean isCanAddChildren() {
    return false;
  }

  @Override public int getOrder() {
    return ORDER;
  }

  @Override public String getProvider() {
    return RecentFileProvider.TYPE;
  }

  @Override public boolean isHasChildren() {
    return false;
  }
}
