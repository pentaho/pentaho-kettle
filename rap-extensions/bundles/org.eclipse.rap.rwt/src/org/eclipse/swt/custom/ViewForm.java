/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * Instances of this class implement a Composite that positions and sizes
 * children and allows programmatic control of layout and border parameters.
 * ViewForm is used in the workbench to lay out a view's label/menu/toolbar
 * local bar.
 * <p>
 * Note that although this class is a subclass of <code>Composite</code>, it
 * does not make sense to set a layout on it.
 * </p>
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER, FLAT</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(None)</dd>
 * </dl>
 * <p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * </p>
 */
@SuppressWarnings("all")
public class ViewForm extends Composite {

  /**
   * marginWidth specifies the number of pixels of horizontal margin that will
   * be placed along the left and right edges of the form. The default value is
   * 0.
   */
  public int marginWidth = 0;
  /**
   * marginHeight specifies the number of pixels of vertical margin that will be
   * placed along the top and bottom edges of the form. The default value is 0.
   */
  public int marginHeight = 0;
  /**
   * horizontalSpacing specifies the number of pixels between the right edge of
   * one cell and the left edge of its neighbouring cell to the right. The
   * default value is 1.
   */
  public int horizontalSpacing = 1;
  /**
   * verticalSpacing specifies the number of pixels between the bottom edge of
   * one cell and the top edge of its neighbouring cell underneath. The default
   * value is 1.
   */
  public int verticalSpacing = 1;
  // /**
  // * Color of innermost line of drop shadow border.
  // *
  // * NOTE This field is badly named and can not be fixed for backwards
  // compatability.
  // * It should be capitalized.
  // *
  // * @deprecated
  // */
  // public static RGB borderInsideRGB = new RGB (132, 130, 132);
  // /**
  // * Color of middle line of drop shadow border.
  // *
  // * NOTE This field is badly named and can not be fixed for backwards
  // compatability.
  // * It should be capitalized.
  // *
  // * @deprecated
  // */
  // public static RGB borderMiddleRGB = new RGB (143, 141, 138);
  // /**
  // * Color of outermost line of drop shadow border.
  // *
  // * NOTE This field is badly named and can not be fixed for backwards
  // compatability.
  // * It should be capitalized.
  // *
  // * @deprecated
  // */
  // public static RGB borderOutsideRGB = new RGB (171, 168, 165);
  // SWT widgets
  Control topLeft;
  Control topCenter;
  Control topRight;
  Control content;
  // Configuration and state info
  boolean separateTopCenter = false;
  boolean showBorder = false;
  int separator = -1;
  int borderTop = 0;
  int borderBottom = 0;
  int borderLeft = 0;
  int borderRight = 0;
  int highlight = 0;
  Point oldSize;
  Color selectionBackground;
  static final int OFFSCREEN = -200;

  // static final int BORDER1_COLOR = SWT.COLOR_WIDGET_NORMAL_SHADOW;
  static final int SELECTION_BACKGROUND = SWT.COLOR_LIST_BACKGROUND;
  /**
   * Constructs a new instance of this class given its parent and a style value
   * describing its behavior and appearance.
   * <p>
   * The style value is either one of the style constants defined in class
   * <code>SWT</code> which is applicable to instances of this class, or must
   * be built by <em>bitwise OR</em>'ing together (that is, using the
   * <code>int</code> "|" operator) two or more of those <code>SWT</code>
   * style constants. The class description lists the style constants that are
   * applicable to the class. Style bits are also inherited from superclasses.
   * </p>
   * 
   * @param parent a widget which will be the parent of the new instance (cannot
   *          be null)
   * @param style the style of widget to construct
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the parent</li>
   *              </ul>
   * @see SWT#BORDER
   * @see SWT#FLAT
   * @see #getStyle()
   */
  public ViewForm( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    super.setLayout( new ViewFormLayout() );
    setBorderVisible( ( style & SWT.BORDER ) != 0 );
    // Listener listener = new Listener() {
    // public void handleEvent(Event e) {
    // switch (e.type) {
    // case SWT.Dispose: onDispose(); break;
    // case SWT.Paint: onPaint(e.gc); break;
    // case SWT.Resize: onResize(); break;
    // }
    // }
    // };
    //
    // int[] events = new int[] {SWT.Dispose, SWT.Paint, SWT.Resize};
    //
    // for (int i = 0; i < events.length; i++) {
    // addListener(events[i], listener);
    // }
    addControlListener( new ControlListener() {

      public void controlMoved( ControlEvent event ) {
      }

      public void controlResized( ControlEvent event ) {
        onResize();
      }
    } );
    addDisposeListener( new DisposeListener() {

      public void widgetDisposed( DisposeEvent event ) {
        onDispose();
      }
    } );
  }

  static int checkStyle( int style ) {
    int mask = SWT.FLAT | SWT.LEFT_TO_RIGHT /*| SWT.RIGHT_TO_LEFT*/ ;
    return style & mask /* | SWT.NO_REDRAW_RESIZE */ ;
  }

   protected void checkSubclass() {
    String name = getClass().getName();
    String validName = ViewForm.class.getName();
    if( !validName.equals( name ) ) {
      SWT.error( SWT.ERROR_INVALID_SUBCLASS );
    }
  }

   public Rectangle computeTrim( int x, int y, int width, int height ) {
    checkWidget();
    int trimX = x - borderLeft - highlight;
    int trimY = y - borderTop - highlight;
    int trimWidth = width + borderLeft + borderRight + 2 * highlight;
    int trimHeight = height + borderTop + borderBottom + 2 * highlight;
    return new Rectangle( trimX, trimY, trimWidth, trimHeight );
  }

  public Rectangle getClientArea() {
    checkWidget();
    Rectangle clientArea = super.getClientArea();
    clientArea.x += borderLeft;
    clientArea.y += borderTop;
    clientArea.width -= borderLeft + borderRight;
    clientArea.height -= borderTop + borderBottom;
    return clientArea;
  }

  /**
   * Returns the content area.
   * 
   * @return the control in the content area of the pane or null
   */
  public Control getContent() {
    checkWidget();
    return content;
  }

  /**
   * Returns Control that appears in the top center of the pane. Typically this
   * is a toolbar.
   * 
   * @return the control in the top center of the pane or null
   */
  public Control getTopCenter() {
    checkWidget();
    return topCenter;
  }

  /**
   * Returns the Control that appears in the top left corner of the pane.
   * Typically this is a label such as CLabel.
   * 
   * @return the control in the top left corner of the pane or null
   */
  public Control getTopLeft() {
    checkWidget();
    return topLeft;
  }

  /**
   * Returns the control in the top right corner of the pane. Typically this is
   * a Close button or a composite with a Menu and Close button.
   * 
   * @return the control in the top right corner of the pane or null
   */
  public Control getTopRight() {
    checkWidget();
    return topRight;
  }

  void onDispose() {
    topLeft = null;
    topCenter = null;
    topRight = null;
    content = null;
    oldSize = null;
    selectionBackground = null;
  }

  // Draws the border, sets fg and bg colors
  /*
   * void onPaint(GC gc) { Color gcForeground = gc.getForeground(); Point size =
   * getSize(); Color border = getDisplay().getSystemColor(BORDER1_COLOR); if
   * (showBorder) { gc.setForeground(border); gc.drawRectangle(0, 0, size.x - 1,
   * size.y - 1); if (highlight > 0) { int x1 = 1; int y1 = 1; int x2 = size.x -
   * 1; int y2 = size.y - 1; int[] shape = new int[] {x1,y1, x2,y1, x2,y2,
   * x1,y2, x1,y1+highlight, x1+highlight,y1+highlight,
   * x1+highlight,y2-highlight, x2-highlight,y2-highlight,
   * x2-highlight,y1+highlight, x1,y1+highlight}; Color highlightColor =
   * getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
   * gc.setBackground(highlightColor); gc.fillPolygon(shape); } } if (separator >
   * -1) { gc.setForeground(border); gc.drawLine(borderLeft + highlight,
   * separator, size.x - borderLeft - borderRight - highlight, separator); }
   * gc.setForeground(gcForeground); }
   */
  void onResize() {
    Point size = getSize();
    if( oldSize == null || oldSize.x == 0 || oldSize.y == 0 ) {
      redraw();
    } else {
      int width = 0;
      if( oldSize.x < size.x ) {
        width = size.x - oldSize.x + borderRight + highlight;
      } else if( oldSize.x > size.x ) {
        width = borderRight + highlight;
      }
      redraw( size.x - width, 0, width, size.y, false );
      int height = 0;
      if( oldSize.y < size.y ) {
        height = size.y - oldSize.y + borderBottom + highlight;
      }
      if( oldSize.y > size.y ) {
        height = borderBottom + highlight;
      }
      redraw( 0, size.y - height, size.x, height, false );
    }
    oldSize = size;
  }

  /**
   * Sets the content. Setting the content to null will remove it from the pane -
   * however, the creator of the content must dispose of the content.
   * 
   * @param content the control to be displayed in the content area or null
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the control is not a child of
   *              this ViewForm</li>
   *              </ul>
   */
  public void setContent( Control content ) {
    checkWidget();
    if( content != null && content.getParent() != this ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( this.content != null && !this.content.isDisposed() ) {
      this.content.setBounds( OFFSCREEN, OFFSCREEN, 0, 0 );
    }
    this.content = content;
    layout(false);
  }

  /**
   * Sets the layout which is associated with the receiver to be the argument
   * which may be null.
   * <p>
   * Note: No Layout can be set on this Control because it already manages the
   * size and position of its children.
   * </p>
   * 
   * @param layout the receiver's new layout or null
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setLayout( Layout layout ) {
    checkWidget();
    return;
  }

  void setSelectionBackground( Color color ) {
    checkWidget();
    if( selectionBackground == color )
      return;
    if (color == null) color = getDisplay().getSystemColor(SELECTION_BACKGROUND);
    selectionBackground = color;
    redraw();
  }

  /**
   * Set the control that appears in the top center of the pane. Typically this
   * is a toolbar. The topCenter is optional. Setting the topCenter to null will
   * remove it from the pane - however, the creator of the topCenter must
   * dispose of the topCenter.
   * 
   * @param topCenter the control to be displayed in the top center or null
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the control is not a child of
   *              this ViewForm</li>
   *              </ul>
   */
  public void setTopCenter( Control topCenter ) {
    checkWidget();
    if( topCenter != null && topCenter.getParent() != this ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( this.topCenter != null && !this.topCenter.isDisposed() ) {
      Point size = this.topCenter.getSize();
      this.topCenter.setLocation( OFFSCREEN - size.x, OFFSCREEN - size.y );
    }
    this.topCenter = topCenter;
    layout(false);
  }

  /**
   * Set the control that appears in the top left corner of the pane. Typically
   * this is a label such as CLabel. The topLeft is optional. Setting the top
   * left control to null will remove it from the pane - however, the creator of
   * the control must dispose of the control.
   * 
   * @param c the control to be displayed in the top left corner or null
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the control is not a child of
   *              this ViewForm</li>
   *              </ul>
   */
  public void setTopLeft( Control c ) {
    checkWidget();
    if( c != null && c.getParent() != this ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( this.topLeft != null && !this.topLeft.isDisposed() ) {
      Point size = this.topLeft.getSize();
      this.topLeft.setLocation( OFFSCREEN - size.x, OFFSCREEN - size.y );
    }
    this.topLeft = c;
    layout(false);
  }

  /**
   * Set the control that appears in the top right corner of the pane. Typically
   * this is a Close button or a composite with a Menu and Close button. The
   * topRight is optional. Setting the top right control to null will remove it
   * from the pane - however, the creator of the control must dispose of the
   * control.
   * 
   * @param c the control to be displayed in the top right corner or null
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              <li>ERROR_INVALID_ARGUMENT - if the control is not a child of
   *              this ViewForm</li>
   *              </ul>
   */
  public void setTopRight( Control c ) {
    checkWidget();
    if( c != null && c.getParent() != this ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    if( this.topRight != null && !this.topRight.isDisposed() ) {
      Point size = this.topRight.getSize();
      this.topRight.setLocation( OFFSCREEN - size.x, OFFSCREEN - size.y );
    }
    this.topRight = c;
    layout(false);
  }

  /**
   * Specify whether the border should be displayed or not.
   * 
   * @param show true if the border should be displayed
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setBorderVisible( boolean show ) {
    checkWidget();
    if( showBorder == show )
      return;
    showBorder = show;
    if( showBorder ) {
      borderLeft = borderTop = borderRight = borderBottom = 1;
      if( ( getStyle() & SWT.FLAT ) == 0 )
        highlight = 2;
    } else {
      borderBottom = borderTop = borderLeft = borderRight = 0;
      highlight = 0;
    }
    layout(false);
    redraw();
  }

  /**
   * If true, the topCenter will always appear on a separate line by itself,
   * otherwise the topCenter will appear in the top row if there is room and
   * will be moved to the second row if required.
   * 
   * @param show true if the topCenter will always appear on a separate line by
   *          itself
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
   *              thread that created the receiver</li>
   *              </ul>
   */
  public void setTopCenterSeparate( boolean show ) {
    checkWidget();
    separateTopCenter = show;
    layout(false);
  }
}
