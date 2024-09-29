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

package org.pentaho.di.plugins.fileopensave.providers.vfs.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmorrise on 2/13/19.
 */
public class VFSTree implements Tree<VFSLocation> {

  public static final String PROVIDER = "vfs";
  private static final int ORDER = 3;

  public VFSTree( String name ) {
    this.name = name;
  }

  public String getProvider() {
    return PROVIDER;
  }

  private String name;

  @Override public String getName() {
    return name;
  }

  private List<VFSLocation> vfsLocations = new ArrayList<>();

  @Override public List<VFSLocation> getChildren() {
    return vfsLocations;
  }

  @Override public void addChild( VFSLocation child ) {
    vfsLocations.add( child );
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

  @Override
  public String getNameDecoded() {
    try {
      return UriParser.decode( getName() );
    } catch ( FileSystemException e ) {
      // Fallback to original name
      return getName();
    }
  }
}
