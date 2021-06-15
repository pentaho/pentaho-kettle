/*******************************************************************************
 * Copyright (c) 2015, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getProtocolWriter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.client.service.ClientFileLoader;


public class ClientFileLoaderImpl implements ClientFileLoader {

  private final Set<String> loadedUrls = new HashSet<>();

  @Override
  public void requireJs( String url ) {
    if( loadedUrls.add( url ) ) {
      load( url, "js" );
    }
  }

  @Override
  public void requireCss( String url ) {
    if( loadedUrls.add( url ) ) {
      load( url, "css" );
    }
  }

  private static void load( String url, String type ) {
    JsonObject parameters = new JsonObject().add( "file", url ).add( "type", type );
    getProtocolWriter().appendCall( "rwt.client.ClientFileLoader", "load", parameters );
  }

}
