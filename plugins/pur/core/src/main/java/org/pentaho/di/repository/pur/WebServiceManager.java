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

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;

import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.repository.pur.WebServiceSpecification.ServiceType;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;
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
    synchronized ( serviceCache ) {
      validateRequest( username );

      final WebServiceSpecification webServiceSpecification = serviceNameMap.get( clazz );
      final String serviceName = webServiceSpecification.getServiceName();
      if ( serviceName == null ) {
        throw new IllegalStateException();
      }

      final Future<Object> resultFuture = resolveServiceFuture( username, password, clazz,
          webServiceSpecification, serviceName );

      return unwrapFuture( resultFuture, clazz );
    }
  }

  private void validateRequest( final String username ) {
    // if this is true, a coder did not make sure that clearServices was called on disconnect
    if ( lastUsername != null && !lastUsername.equals( username ) ) {
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings( "unchecked" )
  private <T> Future<Object> resolveServiceFuture( final String username, final String password,
      final Class<T> clazz, final WebServiceSpecification webServiceSpecification,
      final String serviceName ) throws MalformedURLException {
    if ( webServiceSpecification.getServiceType().equals( ServiceType.JAX_WS ) ) {
      return getOrCreateJaxWsFuture( username, password, clazz, serviceName );
    } else if ( webServiceSpecification.getServiceType().equals( ServiceType.JAX_RS ) ) {
      return getOrCreateJaxRsFuture( username, password, clazz, webServiceSpecification, serviceName );
    }
    throw new IllegalStateException( "Unknown service type: " + webServiceSpecification.getServiceType() );
  }

  @SuppressWarnings( "unchecked" )
  private <T> Future<Object> getOrCreateJaxWsFuture( final String username, final String password,
      final Class<T> clazz, final String serviceName ) throws MalformedURLException {
    // build the url handling whether or not baseUrl ends with a slash
    final URL url =
        new URL( baseUrl + ( baseUrl.endsWith( "/" ) ? "" : "/" ) + "webservices/" + serviceName + "?wsdl" ); //$NON-NLS-1$ //$NON-NLS-2$

    String key = url.toString() + '_' + serviceName + '_' + clazz.getName();
    return serviceCache.computeIfAbsent( key,
        k -> executor.submit( () -> createJaxWsPort( username, password, clazz, serviceName, url ) ) );
  }

  @SuppressWarnings( "unchecked" )
  private <T> T createJaxWsPort( final String username, final String password, final Class<T> clazz,
      final String serviceName, final URL url ) {
    Service service = Service.create( url, new QName( NAMESPACE_URI, serviceName ) );
    T port = service.getPort( clazz );
    configureJaxWsAuthentication( (BindingProvider) port, username, password );
    // accept cookies to maintain session on server
    ( (BindingProvider) port ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    // support streaming binary data
    // TODO mlowery this is not portable between JAX-WS implementations (uses com.sun)
    ( (BindingProvider) port ).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192 );
    SOAPBinding binding = (SOAPBinding) ( (BindingProvider) port ).getBinding();
    binding.setMTOMEnabled( true );
    return port;
  }

  private void configureJaxWsAuthentication( final BindingProvider bp, final String username,
      final String password ) {
    String sessionId = getJSessionId();
    if ( isSessionAuthEnabled() && sessionId != null && !sessionId.trim().isEmpty() ) {
      // Use JSESSIONID cookie for authentication
      Map<String, java.util.List<String>> headers = new HashMap<>();
      headers.put( "Cookie", Collections.singletonList( "JSESSIONID=" + sessionId ) );
      bp.getRequestContext().put( MessageContext.HTTP_REQUEST_HEADERS, headers );
      bp.getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    } else if ( StringUtils.isNotBlank( System.getProperty( "pentaho.repository.client.attemptTrust" ) ) ) {
      // add TRUST_USER if necessary
      bp.getRequestContext().put( MessageContext.HTTP_REQUEST_HEADERS,
          Collections.singletonMap( TRUST_USER, Collections.singletonList( username ) ) );
      bp.getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    } else {
      // http basic authentication
      bp.getRequestContext().put( BindingProvider.USERNAME_PROPERTY, username );
      bp.getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password );
    }
  }

  @SuppressWarnings( "unchecked" )
  private <T> Future<Object> getOrCreateJaxRsFuture( final String username, final String password,
      final Class<T> clazz, final WebServiceSpecification webServiceSpecification,
      final String serviceName ) {
    String key = baseUrl + '_' + serviceName + '_' + clazz.getName();
    return serviceCache.computeIfAbsent( key,
        k -> executor.submit( () -> createJaxRsPort( username, password, webServiceSpecification ) ) );
  }

  @SuppressWarnings( "unchecked" )
  private <T> T createJaxRsPort( final String username, final String password,
      final WebServiceSpecification webServiceSpecification )
    throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, java.net.URISyntaxException,
    IllegalAccessException {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.property( ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE );
    Client client = ClientBuilder.newClient( clientConfig );
    configureJaxRsAuthentication( client, username, password );

    Class<?>[] parameterTypes = new Class<?>[] { Client.class, URI.class };
    String factoryClassName = webServiceSpecification.getServiceClass().getName();
    factoryClassName = factoryClassName.substring( 0, factoryClassName.lastIndexOf( "$" ) );
    Class<?> factoryClass = Class.forName( factoryClassName );
    Method method = factoryClass.getDeclaredMethod( webServiceSpecification.getServiceName(), parameterTypes );
    return (T) method.invoke( (Object) null, client, new URI( baseUrl + "/plugin" ) );
  }

  private void configureJaxRsAuthentication( final Client client, final String username, final String password ) {
    String sessionId = getJSessionId();
    if ( isSessionAuthEnabled() && sessionId != null && !sessionId.trim().isEmpty() ) {
      // Use JSESSIONID cookie for REST authentication
      client.register( (jakarta.ws.rs.client.ClientRequestFilter) requestContext ->
          requestContext.getHeaders().add( "Cookie", "JSESSIONID=" + sessionId )
      );
    } else {
      // Use basic authentication
      client.register( HttpAuthenticationFeature.basic( username, password ) );
    }
  }

  @SuppressWarnings( "unchecked" )
  private <T> T unwrapFuture( final Future<Object> resultFuture, final Class<T> clazz ) throws MalformedURLException {
    try {
      T service = (T) resultFuture.get();
      return clazz.isInterface() ? UnifiedRepositoryInvocationHandler.forObject( service, clazz ) : service;
    } catch ( InterruptedException e ) {
      throw new RuntimeException( e );
    } catch ( ExecutionException e ) {
      Throwable cause = e.getCause();
      if ( cause instanceof RuntimeException ) {
        throw (RuntimeException) cause;
      } else if ( cause instanceof MalformedURLException ) {
        throw (MalformedURLException) cause;
      }
      throw new RuntimeException( e );
    }
  }

  @Override
  @SuppressWarnings( "squid:S3776" )
  public void close() {
    synchronized ( serviceCache ) {
      for ( Map.Entry<String, Future<Object>> entry : serviceCache.entrySet() ) {
        String key = entry.getKey();
        Future<Object> future = entry.getValue();
        if ( future.isDone() ) {
          try {
            Object service = future.get();
            String className = key.substring( key.lastIndexOf( "_" ) + 1, key.length() );
            Class<?> clazz = Class.forName( className );
            // if the service has a logout method, call it
            try {
              Method[] methods = clazz.getMethods();
              if ( null != methods && methods.length > 0 ) {
                for ( Method method : methods ) {
                  if ( "logout".equals( method.getName() ) ) {
                    method.invoke( service );
                    break;
                  }
                }
              }
            } catch ( InvocationTargetException e ) {
              // Session expired errors during close are expected and can be ignored
              if ( !isSessionExpiredException( e.getCause() ) ) {
                // Unexpected invocation error during close
              }
            } catch ( Exception e ) {
              e.printStackTrace();
            }
            if ( service instanceof Closeable closeable ) {
              closeable.close();
            }
          } catch ( Exception e ) {
            if ( e instanceof InterruptedException ) {
              Thread.currentThread().interrupt();
            }
            e.printStackTrace();
          }
        }
      }
      serviceCache.clear();
    }
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

  /**
   * Check if an exception is related to session expiration (HTTP 401).
   * This helps identify expected errors during logout when the session has already expired.
   *
   * @param throwable The exception to check
   * @return true if the exception indicates a 401/session expired error, false otherwise
   */
  private boolean isSessionExpiredException( Throwable throwable ) {
    // Check the exception chain for ClientTransportException with 401 status
    Throwable current = throwable;
    while ( current != null ) {
      if ( current instanceof ClientTransportException ) {
        String message = current.getMessage();
        // Check if message contains "401" status code
        if ( message != null && message.contains( "401" ) ) {
          return true;
        }
      }
      current = current.getCause();
    }
    return false;
  }

  /**
   * Get a valid AuthenticationContext if session-based authentication is available.
   *
   * @return AuthenticationContext if session auth is valid, null otherwise
   */
  private AuthenticationContext getValidAuthContext() {
    try {
      AuthenticationContext authContext =
        SpoonSessionManager.getInstance().getAuthenticationContext( baseUrl );

      if ( authContext != null && authContext.isAuthenticated()
           && authContext.validateAndClearIfExpired() ) {
        return authContext;
      }
    } catch ( Exception e ) {
      // Session auth not available (e.g., running in headless mode)
    }
    return null;
  }

  private boolean isSessionAuthEnabled() {
    return getValidAuthContext() != null;
  }

  private String getJSessionId() {
    AuthenticationContext authContext = getValidAuthContext();
    return authContext != null ? authContext.getJSessionId() : null;
  }

}
