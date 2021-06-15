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

// import org.eclipse.swt.*;
// import org.eclipse.swt.graphics.*;
// import org.eclipse.rap.widgets.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * This Layout stacks all the controls one on top of the other and resizes all
 * controls to have the same size and location. The control specified in
 * topControl is visible and all other controls are not visible. Users must set
 * the topControl value to flip between the visible items and then call layout()
 * on the composite which has the StackLayout.
 * <p>
 * Here is an example which places ten buttons in a stack layout and flips
 * between them:
 * 
 * <pre><code>
 * public static void main( String[] args ) {
 *   Display display = new Display();
 *   Shell shell = new Shell( display );
 *   shell.setLayout( new GridLayout() );
 *   final Composite parent = new Composite( shell, SWT.NONE );
 *   parent.setLayoutData( new GridData( GridData.FILL_BOTH ) );
 *   final StackLayout layout = new StackLayout();
 *   parent.setLayout( layout );
 *   final Button[] bArray = new Button[ 10 ];
 *   for( int i = 0; i &lt; 10; i++ ) {
 *     bArray[ i ] = new Button( parent, SWT.PUSH );
 *     bArray[ i ].setText( &quot;Button &quot; + i );
 *   }
 *   layout.topControl = bArray[ 0 ];
 *   Button b = new Button( shell, SWT.PUSH );
 *   b.setText( &quot;Show Next Button&quot; );
 *   final int[] index = new int[ 1 ];
 *   b.addListener( SWT.Selection, new Listener() {
 * 
 *     public void handleEvent( Event e ) {
 *       index[ 0 ] = ( index[ 0 ] + 1 ) % 10;
 *       layout.topControl = bArray[ index[ 0 ] ];
 *       parent.layout();
 *     }
 *   } );
 *   shell.open();
 *   while( shell != null &amp;&amp; !shell.isDisposed() ) {
 *     if( !display.readAndDispatch() )
 *       display.sleep();
 *   }
 * }
 * </code></pre>
 */
public class StackLayout extends Layout {
  /**
   * marginWidth specifies the number of pixels of horizontal margin that will
   * be placed along the left and right edges of the layout. The default value
   * is 0.
   */
  public int marginWidth = 0;
  /**
   * marginHeight specifies the number of pixels of vertical margin that will be
   * placed along the top and bottom edges of the layout. The default value is
   * 0.
   */
  public int marginHeight = 0;
  /**
   * topControl the Control that is displayed at the top of the stack. All other
   * controls that are children of the parent composite will not be visible.
   */
  public Control topControl;

  protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
    Control children[] = composite.getChildren();
    int maxWidth = 0;
    int maxHeight = 0;
    for( int i = 0; i < children.length; i++ ) {
      Point size = children[ i ].computeSize( wHint, hHint, flushCache );
      maxWidth = Math.max( size.x, maxWidth );
      maxHeight = Math.max( size.y, maxHeight );
    }
    int width = maxWidth + 2 * marginWidth;
    int height = maxHeight + 2 * marginHeight;
    if( wHint != SWT.DEFAULT )
      width = wHint;
    if( hHint != SWT.DEFAULT )
      height = hHint;
    return new Point( width, height );
  }

  protected boolean flushCache( Control control ) {
    return true;
  }

  protected void layout( Composite composite, boolean flushCache ) {
    Control children[] = composite.getChildren();
    Rectangle rect = composite.getClientArea();
    rect.x += marginWidth;
    rect.y += marginHeight;
    rect.width -= 2 * marginWidth;
    rect.height -= 2 * marginHeight;
    for( int i = 0; i < children.length; i++ ) {
      children[ i ].setBounds( rect );
      children[ i ].setVisible( children[ i ] == topControl );
    }
  }

  String getName() {
    String string = getClass().getName();
    int index = string.lastIndexOf( '.' );
    if( index == -1 )
      return string;
    return string.substring( index + 1, string.length() );
  }

  /**
   * Returns a string containing a concise, human-readable description of the
   * receiver.
   * 
   * @return a string representation of the layout
   */
  public String toString() {
    String string = getName() + " {";
    if( marginWidth != 0 )
      string += "marginWidth=" + marginWidth + " ";
    if( marginHeight != 0 )
      string += "marginHeight=" + marginHeight + " ";
    if( topControl != null )
      string += "topControl=" + topControl + " ";
    string = string.trim();
    string += "}";
    return string;
  }
}
