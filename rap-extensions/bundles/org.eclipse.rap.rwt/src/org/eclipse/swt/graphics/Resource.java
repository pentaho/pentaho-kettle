/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Display;

/**
 * This class is the abstract superclass of all graphics resource objects.  
 * Resources created by the application are shared across all sessions of the
 * application.
 * 
 * <p>
 * IMPORTANT: This class is intended to be subclassed <em>only</em>
 * within the RWT implementation. However, it has not been marked
 * final to allow those outside of the RWT development team to implement
 * patched versions of the class in order to get around specific
 * limitations in advance of when those limitations can be addressed
 * by the team.  Any class built using subclassing to access the internals
 * of this class will likely fail to compile or run between releases and
 * may be strongly platform specific. Subclassing should not be attempted
 * without an intimate and detailed understanding of the workings of the
 * hierarchy. No support is provided for user-written classes which are
 * implemented as subclasses of this class.
 * </p>
 *
 * @since 1.0
 */
public abstract class Resource implements SerializableCompatibility {

  final Device device;
  private boolean disposed;
  
  Resource( Device device ) {
    this.device = device;
  }
  
  /**
   * Returns the <code>Device</code> where this resource was
   * created.
   *
   * @return <code>Device</code> the device of the receiver
   * 
   * @since 1.3
   */
  public Device getDevice() {
    if( disposed ) {
      SWT.error( SWT.ERROR_GRAPHIC_DISPOSED );
    }
    Device result = device;
    // Currently, factory-managed resources (device == null) return the current 
    // display. This is done under the assumption that resource methods are
    // only called from the UI thread. This way also shared resources appear to 
    // belong to the current session.
    // Note that this is still under investigation.
    if( result == null ) {
      result = Display.getCurrent();
    }
    return result;
  }

  /**
   * Disposes of the resource. Applications must dispose of all resources
   * which they allocate.
   * This method does nothing if the resource is already disposed.
   * 
   * @since 1.3
   */
  public void dispose() {
    if( device == null ) {
      throw new IllegalStateException( "A factory-created resource cannot be disposed." );
    }
    destroy();
    disposed = true;
  }

  void destroy() {
  }

  /**
   * Returns <code>true</code> if the resource has been disposed,
   * and <code>false</code> otherwise.
   * <p>
   * This method gets the dispose state for the resource.
   * When a resource has been disposed, it is an error to
   * invoke any other method (except {@link #dispose()}) using the resource.
   *
   * @return <code>true</code> when the resource is disposed and <code>false</code> otherwise
   * 
   * @since 1.3
   */
  public boolean isDisposed() {
    return disposed;
  }

  static Device checkDevice( Device device ) {
    Device result = device;
    if( result == null ) {
      result = Display.getCurrent();
    }
    if( result == null ) {
      SWT.error( SWT.ERROR_NULL_ARGUMENT );
    }
    return result;
  }
}
