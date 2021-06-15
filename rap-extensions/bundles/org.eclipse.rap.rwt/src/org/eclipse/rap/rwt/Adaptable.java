/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt;


/**
 * <p>Adaptable objects lets you add interfaces to a class and lets clients
 * query whether an object has a particular extension. This means adaptable
 * objects can be dynamically extended. Adapters are created by adapter
 * factories, which are registered with an global adapter manager.</p>
 * <p>
 *
 * <pre>
 *     Adaptable a = ...;
 *     MyExtension x = a.getAdapter( MyExtension.class );
 *     if( x != null ) {
 *       // invoke MyExtension methods on x ...
 *     }
 * </pre>
 * </p>
 *
 * @since 2.0
 */
public interface Adaptable {

  /**
   * <p>Returns an object which is an instance of the given class parameter
   * associated with this object or <code>null</code> if no association
   * exists.</p>
   *
   * @param adapter the lookup class
   * @return an object that can be cast to the given class or <code>null</code> if
   *         there is no adapter associated with the given class.
   */
  <T> T getAdapter( Class<T> adapter );

}
