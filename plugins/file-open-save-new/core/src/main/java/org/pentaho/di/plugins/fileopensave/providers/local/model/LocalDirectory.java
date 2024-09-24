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
