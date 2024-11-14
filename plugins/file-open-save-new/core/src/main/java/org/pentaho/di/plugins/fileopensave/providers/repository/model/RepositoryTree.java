/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
