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
