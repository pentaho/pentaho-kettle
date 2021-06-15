/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.Client;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.internal.SingletonManager;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.client.ClientSelector;
import org.eclipse.rap.rwt.internal.lifecycle.CurrentPhase;
import org.eclipse.rap.rwt.internal.lifecycle.PhaseId;
import org.eclipse.rap.rwt.internal.protocol.ClientMessage;
import org.eclipse.rap.rwt.internal.protocol.ProtocolUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceContext;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.rap.rwt.internal.service.UISessionImpl;
import org.eclipse.rap.rwt.internal.textsize.MeasurementUtil;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;
import org.eclipse.rap.rwt.internal.theme.ThemeUtil;
import org.eclipse.rap.rwt.internal.util.HTTP;
import org.eclipse.rap.rwt.remote.Connection;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.rap.rwt.testfixture.internal.TestRequest;
import org.eclipse.rap.rwt.testfixture.internal.TestResourceManager;
import org.eclipse.rap.rwt.testfixture.internal.TestResponse;
import org.eclipse.rap.rwt.testfixture.internal.TestServletContext;
import org.eclipse.rap.rwt.testfixture.internal.TestHttpSession;
import org.eclipse.rap.rwt.testfixture.internal.engine.ThemeManagerHelper;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;


/**
 * JUnit rule that sets up a RAP context including UISession and ApplicationContext for test cases.
 * Every test method will run in an isolated test context. The running thread will be marked as
 * UIThread for the execution of a test method.
 * <p>
 * To use this rule, add the following line to a test case.
 * </p>
 *
 * <pre>
 * &#064;Rule
 * public TestContext context = new TestContext();
 * </pre>
 *
 * @since 3.0
 */
@SuppressWarnings( "deprecation" )
public class TestContext implements TestRule {

  private ApplicationContextImpl applicationContext;
  private UISessionImpl uiSession;

  public Statement apply( final Statement base, final Description description ) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          setup( description );
          base.evaluate();
        } finally {
          tearDown();
        }
      }
    };
  }

  /**
   * Returns the UISession that is part of this test context. Within the running thread, this is
   * equivalent to <code>RWT.getUISession()</code>.
   *
   * @return the UISession of this test context
   */
  public UISession getUISession() {
    return uiSession;
  }

  /**
   * Returns the ApplicationContext that is part of this test context. Within the running thread,
   * this is equivalent to <code>RWT.getApplicationContext()</code>.
   *
   * @return the ApplicationContext of this test context
   */
  public ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * Replaces the client implementation of the current UISession. Can be used to return mocked
   * ClientServices. Example:
   * <pre>
   * context.replaceClient( new Client() {
   *   public &lt;T extends ClientService&gt; T getService( Class&lt;T&gt; type ) {
   *     return mockedClientService;
   *   }
   * } );
   * </pre>
   * @param client a new Client implementation
   */
  public void replaceClient( Client client ) {
    uiSession.setAttribute( ClientSelector.SELECTED_CLIENT, client );
  }

  /**
   * Replaces the connection of the current UISession. Can be used to return mocked RemoteObjects
   * for a given type. Example:
   * <pre>
   * context.replaceConnection( new Connection() {
   *   public RemoteObject createRemoteObject( String remoteType ) {
   *     return mockedRemoteObject;
   *   }
   * } );
   * </pre>
   * @param connection a new Connection implementation
   */
  public void replaceConnection( Connection connection ) {
    uiSession.setConnection( connection );
  }

  private void setup( Description description ) {
    ApplicationConfiguration config = createApplicationConfiguration();
    applicationContext = createApplicationContext( config );
    uiSession = new UISessionImpl( applicationContext, new TestHttpSession() );
    ContextProvider.setContext( createServiceContext( applicationContext, uiSession ) );
    SingletonManager.install( applicationContext );
    SingletonManager.install( uiSession );
    MeasurementUtil.installMeasurementOperator( uiSession );
    ThemeUtil.setCurrentThemeId( uiSession, RWT.DEFAULT_THEME_ID );
    uiSession.setAttribute( ClientSelector.SELECTED_CLIENT, new WebClient() );
    ProtocolUtil.setClientMessage( createMessage() );
    CurrentPhase.set( PhaseId.PROCESS_ACTION );
  }

  private void tearDown() {
    ThemeManagerHelper.resetThemeManagerIfNeeded();
    ContextProvider.disposeContext();
    applicationContext.deactivate();
    applicationContext.removeFromServletContext();
    applicationContext = null;
    uiSession = null;
  }

  private static ApplicationConfiguration createApplicationConfiguration() {
    ContextProvider.disposeContext();
    return new ApplicationConfiguration() {
      public void configure( Application application ) {
      }
    };
  }

  private static ApplicationContextImpl createApplicationContext( ApplicationConfiguration config )
  {
    TestServletContext servletContext = new TestServletContext();
    ApplicationContextImpl appContext = new ApplicationContextImpl( config, servletContext ) {
      @Override
      protected ThemeManager createThemeManager() {
        return ThemeManagerHelper.ensureThemeManager();
      }
      @Override
      protected ResourceManager createResourceManager() {
        return new TestResourceManager();
      }
    };
    appContext.attachToServletContext();
    appContext.activate();
    return appContext;
  }

  private static ServiceContext createServiceContext( ApplicationContextImpl applicationContext,
                                                      UISession uiSession )
  {
    HttpServletRequest request = createRequest( uiSession.getHttpSession() );
    HttpServletResponse response = new TestResponse();
    ServiceContext serviceContext = new ServiceContext( request, response, applicationContext );
    serviceContext.setServiceStore( new ServiceStore() );
    serviceContext.setUISession( uiSession );
    return serviceContext;
  }

  private static HttpServletRequest createRequest( HttpSession httpSession ) {
    TestRequest request = new TestRequest();
    request.setContentType( HTTP.CONTENT_TYPE_JSON );
    request.setMethod( HTTP.METHOD_POST );
    request.setSession( httpSession );
    request.setBody( createMessage().toString() );
    return request;
  }

  private static ClientMessage createMessage() {
    JsonObject json = new JsonObject()
      .add( "head", new JsonObject() )
      .add( "operations", new JsonArray() );
    return new ClientMessage( json );
  }

}
