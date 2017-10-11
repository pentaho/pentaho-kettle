/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.auth.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.pentaho.di.core.auth.DelegatingKerberosConsumer;
import org.pentaho.di.core.auth.DelegatingKerberosConsumerForClassloaderBridging;
import org.pentaho.di.core.auth.DelegatingNoAuthConsumer;
import org.pentaho.di.core.auth.DelegatingUsernamePasswordConsumer;
import org.pentaho.di.core.auth.KerberosAuthenticationProvider;
import org.pentaho.di.core.auth.KerberosAuthenticationProviderProxyInterface;
import org.pentaho.di.core.auth.NoAuthenticationAuthenticationProvider;
import org.pentaho.di.core.auth.UsernamePasswordAuthenticationProvider;
import org.pentaho.di.core.auth.core.impl.ClassloaderBridgingAuthenticationPerformer;

public class AuthenticationManagerTest {
  private AuthenticationManager manager;
  private NoAuthenticationAuthenticationProvider noAuthenticationAuthenticationProvider;

  @Before
  public void setup() {
    manager = new AuthenticationManager();
    noAuthenticationAuthenticationProvider = new NoAuthenticationAuthenticationProvider();
    manager.registerAuthenticationProvider( noAuthenticationAuthenticationProvider );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testNoAuthProviderAndConsumer() throws AuthenticationConsumptionException, AuthenticationFactoryException {
    manager.registerConsumerClass( DelegatingNoAuthConsumer.class );
    AuthenticationConsumer<Object, NoAuthenticationAuthenticationProvider> consumer =
        mock( AuthenticationConsumer.class );
    manager.getAuthenticationPerformer( Object.class, AuthenticationConsumer.class,
        NoAuthenticationAuthenticationProvider.NO_AUTH_ID ).perform( consumer );
    verify( consumer ).consume( noAuthenticationAuthenticationProvider );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testUsernamePasswordProviderConsumer() throws AuthenticationConsumptionException,
    AuthenticationFactoryException {
    manager.registerConsumerClass( DelegatingNoAuthConsumer.class );
    manager.registerConsumerClass( DelegatingUsernamePasswordConsumer.class );
    UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider =
        new UsernamePasswordAuthenticationProvider( "upass", "u", "pass" );
    manager.registerAuthenticationProvider( usernamePasswordAuthenticationProvider );
    AuthenticationConsumer<Object, UsernamePasswordAuthenticationProvider> consumer =
        mock( AuthenticationConsumer.class );
    manager.getAuthenticationPerformer( Object.class, AuthenticationConsumer.class,
        usernamePasswordAuthenticationProvider.getId() ).perform( consumer );
    verify( consumer ).consume( usernamePasswordAuthenticationProvider );
  }

  @SuppressWarnings( "unchecked" )
  @Test
  public void testKerberosProviderConsumer() throws AuthenticationConsumptionException, AuthenticationFactoryException {
    manager.registerConsumerClass( DelegatingNoAuthConsumer.class );
    manager.registerConsumerClass( DelegatingUsernamePasswordConsumer.class );
    manager.registerConsumerClass( DelegatingKerberosConsumer.class );
    KerberosAuthenticationProvider kerberosAuthenticationProvider =
        new KerberosAuthenticationProvider( "kerb", "kerb", true, "pass", true, "none" );
    manager.registerAuthenticationProvider( kerberosAuthenticationProvider );
    AuthenticationConsumer<Object, KerberosAuthenticationProvider> consumer = mock( AuthenticationConsumer.class );
    manager.getAuthenticationPerformer( Object.class, AuthenticationConsumer.class,
        kerberosAuthenticationProvider.getId() ).perform( consumer );
    verify( consumer ).consume( kerberosAuthenticationProvider );
  }

  @SuppressWarnings( "rawtypes" )
  @Test
  public void testGetSupportedPerformers() throws AuthenticationConsumptionException, AuthenticationFactoryException {
    manager.registerConsumerClass( DelegatingNoAuthConsumer.class );
    manager.registerConsumerClass( DelegatingUsernamePasswordConsumer.class );
    manager.registerConsumerClass( DelegatingKerberosConsumer.class );
    UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider =
        new UsernamePasswordAuthenticationProvider( "upass", "u", "pass" );
    manager.registerAuthenticationProvider( usernamePasswordAuthenticationProvider );
    KerberosAuthenticationProvider kerberosAuthenticationProvider =
        new KerberosAuthenticationProvider( "kerb", "kerb", true, "pass", true, "none" );
    manager.registerAuthenticationProvider( kerberosAuthenticationProvider );
    List<AuthenticationPerformer<Object, AuthenticationConsumer>> performers =
        manager.getSupportedAuthenticationPerformers( Object.class, AuthenticationConsumer.class );
    assertEquals( 3, performers.size() );
    Set<String> ids =
        new HashSet<String>( Arrays.asList( NoAuthenticationAuthenticationProvider.NO_AUTH_ID,
            usernamePasswordAuthenticationProvider.getId(), kerberosAuthenticationProvider.getId() ) );
    for ( AuthenticationPerformer<Object, AuthenticationConsumer> performer : performers ) {
      ids.remove( performer.getAuthenticationProvider().getId() );
    }
    assertEquals( 0, ids.size() );
  }

  @SuppressWarnings( "rawtypes" )
  @Test
  public void testRegisterUnregisterProvider() throws AuthenticationFactoryException {
    manager.registerConsumerClass( DelegatingNoAuthConsumer.class );
    manager.registerConsumerClass( DelegatingUsernamePasswordConsumer.class );
    List<AuthenticationPerformer<Object, AuthenticationConsumer>> performers =
        manager.getSupportedAuthenticationPerformers( Object.class, AuthenticationConsumer.class );
    assertEquals( 1, performers.size() );
    Set<String> ids = new HashSet<String>( Arrays.asList( NoAuthenticationAuthenticationProvider.NO_AUTH_ID ) );
    for ( AuthenticationPerformer<Object, AuthenticationConsumer> performer : performers ) {
      ids.remove( performer.getAuthenticationProvider().getId() );
    }
    assertEquals( 0, ids.size() );
    UsernamePasswordAuthenticationProvider usernamePasswordAuthenticationProvider =
        new UsernamePasswordAuthenticationProvider( "upass", "u", "pass" );
    manager.registerAuthenticationProvider( usernamePasswordAuthenticationProvider );
    performers = manager.getSupportedAuthenticationPerformers( Object.class, AuthenticationConsumer.class );
    assertEquals( 2, performers.size() );
    ids =
        new HashSet<String>( Arrays.asList( NoAuthenticationAuthenticationProvider.NO_AUTH_ID,
            usernamePasswordAuthenticationProvider.getId() ) );
    for ( AuthenticationPerformer<Object, AuthenticationConsumer> performer : performers ) {
      ids.remove( performer.getAuthenticationProvider().getId() );
    }
    assertEquals( 0, ids.size() );
    manager.unregisterAuthenticationProvider( usernamePasswordAuthenticationProvider );
    performers = manager.getSupportedAuthenticationPerformers( Object.class, AuthenticationConsumer.class );
    assertEquals( 1, performers.size() );
    ids = new HashSet<String>( Arrays.asList( NoAuthenticationAuthenticationProvider.NO_AUTH_ID ) );
    for ( AuthenticationPerformer<Object, AuthenticationConsumer> performer : performers ) {
      ids.remove( performer.getAuthenticationProvider().getId() );
    }
    assertEquals( 0, ids.size() );
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Test
  public void testRegisterConsumerFactory() throws AuthenticationConsumptionException, AuthenticationFactoryException {
    AuthenticationConsumer<Object, KerberosAuthenticationProvider> authConsumer = mock( AuthenticationConsumer.class );
    AuthenticationConsumerFactory<Object, AuthenticationConsumer, KerberosAuthenticationProvider> factory =
        mock( AuthenticationConsumerFactory.class );
    when( factory.getReturnType() ).thenReturn( Object.class );
    when( factory.getCreateArgType() ).thenReturn( AuthenticationConsumer.class );
    when( factory.getConsumedType() ).thenReturn( KerberosAuthenticationProvider.class );
    when( factory.create( authConsumer ) ).thenReturn( authConsumer );
    KerberosAuthenticationProvider kerberosAuthenticationProvider =
        new KerberosAuthenticationProvider( "kerb", "kerb", true, "pass", true, "none" );
    manager.registerAuthenticationProvider( kerberosAuthenticationProvider );
    manager.registerConsumerFactory( factory );
    manager.getAuthenticationPerformer( Object.class, AuthenticationConsumer.class,
        kerberosAuthenticationProvider.getId() ).perform( authConsumer );
    verify( authConsumer ).consume( kerberosAuthenticationProvider );
  }

  @Test
  @SuppressWarnings( "unchecked" )
  public void testClassLoaderBridgingPerformer() throws AuthenticationConsumptionException,
    AuthenticationFactoryException {
    manager.setAuthenticationPerformerFactory( new AuthenticationPerformerFactory() {

      @Override
      public <ReturnType, CreateArgType, ConsumedType> AuthenticationPerformer<ReturnType, CreateArgType> create(
          AuthenticationProvider authenticationProvider,
          AuthenticationConsumerFactory<ReturnType, CreateArgType, ConsumedType> authenticationConsumer ) {
        if ( AuthenticationConsumerInvocationHandler.isCompatible( authenticationConsumer.getConsumedType(),
            authenticationProvider ) ) {
          return new ClassloaderBridgingAuthenticationPerformer<ReturnType, CreateArgType, ConsumedType>(
              authenticationProvider, authenticationConsumer );
        }
        return null;
      }
    } );
    manager.registerConsumerClass( DelegatingNoAuthConsumer.class );
    manager.registerConsumerClass( DelegatingUsernamePasswordConsumer.class );
    manager.registerConsumerClass( DelegatingKerberosConsumerForClassloaderBridging.class );
    KerberosAuthenticationProvider kerberosAuthenticationProvider =
        new KerberosAuthenticationProvider( "kerb", "kerb", true, "pass", true, "none" );
    manager.registerAuthenticationProvider( kerberosAuthenticationProvider );
    AuthenticationConsumer<Object, KerberosAuthenticationProviderProxyInterface> consumer =
        mock( AuthenticationConsumer.class );

    @SuppressWarnings( "rawtypes" )
    AuthenticationPerformer<Object, AuthenticationConsumer> performer =
        manager.getAuthenticationPerformer( Object.class, AuthenticationConsumer.class, kerberosAuthenticationProvider
            .getId() );
    assertNotNull( performer );
    performer.perform( consumer );

    ArgumentCaptor<KerberosAuthenticationProviderProxyInterface> captor =
        ArgumentCaptor.forClass( KerberosAuthenticationProviderProxyInterface.class );
    verify( consumer ).consume( captor.capture() );
    assertEquals( kerberosAuthenticationProvider.getId(), captor.getValue().getId() );
    assertEquals( kerberosAuthenticationProvider.getDisplayName(), captor.getValue().getDisplayName() );
    assertEquals( kerberosAuthenticationProvider.getPrincipal(), captor.getValue().getPrincipal() );
    assertEquals( kerberosAuthenticationProvider.getPassword(), captor.getValue().getPassword() );
    assertEquals( kerberosAuthenticationProvider.getKeytabLocation(), captor.getValue().getKeytabLocation() );
    assertEquals( kerberosAuthenticationProvider.isUseKeytab(), captor.getValue().isUseKeytab() );
    assertEquals( kerberosAuthenticationProvider.isUseExternalCredentials(), captor.getValue()
        .isUseExternalCredentials() );
  }
}
