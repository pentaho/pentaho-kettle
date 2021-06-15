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
package org.eclipse.rap.rwt.internal.client;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.client.service.JavaScriptLoader;
import org.eclipse.rap.rwt.internal.protocol.ProtocolMessageWriter;
import org.eclipse.rap.rwt.internal.service.ContextProvider;


public class JavaScriptLoaderImpl implements JavaScriptLoader {

  private final Set<String> loadedUrls = new HashSet<>();

  @Override
  public void require( String url ) {
    JsonArray urlsToLoad = new JsonArray();
    if( !loadedUrls.contains( url ) ) {
      urlsToLoad.add( url );
      loadedUrls.add( url );
    }
    load( urlsToLoad );
  }

  private static void load( JsonArray urlsToLoad ) {
    if( !urlsToLoad.isEmpty() ) {
      ProtocolMessageWriter writer = ContextProvider.getProtocolWriter();
      JsonObject parameters = new JsonObject().add( "files", urlsToLoad );
      writer.appendCall( "rwt.client.JavaScriptLoader", "load", parameters );
    }
  }

}
