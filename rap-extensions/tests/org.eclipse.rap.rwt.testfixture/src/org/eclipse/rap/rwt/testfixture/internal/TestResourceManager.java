/*******************************************************************************
 * Copyright (c) 2009, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.rap.rwt.internal.resources.ResourceDirectory;
import org.eclipse.rap.rwt.internal.resources.ResourceManagerImpl;


public class TestResourceManager extends ResourceManagerImpl {
  private final Set<String> registeredResources;

  public TestResourceManager() {
    super( null );
    registeredResources = new HashSet<String>();
  }

  @Override
  public String getLocation( String name ) {
    return ResourceDirectory.DIRNAME + "/" + name;
  }

  @Override
  public boolean isRegistered( String name ) {
    return registeredResources.contains( name );
  }

  @Override
  public void register( String name, InputStream is ) {
    registeredResources.add( name );
  }

  @Override
  public boolean unregister( String name ) {
    return registeredResources.remove( name );
  }

  @Override
  public InputStream getRegisteredContent( String name ) {
    return null;
  }

}
