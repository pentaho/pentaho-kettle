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

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.internal.widgets.WidgetTreeVisitor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;


class RestoreScrolledCompositeOriginsVisitor implements WidgetTreeVisitor {

  @Override
  public boolean visit( Widget widget ) {
    if( widget instanceof ScrolledComposite ) {
      ScrolledComposite scrolledComposite = ( ScrolledComposite )widget;
      restoreSize( scrolledComposite );
      restoreContentSize( scrolledComposite );
      restoreOrigin( scrolledComposite );
    }
    return true;
  }

  private static void restoreSize( ScrolledComposite composite ) {
    Point oldSize = getBufferedSize( composite );
    if( oldSize != null ) {
      composite.setSize( oldSize );
      composite.setData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_SIZE, null );
    }
  }

  private static void restoreOrigin( ScrolledComposite composite ) {
    Point oldOrigin = getBufferedOrigin( composite );
    if( oldOrigin != null ) {
      composite.setOrigin( oldOrigin );
      composite.setData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_ORIGIN, null );
    }
  }

  private static void restoreContentSize( ScrolledComposite composite ) {
    Control content = composite.getContent();
    if( content != null ) {
      restoreContentSize( content );
    }
  }

  private static void restoreContentSize( Control content ) {
    Point size = getBufferedContentSize( content );
    if( size != null ) {
      content.setSize( size );
      content.setData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_CONTENT_SIZE, null );
    }
  }

  private static Point getBufferedContentSize( Control content ) {
    return ( Point )content.getData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_CONTENT_SIZE );
  }

  private static Point getBufferedOrigin( ScrolledComposite composite ) {
    return ( Point )composite.getData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_ORIGIN );
  }

  private static Point getBufferedSize( ScrolledComposite composite ) {
    return ( Point )composite.getData( TextSizeRecalculation.KEY_SCROLLED_COMPOSITE_SIZE );
  }

}
