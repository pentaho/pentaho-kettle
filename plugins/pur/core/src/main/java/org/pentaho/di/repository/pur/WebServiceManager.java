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

package org.pentaho.di.repository.pur;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.repository.pur.WebServiceSpecification.ServiceType;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService;
import org.pentaho.platform.security.policy.rolebased.ws.IRoleAuthorizationPolicyRoleBindingDaoWebService;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;

import com.pentaho.di.services.PentahoDiPlugin;
import com.pentaho.pdi.ws.IRepositorySyncWebService;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;


/**
 * Web service factory. Not a true factory in that the things that this factory can create are not configurable. But it
 * does cache the services.
 * 
 * @author mlowery
 */
public class WebServiceManager implements ServiceManager {

  /**
   * Header name must match that specified in ProxyTrustingFilter. Note that an header has the following form: initial
   * capital letter followed by all lowercase letters.
   */
  private static final String TRUST_USER = "_trust_user_"; //$NON-NLS-1$

  private static final String NAMESPACE_URI = "http://www.pentaho.org/ws/1.0"; //$NON-NLS-1$

  private static final ExecutorService executor = ExecutorUtil.getExecutor();

  private final Map<String, Future<Object>> serviceCache = new HashMap<String, Future<Object>>();

  private final Map<Class<?>, WebServiceSpecification> serviceNameMap;

  private final String baseUrl;

  private final String lastUsername;

  private Map<Class<?>, WebServiceSpecification> tempServiceNameMap; // hold the map while building

  public WebServiceManager( String baseUrl, String username ) {
    this.baseUrl = baseUrl;
    this.lastUsername = username;
    tempServiceNameMap = new HashMap<Class<?>, WebServiceSpecification>();
    registerWsSpecification( IUnifiedRepositoryJaxwsWebService.class, "unifiedRepository" ); //$NON-NLS-1$
    registerWsSpecification( IRepositorySyncWebService.class, "repositorySync" ); //$NON-NLS-1$
    registerWsSpecification( IUserRoleListWebService.class, "userRoleListService" ); //$NON-NLS-1$
    registerWsSpecification( IUserRoleWebService.class, "userRoleService" ); //$NON-NLS-1$
    registerWsSpecification( IRoleAuthorizationPolicyRoleBindingDaoWebService.class, "roleBindingDao" ); //$NON-NLS-1$
    registerWsSpecification( IAuthorizationPolicyWebService.class, "authorizationPolicy" ); //$NON-NLS-1$

    registerRestSpecification( PentahoDiPlugin.PurRepositoryPluginApiRevision.class, "purRepositoryPluginApiRevision" ); //$NON-NLS-1$

    this.serviceNameMap = Collections.unmodifiableMap( tempServiceNameMap );
    tempServiceNameMap = null;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T createService( final String username, final String password, final Class<T> clazz )
    throws MalformedURLException {
    final Future<Object> resultFuture;
    synchronized ( serviceCache ) {
      // if this is true, a coder did not make sure that clearServices was called on disconnect
      if ( lastUsername != null && !lastUsername.equals( username ) ) {
        throw new IllegalStateException();
      }

      final WebServiceSpecification webServiceSpecification = serviceNameMap.get( clazz );
      final String serviceName = webServiceSpecification.getServiceName();
      if ( serviceName == null ) {
        throw new IllegalStateException();
      }

      if ( webServiceSpecification.getServiceType().equals( ServiceType.JAX_WS ) ) {
        // build the url handling whether or not baseUrl ends with a slash
        // String baseUrl = repositoryMeta.getRepositoryLocation().getUrl();
        final URL url =
            new URL( baseUrl + ( baseUrl.endsWith( "/" ) ? "" : "/" ) + "webservices/" + serviceName + "?wsdl" ); //$NON-NLS-1$ //$NON-NLS-2$

        String key = url.toString() + '_' + serviceName + '_' + clazz.getName();
        if ( !serviceCache.containsKey( key ) ) {
          resultFuture = executor.submit( new Callable<Object>() {

            @Override
            public Object call() throws Exception {
              Service service = Service.create( url, new QName( NAMESPACE_URI, serviceName ) );
              T port = service.getPort( clazz );
              // add TRUST_USER if necessary
              if ( StringUtils.isNotBlank( System.getProperty( "pentaho.repository.client.attemptTrust" ) ) ) {
                ( (BindingProvider) port ).getRequestContext().put( MessageContext.HTTP_REQUEST_HEADERS,
                    Collections.singletonMap( TRUST_USER, Collections.singletonList( username ) ) );
              } else {
                // http basic authentication
                ( (BindingProvider) port ).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, username );
                ( (BindingProvider) port ).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password );
              }
              // accept cookies to maintain session on server
              ( (BindingProvider) port ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
              // support streaming binary data
              // TODO mlowery this is not portable between JAX-WS implementations (uses com.sun)
              Client client = ClientBuilder.newBuilder()
                      .property( ClientProperties.CHUNKED_ENCODING_SIZE, 8192 )
                      .build();
              SOAPBinding binding = (SOAPBinding) ( (BindingProvider) port ).getBinding();
              binding.setMTOMEnabled( true );
              return port;
            }
          } );
          serviceCache.put( key, resultFuture );
        } else {
          resultFuture = serviceCache.get( key );
        }
      } else {
        if ( webServiceSpecification.getServiceType().equals( ServiceType.JAX_RS ) ) {

          String key = baseUrl.toString() + '_' + serviceName + '_' + clazz.getName();
          if ( !serviceCache.containsKey( key ) ) {

            resultFuture = executor.submit( new Callable<Object>() {

              @Override
              public Object call() throws Exception {
                ClientConfig clientConfig = new ClientConfig();
                clientConfig.property( ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE );
                Client client = ClientBuilder.newClient( clientConfig );
                HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic( username, password );
                client.register( feature );

                Class<?>[] parameterTypes = new Class<?>[] { Client.class, URI.class };
                String factoryClassName = webServiceSpecification.getServiceClass().getName();
                factoryClassName = factoryClassName.substring( 0, factoryClassName.lastIndexOf( "$" ) );
                Class<?> factoryClass = Class.forName( factoryClassName );
                Method method =
                    factoryClass.getDeclaredMethod( webServiceSpecification.getServiceName(), parameterTypes );
                T port = (T) method.invoke( null, new Object[] { client, new URI( baseUrl + "/plugin" ) } );

                return port;
              }
            } );
            serviceCache.put( key, resultFuture );
          } else {
            resultFuture = serviceCache.get( key );
          }
        } else {
          resultFuture = null;
        }
      }

      try {
        if ( clazz.isInterface() ) {
          return UnifiedRepositoryInvocationHandler.forObject( (T) resultFuture.get(), clazz );
        } else {
          return (T) resultFuture.get();
        }

      } catch ( InterruptedException e ) {
        throw new RuntimeException( e );
      } catch ( ExecutionException e ) {
        Throwable cause = e.getCause();
        if ( cause != null ) {
          if ( cause instanceof RuntimeException ) {
            throw (RuntimeException) cause;
          } else if ( cause instanceof MalformedURLException ) {
            throw (MalformedURLException) cause;
          }
        }
        throw new RuntimeException( e );
      }
    }
  }

  @Override
  public synchronized void close() {
    serviceCache.clear();
  }

  private void registerWsSpecification( Class<?> serviceClass, String serviceName ) {
    registerSpecification( WebServiceSpecification.getWsServiceSpecification( serviceClass, serviceName ) );
  }

  private void registerRestSpecification( Class<?> serviceClass, String serviceName ) {
    try {
      registerSpecification( WebServiceSpecification.getRestServiceSpecification( serviceClass, serviceName ) );
    } catch ( NoSuchMethodException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch ( SecurityException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void registerSpecification( WebServiceSpecification webServiceSpecification ) {
    tempServiceNameMap.put( webServiceSpecification.getServiceClass(), webServiceSpecification );
  }

}
