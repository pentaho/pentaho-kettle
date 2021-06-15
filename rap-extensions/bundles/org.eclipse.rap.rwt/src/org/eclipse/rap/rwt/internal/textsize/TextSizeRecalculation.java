/*******************************************************************************
 * Copyright (c) 2011, 2017 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleUtil;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.service.ServiceStore;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.widgets.ControlUtil;
import org.eclipse.swt.internal.widgets.IDisplayAdapter;
import org.eclipse.swt.internal.widgets.IShellAdapter;
import org.eclipse.swt.internal.widgets.WidgetTreeUtil;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


class TextSizeRecalculation {

  static final String TEMPORARY_RESIZE = TextSizeRecalculation.class.getName() + "#temporaryResize";
  static final String KEY_SCROLLED_COMPOSITE_SIZE = "org.eclipse.rap.sc-size";
  static final String KEY_SCROLLED_COMPOSITE_ORIGIN = "org.eclipse.rap.sc-origin";
  static final String KEY_SCROLLED_COMPOSITE_CONTENT_SIZE = "org.eclipse.rap.content-size";
  static final int RESIZE_OFFSET = 1000;

  static void execute() {
    for( Shell shell : getShells() ) {
      forceShellRecalculations( shell );
    }
  }

  private static void forceShellRecalculations( Shell shell ) {
    boolean isPacked = ControlUtil.getControlAdapter( shell ).isPacked();
    Rectangle boundsBuffer = shell.getBounds();
    bufferScrolledCompositeOrigins( shell );
    clearLayoutBuffers( shell );
    setTemporaryResize( true );
    enlargeScrolledCompositeContent( shell );
    enlargeShell( shell );
    setTemporaryResize( false );
    clearLayoutBuffers( shell );
    markLayoutNeeded( shell );
    rePack( shell );
    restoreScrolledCompositeOrigins( shell );
    restoreShellSize( shell, boundsBuffer, isPacked );
  }

  private static void rePack( Shell shell ) {
    WidgetTreeUtil.accept( shell, new RePackVisitor() );
  }

  private static void clearLayoutBuffers( Shell shell ) {
    WidgetTreeUtil.accept( shell, new ClearLayoutBuffersVisitor() );
  }

  private static void markLayoutNeeded( Shell shell ) {
    WidgetTreeUtil.accept( shell, new MarkLayoutNeededVisitor() );
  }

  private static void bufferScrolledCompositeOrigins( Shell shell ) {
    WidgetTreeUtil.accept( shell, new BufferScrolledCompositeOriginsVisitor() );
  }

  private static void enlargeScrolledCompositeContent( Shell shell ) {
    WidgetTreeUtil.accept( shell, new EnlargeScrolledCompositeContentVisitor() );
  }

  private static void restoreScrolledCompositeOrigins( Shell shell ) {
    WidgetTreeUtil.accept( shell, new RestoreScrolledCompositeOriginsVisitor() );
  }

  private static void restoreShellSize( Shell shell, Rectangle bufferedBounds, boolean isPacked ) {
    if( isPacked ) {
      shell.pack();
      ControlUtil.getControlAdapter( shell ).clearPacked();
    } else {
      setShellSize( shell, bufferedBounds );
    }
  }

  private static void enlargeShell( Shell shell ) {
    Rectangle bounds = shell.getBounds();
    int xPos = bounds.x;
    int yPos = bounds.y;
    int width = bounds.width + RESIZE_OFFSET;
    int height = bounds.height + RESIZE_OFFSET;
    setShellSize( shell, new Rectangle( xPos, yPos, width, height ) );
  }

  private static Shell[] getShells() {
    return getShells( LifeCycleUtil.getSessionDisplay() );
  }

  private static Shell[] getShells( Display display ) {
    return display.getAdapter( IDisplayAdapter.class ).getShells();
  }

  private static void setShellSize( Shell shell, Rectangle bounds ) {
    shell.getAdapter( IShellAdapter.class ).setBounds( bounds );
  }

  private static void setTemporaryResize( boolean value ) {
    ServiceStore serviceStore = ContextProvider.getServiceStore();
    if( value ) {
      serviceStore.setAttribute( TEMPORARY_RESIZE, Boolean.TRUE );
    } else {
      serviceStore.removeAttribute( TEMPORARY_RESIZE );
    }
  }

  private TextSizeRecalculation() {
    // prevent instantiation
  }

}
