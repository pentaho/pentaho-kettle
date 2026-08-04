/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.repository.pur;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.CookieManager;
import java.net.CookiePolicy;
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
import com.sun.xml.ws.developer.HttpConfigFeature;
import com.sun.xml.ws.developer.JAXWSProperties;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.SOAPBinding;

import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
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

  private static final LogChannelInterface log =
      KettleLogStore.getLogChannelInterfaceFactory().create( WebServiceManager.class );

  private static final ExecutorService executor = ExecutorUtil.getExecutor();

  private final Map<String, Future<Object>> serviceCache = new HashMap<String, Future<Object>>();

  /**
   * Cookie jar shared by every JAX-WS port created by this manager.
   * <p>
   * By default the JAX-WS RI gives each port its own {@code HttpConfigFeature}, and therefore its own
   * {@link CookieManager} (see {@code com.sun.xml.ws.transport.DeferredTransportPipe}). Because the ports all
   * authenticate against the same server, that means a single repository connection would establish a separate
   * {@code HttpSession} per service. Logging out of one of them left the others stranded, and the server-side
   * logout listeners are keyed by user rather than by session, so the first logout tore down state the remaining
   * sessions still needed and they responded {@code HTTP 401}.
   * <p>
   * Sharing one jar collapses those sessions into a single one, which can be logged out exactly once and reset
   * on {@link #close()}.
   */
  private final CookieManager cookieManager = new CookieManager( null, CookiePolicy.ACCEPT_ALL );

  private final HttpConfigFeature httpConfigFeature = new HttpConfigFeature( cookieManager );

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
    T port = service.getPort( clazz, httpConfigFeature );
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
    AuthenticationContext authContext = getValidAuthContext( username );
    String sessionId = authContext != null ? authContext.getJSessionId() : null;
    if ( authContext != null && sessionId != null && !sessionId.trim().isEmpty() ) {
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
    AuthenticationContext authContext = getValidAuthContext( username );
    String sessionId = authContext != null ? authContext.getJSessionId() : null;
    if ( authContext != null && sessionId != null && !sessionId.trim().isEmpty() ) {
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
  public void close() {
    synchronized ( serviceCache ) {
      logoutSharedSession();
      for ( Future<Object> future : serviceCache.values() ) {
        closeService( future );
      }
      serviceCache.clear();
      clearClientSessionState();
    }
  }

  /**
   * Logs out of the server session shared by every port created by this manager.
   * <p>
   * All ports share {@link #cookieManager}, so they share a single {@code HttpSession} and one successful
   * {@code logout()} ends it for all of them. Invoking {@code logout()} on each port in turn — as this method used
   * to do — made the first call invalidate the session and every subsequent call fail with {@code HTTP 401}.
   */
  private void logoutSharedSession() {
    for ( Map.Entry<String, Future<Object>> entry : serviceCache.entrySet() ) {
      Future<Object> future = entry.getValue();
      if ( !future.isDone() ) {
        // still being created; blocking here would stall disconnect
        continue;
      }
      if ( invokeLogout( entry.getKey(), future ) ) {
        // the shared session is gone, no other port needs to log out
        return;
      }
    }
  }

  /**
   * @return {@code true} when {@code logout()} was invoked and the shared server session can be considered closed
   */
  private boolean invokeLogout( String key, Future<Object> future ) {
    try {
      Object service = future.get();
      Class<?> clazz = Class.forName( key.substring( key.lastIndexOf( '_' ) + 1 ) );
      Method logout = findLogoutMethod( clazz );
      if ( logout == null ) {
        return false;
      }
      logout.invoke( service );
      return true;
    } catch ( InvocationTargetException e ) {
      // A 401 means the session is already gone, which is the outcome logout is trying to achieve.
      if ( isSessionExpiredException( e.getCause() ) ) {
        return true;
      }
      log.logDebug( "Unexpected error invoking logout() during close", e.getCause() );
      return false;
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      return false;
    } catch ( Exception e ) {
      log.logDebug( "Unable to invoke logout() during close", e );
      return false;
    }
  }

  private Method findLogoutMethod( Class<?> clazz ) {
    for ( Method method : clazz.getMethods() ) {
      if ( "logout".equals( method.getName() ) && method.getParameterCount() == 0 ) {
        return method;
      }
    }
    return null;
  }

  private void closeService( Future<Object> future ) {
    if ( !future.isDone() ) {
      return;
    }
    try {
      if ( future.get() instanceof Closeable closeable ) {
        closeable.close();
      }
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
    } catch ( Exception e ) {
      log.logDebug( "Unable to close service during disconnect", e );
    }
  }

  /**
   * Drops every cookie cached by this manager so that a later connection cannot replay a session cookie that
   * belongs to a previous connection, or to a previous user.
   */
  private void clearClientSessionState() {
    cookieManager.getCookieStore().removeAll();
  }

  /**
   * Exposed for testing so the shared cookie jar can be inspected.
   */
  CookieManager getCookieManager() {
    return cookieManager;
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
   * Get a valid AuthenticationContext if session-based authentication is available for the given user.
   * <p>
   * A cached session is only returned when it was issued to {@code username}. Reusing a session that
   * belongs to a different user would silently ignore the credentials supplied for this connection
   * and send the previous user's cookie instead.
   *
   * @param username The user the service is being created for
   * @return AuthenticationContext if session auth is valid for this user, null otherwise
   */
  private AuthenticationContext getValidAuthContext( String username ) {
    try {
      AuthenticationContext authContext =
        SpoonSessionManager.getInstance().getAuthenticationContext( baseUrl );

      if ( authContext != null && authContext.isAuthenticated()
           && authContext.validateAndClearIfExpired() ) {
        if ( !authContext.isSessionOwnedBy( username ) ) {
          log.logDetailed( "Ignoring cached session for " + baseUrl
            + " because it was issued to a different user" );
          return null;
        }
        return authContext;
      }
    } catch ( Exception e ) {
      // Session auth not available (e.g., running in headless mode)
    }
    return null;
  }

}
