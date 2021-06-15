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
import org.eclipse.rap.rwt.theme.ControlThemeAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scrollable;

/**
 * This class provides the layout for ViewForm
 * 
 * @see ViewForm
 */
class ViewFormLayout extends Layout {

  protected Point computeSize( Composite composite,
                               int wHint,
                               int hHint,
                               boolean flushCache )
  {
    ViewForm form = ( ViewForm )composite;
    Control left = form.topLeft;
    Control center = form.topCenter;
    Control right = form.topRight;
    Control content = form.content;
    Point leftSize = new Point( 0, 0 );
    if( left != null ) {
      leftSize = computeChildSize( left, SWT.DEFAULT, SWT.DEFAULT, flushCache );
    }
    Point centerSize = new Point( 0, 0 );
    if( center != null ) {
      centerSize = computeChildSize( center,
                                     SWT.DEFAULT,
                                     SWT.DEFAULT,
                                     flushCache );
    }
    Point rightSize = new Point( 0, 0 );
    if( right != null ) {
      rightSize = computeChildSize( right, SWT.DEFAULT, SWT.DEFAULT, flushCache );
    }
    Point size = new Point( 0, 0 );
    // calculate width of title bar
    if( form.separateTopCenter
        || ( wHint != SWT.DEFAULT && leftSize.x + centerSize.x + rightSize.x > wHint ) )
    {
      size.x = leftSize.x + rightSize.x;
      if( leftSize.x > 0 && rightSize.x > 0 )
        size.x += form.horizontalSpacing;
      size.x = Math.max( centerSize.x, size.x );
      size.y = Math.max( leftSize.y, rightSize.y );
      if( center != null ) {
        size.y += centerSize.y;
        if( left != null || right != null )
          size.y += form.verticalSpacing;
      }
    } else {
      size.x = leftSize.x + centerSize.x + rightSize.x;
      int count = -1;
      if( leftSize.x > 0 )
        count++;
      if( centerSize.x > 0 )
        count++;
      if( rightSize.x > 0 )
        count++;
      if( count > 0 )
        size.x += count * form.horizontalSpacing;
      size.y = Math.max( leftSize.y, Math.max( centerSize.y, rightSize.y ) );
    }
    if( content != null ) {
      if( left != null || right != null || center != null )
        size.y += 1; // allow space for a vertical separator
      Point contentSize = new Point( 0, 0 );
      contentSize = computeChildSize( content,
                                      SWT.DEFAULT,
                                      SWT.DEFAULT,
                                      flushCache );
      size.x = Math.max( size.x, contentSize.x );
      size.y += contentSize.y;
      if( size.y > contentSize.y )
        size.y += form.verticalSpacing;
    }
    size.x += 2 * form.marginWidth;
    size.y += 2 * form.marginHeight;
    if( wHint != SWT.DEFAULT )
      size.x = wHint;
    if( hHint != SWT.DEFAULT )
      size.y = hHint;
    return size;
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

  protected boolean flushCache( Control control ) {
    Object data = control.getLayoutData();
    if( data != null && data instanceof CLayoutData )
      ( ( CLayoutData )data ).flushCache();
    return true;
  }

  protected void layout( Composite composite, boolean flushCache ) {
    ViewForm form = ( ViewForm )composite;
    Control left = form.topLeft;
    Control center = form.topCenter;
    Control right = form.topRight;
    Control content = form.content;
    Rectangle rect = composite.getClientArea();
    Point leftSize = new Point( 0, 0 );
    if( left != null && !left.isDisposed() ) {
      leftSize = computeChildSize( left, SWT.DEFAULT, SWT.DEFAULT, flushCache );
    }
    Point centerSize = new Point( 0, 0 );
    if( center != null && !center.isDisposed() ) {
      centerSize = computeChildSize( center,
                                     SWT.DEFAULT,
                                     SWT.DEFAULT,
                                     flushCache );
    }
    Point rightSize = new Point( 0, 0 );
    if( right != null && !right.isDisposed() ) {
      rightSize = computeChildSize( right, SWT.DEFAULT, SWT.DEFAULT, flushCache );
    }
    int minTopWidth = leftSize.x
                      + centerSize.x
                      + rightSize.x
                      + 2
                      * form.marginWidth
                      + 2
                      * form.highlight;

    int count = -1;
    if( leftSize.x > 0 )
      count++;
    if( centerSize.x > 0 )
      count++;
    if( rightSize.x > 0 )
      count++;
    if( count > 0 )
      minTopWidth += count * form.horizontalSpacing;
//    int x = rect.x + rect.width - form.marginWidth - form.highlight;
// TODO [rh] hack to fix CTabFolder layout problems
    int x = rect.x + rect.width - form.marginWidth - form.highlight - 1;
    int y = rect.y + form.marginHeight + form.highlight;
    boolean top = false;
    if( form.separateTopCenter || minTopWidth > rect.width ) {
      int topHeight = Math.max( rightSize.y, leftSize.y );
      if( right != null && !right.isDisposed() ) {
        top = true;
        x -= rightSize.x;
        right.setBounds( x, y, rightSize.x, topHeight );
        x -= form.horizontalSpacing;
      }
      if( left != null && !left.isDisposed() ) {
        top = true;
        int trim = computeTrim( left );
        int leftW = x - rect.x - form.marginWidth - form.highlight - trim;
        leftSize = computeChildSize( left, leftW, SWT.DEFAULT, false );
        left.setBounds( rect.x + form.marginWidth + form.highlight,
                        y,
                        leftSize.x,
                        topHeight );
      }
      if( top )
        y += topHeight + form.verticalSpacing;
      if( center != null && !center.isDisposed() ) {
        top = true;
        int trim = computeTrim( center );
        int w = rect.width - 2 * form.marginWidth - 2 * form.highlight - trim;
        centerSize = computeChildSize( center, w, SWT.DEFAULT, false );
        center.setBounds( rect.x
                          + rect.width
                          - form.marginWidth
                          - form.highlight
                          - centerSize.x, y, centerSize.x, centerSize.y );
        y += centerSize.y + form.verticalSpacing;
      }
    } else {
      int topHeight = Math.max( rightSize.y,
                                Math.max( centerSize.y, leftSize.y ) );
      if( right != null && !right.isDisposed() ) {
        top = true;
        x -= rightSize.x;
        right.setBounds( x, y, rightSize.x, topHeight );
        x -= form.horizontalSpacing;
      }
      if( center != null && !center.isDisposed() ) {
        top = true;
        x -= centerSize.x;
        center.setBounds( x, y, centerSize.x, topHeight );
        x -= form.horizontalSpacing;
      }
      if( left != null && !left.isDisposed() ) {
        top = true;
        Rectangle trim = left instanceof Composite
                                                  ? ( ( Composite )left ).computeTrim( 0,
                                                                                       0,
                                                                                       0,
                                                                                       0 )
                                                  : new Rectangle( 0, 0, 0, 0 );
        int w = x - rect.x - form.marginWidth - form.highlight - trim.width;
        int h = topHeight - trim.height;
        leftSize = computeChildSize( left, w, h, false );
        left.setBounds( rect.x + form.marginWidth + form.highlight,
                        y,
                        leftSize.x,
                        topHeight );
      }
      if( top )
        y += topHeight + form.verticalSpacing;
    }
//    int oldSeperator = form.separator;
    form.separator = -1;
    if( content != null && !content.isDisposed() ) {
      if( left != null || right != null || center != null ) {
        form.separator = y;
        y++;
      }
      content.setBounds( rect.x + form.marginWidth + form.highlight,
                         y,
                         rect.width - 2 * form.marginWidth - 2 * form.highlight,
                         rect.y
                             + rect.height
                             - y
                             - form.marginHeight
                             - form.highlight );
    }
//    if( oldSeperator != -1 && form.separator != -1 ) {
//      int t = Math.min( form.separator, oldSeperator );
//      int b = Math.max( form.separator, oldSeperator );
//      form.redraw(form.borderLeft, t, form.getSize().x - form.borderLeft -
//      form.borderRight, b - t, false);
//    }
  }
}
