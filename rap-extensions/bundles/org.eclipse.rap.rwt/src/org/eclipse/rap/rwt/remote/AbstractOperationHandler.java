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
package org.eclipse.rap.rwt.remote;

import java.io.Serializable;

import org.eclipse.rap.json.JsonObject;


/**
 * This class provides an empty implementation of the <code>OperationHandler</code> interface, to
 * minimize the effort required to implement this interface.
 * <p>
 * Subclasses only need to override those methods that are needed to handle the expected operations
 * for the corresponding remote type. Methods that are not overridden will throw an
 * {@link UnsupportedOperationException}.
 * </p>
 * <p>
 * It is recommended to extend this base class rather than to implement the OperationHandler
 * interface itself.
 * </p>
 *
 * @since 2.0
 */
public abstract class AbstractOperationHandler implements OperationHandler, Serializable {

  /**
   * @since 2.1
   */
  @Override
  public void handleSet( JsonObject properties ) {
    throw new UnsupportedOperationException( "set operations not supported by this handler" );
  }

  /**
   * @since 2.1
   */
  @Override
  public void handleCall( String method, JsonObject parameters ) {
    throw new UnsupportedOperationException( "call operations not supported by this handler" );
  }

  /**
   * @since 2.1
   */
  @Override
  public void handleNotify( String event, JsonObject properties ) {
    throw new UnsupportedOperationException( "notify operations not supported by this handler" );
  }

}
