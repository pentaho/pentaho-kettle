/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import org.junit.Assert;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.shared.SharedObjectInterface;

/**
 * @author Andrey Khayrutdinov
 */
public class DialogUtilsTest {

  @Test
  public void nullObject() {
    Assert.assertNull( DialogUtils.getPathOf( null ) );
  }

  @Test
  public void deletedObject() {
    RepositoryElementMetaInterface object = Mockito.mock( RepositoryElementMetaInterface.class );
    Mockito.when( object.isDeleted() ).thenReturn( true );
    Assert.assertNull( DialogUtils.getPathOf( object ) );
  }

  @Test
  public void nullDirectory() {
    RepositoryElementMetaInterface object = Mockito.mock( RepositoryElementMetaInterface.class );
    Mockito.when( object.getRepositoryDirectory() ).thenReturn( null );
    Assert.assertNull( DialogUtils.getPathOf( object ) );
  }

  @Test
  public void nullDirectoryPath() {
    RepositoryElementMetaInterface object = Mockito.mock( RepositoryElementMetaInterface.class );

    RepositoryDirectoryInterface directory = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when( object.getRepositoryDirectory() ).thenReturn( directory );

    Assert.assertNull( DialogUtils.getPathOf( object ) );
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
    RepositoryElementMetaInterface object = Mockito.mock( RepositoryElementMetaInterface.class );

    RepositoryDirectoryInterface directory = Mockito.mock( RepositoryDirectoryInterface.class );
    Mockito.when( directory.getPath() ).thenReturn( path );
    Mockito.when( object.getRepositoryDirectory() ).thenReturn( directory );

    Mockito.when( object.getName() ).thenReturn( name );

    Assert.assertEquals( expected, DialogUtils.getPathOf( object ) );
  }

  @Test
  public void objectWithTheSameNameExists_true_if_exists() {
    SharedObjectInterface sharedObject = Mockito.mock( SharedObjectInterface.class );
    Mockito.when( sharedObject.getName() ).thenReturn( "TEST_OBJECT" );

    Assert.assertTrue( DialogUtils.objectWithTheSameNameExists( sharedObject, createTestScope( "TEST_OBJECT" ) ) );
  }

  @Test
  public void objectWithTheSameNameExists_false_if_not_exist() {
    SharedObjectInterface sharedObject = Mockito.mock( SharedObjectInterface.class );
    Mockito.when( sharedObject.getName() ).thenReturn( "NEW_TEST_OBJECT" );

    Assert.assertFalse( DialogUtils.objectWithTheSameNameExists( sharedObject, createTestScope( "TEST_OBJECT" ) ) );
  }

  @Test
  public void objectWithTheSameNameExists_false_if_same_object() {
    SharedObjectInterface sharedObject = Mockito.mock( SharedObjectInterface.class );
    Mockito.when( sharedObject.getName() ).thenReturn( "TEST_OBJECT" );

    Assert.assertFalse( DialogUtils.objectWithTheSameNameExists( sharedObject, Collections.singleton( sharedObject ) ) );
  }

  @Test
  public void objectWithTheSameNameExistsTrimSpaces() {
    Collection<SharedObjectInterface> testScope = createTestScope( "test", "test1", "test2" );
    SharedObjectInterface sharedObject = createSharedObject( " test      " );

    Assert.assertTrue( DialogUtils.objectWithTheSameNameExists( sharedObject, testScope, true ) );
  }

  @Test
  public void objectWithTheSameNameExistsUpperLower() {
    Collection<SharedObjectInterface> testScope = createTestScope( "test", "test1", "test2" );
    SharedObjectInterface sharedObject = createSharedObject( " Test" );

    Assert.assertFalse( DialogUtils.objectWithTheSameNameExists( sharedObject, testScope ) );
  }

  @Test
  public void objectWithTheSameNameExists() {
    Collection<SharedObjectInterface> testScope = createTestScope( "test", "test1", "test2" );
    SharedObjectInterface sharedObject = createSharedObject( " test      " );

    Assert.assertFalse( DialogUtils.objectWithTheSameNameExists( sharedObject, testScope ) );
  }

  private static Collection<SharedObjectInterface> createTestScope( String... names ) {
    List<SharedObjectInterface> scope = new ArrayList<>();
    for ( String name : names ) {
      scope.add( createSharedObject( name ) );
    }

    return scope;
  }

  private static SharedObjectInterface createSharedObject( String name ) {
    SharedObjectInterface sharedObject = Mockito.mock( SharedObjectInterface.class );
    Mockito.when( sharedObject.getName() ).thenReturn( name );
    return sharedObject;
  }

}
