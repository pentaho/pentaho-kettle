/*******************************************************************************
 * Copyright (c) 2011, 2015 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.application;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.application.ExceptionHandler;
import org.eclipse.rap.rwt.internal.client.ClientProvider;
import org.eclipse.rap.rwt.internal.lifecycle.RWTLifeCycle;
import org.eclipse.rap.rwt.internal.theme.Theme;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;
import org.eclipse.rap.rwt.internal.theme.css.CssFileReader;
import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ServiceHandler;
import org.eclipse.rap.rwt.service.ServiceManager;
import org.eclipse.rap.rwt.service.SettingStoreFactory;
import org.eclipse.swt.widgets.Widget;


public class ApplicationImpl implements Application {

  private final ApplicationContextImpl applicationContext;
  private final ApplicationConfiguration configuration;

  public ApplicationImpl( ApplicationContextImpl applicationContext,
                          ApplicationConfiguration configuration )
  {
    this.applicationContext = applicationContext;
    this.configuration = configuration;
  }

  @Override
  public void setOperationMode( OperationMode operationMode ) {
    ParamCheck.notNull( operationMode, "operationMode" );
    switch( operationMode ) {
      case JEE_COMPATIBILITY:
        break;
      case SWT_COMPATIBILITY:
        applicationContext.getLifeCycleFactory().configure( RWTLifeCycle.class );
        break;
      case SESSION_FAILOVER:
        new SessionFailoverConfigurator( applicationContext ).configure();
        break;
      default:
        throw new IllegalArgumentException( "Unsupported operation mode: " + operationMode );
    }
  }

  @Override
  public void setSettingStoreFactory( SettingStoreFactory settingStoreFactory ) {
    ParamCheck.notNull( settingStoreFactory, "settingStoreFactory" );

    applicationContext.getSettingStoreManager().register( settingStoreFactory );
  }

  @Override
  public void setExceptionHandler( ExceptionHandler exceptionHandler ) {
    ParamCheck.notNull( exceptionHandler, "exceptionHandler" );

    applicationContext.setExceptionHandler( exceptionHandler );
  }

  @Override
  public void addEntryPoint( String path,
                             Class<? extends EntryPoint> entryPointType,
                             Map<String, String> properties )
  {
    ParamCheck.notNull( path, "path" );
    ParamCheck.notNull( entryPointType, "entryPointType" );

    applicationContext.getEntryPointManager().register( path, entryPointType, properties );
  }

  @Override
  public void addEntryPoint( String path,
                             EntryPointFactory entryPointFactory,
                             Map<String, String> properties )
  {
    ParamCheck.notNull( path, "path" );
    ParamCheck.notNull( entryPointFactory, "entryPointFactory" );

    applicationContext.getEntryPointManager().register( path, entryPointFactory, properties );
  }

  @Override
  public void addResource( String resourceName, ResourceLoader resourceLoader ) {
    ParamCheck.notNull( resourceName, "resourceName" );
    ParamCheck.notNull( resourceLoader, "resourceLoader" );

    applicationContext.getResourceRegistry().add( resourceName, resourceLoader );
  }

  @Override
  public void addServiceHandler( String serviceHandlerId, ServiceHandler serviceHandler ) {
    ParamCheck.notNull( serviceHandlerId, "serviceHandlerId" );
    ParamCheck.notNull( serviceHandler, "serviceHandler" );

    ServiceManager serviceManager = applicationContext.getServiceManager();
    serviceManager.registerServiceHandler( serviceHandlerId, serviceHandler );
  }

  @Override
  public void addStyleSheet( String themeId, String styleSheetLocation ) {
    addStyleSheet( themeId, styleSheetLocation, new ResourceLoaderImpl( getClassLoader() ) );
  }

  @Override
  public void addStyleSheet( String themeId, String styleSheetLocation, ResourceLoader resourceLoader ) {
    ParamCheck.notNull( themeId, "themeId" );
    ParamCheck.notNull( styleSheetLocation, "styleSheetLocation" );
    ParamCheck.notNull( resourceLoader, "resourceLoader" );

    StyleSheet styleSheet = readStyleSheet( styleSheetLocation, resourceLoader );
    ThemeManager themeManager = applicationContext.getThemeManager();
    Theme theme = themeManager.getTheme( themeId );
    if( theme != null ) {
      theme.addStyleSheet( styleSheet );
    } else {
      themeManager.registerTheme( new Theme( themeId, "unknown", styleSheet ) );
    }
  }

  @Override
  public void addThemeableWidget( Class<? extends Widget> widget ) {
    addThemeableWidget( widget, new ResourceLoaderImpl( widget.getClassLoader() ) );
  }

  public void addThemeableWidget( Class<? extends Widget> widget, ResourceLoader resourceLoader ) {
    ParamCheck.notNull( widget, "widget" );
    ParamCheck.notNull( resourceLoader, "resourceLoader" );

    applicationContext.getThemeManager().addThemeableWidget( widget.getName(), resourceLoader );
  }

  public void addClientProvider( ClientProvider clientProvider ) {
    applicationContext.getClientSelector().addClientProvider( clientProvider );
  }

  @Override
  public void setAttribute( String name, Object value ) {
    applicationContext.setAttribute( name, value );
  }

  public ApplicationContextImpl getApplicationContext() {
    return applicationContext;
  }

  private ClassLoader getClassLoader() {
    return configuration.getClass().getClassLoader();
  }

  private static StyleSheet readStyleSheet( String styleSheetLocation, ResourceLoader loader ) {
    try {
      return CssFileReader.readStyleSheet( styleSheetLocation, loader );
    } catch( IOException ioe ) {
      String message = "Failed to read stylesheet from resource: " + styleSheetLocation;
      throw new IllegalArgumentException( message, ioe );
    }
  }

  static class ResourceLoaderImpl implements ResourceLoader {

    private final ClassLoader loader;

    private ResourceLoaderImpl( ClassLoader loader ) {
      this.loader = loader;
    }

    @Override
    public InputStream getResourceAsStream( String resourceName ) throws IOException {
      return loader.getResourceAsStream( resourceName );
    }
  }

}
