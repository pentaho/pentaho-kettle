/*******************************************************************************
 * Copyright (c) 2002, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.widgets;

import org.eclipse.rap.rwt.internal.lifecycle.WidgetLCA;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.graphics.GCAdapter;
import org.eclipse.swt.internal.widgets.canvaskit.CanvasLCA;


/**
 * Instances of this class provide a surface for drawing
 * arbitrary graphics.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * This class may be subclassed by custom control implementors
 * who are building controls that are <em>not</em> constructed
 * from aggregates of other controls. That is, they are either
 * painted using SWT graphics calls or are handled by native
 * methods.
 * </p>
 *
 * @see Composite
 * @since 1.0
 */
public class Canvas extends Composite {

  private transient GCAdapter gcAdapter;

  Canvas( Composite parent ) {
    // prevent instantiation from outside this package
    super( parent );
  }

  /**
   * Constructs a new instance of this class given its parent
   * and a style value describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in
   * class <code>SWT</code> which is applicable to instances of this
   * class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more
   * of those <code>SWT</code> style constants. The class description
   * lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p>
   *
   * @param parent a composite control which will be the parent of the new
   *        instance (cannot be null)
   * @param style the style of control to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that
   *    created the parent</li>
   * </ul>
   *
   * @see SWT
   * @see Widget#checkSubclass
   * @see Widget#getStyle
   */
  public Canvas( Composite parent, int style ) {
    super( parent, style );
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getAdapter( Class<T> adapter ) {
    if( adapter == GCAdapter.class ) {
      if( gcAdapter == null ) {
        gcAdapter = new GCAdapter();
      }
      return ( T )gcAdapter;
    }
    if( adapter == WidgetLCA.class ) {
      return ( T )CanvasLCA.INSTANCE;
    }
    return super.getAdapter( adapter );
  }

  /**
   * Adds the listener to the collection of listeners who will
   * be notified when the receiver needs to be painted, by sending it
   * one of the messages defined in the <code>PaintListener</code>
   * interface.
   *
   * @param listener the listener which should be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see PaintListener
   * @see #removePaintListener
   * @since 1.3
   */
  public void addPaintListener( PaintListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    TypedListener typedListener = new TypedListener( listener );
    addListener( SWT.Paint, typedListener );
  }

  /**
   * Removes the listener from the collection of listeners who will
   * be notified when the receiver needs to be painted.
   *
   * @param listener the listener which should no longer be notified
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @see PaintListener
   * @see #addPaintListener
   * @since 1.3
   */
  public void removePaintListener( PaintListener listener ) {
    checkWidget();
    if( listener == null ) {
      error( SWT.ERROR_NULL_ARGUMENT );
    }
    removeListener( SWT.Paint, listener );
  }

  /////////////
  // repainting

  @Override
  void notifyResize( Point oldSize ) {
    super.notifyResize( oldSize );
    if( !oldSize.equals( getSize() ) ) {
      repaint( getClientArea() );
    }
  }

  @Override
  void internalSetRedraw( boolean redraw ) {
    super.internalSetRedraw( redraw );
    if( redraw ) {
      repaint( getClientArea() );
    }
  }

  @Override
  void internalSetRedraw( boolean redraw, int x, int y, int width, int height ) {
    super.internalSetRedraw( redraw, x, y, width, height );
    if( redraw ) {
      repaint( new Rectangle( x, y, width, height ) );
    }
  }

  private void repaint( Rectangle paintRect ) {
    if( gcAdapter != null ) {
      gcAdapter.clearGCOperations();
      gcAdapter.setForceRedraw( true );
    }
    GC gc = new GC( this );
    Event paintEvent = new Event();
    paintEvent.gc = gc;
    paintEvent.setBounds( paintRect );
    notifyListeners( SWT.Paint, paintEvent );
    gc.dispose();
    if( gcAdapter != null ) {
      gcAdapter.setPaintRect( paintRect );
    }
  }

}
