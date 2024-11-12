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


package org.pentaho.di.plugins.fileopensave.providers.model;

import org.pentaho.di.plugins.fileopensave.api.providers.Directory;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.providers.TestFileProvider;

import java.util.Date;

public class TestDirectory extends TestFile implements Directory {
  @Override public boolean isCanAddChildren() {
    return false;
  }

  public static TestDirectory create( String name, String path, String parent ) {
    TestDirectory testDirectory = new TestDirectory();
    testDirectory.setName( name );
    testDirectory.setPath( path );
    testDirectory.setParent( parent );
    testDirectory.setDate( new Date() );
    testDirectory.setRoot( TestFileProvider.NAME );
    testDirectory.setCanEdit( true );
    return testDirectory;
  }

  public EntityType getEntityType(){
    return EntityType.TEST_DIRECTORY;
  }
}
