/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.repository.model;

import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 2/28/18.
 */
public class RepositoryTree implements Tree<RepositoryFile> {

  private static final int ORDER = 1;
  private String name;
  private boolean includeRoot;

  @Override public String getProvider() {
    return RepositoryFileProvider.TYPE;
  }

  public RepositoryTree( String name ) {
    this.name = name;
  }

  @Override public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  private List<RepositoryFile> children = new ArrayList<>();

  public List<RepositoryFile> getChildren() {
    return children;
  }

  public void setChildren( List<RepositoryFile> children ) {
    this.children = children;
  }

  @Override
  public void addChild( RepositoryFile repositoryFile ) {
    children.add( repositoryFile );
  }

  public boolean isIncludeRoot() {
    return includeRoot;
  }

  public void setIncludeRoot( boolean includeRoot ) {
    this.includeRoot = includeRoot;
  }

  @Override public boolean isCanAddChildren() {
    return false;
  }

  @Override public int getOrder() {
    return ORDER;
  }

  @Override public boolean isHasChildren() {
    return true;
  }
}
