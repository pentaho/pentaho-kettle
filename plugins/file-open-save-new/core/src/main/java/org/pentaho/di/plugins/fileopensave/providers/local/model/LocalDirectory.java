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

package org.pentaho.di.plugins.fileopensave.providers.local.model;

import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by bmorrise on 2/16/19.
 */
public class LocalDirectory extends LocalFile implements Directory {
  private static String DIRECTORY = "folder";
  private boolean canAddChildren;

  private boolean hasChildren;
  private List<LocalFile> children = new ArrayList<>();

  @Override public String getType() {
    return DIRECTORY;
  }

  public boolean isHasChildren() {
    return hasChildren;
  }

  public void setHasChildren( boolean hasChildren ) {
    this.hasChildren = hasChildren;
  }

  public List<LocalFile> getChildren() {
    return children;
  }

  public void setChildren( List<LocalFile> children ) {
    this.children = children;
  }

  public void addChild( LocalFile file ) {
    this.children.add( file );
  }

  @Override public boolean isCanAddChildren() {
    return canAddChildren;
  }

  public void setCanAddChildren( boolean canAddChildren ) {
    this.canAddChildren = canAddChildren;
  }

  public static LocalDirectory create( String parent, Path path ) {
    LocalDirectory localDirectory = new LocalDirectory();
    localDirectory.setName( path.getFileName() == null ? null : path.getFileName().toString() );
    localDirectory.setPath( path.toString() );
    localDirectory.setParent( parent );
    try {
      localDirectory.setDate( new Date( Files.getLastModifiedTime( path ).toMillis() ) );
    } catch ( IOException e ) {
      localDirectory.setDate( new Date() );
    }
    localDirectory.setRoot( LocalFileProvider.NAME );
    localDirectory.setCanAddChildren( true );
    localDirectory.setCanEdit( true );
    localDirectory.setHasChildren( true );
    return localDirectory;
  }

  @Override
  public EntityType getEntityType(){
    return EntityType.LOCAL_DIRECTORY;
  }
}
