/*!
 * Copyright 2018-2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur.provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.pur.PurRepository;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PurRepositoryProxyTest {

  PurRepositoryProxy proxy;
  PluginRegistry mockRegistry;
  PluginInterface mockPluginInterface;
  ClassLoader mockClassLoader;
  PurRepository mockRepository;

  @Before
  public void setUp() {
    mockRegistry = mock( PluginRegistry.class );
    mockPluginInterface = mock( PluginInterface.class );
    mockClassLoader = mock( ClassLoader.class );
    mockRepository = mock( PurRepository.class );
    try {
      when( mockRegistry.findPluginWithId( any(), anyString() ) ).thenReturn( mockPluginInterface );
      when( mockRegistry.getClassLoader( any() ) ).thenReturn( mockClassLoader );
      proxy = spy( new PurRepositoryProxy( mockRegistry ) );
    } catch ( KettlePluginException e ) {
      e.printStackTrace();
    }
  }

  @Test
  public void getDelegateTest() {
    Repository repository = null;

    try {
      Mockito.<Class<?>>when( mockClassLoader.loadClass( anyString() ) ).thenReturn( Class.forName( "org.pentaho"
          + ".di.repository.pur.PurRepository" ) );
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
    }

    repository = proxy.getDelegate();

    assertNotNull( repository );
  }

  @Test
  public void getLocationUrlTest() {
    String someString = "SomeString";
    String returnString;
    RepositoryMeta mockRepositoryMeta = mock( RepositoryMeta.class );

    //Both these mocks are needed. If you take the first one the other fails... Probably the first one stubs
    // something that makes the second one work...?
    doReturn( mockRepositoryMeta ).when( proxy ).createPurRepositoryMetaRepositoryMeta( anyString() );
    when( proxy.createPurRepositoryMetaRepositoryMeta( anyString() ) ).thenReturn( mockRepositoryMeta );

    //Both these mocks are needed. If you take the first one the other fails... Probably the first one stubs
    // something that makes the second one work...?
    doReturn( mockRepository ).when( proxy ).getDelegate();
    when( proxy.getDelegate() ).thenReturn( mockRepository );

    proxy.setLocationUrl( someString );
    returnString = proxy.getLocationUrl();

    verify( proxy, times( 1 ) ).getDelegate();
    verify( proxy, times( 1 ) ).createPurRepositoryMetaRepositoryMeta( someString );
    assertEquals( someString, returnString );
  }

  @Test
  public void createPurRepositoryMetaRepositoryMetaTest() {
    RepositoryMeta repositoryMeta = null;
    try {
      Mockito.<Class<?>>when( mockClassLoader.loadClass( "org.pentaho.di.repository.pur"
          + ".PurRepositoryLocation" ) ).thenReturn( Class.forName( "org.pentaho.di.repository.pur"
          + ".PurRepositoryLocation" ) );
      Mockito.<Class<?>>when( mockClassLoader.loadClass( "org.pentaho.di.repository.pur.PurRepositoryMeta" ) )
          .thenReturn( Class.forName( "org.pentaho.di.repository.pur.PurRepositoryMeta" ) );
    } catch ( ClassNotFoundException e ) {
      e.printStackTrace();
    }

    repositoryMeta = proxy.createPurRepositoryMetaRepositoryMeta( "SomeUrl" );

    assertNotNull( repositoryMeta );
  }
}
