/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload.internal;

import static org.eclipse.rap.rwt.SingletonUtil.getUniqueInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.rwt.RWT;


public final class FileUploadHandlerStore {

  private final Map<String, FileUploadHandler> handlers;

  private FileUploadHandlerStore() {
    handlers = Collections.synchronizedMap( new HashMap<String, FileUploadHandler>() );
    RWT.getServiceManager().registerServiceHandler( FileUploadServiceHandler.SERVICE_HANDLER_ID,
                                                    new FileUploadServiceHandler() );
  }

  public static FileUploadHandlerStore getInstance() {
    return getUniqueInstance( FileUploadHandlerStore.class, RWT.getApplicationContext() );
  }

  public void registerHandler( String token, FileUploadHandler fileUploadHandler ) {
    handlers.put( token, fileUploadHandler );
  }

  public void deregisterHandler( String token ) {
    handlers.remove( token );
  }

  public FileUploadHandler getHandler( String token ) {
    return handlers.get( token );
  }

  public static String createToken() {
    int random1 = ( int )( Math.random() * 0xfffffff );
    int random2 = ( int )( Math.random() * 0xfffffff );
    return Integer.toHexString( random1 ) + Integer.toHexString( random2 );
  }

}
