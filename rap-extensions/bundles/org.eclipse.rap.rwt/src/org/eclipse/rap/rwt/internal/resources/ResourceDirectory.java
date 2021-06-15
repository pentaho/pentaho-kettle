/*******************************************************************************
 * Copyright (c) 2002, 2013 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.resources;

import java.io.File;

import org.eclipse.rap.rwt.application.ApplicationRunner;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


public class ResourceDirectory {

  public static final String DIRNAME = "rwt-resources";

  private File resourcesDir;

  public void configure( String contextDirectory ) {
    ParamCheck.notNull( contextDirectory, "contextDirectory" );
    resourcesDir = new File( contextDirectory, ApplicationRunner.RESOURCES );
  }

  public void reset() {
    resourcesDir = null;
  }

  public void createDirectory() {
    if( !resourcesDir.exists() ) {
      resourcesDir.mkdirs();
    }
  }

  public void deleteDirectory() {
    delete( resourcesDir );
  }

  public File getDirectory() {
    if( resourcesDir == null ) {
      throw new IllegalStateException( "Resources directory not configured" );
    }
    return resourcesDir;
  }

  private static void delete( File file ) {
    if( file.isDirectory() ) {
      deleteChildren( file );
    }
    deleteFile( file );
  }

  private static void deleteChildren( File file ) {
    for( File child : file.listFiles() ) {
      delete( child );
    }
  }

  private static void deleteFile( File file ) {
    if( !file.delete() && file.exists() ) {
      throw new IllegalStateException( "Could not delete: " + file.getPath() );
    }
  }

}
