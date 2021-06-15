/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.rap.rwt.theme.BoxDimensions;
// import org.eclipse.swt.*;
// import org.eclipse.swt.graphics.*;
// import org.eclipse.swt.widgets.*;
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Scrollable;

/**
 * This class provides the layout for CBanner
 *
 * @see CBanner
 */
class CBannerLayout extends Layout {

  @Override
  protected Point computeSize( Composite composite,
                               int wHint,
                               int hHint,
                               boolean flushCache )
  {
    CBanner banner = ( CBanner )composite;
    Control left = banner.left;
    Control right = banner.right;
    Control bottom = banner.bottom;
    boolean showCurve = left != null && right != null;
    int height = hHint;
    int width = wHint;
    // Calculate component sizes
    Point bottomSize = new Point( 0, 0 );
    if( bottom != null ) {
      int trim = computeTrim( bottom );
      int w = wHint == SWT.DEFAULT
                                  ? SWT.DEFAULT
                                  : Math.max( 0, width - trim );
      bottomSize = computeChildSize( bottom, w, SWT.DEFAULT, flushCache );
    }
    Point rightSize = new Point( 0, 0 );
    if( right != null ) {
      int trim = computeTrim( right );
      int w = SWT.DEFAULT;
      if( banner.rightWidth != SWT.DEFAULT ) {
        w = banner.rightWidth - trim;
        if( left != null ) {
          w = Math.min( w, width
                           - banner.curve_width
                           + 2
                           * banner.curve_indent
                           - CBanner.MIN_LEFT
                           - trim );
        }
        w = Math.max( 0, w );
      }
      rightSize = computeChildSize( right, w, SWT.DEFAULT, flushCache );
      if( wHint != SWT.DEFAULT ) {
        width -= rightSize.x + banner.curve_width - 2 * banner.curve_indent;
      }
    }
    Point leftSize = new Point( 0, 0 );
    if( left != null ) {
      int trim = computeTrim( left );
      int w = wHint == SWT.DEFAULT
                                  ? SWT.DEFAULT
                                  : Math.max( 0, width - trim );
      leftSize = computeChildSize( left, w, SWT.DEFAULT, flushCache );
    }
    // Add up sizes
    width = leftSize.x + rightSize.x;
    height = bottomSize.y;
    if( bottom != null && ( left != null || right != null ) ) {
      height += CBanner.BORDER_STRIPE + 2;
    }
    if( left != null ) {
      if( right == null ) {
        height += leftSize.y;
      } else {
        height += Math.max( leftSize.y,
                            banner.rightMinHeight == SWT.DEFAULT
                                                                ? rightSize.y
                                                                : banner.rightMinHeight );
      }
    } else {
      height += rightSize.y;
    }
    if( showCurve ) {
      width += banner.curve_width - 2 * banner.curve_indent;
      height += CBanner.BORDER_TOP
                + CBanner.BORDER_BOTTOM
                + 2
                * CBanner.BORDER_STRIPE;
    }
    if( wHint != SWT.DEFAULT ) {
      width = wHint;
    }
    if( hHint != SWT.DEFAULT ) {
      height = hHint;
    }
    return new Point( width, height );
  }

  Point computeChildSize( Control control,
                          int wHint,
                          int hHint,
                          boolean flushCache )
  {
    Object data = control.getLayoutData();
    if( data == null || !( data instanceof CLayoutData ) ) {
      data = new CLayoutData();
      control.setLayoutData( data );
    }
    return ( ( CLayoutData )data ).computeSize( control,
                                                wHint,
                                                hHint,
                                                flushCache );
  }

  int computeTrim( Control c ) {
    if( c instanceof Scrollable ) {
      Rectangle rect = ( ( Scrollable )c ).computeTrim( 0, 0, 0, 0 );
      return rect.width;
    }
    BoxDimensions border = c.getAdapter( ControlThemeAdapter.class ).getBorder( c );
    return border.left + border.right;
  }

  @Override
  protected boolean flushCache( Control control ) {
    Object data = control.getLayoutData();
    if( data != null && data instanceof CLayoutData ) {
      ( ( CLayoutData )data ).flushCache();
    }
    return true;
  }

  @Override
  @SuppressWarnings("unused")
  protected void layout( Composite composite, boolean flushCache ) {
    CBanner banner = ( CBanner )composite;
    Control left = banner.left;
    Control right = banner.right;
    Control bottom = banner.bottom;
    Sash separator = banner.separator;
    Point size = banner.getSize();
    boolean showCurve = left != null && right != null;
    BoxDimensions border = banner.getAdapter( ControlThemeAdapter.class ).getBorder( banner );
    int width = size.x - ( border.left + border.right );
    int height = size.y - ( border.top + border.bottom );
    Point bottomSize = new Point( 0, 0 );
    if( bottom != null ) {
      int trim = computeTrim( bottom );
      int w = Math.max( 0, width - trim );
      bottomSize = computeChildSize( bottom, w, SWT.DEFAULT, flushCache );
      height -= bottomSize.y + CBanner.BORDER_STRIPE + 2;
    }
    if( showCurve ) {
      height -= CBanner.BORDER_TOP
                + CBanner.BORDER_BOTTOM
                + 2
                * CBanner.BORDER_STRIPE;
    }
    height = Math.max( 0, height );
    Point rightSize = new Point( 0, 0 );
    if( right != null ) {
      int trim = computeTrim( right );
      int w = SWT.DEFAULT;
      if( banner.rightWidth != SWT.DEFAULT ) {
        w = banner.rightWidth - trim;
        if( left != null ) {
          w = Math.min( w, width
                           - banner.curve_width
                           + 2
                           * banner.curve_indent
                           - CBanner.MIN_LEFT
                           - trim );
        }
        w = Math.max( 0, w );
      }
      rightSize = computeChildSize( right, w, SWT.DEFAULT, flushCache );
      width = width
              - ( rightSize.x - banner.curve_indent + banner.curve_width - banner.curve_indent );
    }
    Point leftSize = new Point( 0, 0 );
    if( left != null ) {
      int trim = computeTrim( left );
      int w = Math.max( 0, width - trim );
      leftSize = computeChildSize( left, w, SWT.DEFAULT, flushCache );
    }
    int x = 0;
    int y = 0;
    int oldStart = banner.curveStart;
    Rectangle leftRect = null;
    Rectangle rightRect = null;
    Rectangle bottomRect = null;
    Rectangle curveRect = null;
    if( bottom != null ) {
      bottomRect = new Rectangle( x,
                                  y + size.y - bottomSize.y,
                                  bottomSize.x,
                                  bottomSize.y );
    }
    if( showCurve ) {
      y += CBanner.BORDER_TOP + CBanner.BORDER_STRIPE;
    }
    if( left != null ) {
      leftRect = new Rectangle( x, y, leftSize.x, leftSize.y );
      banner.curveStart = x + leftSize.x - banner.curve_indent;
      x += leftSize.x
           - banner.curve_indent
           + banner.curve_width
           - banner.curve_indent;
    }
    if( right != null ) {
      if( left != null ) {
        rightSize.y = Math.max( leftSize.y,
                                banner.rightMinHeight == SWT.DEFAULT
                                                                    ? rightSize.y
                                                                    : banner.rightMinHeight );
      }
      rightRect = new Rectangle( x, y, rightSize.x, rightSize.y );
    }
//    if( banner.curveStart < oldStart ) {
//      banner.redraw( banner.curveStart - CBanner.CURVE_TAIL,
//                     0,
//                     oldStart
//                         + banner.curve_width
//                         - banner.curveStart
//                         + CBanner.CURVE_TAIL
//                         + 5,
//                     size.y,
//                     false );
//    }
//    if( banner.curveStart > oldStart ) {
//      banner.redraw( oldStart - CBanner.CURVE_TAIL, 0, banner.curveStart
//                                                       + banner.curve_width
//                                                       - oldStart
//                                                       + CBanner.CURVE_TAIL
//                                                       + 5, size.y, false );
//    }
    if( rightRect != null ) {
      curveRect = new Rectangle( banner.curveStart,
                                 y,
                                 banner.curve_width,
                                 height );
    }
    /*
     * The paint events must be flushed in order to make the curve draw smoothly
     * while the user drags the divider. On Windows, it is necessary to flush
     * the paints before the children are resized because otherwise the children
     * (particularly toolbars) will flash.
     */
    // banner.update();
    banner.curveRect = new Rectangle( banner.curveStart,
                                      0,
                                      banner.curve_width,
                                      size.y );
    if( bottomRect != null ) {
      bottom.setBounds( bottomRect );
    }
    if( rightRect != null ) {
      right.setBounds( rightRect );
    }
    if( leftRect != null ) {
      left.setBounds( leftRect );
    }
    if( curveRect != null ) {
      separator.setBounds( curveRect );
    }
  }
}
