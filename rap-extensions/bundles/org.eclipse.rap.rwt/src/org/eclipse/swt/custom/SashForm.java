/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    EclipseSource - ongoing development
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;


/**
 * The SashForm is a composite control that lays out its children in a row or column arrangement (as
 * specified by the orientation) and places a Sash between each child. One child may be maximized to
 * occupy the entire size of the SashForm. The relative sizes of the children may be specified using
 * weights.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>HORIZONTAL, VERTICAL, SMOOTH</dd>
 * </dl>
 * </p>
 *
 * @since 1.0
 */
public class SashForm extends Composite {

  public int SASH_WIDTH = 3;
  private static final int DRAG_MINIMUM = 20;

  int sashStyle;
  Sash[] sashes = new Sash[ 0 ];
  // Remember background and foreground
  // colors to determine whether to set
  // sashes to the default color (null) or
  // a specific color
  Color background = null;
  Color foreground = null;
  Control[] controls = new Control[ 0 ];
  Control maxControl = null;
  Listener sashListener;

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
   * @param parent a widget which will be the parent of the new instance (cannot be null)
   * @param style the style of widget to construct
   *
   * @exception IllegalArgumentException <ul>
   *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   * </ul>
   * @exception SWTException <ul>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
   * </ul>
   *
   * @see SWT#HORIZONTAL
   * @see SWT#VERTICAL
   * @see #getStyle()
   */
  public SashForm( Composite parent, int style ) {
    super( parent, checkStyle( style ) );
    super.setLayout( new SashFormLayout() );
    sashStyle = ( ( style & SWT.VERTICAL ) != 0 ) ? SWT.HORIZONTAL : SWT.VERTICAL;
    if( ( style & SWT.BORDER ) != 0 ) {
      sashStyle |= SWT.BORDER;
    }
    if( ( style & SWT.SMOOTH ) != 0 ) {
      sashStyle |= SWT.SMOOTH;
    }
    sashListener = new Listener() {
      @Override
      public void handleEvent( Event e ) {
        onDragSash( e );
      }
    };
  }

  static int checkStyle( int style ) {
    int mask = SWT.BORDER | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
    return style & mask;
  }

  /**
   * Returns SWT.HORIZONTAL if the controls in the SashForm are laid out side by side
   * or SWT.VERTICAL   if the controls in the SashForm are laid out top to bottom.
   *
   * <p>
   * To retrieve the bidi orientation of the SashForm use <code>{@link #getStyle()}</code>
   * and test if the SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT bits are set.
   * </p>
   *
   * @return SWT.HORIZONTAL or SWT.VERTICAL
   */
  @Override
  public int getOrientation() {
    // checkWidget();
    return ( sashStyle & SWT.VERTICAL ) != 0 ? SWT.HORIZONTAL : SWT.VERTICAL;
  }

  /**
   * Returns the width of the sashes when the controls in the SashForm are
   * laid out.
   *
   * @return the width of the sashes
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public int getSashWidth() {
    checkWidget();
    return SASH_WIDTH;
  }

  @Override
  public int getStyle() {
    int style = super.getStyle();
    style |= getOrientation() == SWT.VERTICAL ? SWT.VERTICAL : SWT.HORIZONTAL;
    if( ( sashStyle & SWT.SMOOTH ) != 0 ) {
      style |= SWT.SMOOTH;
    }
    return style;
  }

  /**
   * Answer the control that currently is maximized in the SashForm.
   * This value may be null.
   *
   * @return the control that currently is maximized or null
   */
  public Control getMaximizedControl() {
    // checkWidget();
    return maxControl;
  }

  /**
   * Answer the relative weight of each child in the SashForm.  The weight represents the
   * percent of the total width (if SashForm has Horizontal orientation) or
   * total height (if SashForm has Vertical orientation) each control occupies.
   * The weights are returned in order of the creation of the widgets (weight[0]
   * corresponds to the weight of the first child created).
   *
   * @return the relative weight of each child
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  public int[] getWeights() {
    checkWidget();
    Control[] cArray = getControls( false );
    int[] ratios = new int[ cArray.length ];
    for( int i = 0; i < cArray.length; i++ ) {
      Object data = cArray[ i ].getLayoutData();
      if( data != null && data instanceof SashFormData ) {
        ratios[ i ] = ( int )( ( ( SashFormData )data ).weight * 1000 >> 16 );
      } else {
        ratios[ i ] = 200;
      }
    }
    return ratios;
  }

  Control[] getControls( boolean onlyVisible ) {
    Control[] children = getChildren();
    Control[] result = new Control[ 0 ];
    for( int i = 0; i < children.length; i++ ) {
      if( children[ i ] instanceof Sash ) {
        continue;
      }
      if( onlyVisible && !children[ i ].getVisible() ) {
        continue;
      }
      Control[] newResult = new Control[ result.length + 1 ];
      System.arraycopy( result, 0, newResult, 0, result.length );
      newResult[ result.length ] = children[ i ];
      result = newResult;
    }
    return result;
  }

  void onDragSash( Event event ) {
    Sash sash = ( Sash )event.widget;
    int sashIndex = -1;
    for( int i = 0; i < sashes.length; i++ ) {
      if( sashes[ i ] == sash ) {
        sashIndex = i;
        break;
      }
    }
    if( sashIndex == -1 ) {
      return;
    }
    Control c1 = controls[ sashIndex ];
    Control c2 = controls[ sashIndex + 1 ];
    Rectangle b1 = c1.getBounds();
    Rectangle b2 = c2.getBounds();
    Rectangle sashBounds = sash.getBounds();
    Rectangle area = getClientArea();
    boolean correction = false;
    if( getOrientation() == SWT.HORIZONTAL ) {
      correction = b1.width < DRAG_MINIMUM || b2.width < DRAG_MINIMUM;
      int totalWidth = b2.x + b2.width - b1.x;
      int shift = event.x - sashBounds.x;
      b1.width += shift;
      b2.x += shift;
      b2.width -= shift;
      if( b1.width < DRAG_MINIMUM ) {
        b1.width = DRAG_MINIMUM;
        b2.x = b1.x + b1.width + sashBounds.width;
        b2.width = totalWidth - b2.x;
        event.x = b1.x + b1.width;
        event.doit = false;
      }
      if( b2.width < DRAG_MINIMUM ) {
        b1.width = totalWidth - DRAG_MINIMUM - sashBounds.width;
        b2.x = b1.x + b1.width + sashBounds.width;
        b2.width = DRAG_MINIMUM;
        event.x = b1.x + b1.width;
        event.doit = false;
      }
      Object data1 = c1.getLayoutData();
      if( data1 == null || !( data1 instanceof SashFormData ) ) {
        data1 = new SashFormData();
        c1.setLayoutData( data1 );
      }
      Object data2 = c2.getLayoutData();
      if( data2 == null || !( data2 instanceof SashFormData ) ) {
        data2 = new SashFormData();
        c2.setLayoutData( data2 );
      }
      ( ( SashFormData )data1 ).weight = ( ( ( long )b1.width << 16 ) + area.width - 1 )
                                         / area.width;
      ( ( SashFormData )data2 ).weight = ( ( ( long )b2.width << 16 ) + area.width - 1 )
                                         / area.width;
    } else {
      correction = b1.height < DRAG_MINIMUM || b2.height < DRAG_MINIMUM;
      int totalHeight = b2.y + b2.height - b1.y;
      int shift = event.y - sashBounds.y;
      b1.height += shift;
      b2.y += shift;
      b2.height -= shift;
      if( b1.height < DRAG_MINIMUM ) {
        b1.height = DRAG_MINIMUM;
        b2.y = b1.y + b1.height + sashBounds.height;
        b2.height = totalHeight - b2.y;
        event.y = b1.y + b1.height;
        event.doit = false;
      }
      if( b2.height < DRAG_MINIMUM ) {
        b1.height = totalHeight - DRAG_MINIMUM - sashBounds.height;
        b2.y = b1.y + b1.height + sashBounds.height;
        b2.height = DRAG_MINIMUM;
        event.y = b1.y + b1.height;
        event.doit = false;
      }
      Object data1 = c1.getLayoutData();
      if( data1 == null || !( data1 instanceof SashFormData ) ) {
        data1 = new SashFormData();
        c1.setLayoutData( data1 );
      }
      Object data2 = c2.getLayoutData();
      if( data2 == null || !( data2 instanceof SashFormData ) ) {
        data2 = new SashFormData();
        c2.setLayoutData( data2 );
      }
      ( ( SashFormData )data1 ).weight = ( ( ( long )b1.height << 16 ) + area.height - 1 )
                                         / area.height;
      ( ( SashFormData )data2 ).weight = ( ( ( long )b2.height << 16 ) + area.height - 1 )
                                         / area.height;
    }
    if( correction || ( event.doit && event.detail != SWT.DRAG ) ) {
      c1.setBounds( b1 );
      sash.setBounds( event.x, event.y, event.width, event.height );
      c2.setBounds( b2 );
    }
  }

  /**
   * If orientation is SWT.HORIZONTAL, lay the controls in the SashForm
   * out side by side.  If orientation is SWT.VERTICAL, lay the
   * controls in the SashForm out top to bottom.
   *
   * <p>
   * Since 3.1, this method can also be called with SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
   * to change the bidi orientation of the SashForm.
   * </p>
   *
   * @param orientation SWT.HORIZONTAL or SWT.VERTICAL, SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
   *
   * @see Control#setOrientation(int)
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the value of orientation is not SWT.HORIZONTAL or SWT.VERTICAL, SWT.RIGHT_TO_LEFT or SWT.LEFT_TO_RIGHT
   * </ul>
   */
  @Override
  public void setOrientation( int orientation ) {
    checkWidget();
    if( orientation == SWT.RIGHT_TO_LEFT || orientation == SWT.LEFT_TO_RIGHT ) {
      super.setOrientation( orientation );
      return;
    }
    if( getOrientation() == orientation ) {
      return;
    }
    if( orientation != SWT.HORIZONTAL && orientation != SWT.VERTICAL ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    sashStyle &= ~( SWT.HORIZONTAL | SWT.VERTICAL );
    sashStyle |= orientation == SWT.VERTICAL ? SWT.HORIZONTAL : SWT.VERTICAL;
    for( int i = 0; i < sashes.length; i++ ) {
      sashes[ i ].dispose();
      sashes[ i ] = new Sash( this, sashStyle );
      sashes[ i ].setBackground( background );
      sashes[ i ].setForeground( foreground );
      sashes[ i ].addListener( SWT.Selection, sashListener );
    }
    layout( false );
  }

  @Override
  public void setBackground( Color color ) {
    super.setBackground( color );
    background = color;
    for( int i = 0; i < sashes.length; i++ ) {
      sashes[ i ].setBackground( background );
    }
  }

  @Override
  public void setForeground( Color color ) {
    super.setForeground( color );
    foreground = color;
    for( int i = 0; i < sashes.length; i++ ) {
      sashes[ i ].setForeground( foreground );
    }
  }

  /**
   * Sets the layout which is associated with the receiver to be
   * the argument which may be null.
   * <p>
   * Note: No Layout can be set on this Control because it already
   * manages the size and position of its children.
   * </p>
   *
   * @param layout the receiver's new layout or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   */
  @Override
  public void setLayout( Layout layout ) {
    checkWidget();
    return;
  }

  /**
   * Specify the control that should take up the entire client area of the SashForm.
   * If one control has been maximized, and this method is called with a different control,
   * the previous control will be minimized and the new control will be maximized.
   * If the value of control is null, the SashForm will minimize all controls and return to
   * the default layout where all controls are laid out separated by sashes.
   *
   * @param control the control to be maximized or null
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public void setMaximizedControl( Control control ) {
    checkWidget();
    if( control == null ) {
      if( maxControl != null ) {
        this.maxControl = null;
        layout( false );
        for( int i = 0; i < sashes.length; i++ ) {
          sashes[ i ].setVisible( true );
        }
      }
      return;
    }
    for( int i = 0; i < sashes.length; i++ ) {
      sashes[ i ].setVisible( false );
    }
    maxControl = control;
    layout( false );
  }

  /**
   * Specify the width of the sashes when the controls in the SashForm are
   * laid out.
   *
   * @param width the width of the sashes
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   * </ul>
   *
   * @since 1.2
   */
  public void setSashWidth( int width ) {
    checkWidget();
    if( SASH_WIDTH == width ) {
      return;
    }
    SASH_WIDTH = width;
    layout( false );
  }

  /**
   * Specify the relative weight of each child in the SashForm.  This will determine
   * what percent of the total width (if SashForm has Horizontal orientation) or
   * total height (if SashForm has Vertical orientation) each control will occupy.
   * The weights must be positive values and there must be an entry for each
   * non-sash child of the SashForm.
   *
   * @param weights the relative weight of each child
   *
   * @exception SWTException <ul>
   *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
   *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
   *    <li>ERROR_INVALID_ARGUMENT - if the weights value is null or of incorrect length (must match the number of children)</li>
   * </ul>
   */
  public void setWeights( int[] weights ) {
    checkWidget();
    Control[] cArray = getControls( false );
    if( weights == null || weights.length != cArray.length ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    int total = 0;
    for( int i = 0; i < weights.length; i++ ) {
      if( weights[ i ] < 0 ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      total += weights[ i ];
    }
    if( total == 0 ) {
      SWT.error( SWT.ERROR_INVALID_ARGUMENT );
    }
    for( int i = 0; i < cArray.length; i++ ) {
      Object data = cArray[ i ].getLayoutData();
      if( data == null || !( data instanceof SashFormData ) ) {
        data = new SashFormData();
        cArray[ i ].setLayoutData( data );
      }
      ( ( SashFormData )data ).weight = ( ( ( long )weights[ i ] << 16 ) + total - 1 ) / total;
    }
    layout( false );
  }

}
