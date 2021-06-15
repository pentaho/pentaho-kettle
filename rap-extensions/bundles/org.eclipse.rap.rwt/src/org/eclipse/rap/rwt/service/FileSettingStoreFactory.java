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
 ******************************************************************************/
package org.eclipse.rap.rwt.service;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServletLog;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


/**
 * A setting store factory that creates instances of {@link FileSettingStore}.
 * <p>
 * This implementation uses the following strategy to determine the path for persisting the data of
 * the session store instances.
 * <ol>
 * <li>Use the directory specified by the init-parameter
 * <code>"org.eclipse.rap.rwt.service.FileSettingStore.dir"</code> in the web.xml.</li>
 * <li>Use the directory specified by the <code>"javax.servlet.context.tempdir"</code> attribute in
 * the servlet context.</li>
 * <li>Use the directory specified by the <code>"java.io.tempdir"</code> property.</li>
 * </ol>
 * The first path that can be obtained from the above choices (in the order given above) will be
 * used. If the path determined does not exist it will be created.
 * </p>
 *
 * @since 2.0
 */
public final class FileSettingStoreFactory implements SettingStoreFactory {

  private File directory;

  @Override
  public SettingStore createSettingStore( String id ) {
    ParamCheck.notNullOrEmpty( id, "id" );
    SettingStore store = new FileSettingStore( getWorkingDir() );
    try {
      store.loadById( id );
    } catch( IOException sse ) {
      ServletLog.log( sse.getMessage(), sse );
    }
    return store;
  }

  private File getWorkingDir() {
    if( directory == null ) {
      directory = selectWorkingDir();
      createDirectory( directory );
    }
    return directory;
  }

  private static File selectWorkingDir() {
    File directory = getDirectoryFromServletContext();
    if( directory == null ) {
      directory = getDirectoryFromServletContextTempDir();
      if ( directory == null ) {
        directory = getDirectoryFromSystemTempDir();
      }
    }
    return directory;
  }

  private static void createDirectory( File directory ) {
    if( !directory.mkdirs() ) {
      if( !directory.isDirectory() ) {
        String message = "Could not create directory: " + directory.getAbsolutePath();
        throw new IllegalArgumentException( message );
      }
    }
  }

  private static File getDirectoryFromSystemTempDir() {
    File result;
    String parent = System.getProperty( "java.io.tmpdir" );
    result = new File( parent, FileSettingStore.class.getName() );
    return result;
  }

  private static File getDirectoryFromServletContext() {
    String path = getServletContext().getInitParameter( FileSettingStore.FILE_SETTING_STORE_DIR );
    return path != null ? new File( path ) : null;
  }

  private static File getDirectoryFromServletContextTempDir() {
    File parent = ( File )getServletContext().getAttribute( "javax.servlet.context.tempdir" );
    return parent != null ? new File( parent, FileSettingStore.class.getName() ) : null;
  }

  private static ServletContext getServletContext() {
    HttpSession session = ContextProvider.getRequest().getSession();
    return session.getServletContext();
  }

}
