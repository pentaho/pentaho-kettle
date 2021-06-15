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
package org.eclipse.rap.rwt.internal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.rap.rwt.internal.util.StreamUtil;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.ResourceManager;


public class ResourceRegistry {

  private final Collection<ResourceRegistration> resources;
  private final ResourceManager resourceManager;

  public ResourceRegistry( ResourceManager resourceManager ) {
    this.resourceManager = resourceManager;
    resources = new LinkedList<>();
  }

  public void add( String resourceName, ResourceLoader resourceLoader ) {
    resources.add( new ResourceRegistration( resourceName, resourceLoader ) );
  }

  public void registerResources() {
    for( ResourceRegistration resourceRegistration : getResourceRegistrations() ) {
      registerResource( resourceRegistration );
    }
    clear();
  }

  public ResourceRegistration[] getResourceRegistrations() {
    return resources.toArray( new ResourceRegistration[ resources.size() ] );
  }

  public void clear() {
    resources.clear();
  }

  private void registerResource( ResourceRegistration resourceRegistration ) {
    InputStream inputStream = resourceRegistration.openResource();
    try {
      resourceManager.register( resourceRegistration.getResourceName(), inputStream );
    } finally {
      StreamUtil.close( inputStream );
    }
  }

  public static class ResourceRegistration {
    private final String resourceName;
    private final ResourceLoader resourceLoader;

    ResourceRegistration( String resourceName, ResourceLoader resourceLoader ) {
      this.resourceName = resourceName;
      this.resourceLoader = resourceLoader;
    }

    public String getResourceName() {
      return resourceName;
    }

    public ResourceLoader getResourceLoader() {
      return resourceLoader;
    }

    InputStream openResource() {
      InputStream result;
      try {
        result = resourceLoader.getResourceAsStream( resourceName );
      } catch( IOException ioe ) {
        throw new RuntimeException( "Failed to load resource: " + resourceName, ioe );
      }
      if( result == null ) {
        String msg = "Resource loader returned null for resource: " + resourceName;
        throw new IllegalStateException( msg );
      }
      return result;
    }

  }

}
