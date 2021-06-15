/*******************************************************************************
 * Copyright (c) 2007, 2012 Innoopract Informationssysteme GmbH and others.
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

import java.io.IOException;
import java.io.InputStream;


/**
 * A resource loader is used to load the contents of a named resource.
 *
 * @since 2.0
 */
public interface ResourceLoader {

  /**
   * Returns an input stream to the resource contents.
   *
   * @param a name to identify the resource
   * @return an input stream or <code>null</code> if the resource could not be found
   */
  InputStream getResourceAsStream( String resourceName ) throws IOException;

}
