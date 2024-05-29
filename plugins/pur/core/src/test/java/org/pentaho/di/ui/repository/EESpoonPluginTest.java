/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.ui.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEEDatabaseConnection;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEEJob;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEERepositoryDirectory;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEERepositoryUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEETransformation;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIJob;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryUser;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UITransformation;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener.SpoonLifeCycleEvent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.any;

public class EESpoonPluginTest {

  private Spoon spoon = mock( Spoon.class );
  private Repository repository = mock( Repository.class );
  private EESpoonPlugin eeSpoonPlugin = mock( EESpoonPlugin.class );

  @Before
  public void setUp() throws KettleException {
    IAbsSecurityProvider provider = mock( IAbsSecurityProvider.class );

    doReturn( provider ).when( repository ).getService( eq( IAbsSecurityProvider.class ) );

    doReturn( repository ).when( spoon ).getRepository();

    doCallRealMethod().when( eeSpoonPlugin ).onEvent( any() );
  }

  @Test
  public void testOnEvent_REPOSITORY_CHANGED_noservice() {
    doReturn( spoon ).when( eeSpoonPlugin ).getSpoonInstance();
    eeSpoonPlugin.onEvent( SpoonLifeCycleEvent.REPOSITORY_CHANGED );

    UIObjectRegistry registry = UIObjectRegistry.getInstance();
    assertEquals( UIRepositoryUser.class, registry.getRegisteredUIRepositoryUserClass() );
    assertEquals( UIRepositoryDirectory.class, registry.getRegisteredUIRepositoryDirectoryClass() );
    assertEquals( UIDatabaseConnection.class, registry.getRegisteredUIDatabaseConnectionClass() );
    assertEquals( UIJob.class, registry.getRegisteredUIJobClass() );
    assertEquals( UITransformation.class, registry.getRegisteredUITransformationClass() );
  }

  @Test
  public void testOnEvent_REPOSITORY_CHANGED_service() throws KettleException {
    doReturn( spoon ).when( eeSpoonPlugin ).getSpoonInstance();
    doReturn( true ).when( repository ).hasService( anyClass() );

    eeSpoonPlugin.onEvent( SpoonLifeCycleEvent.REPOSITORY_CHANGED );

    UIObjectRegistry registry = UIObjectRegistry.getInstance();
    assertEquals( UIEERepositoryUser.class, registry.getRegisteredUIRepositoryUserClass() );
    assertEquals( UIEERepositoryDirectory.class, registry.getRegisteredUIRepositoryDirectoryClass() );
    assertEquals( UIEEDatabaseConnection.class, registry.getRegisteredUIDatabaseConnectionClass() );
    assertEquals( UIEEJob.class, registry.getRegisteredUIJobClass() );
    assertEquals( UIEETransformation.class, registry.getRegisteredUITransformationClass() );
  }

  @Test
  public void testOnEvent_REPOSITORY_CONNECTED_noservice() {
    doReturn( null ).doReturn( spoon ).when( eeSpoonPlugin ).getSpoonInstance();
    eeSpoonPlugin.onEvent( SpoonLifeCycleEvent.REPOSITORY_CONNECTED );

    UIObjectRegistry registry = UIObjectRegistry.getInstance();
    assertEquals( UIRepositoryUser.class, registry.getRegisteredUIRepositoryUserClass() );
    assertEquals( UIRepositoryDirectory.class, registry.getRegisteredUIRepositoryDirectoryClass() );
    assertEquals( UIEEDatabaseConnection.class, registry.getRegisteredUIDatabaseConnectionClass() );
    assertEquals( UIJob.class, registry.getRegisteredUIJobClass() );
    assertEquals( UITransformation.class, registry.getRegisteredUITransformationClass() );
  }

  @Test
  public void testOnEvent_REPOSITORY_CONNECTED_service() throws KettleException {
    doReturn( true ).when( repository ).hasService( anyClass() );
    doReturn( null ).doReturn( spoon ).when( eeSpoonPlugin ).getSpoonInstance();

    eeSpoonPlugin.onEvent( SpoonLifeCycleEvent.REPOSITORY_CONNECTED );

    UIObjectRegistry registry = UIObjectRegistry.getInstance();
    assertEquals( UIEERepositoryUser.class, registry.getRegisteredUIRepositoryUserClass() );
    assertEquals( UIEERepositoryDirectory.class, registry.getRegisteredUIRepositoryDirectoryClass() );
    assertEquals( UIEEDatabaseConnection.class, registry.getRegisteredUIDatabaseConnectionClass() );
    assertEquals( UIEEJob.class, registry.getRegisteredUIJobClass() );
    assertEquals( UIEETransformation.class, registry.getRegisteredUITransformationClass() );
  }

  private Class<? extends IRepositoryService> anyClass() {
    return argThat( new EESpoonPluginTest.AnyClassMatcher() );
  }

  private class AnyClassMatcher implements ArgumentMatcher<Class<? extends IRepositoryService>> {

    @Override public boolean matches( Class<? extends IRepositoryService> argument ) {
      return true;
    }

    @Override public Class<?> type() {
      return ArgumentMatcher.super.type();
    }
  }

}

