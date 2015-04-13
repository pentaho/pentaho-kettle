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

package org.pentaho.di.trans;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.plugins.ClassLoadingPluginInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;

import java.util.HashMap;
import java.util.Map;

public class TransExecutionConfigurationTest {

  @Test
  public void testConnectRepository() throws KettleException {
    TransExecutionConfiguration transExecConf = new TransExecutionConfiguration();
    final RepositoriesMeta repositoriesMeta = mock( RepositoriesMeta.class );
    final RepositoryMeta repositoryMeta = mock( RepositoryMeta.class );
    final Repository repository = mock( Repository.class );
    final String mockRepo = "mockRepo";
    final boolean[] connectionSuccess = {false};

    Repository initialRepo = mock( Repository.class );
    transExecConf.setRepository( initialRepo );

    KettleLogStore.init();

    //Create mock repository plugin
    MockRepositoryPlugin mockRepositoryPlugin = mock( MockRepositoryPlugin.class );
    when( mockRepositoryPlugin.getIds() ).thenReturn( new String[]{"mockRepo"} );
    when( mockRepositoryPlugin.matches( "mockRepo" ) ).thenReturn( true );
    when( mockRepositoryPlugin.getName() ).thenReturn( "mock-repository" );
    when( mockRepositoryPlugin.getClassMap() ).thenAnswer( new Answer<Map<Class<?>, String>>() {
      @Override
      public Map<Class<?>, String> answer( InvocationOnMock invocation ) throws Throwable {
        Map<Class<?>, String> dbMap = new HashMap<Class<?>, String>();
        dbMap.put( Repository.class, repositoryMeta.getClass().getName() );
        return dbMap;
      }
    } );
    PluginRegistry.getInstance().registerPlugin( RepositoryPluginType.class, mockRepositoryPlugin );

    // Define valid connection criteria
    when( repositoriesMeta.findRepository( anyString() ) ).thenAnswer( new Answer<RepositoryMeta>() {
      @Override
      public RepositoryMeta answer( InvocationOnMock invocation ) throws Throwable {
        return mockRepo.equals( invocation.getArguments()[0] ) ? repositoryMeta : null;
      }
    } );
    when( mockRepositoryPlugin.loadClass( Repository.class ) ).thenReturn( repository );
    doAnswer( new Answer() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        if ( "username".equals( invocation.getArguments()[0] ) &&  "password".equals( invocation.getArguments()[1] ) ) {
          connectionSuccess[0] = true;
        } else {
          connectionSuccess[0] = false;
          throw new KettleException( "Mock Repository connection failed" );
        }
        return null;
      }
    } ).when( repository ).connect( anyString(), anyString() );

    //Ignore repository not found in RepositoriesMeta
    transExecConf.connectRepository( repositoriesMeta, "notFound", "username", "password" );
    assertEquals( "Repository Changed", initialRepo, transExecConf.getRepository() );

    //Ignore failed attempt to connect
    transExecConf.connectRepository( repositoriesMeta, mockRepo, "username", "" );
    assertEquals( "Repository Changed", initialRepo, transExecConf.getRepository() );

    //Save repository if connection passes
    transExecConf.connectRepository( repositoriesMeta, mockRepo, "username", "password" );
    assertEquals( "Repository didn't change", repository, transExecConf.getRepository() );
    assertTrue( "Repository not connected", connectionSuccess[0] );
  }
  private interface MockRepositoryPlugin extends PluginInterface, ClassLoadingPluginInterface { }
}
