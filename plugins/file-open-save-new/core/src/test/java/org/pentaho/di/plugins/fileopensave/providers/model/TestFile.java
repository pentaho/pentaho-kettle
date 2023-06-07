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

package org.pentaho.di.plugins.fileopensave.providers.model;

import org.pentaho.di.plugins.fileopensave.api.providers.BaseEntity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.providers.TestFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
