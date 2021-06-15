/*******************************************************************************
 * Copyright (c) 2011, 2016 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 *    Rüdiger Herrmann - exception handler (bug 367773)
 ******************************************************************************/
package org.eclipse.rap.rwt.application;

import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.SettingStore;
import org.eclipse.rap.rwt.service.SettingStoreFactory;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;


/**
 * This interface represents an RWT application before it is started. It's used
 * to configure the various aspects that form an application such as
 * entrypoints, URL mapping, styling, etc.
 * <p>
 * An instance of this interface is provided by the framework to the
 * <code>configure</code> method of an <code>ApplicationConfiguration</code>.
 * </p>
 *
 * @see ApplicationConfiguration
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 2.0
 */
public interface Application {

  /**
   * Instances of this class represent a mode of operation for an RWT
   * application. The major difference between the operation modes is whether a
   * separate UI thread is started for every session (SWT_COMPATIBILITY) or not
   * (JEE_COMPATIBILITY).
   */
  public static enum OperationMode {
    /**
     * In this mode, the request thread will be marked as UI thread in SWT.
     * Information that is attached to the request thread, such as security or
     * transaction contexts, can be directly accessed. This mode is compatible
     * with the JEE specification.
     * <p>
     * As its only limitation, it does not support the SWT main loop (more
     * specifically, the method {@link Display#sleep()} is not implemented). As
     * a consequence, blocking dialogs aren't possible with this operation mode.
     * As an alternative to blocking dialogs, the class {@link Dialog} provides
     * a non-blocking <code>open</code> method that accepts a callback to react
     * on the closing of a dialog.
     * Moreover, the blocking {@link Browser} methods <code>execute</code> and
     * <code>evaluate</code> are not supported in this mode. A non-blocking
     * <code>evaluate</code> method exists that also accepts a callback.
     * </p>
     * <p>
     * Unless there is a need for blocking dialogs (e.g. when using the Eclipse
     * workbench), this mode is recommended as it is more lightweight than
     * <code>SWT_COMPATIBILITY</code> .
     * </p>
     */
    JEE_COMPATIBILITY,
    /**
     * In this mode, a separate UI thread will be started for each user session.
     * All UI requests are processed in this thread while the request thread is
     * put on hold. After processing all events, the method
     * {@link Display#sleep()} lets the request thread continue and puts the UI
     * thread to sleep. This approach fully supports the SWT main loop and thus
     * also allows for blocking dialogs.
     * <p>
     * Information that is attached to the request thread, such as security or
     * transaction contexts, can only be accessed using the method
     * {@link RWT#requestThreadExec(Runnable)}.
     * </p>
     */
    SWT_COMPATIBILITY,
    /**
     * This mode behaves just like <code>JEE_COMAPTIBILTIY</code> but in
     * addition it registers the required servlet filter to support clustering.
     * This mode requires the servlet API 3.0.
     */
    SESSION_FAILOVER
  }

  /**
   * The operation mode in which the application will be running. The default is
   * <code>JEE_COMPATIBILITY</code>.
   *
   * @param operationMode the operation mode to be used, must not be
   *          <code>null</code>
   * @see OperationMode
   */
  void setOperationMode( OperationMode operationMode );

  /**
   * Registers an entrypoint at the given servlet path. A valid path must start with a slash ('/')
   * and must not contain any other slashes. The servlet path &quot;/&quot; denotes the root path.
   * Nested paths (e.g. &quot;/path/subpath&quot;) are currently not supported.
   * <p>
   * Properties can be specified to control client-specific aspects of the entrypoint such as theme,
   * icons, etc. The acceptable keys and values depend on the client implementation. The class
   * {@link WebClient} provides constants for the default RAP client.
   * </p>
   *
   * @param path a valid path to register the entry point at
   * @param entryPointType the entry point class to be registered, must not be <code>null</code>
   * @param properties properties that control client-specific aspects of the application, such as
   *          theme, icons, etc., may be <code>null</code>
   */
  void addEntryPoint( String path,
                      Class<? extends EntryPoint> entryPointType,
                      Map<String, String> properties );

  /**
   * Registers an entrypoint at the given servlet path. A valid path must start with a slash ('/')
   * and must not contain any other slashes. The servlet path &quot;/&quot; denotes the root path.
   * Nested paths (e.g. &quot;/path/subpath&quot;) are currently not supported.
   * <p>
   * Properties can be specified to control client-specific aspects of the entrypoint such as theme,
   * icons, etc. The acceptable keys and values depend on the client implementation. The class
   * {@link WebClient} provides constants for the default RAP client.
   * </p>
   *
   * @param path a valid path to register the entry point at
   * @param entryPointFactory the entry point factory to be registered, must not be
   *          <code>null</code>
   * @param properties properties that control client-specific aspects of the application, such as
   *          theme, icons, etc., may be <code>null</code>
   */
  void addEntryPoint( String path,
                      EntryPointFactory entryPointFactory,
                      Map<String, String> properties );

  /**
   * Adds the given stylesheet to a theme. If a theme with the given ID does not exist, it is
   * created and initialized with the stylesheet. If the theme exists already, the stylesheet is
   * appended to the theme. The stylesheet file will be loaded with the classloader of the
   * <code>ApplicationConfiguration</code>.
   *
   * @param themeId the id of the theme to register or to contribute to
   * @param styleSheetLocation the location of the CSS file in the format accepted by
   *          {@link ClassLoader#getResource(String)}
   * @see ApplicationConfiguration
   * @see RWT#DEFAULT_THEME_ID
   */
  void addStyleSheet( String themeId, String styleSheetLocation );

  /**
   * Adds the given stylesheet to a theme. If a theme with the given ID does not exist, it is
   * created and initialized with the stylesheet. If the theme exists already, the stylesheet is
   * appended to the theme. The stylesheet file will be loaded with the classloader of the
   * <code>ApplicationConfiguration</code>. The stylesheet file will be loaded using the given
   * resource loader.
   *
   * @param themeId the id of the theme to register or to contribute to
   * @param styleSheetLocation the location of the CSS file in the format accepted by the given
   *          resource loader
   * @param resourceLoader the resource loader that is able to load the style sheet from the given
   *          location
   * @see RWT#DEFAULT_THEME_ID
   */
  void addStyleSheet( String themeId, String styleSheetLocation, ResourceLoader resourceLoader );

  /**
   * Set an initial attribute in the application context.
   *
   * @param name the name of the attribute, must not be <code>null</code>
   * @param value the attribute value
   * @see ApplicationContext
   */
  void setAttribute( String name, Object value );

  /**
   * Configure this application to use a custom setting store implementation.
   *
   * @param the setting store implementation to use, must not be <code>null</code>
   * @see SettingStore
   */
  void setSettingStoreFactory( SettingStoreFactory settingStoreFactory );

  /**
   * Sets the exception handler to which exceptions should be forwarded that occur while running
   * the event loop.
   * <p>
   * To give an exception handler the chance to log errors it called for all classes of exceptions.
   * <code>Error</code>s however are re-thrown after the handler was called so that they cannot be
   * swallowed.
   * </p>
   * <p>
   * The default implementation throws the given exception, resulting in a HTTP 500 response.
   * </p>
   *
   * @param the exception handler to use, must not be <code>null</code>
   * @see ExceptionHandler
   * @since 2.1
   */
  void setExceptionHandler( ExceptionHandler exceptionHandler );

  /**
   * Register a themeable widget for this application. A themeable widget is a
   * custom widget that supports theming. To do so, the widget provides a couple
   * of classes and files, such as a theme adapter, that will be found by a
   * naming convention. It's sufficient to register the widget itself. For
   * details on custom widgets, please refer to the documentation.
   *
   * @param widget the widget to register as themeable widget
   * @since 3.0
   */
  void addThemeableWidget( Class<? extends Widget> widget );

  /**
   * Adds a service handler to the application. A service handler is used to
   * handle requests with a certain parameter inside the application. You can
   * think of it like a lightweight servlet that has access to the user's
   * session. Please see the documentation of {@link ServiceHandler} for the
   * URL to access this service handler.
   *
   * @param serviceHandlerId the id for this servlet handler, used in the
   *          parameter
   * @param serviceHandler the servlet handler to register
   * @see ServiceHandler
   */
  void addServiceHandler( String serviceHandlerId, ServiceHandler serviceHandler );

  void addResource( String resourceName, ResourceLoader resourceLoader );

}
