/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.util;

import org.junit.Test;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.ui.util.DialogUtils.getPathOf;

/**
 * @author Andrey Khayrutdinov
 */
public class DialogUtilsTest {

  @Test
  public void nullObject() {
    assertNull( getPathOf( null ) );
  }

  @Test
  public void deletedObject() {
    RepositoryElementMetaInterface object = mock( RepositoryElementMetaInterface.class );
    when( object.isDeleted() ).thenReturn( true );
    assertNull( getPathOf( object ) );
  }

  @Test
  public void nullDirectory() {
    RepositoryElementMetaInterface object = mock( RepositoryElementMetaInterface.class );
    when( object.getRepositoryDirectory() ).thenReturn( null );
    assertNull( getPathOf( object ) );
  }

  @Test
  public void nullDirectoryPath() {
    RepositoryElementMetaInterface object = mock( RepositoryElementMetaInterface.class );

    RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
    when( object.getRepositoryDirectory() ).thenReturn( directory );

    assertNull( getPathOf( object ) );
  }

  @Test
  public void pathWithSlash() {
    testPathAndName( "/path/", "name", "/path/name" );
  }

  @Test
  public void pathWithOutSlash() {
    testPathAndName( "/path", "name", "/path/name" );
  }

  private void testPathAndName( String path, String name, String expected ) {
    RepositoryElementMetaInterface object = mock( RepositoryElementMetaInterface.class );

    RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
    when( directory.getPath() ).thenReturn( path );
    when( object.getRepositoryDirectory() ).thenReturn( directory );

    when( object.getName() ).thenReturn( name );

    assertEquals( expected, getPathOf( object ) );
  }
}
