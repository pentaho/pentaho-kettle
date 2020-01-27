/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.local.model;

import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 2/16/19.
 */
public class LocalTree implements Tree<LocalFile> {

  private static final int ORDER = 2;

  @Override public String getProvider() {
    return LocalFileProvider.TYPE;
  }

  private List<LocalFile> localFiles = new ArrayList<>();
  private String name;

  public LocalTree( String name ) {
    this.name = name;
  }

  @Override public String getName() {
    return name;
  }

  @Override public List<LocalFile> getChildren() {
    return localFiles;
  }

  @Override public void addChild( LocalFile child ) {
    localFiles.add( child );
  }

  public void setFiles( List<LocalFile> localFiles ) {
    this.localFiles = localFiles;
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
