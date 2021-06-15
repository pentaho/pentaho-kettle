/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.internal.util.StreamUtil;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ResourceManager;


/**
 * The resource manager is responsible for registering resources like images,
 * CSS files etc. which are available on the application's classpath. The
 * registered files will be read out from their libraries and delivered if
 * requested. Usually resources are stored in libraries in the WEB-INF/lib
 * directory of a web-application
 * <p>
 * Implementation as singleton.
 * </p>
 * <p>
 * This class is not intended to be used by clients.
 * </p>
 */
public class ResourceManagerImpl implements ResourceManager {

  private final ResourceDirectory resourceDirectory;
  private final Set<String> resources;

  public ResourceManagerImpl( ResourceDirectory resourceDirectory ) {
    this.resourceDirectory = resourceDirectory;
    resources = Collections.synchronizedSet( new HashSet<String>() );
  }

  /////////////////////////////
  // interface ResourceManager

  public void registerOnce( String resource, ResourceLoader loader ) {
    ParamCheck.notNull( resource, "resource" );
    ParamCheck.notNull( loader, "loader" );
    if( !resources.contains( resource ) ) {
      checkPath( resource );
      InputStream stream = null;
      try {
        stream = loader.getResourceAsStream( resource );
        internalRegister( resource, stream );
      } catch( IOException ioe ) {
        throw new RuntimeException( "Failed to register resource: " + resource, ioe );
      } finally {
        if( stream != null ) {
          StreamUtil.close( stream );
        }
      }
    }
  }

  @Override
  public void register( String path, InputStream inputStream ) {
    ParamCheck.notNull( path, "name" );
    ParamCheck.notNull( inputStream, "inputStream" );
    checkPath( path );
    internalRegister( path, inputStream );
  }

  @Override
  public boolean unregister( String name ) {
    ParamCheck.notNull( name, "name" );
    boolean result = false;
    if( resources.remove( name ) ) {
      result = true;
      File file = getDiskLocation( name );
      file.delete();
    }
    return result;
  }

  @Override
  public boolean isRegistered( String name ) {
    ParamCheck.notNull( name, "name" );
    return resources.contains( name );
  }

  @Override
  public String getLocation( String name ) {
    ParamCheck.notNull( name, "name" );
    if( !resources.contains( name ) ) {
      throw new IllegalArgumentException( "Resource does not exist: " + name );
    }
    return createRequestUrl( name );
  }

  @Override
  public InputStream getRegisteredContent( String name ) {
    ParamCheck.notNull( name, "name" );
    InputStream result = null;
    if( resources.contains( name ) ) {
      File file = getDiskLocation( name );
      try {
        result = new FileInputStream( file );
      } catch( FileNotFoundException fnfe ) {
        throw new RuntimeException( fnfe );
      }
    }
    return result;
  }

  //////////////////
  // helping methods

  private static String createRequestUrl( String resourceName ) {
    return new StringBuilder()
      .append( ResourceDirectory.DIRNAME )
      .append( "/" )
      .append( escapeResourceName( resourceName.replace( '\\', '/' ) ) )
      .toString();
  }

  private void internalRegister( String name, InputStream inputStream ) {
    File location = getDiskLocation( name );
    try {
      createDirectories( location );
      writeResource( inputStream, location );
    } catch ( IOException ioe ) {
      throw new RuntimeException( "Failed to register resource: " + name, ioe );
    }
    resources.add( name );
  }

  private static void writeResource( InputStream inputStream, File location )
    throws IOException
  {
    BufferedInputStream bufferedStream = new BufferedInputStream( inputStream );
    OutputStream outputStream = new BufferedOutputStream( new FileOutputStream( location ) );
    try {
      byte[] buffer = new byte[ 256 ];
      int read = bufferedStream.read( buffer );
      while( read != -1 ) {
        outputStream.write( buffer, 0, read );
        read = bufferedStream.read( buffer );
      }
    } finally {
      outputStream.close();
    }
  }

  private static void createDirectories( File file ) throws IOException {
    File dir = new File( file.getParent() );
    if( !dir.mkdirs() ) {
      if( !dir.exists() ) {
        throw new IOException( "Could not create directory structure: " + dir.getAbsolutePath() );
      }
    }
  }

  private File getDiskLocation( String resourceName ) {
    String escapedResourceName = escapeResourceName( resourceName );
    return new File( resourceDirectory.getDirectory(), escapedResourceName );
  }

  //////////////////
  // helping methods

  private static void checkPath( String path ) {
    if( path.length() == 0 ) {
      throw new IllegalArgumentException( "Path must not be empty" );
    }
    if( path.endsWith( "/"  ) || path.endsWith( "\\" ) ) {
      throw new IllegalArgumentException( "Path must not end with path separator" );
    }
  }

  private static String escapeResourceName( String name ) {
    return name
      .replaceAll( "\\$", "\\$\\$" )
      .replaceAll( ":", "\\$1" )
      .replaceAll( "\\?", "\\$2" );
  }

}
