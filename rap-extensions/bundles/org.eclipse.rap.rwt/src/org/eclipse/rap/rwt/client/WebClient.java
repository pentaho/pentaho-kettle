/*******************************************************************************
 * Copyright (c) 2012, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client;

import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.client.service.BrowserNavigation;
import org.eclipse.rap.rwt.client.service.ClientFileLoader;
import org.eclipse.rap.rwt.client.service.ClientFileUploader;
import org.eclipse.rap.rwt.client.service.ClientInfo;
import org.eclipse.rap.rwt.client.service.ClientService;
import org.eclipse.rap.rwt.client.service.ExitConfirmation;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.rap.rwt.client.service.UrlLauncher;
import org.eclipse.rap.rwt.internal.client.BrowserNavigationImpl;
import org.eclipse.rap.rwt.internal.client.ClientFileLoaderImpl;
import org.eclipse.rap.rwt.internal.client.ClientFileUploaderImpl;
import org.eclipse.rap.rwt.internal.client.ClientInfoImpl;
import org.eclipse.rap.rwt.internal.client.ClientMessages;
import org.eclipse.rap.rwt.internal.client.ConnectionMessages;
import org.eclipse.rap.rwt.internal.client.ConnectionMessagesImpl;
import org.eclipse.rap.rwt.internal.client.ExitConfirmationImpl;
import org.eclipse.rap.rwt.internal.client.JavaScriptExecutorImpl;
import org.eclipse.rap.rwt.internal.client.JavaScriptLoaderImpl;
import org.eclipse.rap.rwt.internal.client.StartupParametersImpl;
import org.eclipse.rap.rwt.internal.client.UrlLauncherImpl;
import org.eclipse.rap.rwt.internal.client.WebClientMessages;
import org.eclipse.rap.rwt.internal.resources.JavaScriptModuleLoader;
import org.eclipse.rap.rwt.internal.resources.JavaScriptModuleLoaderImpl;
import org.eclipse.rap.rwt.service.ResourceLoader;


/**
 * The default RWT web client.
 *
 * @since 2.0
 */
@SuppressWarnings( "deprecation" )
public class WebClient implements Client {

  private static final String PREFIX = "org.eclipse.rap.rwt.webclient";

  /**
   * Entrypoint property name for a custom theme to be used with the entrypoint.
   * The value must be the id of a registered theme. If omitted, the default
   * theme will be used.
   *
   * @see RWT#DEFAULT_THEME_ID
   * @see Application#addEntryPoint(String, Class, Map)
   * @see Application#addEntryPoint(String, EntryPointFactory, Map)
   */
  public static final String THEME_ID = PREFIX + ".themeId";

  /**
   * Entrypoint property name for additional HTML elements to be added to the
   * &lt;head&gt; section of the startup page. The value must contain a valid
   * HTML snippet that consists only of HTML elements that are permissible
   * sub-elements of <code>head</code> such as <code>meta</code> or
   * <code>link</code>.
   * <p>
   * <strong>Warning:</strong> the property value will not be validated by the
   * framework. Invalid HTML can break the application entirely or lead to
   * problems that are hard to identify. It's the responsibility of the
   * developer to ensure the correctness of the resulting page.
   * </p>
   *
   * @see Application#addEntryPoint(String, Class, Map)
   * @see Application#addEntryPoint(String, EntryPointFactory, Map)
   */
  public static final String HEAD_HTML = PREFIX + ".additionalHeaders";

  /**
   * Entrypoint property name for custom HTML code to be placed inside the
   * <code>body</code> of the startup page. The value must be proper HTML 4.0 in
   * order not to break the surrounding page.
   * <p>
   * <strong>Warning:</strong> the property value will not be validated by the
   * framework. Invalid HTML can break the application entirely or lead to
   * problems that are hard to identify. It's the responsibility of the
   * developer to ensure the correctness of the resulting page.
   * </p>
   *
   * @see Application#addEntryPoint(String, Class, Map)
   * @see Application#addEntryPoint(String, EntryPointFactory, Map)
   */
  public static final String BODY_HTML = PREFIX + ".bodyHtml";

  /**
   * Entrypoint property name for the title that will be displayed in the
   * browser window. The value must be the title string without any HTML markup.
   *
   * @see Application#addEntryPoint(String, Class, Map)
   * @see Application#addEntryPoint(String, EntryPointFactory, Map)
   */
  public static final String PAGE_TITLE = PREFIX + ".pageTitle";

  /**
   * Entrypoint property name for the page overflow behavior. The value must be one of the
   * "scroll", "scrollX", "scrollY". If this property is not set the page overflow is
   * disabled.
   *
   * @see Application#addEntryPoint(String, Class, Map)
   * @see Application#addEntryPoint(String, EntryPointFactory, Map)
   *
   * @since 3.1
   */
  public static final String PAGE_OVERFLOW = PREFIX + ".pageOverflow";

  /**
   * Entrypoint property name for the website icon (a.k.a favicon or shortcut icon) that will be
   * displayed by the web browser. The value must contain a valid path where the image can be
   * accessed on the server.
   * <p>
   * <strong>Note:</strong> if this property is provided, the image resource must be registered to
   * be available. Favicons are usually expected to be 16x16 px in size. If the icon has to work
   * with legacy browsers, use a file in the <a
   * href="http://en.wikipedia.org/wiki/ICO_%28file_format%29">ICO format</a>.
   * </p>
   *
   * @see Application#addResource(String, ResourceLoader)
   * @see Application#addEntryPoint(String, Class, Map)
   * @see Application#addEntryPoint(String, EntryPointFactory, Map)
   */
  public static final String FAVICON = PREFIX + ".favicon";

  public WebClient() {
    initializeServices();
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <T extends ClientService> T getService( Class<T> type ) {
    T result = null;
    if( type == JavaScriptExecutor.class ) {
      result = ( T )getServiceImpl( JavaScriptExecutorImpl.class );
    } else if( type == JavaScriptLoader.class ) {
      result = ( T )getServiceImpl( JavaScriptLoaderImpl.class );
    } else if( type == UrlLauncher.class ) {
      result = ( T )getServiceImpl( UrlLauncherImpl.class );
    } else if( type == JavaScriptModuleLoader.class ) {
      result = ( T )getServiceImpl( JavaScriptModuleLoaderImpl.class );
    } else if( type == BrowserNavigation.class ) {
      result = ( T )getServiceImpl( BrowserNavigationImpl.class );
    } else if( type == ExitConfirmation.class ) {
      result = ( T )getServiceImpl( ExitConfirmationImpl.class );
    } else if( type == ConnectionMessages.class ) {
      result = ( T )getServiceImpl( ConnectionMessagesImpl.class );
    } else if( type == ClientInfo.class ) {
      result = ( T )getServiceImpl( ClientInfoImpl.class );
    } else if( type == ClientMessages.class ) {
      result = ( T )getServiceImpl( WebClientMessages.class );
    } else if( type == ClientFileLoader.class ) {
      result = ( T )getServiceImpl( ClientFileLoaderImpl.class );
    } else if( type == ClientFileUploader.class ) {
      result = ( T )getServiceImpl( ClientFileUploaderImpl.class );
    } else if( type == StartupParameters.class ) {
      result = ( T )getServiceImpl( StartupParametersImpl.class );
    }
    return result;
  }

  private static <T> T getServiceImpl( Class<T> impl ) {
    return SingletonUtil.getSessionInstance( impl );
  }

  private static void initializeServices() {
    getServiceImpl( ClientInfoImpl.class );
    getServiceImpl( BrowserNavigationImpl.class );
    getServiceImpl( StartupParametersImpl.class );
  }

}
