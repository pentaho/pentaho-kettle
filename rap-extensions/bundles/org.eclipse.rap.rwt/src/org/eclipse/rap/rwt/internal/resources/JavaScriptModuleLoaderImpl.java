/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.SingletonUtil;
import org.eclipse.rap.rwt.internal.protocol.JsonUtil;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.util.ClassUtil;
import org.eclipse.rap.rwt.service.ApplicationContext;
import org.eclipse.rap.rwt.service.ResourceManager;


public class JavaScriptModuleLoaderImpl implements JavaScriptModuleLoader {

  private static final String MODULES_KEY = JavaScriptModuleRegistry.class.getName() + "#instance";
  private static final Object LOCK = new Object();

  @Override
  public void ensureModule( Class< ? extends JavaScriptModule> type ) {
    if( !isLoaded( type ) ) {
      if( !isRegistered( type ) ) {
        registerModule( type );
      }
      loadModule( type );
    }
  }

  private static void registerModule( Class< ? extends JavaScriptModule> type ) {
    JavaScriptModule module = ClassUtil.newInstance( type );
    String[] fileNames = module.getFileNames();
    if( fileNames.length == 0 ) {
      throw new IllegalStateException( "No JavaScript files found!" );
    }
    String[] filePaths = new String[ fileNames.length ];
    try {
      // TODO [tb] : check for duplicates?
      for( int i = 0; i < fileNames.length; i++ ) {
        filePaths[ i ] = registerFile( module, fileNames[ i ] );
      }
    } catch( IOException ioe ) {
      throw new IllegalArgumentException( "Failed to load resources", ioe );
    }
    getApplicationModules().put( type, filePaths );
  }

  private static String registerFile( JavaScriptModule module, String fileName ) throws IOException
  {
    String localPath = getLocalPath( module, fileName );
    InputStream inputStream = module.getLoader().getResourceAsStream( localPath );
    if( inputStream == null ) {
      throw new IOException( "File " + localPath + " does not exist." );
    }
    ResourceManager resourceManager = RWT.getResourceManager();
    String publicPath = getPublicPath( module, fileName );
    try {
      // TODO [tb] : ensure that content is not concatenated to core js library
      resourceManager.register( publicPath, inputStream );
    } finally {
      inputStream.close();
    }
    return resourceManager.getLocation( publicPath );
  }

  private static void loadModule( Class<? extends JavaScriptModule> type ) {
    String[] files = getApplicationModules().get( type );
    ProtocolMessageWriter writer = ContextProvider.getProtocolWriter();
    JsonObject parameters = new JsonObject().add( "files", JsonUtil.createJsonArray( files ) );
    writer.appendCall( "rwt.client.JavaScriptLoader", "load", parameters );
    getSessionModules().put( type, files );
  }

  private static String getPublicPath( JavaScriptModule module, String fileName ) {
    Class<?> type = module.getClass();
    return type.getSimpleName() + type.hashCode() + "/" + fileName;
  }

  private static String getLocalPath( JavaScriptModule module, String fileName ) {
    return module.getDirectory() + "/" + fileName;
  }

  private static boolean isRegistered( Class<? extends JavaScriptModule> clazz ) {
    return getApplicationModules().get( clazz ) != null;
  }

  private static boolean isLoaded( Class<? extends JavaScriptModule> clazz ) {
    return getSessionModules().get( clazz ) != null;
  }

  private static JavaScriptModuleRegistry getApplicationModules() {
    ApplicationContext context = RWT.getApplicationContext();
    synchronized( LOCK ) {
      JavaScriptModuleRegistry result = (JavaScriptModuleRegistry)context.getAttribute( MODULES_KEY );
      if( result == null ) {
        result = new JavaScriptModuleRegistry();
        context.setAttribute( MODULES_KEY, result );
      }
      return result;
    }
  }

  private static JavaScriptModuleRegistry getSessionModules() {
    return SingletonUtil.getSessionInstance( JavaScriptModuleRegistry.class );
  }

  static private class JavaScriptModuleRegistry {

    private final Map<Class<? extends JavaScriptModule>, String[]> map = new HashMap<>();

    public void put( Class<? extends JavaScriptModule> type, String[] files ) {
      map.put( type, files );
    }

    public String[] get( Class<? extends JavaScriptModule> type ) {
      return map.get( type );
    }

  }

}
