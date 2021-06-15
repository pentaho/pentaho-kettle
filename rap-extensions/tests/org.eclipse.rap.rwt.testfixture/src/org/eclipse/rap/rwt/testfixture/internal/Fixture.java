/*******************************************************************************
 * Copyright (c) 2002, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import static org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil.getAdapter;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getProtocolWriter;
import static org.eclipse.rap.rwt.internal.service.ContextProvider.getUISession;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.Application.OperationMode;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.internal.SingletonManager;
import org.eclipse.rap.rwt.internal.application.ApplicationContextHelper;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.client.ClientSelector;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.lifecycle.DisplayUtil;
import org.eclipse.rap.rwt.internal.lifecycle.IUIThreadHolder;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.lifecycle.PhaseId;
import org.eclipse.rap.rwt.internal.lifecycle.RWTLifeCycle;
import org.eclipse.rap.rwt.internal.lifecycle.WidgetUtil;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.resources.ResourceDirectory;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.service.UISessionBuilder;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;
import org.eclipse.rap.rwt.internal.util.HTTP;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.testfixture.internal.engine.ThemeManagerHelper;
import org.eclipse.swt.internal.widgets.WidgetRemoteAdapter;
import org.eclipse.swt.internal.widgets.displaykit.DisplayLCA;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;


/**
 * Test fixture for RWT.
 * <p>
 * <strong>IMPORTANT:</strong> This class is <em>not</em> part the public RAP
 * API. It may change or disappear without further notice. Use this class at
 * your own risk.
 * </p>
 */
@SuppressWarnings( "deprecation" )
public final class Fixture {

  private static final String HEAD = "head";
  private static final String OPERATIONS = "operations";
  private static final String SET = "set";
  private static final String CALL = "call";
  private static final String NOTIFY = "notify";
  public final static File TEMP_DIR = createTempDir();
  public static final File WEB_CONTEXT_DIR = new File( TEMP_DIR, "testapp" );
  public static final File WEB_CONTEXT_RWT_RESOURCES_DIR
    = new File( WEB_CONTEXT_DIR, ResourceDirectory.DIRNAME );
  public static final String IMAGE1 = "resources/images/image1.gif";
  public static final String IMAGE2 = "resources/images/image2.gif";
  public static final String IMAGE3 = "resources/images/image3.gif";
  public static final String IMAGE_100x50 = "resources/images/test-100x50.png";
  public static final String IMAGE_50x100 = "resources/images/test-50x100.png";

  private static final String SYS_PROP_USE_PERFORMANCE_OPTIMIZATIONS
    = "usePerformanceOptimizations";

  static {
    setSkipResourceRegistration( isPerformanceOptimizationsEnabled() );
    setSkipResourceDeletion( isPerformanceOptimizationsEnabled() );
  }

  private static ServletContext servletContext;
  private static ApplicationContextImpl applicationContext;

  ////////////////////////////////////////////
  // Methods to control global servlet context

  public static ServletContext createServletContext() {
    servletContext = new TestServletContext();
    return servletContext;
  }

  public static ServletContext getServletContext() {
    return servletContext;
  }

  public static void disposeOfServletContext() {
    servletContext = null;
  }

  public static void setInitParameter( String name, String value ) {
    ensureServletContext();
    servletContext.setInitParameter( name, value );
  }

  ////////////////////////////////////////
  // Methods to control ApplicationContext

  public static void createApplicationContext() {
    createApplicationContext( false );
  }

  public static void createApplicationContext( final boolean useDefaultResourceManager ) {
    ensureServletContext();
    createWebContextDirectory();
    ApplicationConfiguration config = new FixtureApplicationConfiguration();
    ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader( Fixture.class.getClassLoader() );
      applicationContext = new ApplicationContextImpl( config, servletContext ) {
        @Override
        protected ThemeManager createThemeManager() {
          return ThemeManagerHelper.ensureThemeManager();
        }
        @Override
        protected ResourceManager createResourceManager() {
          if( useDefaultResourceManager ) {
            return super.createResourceManager();
          }
          return new TestResourceManager();
        }
      };
      applicationContext.attachToServletContext();
      SingletonManager.install( applicationContext );
      applicationContext.activate();
    } finally {
      Thread.currentThread().setContextClassLoader( oldContextClassLoader );
    }
  }

  public static void disposeOfApplicationContext() {
    applicationContext.deactivate();
    applicationContext.removeFromServletContext();
    disposeOfServletContext();
    // TODO [ApplicationContext]: At the time being this improves RWTAllTestSuite performance by
    //      50% on my machine without causing any test to fail. However this has a bad smell
    //      with it, so I introduced a flag that can be switch on for fast tests on local machines
    //      and switched of for the integration build tests. Think about a less intrusive solution.
    if( !isPerformanceOptimizationsEnabled() ) {
      deleteWebContextDirectory();
    }
    applicationContext = null;
  }


  ////////////////////////////////////
  // Methods to control ServiceContext

  public static void createServiceContext() {
    TestRequest request = new TestRequest();
    ClientMessage message = createEmptyMessage();
    request.setBody( message.toString() );
    TestResponse response = new TestResponse();
    HttpSession session = createTestSession();
    request.setSession( session );
    createNewServiceContext( request, response );
    ProtocolUtil.setClientMessage( message );
  }

  private static TestHttpSession createTestSession() {
    TestHttpSession result = new TestHttpSession();
    if( servletContext != null ) {
      result.setServletContext( servletContext );
    }
    return result;
  }

  public static void disposeOfServiceContext() {
    ThemeManagerHelper.resetThemeManagerIfNeeded();
    HttpSession session = ContextProvider.getRequest().getSession();
    ContextProvider.disposeContext();
    session.invalidate();
  }


  /////////////////////////////////////////////////////////////////////
  // Methods to control web context directories and resource management

  public static void createWebContextDirectory() {
    WEB_CONTEXT_DIR.mkdirs();
  }

  public static void deleteWebContextDirectory() {
    if( WEB_CONTEXT_DIR.exists() ) {
      FileUtil.delete( WEB_CONTEXT_DIR );
    }
  }

  //////////////////////////////
  // general setup and tear down

  public static void setUp() {
    setUp( false );
  }

  public static void setUp( boolean useDefaultResourceManager ) {
    createApplicationContext( useDefaultResourceManager );
    createServiceContext();
    fakeClient( new WebClient() );
  }

  public static void tearDown() {
    disposeOfServiceContext();
    disposeOfApplicationContext();
    disposeOfServletContext();
  }


  ////////////////////
  // LifeCycle helpers

  public static void readDataAndProcessAction( Display display ) {
    DisplayLCA displayLCA = DisplayUtil.getLCA( display );
    fakePhase( PhaseId.READ_DATA );
    displayLCA.readData( display );
    Fixture.preserveWidgets();
    fakePhase( PhaseId.PROCESS_ACTION );
    while( Display.getCurrent().readAndDispatch() ) {
    }
  }

  public static void readDataAndProcessAction( Widget widget ) {
    WidgetLCA widgetLCA = WidgetUtil.getLCA( widget );
    fakePhase( PhaseId.READ_DATA );
    widgetLCA.readData( widget );
    fakePhase( PhaseId.PROCESS_ACTION );
    while( Display.getCurrent().readAndDispatch() ) {
    }
  }

  public static void markInitialized( Widget widget ) {
    ( ( WidgetRemoteAdapter )getAdapter( widget ) ).setInitialized( true );
  }

  public static void markInitialized( Display display ) {
    ( ( WidgetRemoteAdapter )getAdapter( display ) ).setInitialized( true );
  }

  public static void preserveWidgets() {
    Display display = LifeCycleUtil.getSessionDisplay();
    DisplayLCA displayLCA = DisplayUtil.getLCA( display );
    PhaseId bufferedPhaseId = CurrentPhase.get();
    fakePhase( PhaseId.RENDER );
    displayLCA.clearPreserved( display );
    fakePhase( PhaseId.READ_DATA );
    displayLCA.preserveValues( display );
    fakePhase( bufferedPhaseId );
  }

  public static void clearPreserved() {
    Display display = LifeCycleUtil.getSessionDisplay();
    DisplayLCA displayLCA = DisplayUtil.getLCA( display );
    PhaseId bufferedPhaseId = CurrentPhase.get();
    fakePhase( PhaseId.RENDER );
    displayLCA.clearPreserved( display );
    fakePhase( bufferedPhaseId );
  }

  public static TestMessage getProtocolMessage() {
    TestResponse response = ( TestResponse )ContextProvider.getResponse();
    finishResponse( response );
    return new TestMessage( JsonObject.readFrom( response.getContent() ) );
  }

  private static void finishResponse( TestResponse response ) {
    if( response.getContent().length() == 0 ) {
      try {
        getProtocolWriter().createMessage().toJson().writeTo( response.getWriter() );
      } catch( IOException exception ) {
        throw new IllegalStateException( "Failed to get response writer", exception );
      }
    }
  }

  public static void fakeClient( Client client ) {
    getUISession().setAttribute( ClientSelector.SELECTED_CLIENT, client );
  }

  public static void fakeConnection( Connection connection ) {
    ( ( UISessionImpl )getUISession() ).setConnection( connection );
  }

  public static TestRequest fakeNewRequest() {
    TestRequest request = createNewRequest( HTTP.METHOD_POST );
    request.setContentType( HTTP.CONTENT_TYPE_JSON );
    request.setParameter( "cid", getConnectionId() );
    ClientMessage emptyMessage = createEmptyMessage();
    request.setBody( emptyMessage.toString() );
    createNewServiceContext( request, new TestResponse() );
    ProtocolUtil.setClientMessage( emptyMessage );
    fakeResponseWriter();
    return request;
  }

  public static TestRequest fakeNewGetRequest() {
    TestRequest request = createNewRequest( HTTP.METHOD_GET );
    createNewServiceContext( request, new TestResponse() );
    return request;
  }

  private static TestRequest createNewRequest( String method ) {
    TestRequest request = new TestRequest();
    request.setMethod( method );
    request.setSession( ContextProvider.getRequest().getSession() );
    return request;
  }

  private static void createNewServiceContext( HttpServletRequest request,
                                               HttpServletResponse response )
  {
    ContextProvider.disposeContext();
    ServiceContext serviceContext = new ServiceContext( request, response, applicationContext );
    serviceContext.setServiceStore( new ServiceStore() );
    ContextProvider.setContext( serviceContext );
    ensureUISession( serviceContext );
  }

  private static ClientMessage createEmptyMessage() {
    return new ClientMessage( new JsonObject()
      .add( HEAD, new JsonObject() )
      .add( OPERATIONS, new JsonArray() ) );
  }

  private static String getConnectionId() {
    UISessionImpl uiSession = ( UISessionImpl )getUISession();
    return uiSession == null ? null : uiSession.getConnectionId();
  }

  public static void fakeHeadParameter( String key, long value ) {
    fakeHeadParameter( key, JsonValue.valueOf( value ) );
  }

  public static void fakeHeadParameter( String key, boolean value ) {
    fakeHeadParameter( key, JsonValue.valueOf( value ) );
  }

  public static void fakeHeadParameter( String key, String value ) {
    fakeHeadParameter( key, JsonValue.valueOf( value ) );
  }

  public static void fakeHeadParameter( String key, JsonValue value ) {
    TestRequest request = ( TestRequest )ContextProvider.getRequest();
    String json = request.getBody();
    try {
      JsonObject message = JsonObject.readFrom( json );
      JsonObject header = message.get( HEAD ).asObject();
      header.add( key, value );
      request.setBody( message.toString() );
      ProtocolUtil.setClientMessage( new ClientMessage( message ) );
    } catch( Exception exception ) {
      throw new RuntimeException( "Failed to add header parameter", exception );
    }
  }

  public static void fakeSetProperty( String target, String propertyName, long propertyValue ) {
    fakeSetProperty( target, propertyName, JsonValue.valueOf( propertyValue ) );
  }

  public static void fakeSetProperty( String target, String propertyName, boolean propertyValue ) {
    fakeSetProperty( target, propertyName, JsonValue.valueOf( propertyValue ) );
  }

  public static void fakeSetProperty( String target, String propertyName, String propertyValue ) {
    fakeSetProperty( target, propertyName, JsonValue.valueOf( propertyValue ) );
  }

  public static void fakeSetProperty( String target, String key, JsonValue value ) {
    fakeSetOperation( target, new JsonObject().add( key, value ) );
  }

  public static void fakeSetOperation( String target, JsonObject properties ) {
    TestRequest request = ( TestRequest )ContextProvider.getRequest();
    String json = request.getBody();
    try {
      JsonObject message = JsonObject.readFrom( json );
      JsonArray operations = message.get( OPERATIONS ).asArray();
      JsonArray newOperation = new JsonArray();
      newOperation.add( SET );
      newOperation.add( target );
      newOperation.add( properties != null ? properties : new JsonObject() );
      operations.add( newOperation );
      request.setBody( message.toString() );
      ProtocolUtil.setClientMessage( new ClientMessage( message ) );
    } catch( Exception exception ) {
      throw new RuntimeException( "Failed to add set operation", exception );
    }
  }

  public static void fakeNotifyOperation( String target,
                                          String eventName,
                                          JsonObject properties )
  {
    TestRequest request = ( TestRequest )ContextProvider.getRequest();
    String json = request.getBody();
    try {
      JsonObject message = JsonObject.readFrom( json );
      JsonArray operations = message.get( OPERATIONS ).asArray();
      JsonArray newOperation = new JsonArray();
      newOperation.add( NOTIFY );
      newOperation.add( target );
      newOperation.add( eventName );
      newOperation.add( properties != null ? properties : new JsonObject() );
      operations.add( newOperation );
      request.setBody( message.toString() );
      ProtocolUtil.setClientMessage( new ClientMessage( message ) );
    } catch( Exception exception ) {
      throw new RuntimeException( "Failed to add notify operation", exception );
    }
  }

  public static void fakeCallOperation( String target,
                                        String methodName,
                                        JsonObject parameters )
  {
    TestRequest request = ( TestRequest )ContextProvider.getRequest();
    String json = request.getBody();
    try {
      JsonObject message = JsonObject.readFrom( json );
      JsonArray operations = message.get( OPERATIONS ).asArray();
      JsonArray newOperation = new JsonArray();
      newOperation.add( CALL );
      newOperation.add( target );
      newOperation.add( methodName );
      newOperation.add( parameters != null ? parameters : new JsonObject() );
      operations.add( newOperation );
      request.setBody( message.toString() );
      ProtocolUtil.setClientMessage( new ClientMessage( message ) );
    } catch( Exception exception ) {
      throw new RuntimeException( "Failed to add call operation", exception );
    }
  }

  public static void fakeResponseWriter() {
    TestResponse testResponse = ( TestResponse )ContextProvider.getResponse();
    testResponse.clearContent();
    ContextProvider.getContext().resetProtocolWriter();
  }

  public static void fakePhase( PhaseId phase ) {
    CurrentPhase.set( phase );
  }

  public static void executeLifeCycleFromServerThread() {
    IUIThreadHolder threadHolder = registerCurrentThreadAsUIThreadHolder();
    Thread serverThread = fakeRequestThread( threadHolder );
    simulateRequest( threadHolder, serverThread );
    RWTLifeCycle lifeCycle
      = ( RWTLifeCycle )getApplicationContext().getLifeCycleFactory().getLifeCycle();
    while( LifeCycleUtil.getSessionDisplay().readAndDispatch() ) {
    }
    lifeCycle.sleep();
  }

  public static void replaceServiceStore( ServiceStore serviceStore ) {
    HttpServletRequest request = ContextProvider.getRequest();
    HttpServletResponse response = ContextProvider.getResponse();
    ContextProvider.disposeContext();
    ServiceContext serviceContext = new ServiceContext( request, response, applicationContext );
    if( serviceStore != null ) {
      serviceContext.setServiceStore( serviceStore );
    }
    ContextProvider.setContext( serviceContext );
    ensureUISession( serviceContext );
  }

  private static void ensureUISession( ServiceContext serviceContext ) {
    HttpServletRequest request = serviceContext.getRequest();
    HttpSession httpSession = request.getSession( true );
    String cid = request.getParameter( "cid" );
    UISessionImpl uiSession = UISessionImpl.getInstanceFromSession( httpSession, cid );
    if( uiSession == null ) {
      uiSession = new UISessionBuilder( serviceContext ).buildUISession();
    }
    serviceContext.setUISession( uiSession );
  }

  ////////////////
  // general stuff

  public static void setSkipResourceRegistration( boolean skip ) {
    ApplicationContextHelper.setSkipResoureRegistration( skip );
  }

  public static void resetSkipResourceRegistration() {
    ApplicationContextHelper.setSkipResoureRegistration( isPerformanceOptimizationsEnabled() );
  }

  public static void setSkipResourceDeletion( boolean skip ) {
    ApplicationContextHelper.setSkipResoureDeletion( skip );
  }

  public static void resetSkipResourceDeletion() {
    ApplicationContextHelper.setSkipResoureDeletion( isPerformanceOptimizationsEnabled() );
  }

  public static void copyTestResource( String resourceName, File destination ) throws IOException {
    ClassLoader loader = Fixture.class.getClassLoader();
    InputStream is = loader.getResourceAsStream( resourceName );
    if( is == null ) {
      throw new IllegalArgumentException( "Resource could not be found: " + resourceName );
    }
    BufferedInputStream bis = new BufferedInputStream( is );
    try {
      OutputStream out = new FileOutputStream( destination );
      BufferedOutputStream bout = new BufferedOutputStream( out );
      try {
        int c = bis.read();
        while( c != -1 ) {
          bout.write( c );
          c = bis.read();
        }
      } finally {
        bout.close();
      }
    } finally {
      bis.close();
    }
  }

  private static void ensureServletContext() {
    if( servletContext == null ) {
      createServletContext();
    }
  }

  private static void simulateRequest( IUIThreadHolder threadHolder, Thread serverThread ) {
    RWTLifeCycle lifeCycle
      = ( RWTLifeCycle )getApplicationContext().getLifeCycleFactory().getLifeCycle();
    synchronized( threadHolder.getLock() ) {
      serverThread.start();
      try {
        lifeCycle.sleep();
      } catch( ThreadDeath e ) {
        throw new RuntimeException( e );
      }
    }
  }

  private static Thread fakeRequestThread( final IUIThreadHolder threadHolder ) {
    final RWTLifeCycle lifeCycle
      = ( RWTLifeCycle )getApplicationContext().getLifeCycleFactory().getLifeCycle();
    final ServiceContext context = ContextProvider.getContext();
    Thread result = new Thread( new Runnable() {
      @Override
      public void run() {
        synchronized( threadHolder.getLock() ) {
          ContextProvider.setContext( context );
          try {
            try {
              lifeCycle.execute();
              lifeCycle.setPhaseOrder( null );
            } catch( IOException e ) {
              throw new RuntimeException( e );
            }
          } finally {
            ContextProvider.releaseContextHolder();
            threadHolder.notifyAll();
          }
        }
      }
    }, "ServerThread" );
    return result;
  }

  private static IUIThreadHolder registerCurrentThreadAsUIThreadHolder() {
    final IUIThreadHolder result = new IUIThreadHolder() {
      private final Thread thread = Thread.currentThread();

      @Override
      public void setServiceContext( ServiceContext serviceContext ) {
      }
      @Override
      public void switchThread() {
        synchronized( getLock() ) {
          notifyAll();
          try {
            wait();
          } catch( InterruptedException e ) {
            throw new RuntimeException( e );
          }
        }
      }
      @Override
      public void updateServiceContext() {
      }
      @Override
      public void terminateThread() {
      }
      @Override
      public Thread getThread() {
        return thread;
      }
      @Override
      public Object getLock() {
        return this;
      }
    };
    LifeCycleUtil.setUIThread( getUISession(), result );
    return result;
  }

  ////////////////
  // general stuff

  private static boolean isPerformanceOptimizationsEnabled() {
    return Boolean.getBoolean( SYS_PROP_USE_PERFORMANCE_OPTIMIZATIONS );
  }

  private static File createTempDir() {
    File globalTmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
    String subDirName = "rap-test-" + Long.toHexString( System.currentTimeMillis() );
    File tmpDir = new File( globalTmpDir, subDirName );
    if( !tmpDir.mkdir() ) {
      String message = "Failed to create temp directory: " + tmpDir.getAbsolutePath();
      throw new IllegalStateException( message );
    }
    return tmpDir;
  }

  private Fixture() {
    // prevent instantiation
  }

  private static class FixtureApplicationConfiguration implements ApplicationConfiguration {
    @Override
    public void configure( Application application ) {
      application.setOperationMode( OperationMode.SWT_COMPATIBILITY );
    }
  }

}
