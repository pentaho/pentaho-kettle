/*******************************************************************************
 * Copyright (c) 2002, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.lifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.rap.rwt.application.EntryPointFactory;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


public class EntryPointManager {

  private final Map<String, EntryPointRegistration> entryPoints;

  public EntryPointManager() {
    entryPoints = new HashMap<>();
  }

  public void register( String path,
                        Class<? extends EntryPoint> type,
                        Map<String, String> properties )
  {
    ParamCheck.notNull( path, "path" );
    checkValidPath( path );
    doRegister( path, new DefaultEntryPointFactory( type ), properties );
  }


  public void register( String path,
                        EntryPointFactory entryPointFactory,
                        Map<String, String> properties )
  {
    ParamCheck.notNull( path, "path" );
    ParamCheck.notNull( entryPointFactory, "entryPointFactory" );
    checkValidPath( path );
    doRegister( path, entryPointFactory, properties );
  }

  public void deregisterAll() {
    synchronized( entryPoints ) {
      entryPoints.clear();
    }
  }

  public EntryPointRegistration getEntryPointRegistration( HttpServletRequest request ) {
    String path = request.getServletPath();
    EntryPointRegistration result = getRegistrationByPath( path );
    if( result == null ) {
      throw new IllegalArgumentException( "Entry point not found: " + path );
    }
    return result;
  }

  public EntryPointRegistration getRegistrationByPath( String servletPath ) {
    String normalizedPath = "".equals( servletPath ) ? "/" : servletPath;
    synchronized( entryPoints ) {
      return entryPoints.get( normalizedPath );
    }
  }

  public Collection<String> getServletPaths() {
    synchronized( entryPoints ) {
      return new ArrayList<>( entryPoints.keySet() );
    }
  }

  private void doRegister( String path, EntryPointFactory factory, Map<String, String> properties )
  {
    synchronized( entryPoints ) {
      checkPathAvailable( path );
      entryPoints.put( path, new EntryPointRegistration( factory, properties ) );
    }
  }

  private static void checkValidPath( String path ) {
    if( !path.startsWith( "/" ) ) {
      throw new IllegalArgumentException( "Path must start with '/': " + path );
    }
    if( path.substring( 1 ).contains( "/" ) ) {
      throw new IllegalArgumentException( "Nested paths not supported: " + path );
    }
  }

  private void checkPathAvailable( String path ) {
    if( entryPoints.containsKey( path ) ) {
      throw new IllegalArgumentException( "Entry point already registered for path " + path );
    }
  }

}
