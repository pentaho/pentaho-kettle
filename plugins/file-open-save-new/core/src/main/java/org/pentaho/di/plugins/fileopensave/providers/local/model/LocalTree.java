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
