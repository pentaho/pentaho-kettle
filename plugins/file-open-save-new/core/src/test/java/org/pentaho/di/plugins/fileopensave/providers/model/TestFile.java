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

import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.TestFileProvider;

import java.util.Date;
import java.util.Objects;

public class TestFile extends BaseEntity implements File {
  private static final String TYPE = "file";

  public TestFile() {
  }

  @Override public String getType() {
    return TYPE;
  }

  @Override public String getProvider() {
    return TestFileProvider.TYPE;
  }

  public static TestFile create( String name, String path, String parent ) {
    TestFile testFile = new TestFile();
    testFile.setName( name );
    testFile.setPath( path );
    testFile.setParent( parent );
    testFile.setDate( new Date() );
    testFile.setRoot( TestFileProvider.NAME );
    testFile.setCanEdit( true );
    return testFile;
  }

  @Override
  public int hashCode() {
    return Objects.hash( getProvider(), getPath() );
  }

  @Override public boolean equals( Object obj ) {
    // If the object is compared with itself then return true
    if ( obj == this ) {
      return true;
    }

    if ( !( obj instanceof TestFile ) ) {
      return false;
    }

    TestFile compare = (TestFile) obj;
    return compare.getProvider().equals( getProvider() )
      && ( ( compare.getPath() == null && getPath() == null ) || compare.getPath().equals( getPath() ) );
  }

  public EntityType getEntityType(){
    return EntityType.TEST_FILE;
  }
}
