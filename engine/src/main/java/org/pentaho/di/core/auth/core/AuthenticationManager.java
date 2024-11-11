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


package org.pentaho.di.core.auth.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.pentaho.di.core.auth.core.impl.DefaultAuthenticationConsumerFactory;
import org.pentaho.di.core.auth.core.impl.DefaultAuthenticationPerformerFactory;
import org.pentaho.di.i18n.BaseMessages;

public class AuthenticationManager {
  private static final Class<?> PKG = AuthenticationManager.class;
  private final Map<Class<?>, Map<Class<?>, Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>>> factoryMap =
      new HashMap<Class<?>, Map<Class<?>, Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>>>();
  private AuthenticationPerformerFactory authenticationPerformerFactory = new DefaultAuthenticationPerformerFactory();
  private final List<AuthenticationProvider> authenticationProviders = new ArrayList<AuthenticationProvider>();

  public void registerAuthenticationProvider( AuthenticationProvider authenticationProvider ) {
    synchronized ( authenticationProviders ) {
      authenticationProviders.add( authenticationProvider );
    }
  }

  public boolean unregisterAuthenticationProvider( AuthenticationProvider authenticationProvider ) {
    synchronized ( authenticationProviders ) {
      return authenticationProviders.remove( authenticationProvider );
    }
  }

  public <ReturnType, CreateArgType, ConsumedType> void registerConsumerFactory(
      AuthenticationConsumerFactory<ReturnType, CreateArgType, ConsumedType> factory ) throws AuthenticationFactoryException {
    if ( !factory.getConsumedType().isInterface()
        && !AuthenticationProvider.class.isAssignableFrom( factory.getConsumedType() ) ) {
      throw new AuthenticationFactoryException( BaseMessages.getString( PKG, "AuthenticationManager.ConsumedTypeError",
          factory ) );
    }

    Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>> createTypeMap =
        getRelevantConsumerFactoryMap( factory.getReturnType(), factory.getCreateArgType() );
    synchronized ( createTypeMap ) {
      createTypeMap.put( factory.getConsumedType(), factory );
    }
  }

  public <ReturnType, ConsumedType> void registerConsumerClass(
      Class<? extends AuthenticationConsumer<? extends ReturnType, ? extends ConsumedType>> consumerClass ) throws AuthenticationFactoryException {
    registerConsumerFactory( new DefaultAuthenticationConsumerFactory( consumerClass ) );
  }

  public <ReturnType, CreateArgType, ConsumedType> List<AuthenticationPerformer<ReturnType, CreateArgType>>
    getSupportedAuthenticationPerformers( Class<ReturnType> returnType, Class<CreateArgType> createArgType ) {
    Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>> createTypeMap =
        getRelevantConsumerFactoryMap( returnType, createArgType );
    synchronized ( createTypeMap ) {
      createTypeMap = new HashMap<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>( createTypeMap );
    }

    List<AuthenticationProvider> authenticationProviders;
    synchronized ( this.authenticationProviders ) {
      authenticationProviders = new ArrayList<AuthenticationProvider>( this.authenticationProviders );
    }

    List<AuthenticationPerformer<ReturnType, CreateArgType>> result =
        new ArrayList<AuthenticationPerformer<ReturnType, CreateArgType>>();

    for ( Entry<Class<?>, AuthenticationConsumerFactory<?, ?, ?>> entry : createTypeMap.entrySet() ) {
      for ( AuthenticationProvider provider : authenticationProviders ) {
        @SuppressWarnings( "unchecked" )
        AuthenticationPerformer<ReturnType, CreateArgType> authenticationPerformer =
            (AuthenticationPerformer<ReturnType, CreateArgType>) authenticationPerformerFactory.create( provider, entry
                .getValue() );
        if ( authenticationPerformer != null && authenticationPerformer.getDisplayName() != null ) {
          result.add( authenticationPerformer );
        }
      }
    }

    Collections.sort( result, new Comparator<AuthenticationPerformer<ReturnType, CreateArgType>>() {

      @Override
      public int compare( AuthenticationPerformer<ReturnType, CreateArgType> o1,
          AuthenticationPerformer<ReturnType, CreateArgType> o2 ) {
        return o1.getDisplayName().toUpperCase().compareTo( o2.getDisplayName().toUpperCase() );
      }
    } );

    return result;
  }

  public <ReturnType, CreateArgType, ConsumedType> AuthenticationPerformer<ReturnType, CreateArgType>
    getAuthenticationPerformer( Class<ReturnType> returnType, Class<CreateArgType> createArgType, String providerId ) {
    List<AuthenticationPerformer<ReturnType, CreateArgType>> performers =
        getSupportedAuthenticationPerformers( returnType, createArgType );
    for ( AuthenticationPerformer<ReturnType, CreateArgType> candidatePerformer : performers ) {
      if ( candidatePerformer.getAuthenticationProvider().getId().equals( providerId ) ) {
        return candidatePerformer;
      }
    }
    return null;
  }

  private <ReturnType, CreateArgType> Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>
    getRelevantConsumerFactoryMap( Class<ReturnType> returnType, Class<CreateArgType> createArgType ) {
    synchronized ( factoryMap ) {
      Map<Class<?>, Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>> returnTypeMap = factoryMap.get( returnType );
      if ( returnTypeMap == null ) {
        returnTypeMap = new HashMap<Class<?>, Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>>();
        factoryMap.put( returnType, returnTypeMap );
      }

      Map<Class<?>, AuthenticationConsumerFactory<?, ?, ?>> createTypeMap = returnTypeMap.get( createArgType );
      if ( createTypeMap == null ) {
        createTypeMap = new HashMap<Class<?>, AuthenticationConsumerFactory<?, ?, ?>>();
        returnTypeMap.put( createArgType, createTypeMap );
      }

      return createTypeMap;
    }
  }

  protected void setAuthenticationPerformerFactory( AuthenticationPerformerFactory authenticationPerformerFactory ) {
    this.authenticationPerformerFactory = authenticationPerformerFactory;
  }
}
