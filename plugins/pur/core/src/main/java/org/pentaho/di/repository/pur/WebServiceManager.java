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

import com.pentaho.di.services.PentahoDiPlugin;
import com.pentaho.pdi.ws.IRepositorySyncWebService;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.pan.auth.CredentialProvider;
import org.pentaho.di.pan.auth.DefaultCredentialProvider;
import org.pentaho.di.repository.pur.WebServiceSpecification.ServiceType;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService;
import org.pentaho.platform.security.policy.rolebased.ws.IRoleAuthorizationPolicyRoleBindingDaoWebService;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;
import org.pentaho.platform.security.userroledao.ws.IUserRoleWebService;

import javax.xml.namespace.QName;
import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Web service factory. Not a true factory in that the things that this factory
 * can create are not configurable. But it
 * does cache the services.
 *
 * @author mlowery
 */
public class WebServiceManager implements ServiceManager {

  private static final LogChannelInterface log = new LogChannel( WebServiceManager.class.getSimpleName() );

  private static String message( String key, String... tokens ) {
    return BaseMessages.getString( WebServiceManager.class, key, tokens );
  }

  private static final String NAMESPACE_URI = "http://www.pentaho.org/ws/1.0"; //$NON-NLS-1$
  private static final String WEBSERVICES_PATH = "webservices/";
  private static final String PLUGIN_PATH = "/plugin";
  private static final ExecutorService executor = ExecutorUtil.getExecutor();
  private final Map<String, Future<Object>> serviceCache = new HashMap<>();
  private final Map<Class<?>, WebServiceSpecification> serviceNameMap;
  private final String baseUrl;
  private final String lastUsername;
  private final CredentialHeaderFactory credentialHeaderFactory;
  private Map<Class<?>, WebServiceSpecification> tempServiceNameMap; // hold the map while building

  /**
   * Accepts a {@link CredentialProvider} to eliminate the static singleton dependency on
   * {@code BrowserAuthSessionHolder}.
   */
  public WebServiceManager( String baseUrl, String username, CredentialProvider credentialProvider ) {
    this.baseUrl = baseUrl;
    this.lastUsername = username;
    this.credentialHeaderFactory = new CredentialHeaderFactory( credentialProvider );
    tempServiceNameMap = new HashMap<>();
    registerWsSpecification( IUnifiedRepositoryJaxwsWebService.class, "unifiedRepository" ); //$NON-NLS-1$
    registerWsSpecification( IRepositorySyncWebService.class, "repositorySync" ); //$NON-NLS-1$
    registerWsSpecification( IUserRoleListWebService.class, "userRoleListService" ); //$NON-NLS-1$
    registerWsSpecification( IUserRoleWebService.class, "userRoleService" ); //$NON-NLS-1$
    registerWsSpecification( IRoleAuthorizationPolicyRoleBindingDaoWebService.class, "roleBindingDao" ); //$NON-NLS-1$
    registerWsSpecification( IAuthorizationPolicyWebService.class, "authorizationPolicy" ); //$NON-NLS-1$

    registerRestSpecification( PentahoDiPlugin.PurRepositoryPluginApiRevision.class,
      "purRepositoryPluginApiRevision" ); //$NON-NLS-1$

    this.serviceNameMap = Collections.unmodifiableMap( tempServiceNameMap );
    tempServiceNameMap = null;
  }

  public WebServiceManager( String baseUrl, String username ) {
    this( baseUrl, username, new DefaultCredentialProvider() );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T> T createService( final String username, final String password, final Class<T> clazz )
    throws MalformedURLException {
    synchronized ( serviceCache ) {
      validateRequest( username );
      final WebServiceSpecification webServiceSpecification = serviceNameMap.get( clazz );
      if ( webServiceSpecification == null ) {
        throw new IllegalStateException( message( "WebServiceManager.UnknownServiceClass", clazz.getName() ) );
      }
      final String serviceName = webServiceSpecification.getServiceName();
      if ( serviceName == null ) {
        throw new IllegalStateException();
      }
      return unwrapFuture( resolveServiceFuture( username, password, clazz, webServiceSpecification, serviceName ),
        clazz );
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
            String className = key.substring( key.lastIndexOf( "_" ) + 1 );
            Class<?> clazz = Class.forName( className );
            invokeLogoutIfPresent( service, clazz );
            if ( service instanceof Closeable closeable ) {
              closeable.close();
            }
          } catch ( Exception e ) {
            if ( e instanceof InterruptedException ) {
              Thread.currentThread().interrupt();
            }
            log.logError( message( "WebServiceManager.CloseCachedServiceError" ), e );
          }
        }
      }
      serviceCache.clear();
    }
  }

  private void validateRequest( String username ) {
    if ( lastUsername != null && !lastUsername.equals( username ) ) {
      throw new IllegalStateException();
    }
  }

  private Future<Object> immediateFuture( Callable<Object> callable ) {
    try {
      return CompletableFuture.completedFuture( callable.call() );
    } catch ( Exception e ) {
      return CompletableFuture.failedFuture( e );
    }
  }

  private <T> T createJaxWsPort( URL url, String serviceName, Class<T> clazz,
                                 String username, String password ) throws Exception {
    Service service = Service.create( url, new QName( NAMESPACE_URI, serviceName ) );
    T port = service.getPort( clazz );

    Map<String, List<String>> authHeaders = credentialHeaderFactory.forSoapRequest( baseUrl, username );
    if ( !authHeaders.isEmpty() ) {
      ( (BindingProvider) port ).getRequestContext().put( MessageContext.HTTP_REQUEST_HEADERS, authHeaders );
    } else {
      ( (BindingProvider) port ).getRequestContext().put( BindingProvider.USERNAME_PROPERTY, username );
      ( (BindingProvider) port ).getRequestContext().put( BindingProvider.PASSWORD_PROPERTY, password );
    }
    ( (BindingProvider) port ).getRequestContext().put( BindingProvider.SESSION_MAINTAIN_PROPERTY, true );
    ( (BindingProvider) port ).getRequestContext().put( JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 8192 );
    SOAPBinding binding = (SOAPBinding) ( (BindingProvider) port ).getBinding();
    binding.setMTOMEnabled( true );
    return port;
  }

  private AuthenticationContext getValidAuthContext() {
    try {
      AuthenticationContext authenticationContext = SpoonSessionManager.getInstance()
        .getAuthenticationContext( baseUrl );
      if ( authenticationContext == null || !authenticationContext.isAuthenticated() ) {
        return null;
      }
      return authenticationContext.validateAndClearIfExpired() ? authenticationContext : null;
    } catch ( RuntimeException e ) {
      return null;
    }
  }

  private void configureJaxWsAuthentication( BindingProvider bindingProvider, String username, String password ) {
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    AuthenticationContext authenticationContext = getValidAuthContext();
    String sessionId = authenticationContext == null ? null : authenticationContext.getJSessionId();

    if ( sessionId != null && !sessionId.trim().isEmpty() ) {
      requestContext.put( MessageContext.HTTP_REQUEST_HEADERS,
        Collections.singletonMap( "Cookie", Collections.singletonList( "JSESSIONID=" + sessionId ) ) );
    } else if ( System.getProperty( "pentaho.repository.client.attemptTrust" ) != null
      && !System.getProperty( "pentaho.repository.client.attemptTrust" ).trim().isEmpty() ) {
      requestContext.put( MessageContext.HTTP_REQUEST_HEADERS,
        Collections.singletonMap( "_trust_user_", Collections.singletonList( username ) ) );
    } else {
      requestContext.put( BindingProvider.USERNAME_PROPERTY, username );
      requestContext.put( BindingProvider.PASSWORD_PROPERTY, password );
    }

    requestContext.put( BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE );
  }

  private void configureJaxRsAuthentication( Client client, String username, String password ) {
    AuthenticationContext authenticationContext = getValidAuthContext();
    String sessionId = authenticationContext == null ? null : authenticationContext.getJSessionId();

    if ( sessionId != null && !sessionId.trim().isEmpty() ) {
      client.register(
        (ClientRequestFilter) ctx -> ctx.getHeaders().putSingle( "Cookie", "JSESSIONID=" + sessionId ) );
      return;
    }

    client.register( HttpAuthenticationFeature.basic( username, password ) );
  }

  @SuppressWarnings( "unchecked" )
  private <T> T createJaxRsPort( String username, String password, WebServiceSpecification webServiceSpecification )
    throws ReflectiveOperationException {
    return (T) createJaxRsClientHolder( username, password, webServiceSpecification ).proxy();
  }

  private JaxRsClientHolder createJaxRsClientHolder( String username, String password,
                                                     WebServiceSpecification webServiceSpecification )
    throws ReflectiveOperationException {
    ClientConfig clientConfig = new ClientConfig();
    clientConfig.property( ClientProperties.FOLLOW_REDIRECTS, Boolean.TRUE );
    Client client = ClientBuilder.newClient( clientConfig );
    configureJaxRsAuthentication( client, username, password );

    Class<?>[] parameterTypes = new Class<?>[] { Client.class, URI.class };
    String factoryClassName = webServiceSpecification.getServiceClass().getName();
    factoryClassName = factoryClassName.substring( 0, factoryClassName.lastIndexOf( "$" ) );
    Class<?> factoryClass = Class.forName( factoryClassName );
    Method method = factoryClass.getDeclaredMethod( webServiceSpecification.getServiceName(), parameterTypes );
    Object port = method.invoke( null, new Object[] { client, URI.create( baseUrl + PLUGIN_PATH ) } );
    return new JaxRsClientHolder( port, client );
  }

  private Future<Object> getOrCreateJaxWsFuture( String username, String password, Class<?> clazz, String serviceName )
    throws MalformedURLException {
    URL url = buildWsdlUrl( serviceName );
    String key = url.toString() + '_' + serviceName + '_' + clazz.getName();
    Callable<Object> soapCreation = () -> createJaxWsPort( url, serviceName, clazz, username, password );
    return getOrCreateFuture( key, shouldCacheServices(), soapCreation );
  }

  private Future<Object> getOrCreateJaxRsFuture( String username, String password, Class<?> clazz,
                                                 WebServiceSpecification webServiceSpecification,
                                                 String serviceName ) {
    String key = baseUrl + '_' + serviceName + '_' + clazz.getName();
    Callable<Object> restCreation = () -> createJaxRsClientHolder( username, password, webServiceSpecification );
    return getOrCreateFuture( key, shouldCacheServices(), restCreation );
  }

  private Future<Object> resolveServiceFuture( String username, String password, Class<?> clazz,
                                               WebServiceSpecification webServiceSpecification,
                                               String serviceName ) throws MalformedURLException {
    if ( webServiceSpecification.getServiceType().equals( ServiceType.JAX_WS ) ) {
      return getOrCreateJaxWsFuture( username, password, clazz, serviceName );
    }
    if ( webServiceSpecification.getServiceType().equals( ServiceType.JAX_RS ) ) {
      return getOrCreateJaxRsFuture( username, password, clazz, webServiceSpecification, serviceName );
    }
    throw new IllegalStateException( message( "WebServiceManager.UnknownServiceType",
      String.valueOf( webServiceSpecification.getServiceType() ) ) );
  }

  @SuppressWarnings( "unchecked" )
  private <T> T unwrapFuture( Future<Object> resultFuture, Class<T> clazz ) throws MalformedURLException {
    try {
      Object raw = resultFuture.get();
      Object service = raw instanceof JaxRsClientHolder ? ( (JaxRsClientHolder) raw ).proxy() : raw;
      if ( clazz.isInterface() ) {
        return UnifiedRepositoryInvocationHandler.forObject( (T) service, clazz );
      }
      return (T) service;
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException( message( "WebServiceManager.InterruptedCreatingService", clazz.getName() ),
        e );
    } catch ( ExecutionException e ) {
      Throwable cause = e.getCause();
      if ( cause != null ) {
        if ( cause instanceof RuntimeException ) {
          throw (RuntimeException) cause;
        }
        if ( cause instanceof MalformedURLException ) {
          throw (MalformedURLException) cause;
        }
      }
      throw new IllegalStateException( message( "WebServiceManager.UnableToCreateService", clazz.getName() ), e );
    }
  }

  private boolean shouldCacheServices() {
    return !credentialHeaderFactory.hasNonBasicCredential( baseUrl );
  }

  private Future<Object> getOrCreateFuture( String key, boolean useCache, Callable<Object> creationTask ) {
    if ( useCache ) {
      return serviceCache.computeIfAbsent( key, k -> executor.submit( creationTask ) );
    }
    return immediateFuture( creationTask );
  }

  private URL buildWsdlUrl( String serviceName ) throws MalformedURLException {
    return URI.create( normalizeBaseUrl() + WEBSERVICES_PATH + serviceName + "?wsdl" ).toURL();
  }

  private String normalizeBaseUrl() {
    return baseUrl.endsWith( "/" ) ? baseUrl : baseUrl + '/';
  }

  private void registerWsSpecification( Class<?> serviceClass, String serviceName ) {
    registerSpecification( WebServiceSpecification.getWsServiceSpecification( serviceClass, serviceName ) );
  }

  private void registerRestSpecification( Class<?> serviceClass, String serviceName ) {
    try {
      registerSpecification( WebServiceSpecification.getRestServiceSpecification( serviceClass, serviceName ) );
    } catch ( NoSuchMethodException | SecurityException e ) {
      log.logError( message( "WebServiceManager.UnableToRegisterRestSpecification",
        serviceClass.getName() ), e );
    }
  }

  private void invokeLogoutIfPresent( Object service, Class<?> clazz ) {
    try {
      for ( Method method : clazz.getMethods() ) {
        if ( "logout".equals( method.getName() ) ) {
          invokeLogout( service, method );
          break;
        }
      }
    } catch ( Exception e ) {
      log.logError( message( "WebServiceManager.ServiceLogoutError" ), e );
    }
  }

  private void invokeLogout( Object service, Method method ) throws IllegalAccessException {
    try {
      method.invoke( service );
    } catch ( InvocationTargetException ite ) {
      if ( !isSessionExpiredException( ite.getCause() ) ) {
        log.logDebug( message( "WebServiceManager.UnexpectedLogoutError" ), ite.getCause() );
      }
    }
  }

  private void registerSpecification( WebServiceSpecification webServiceSpecification ) {
    tempServiceNameMap.put( webServiceSpecification.getServiceClass(), webServiceSpecification );
  }

  /**
   * Check if an exception is related to session expiration (HTTP 401).
   * Helps identify expected errors during logout when the session has already
   * expired.
   *
   * @param throwable the exception to check
   * @return true if the exception indicates a 401/session-expired error, false
   * otherwise
   */
  private boolean isSessionExpiredException( Throwable throwable ) {
    Throwable current = throwable;
    while ( current != null ) {
      if ( current instanceof ClientTransportException ) {
        String message = current.getMessage();
        if ( message != null && message.contains( "401" ) ) {
          return true;
        }
      }
      current = current.getCause();
    }
    return false;
  }

  /**
   * Wraps a JAX-RS proxy together with its underlying {@link Client} so that
   * the client can be closed when the service cache is cleared.
   */
  private record JaxRsClientHolder(Object proxy, Client jaxRsClient) implements Closeable {

    @Override
    public void close() {
      jaxRsClient.close();
    }
  }

}
