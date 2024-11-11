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
