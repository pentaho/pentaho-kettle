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


package org.pentaho.di.plugins.fileopensave.providers.model;

import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.providers.TestFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;

import java.util.ArrayList;
import java.util.List;

public class TestTree implements Tree<TestFile> {

  private String name;
  private List<TestFile> children = new ArrayList<>();

  public TestTree( String name ) {
    this.name = name;
  }

  @Override public String getName() {
    return name;
  }

  @Override public List<TestFile> getChildren() {
    return children;
  }

  @Override public void addChild( TestFile child ) {
    children.add( child );
  }

  @Override public boolean isCanAddChildren() {
    return false;
  }

  @Override public int getOrder() {
    return 0;
  }

  @Override public String getProvider() {
    return TestFileProvider.TYPE;
  }

  public void setFiles( List<TestFile> localFiles ) {
    this.children = children;
  }
}
