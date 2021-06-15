/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * A ControlEditor is a manager for a Control that appears above a composite and
 * tracks with the moving and resizing of that composite. It can be used to
 * display one control above another control. This could be used when editing a
 * control that does not have editing capabilities by using a text editor or for
 * launching a dialog by placing a button above a control.
 * <p>
 * Here is an example of using a ControlEditor: <code><pre>
 * Canvas canvas = new Canvas(shell, SWT.BORDER);
 * canvas.setBounds(10, 10, 300, 300);
 * Color color = new Color(null, 255, 0, 0);
 * canvas.setBackground(color);
 * ControlEditor editor = new ControlEditor (canvas);
 * // The editor will be a button in the bottom right corner of the canvas.
 * // When selected, it will launch a Color dialog that will change the background
 * // of the canvas.
 * Button button = new Button(canvas, SWT.PUSH);
 * button.setText("Select Color...");
 * button.addSelectionListener (new SelectionAdapter() {
 * 	public void widgetSelected(SelectionEvent e) {
 * 		ColorDialog dialog = new ColorDialog(shell);
 * 		dialog.open();
 * 		RGB rgb = dialog.getRGB();
 * 		if (rgb != null) {
 * 			if (color != null) color.dispose();
 * 			color = new Color(null, rgb);
 * 			canvas.setBackground(color);
 * 		}
 *
 * 	}
 * });
 *
 * editor.horizontalAlignment = SWT.RIGHT;
 * editor.verticalAlignment = SWT.BOTTOM;
 * editor.grabHorizontal = false;
 * editor.grabVertical = false;
 * Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
 * editor.minimumWidth = size.x;
 * editor.minimumHeight = size.y;
 * editor.setEditor (button);
 * </pre></code>
 *
 * @since 1.0
 */
public class ControlEditor implements SerializableCompatibility {

  /**
   * Specifies how the editor should be aligned relative to the control. Allowed
   * values are SWT.LEFT, SWT.RIGHT and SWT.CENTER. The default value is
   * SWT.CENTER.
   */
  public int horizontalAlignment = SWT.CENTER;
  /**
   * Specifies whether the editor should be sized to use the entire width of the
   * control. True means resize the editor to the same width as the cell. False
   * means do not adjust the width of the editor. The default value is false.
   */
  public boolean grabHorizontal = false;
  /**
   * Specifies the minimum width the editor can have. This is used in
   * association with a true value of grabHorizontal. If the cell becomes
   * smaller than the minimumWidth, the editor will not made smaller than the
   * minimum width value. The default value is 0.
   */
  public int minimumWidth = 0;
  /**
   * Specifies how the editor should be aligned relative to the control. Allowed
   * values are SWT.TOP, SWT.BOTTOM and SWT.CENTER. The default value is
   * SWT.CENTER.
   */
  public int verticalAlignment = SWT.CENTER;
  /**
   * Specifies whether the editor should be sized to use the entire height of
   * the control. True means resize the editor to the same height as the
   * underlying control. False means do not adjust the height of the editor. The
   * default value is false.
   */
  public boolean grabVertical = false;
  /**
   * Specifies the minimum height the editor can have. This is used in
   * association with a true value of grabVertical. If the control becomes
   * smaller than the minimumHeight, the editor will not made smaller than the
   * minimum height value. The default value is 0.
   */
  public int minimumHeight = 0;
  Composite parent;
  Control editor;
  private boolean hadFocus;
  private Listener controlListener;
  private Listener scrollbarListener;
  private final static int[] EVENTS = {/*
                                        * SWT.KeyDown, SWT.KeyUp, SWT.MouseDown,
                                        * SWT.MouseUp,
                                        */
    SWT.Resize
  };

  /**
   * Creates a ControlEditor for the specified Composite.
   *
   * @param parent the Composite above which this editor will be displayed
   */
  public ControlEditor( Composite parent ) {
    this.parent = parent;
    controlListener = new Listener() {
      public void handleEvent( Event e ) {
        layout();
      }
    };
    for( int i = 0; i < EVENTS.length; i++ ) {
      parent.addListener( EVENTS[ i ], controlListener );
    }
    scrollbarListener = new Listener() {
      public void handleEvent( Event e ) {
        scroll( e );
      }
    };
    ScrollBar hBar = parent.getHorizontalBar();
    ScrollBar vBar = parent.getVerticalBar();
    if( hBar != null ) {
      hBar.addListener( SWT.Selection, scrollbarListener );
    }
    if( vBar != null ) {
      vBar.addListener( SWT.Selection, scrollbarListener );
    }
  }

  Rectangle computeBounds() {
    Rectangle clientArea = parent.getClientArea();
    Rectangle editorRect = new Rectangle( clientArea.x,
                                          clientArea.y,
                                          minimumWidth,
                                          minimumHeight );
    if( grabHorizontal )
      editorRect.width = Math.max( clientArea.width, minimumWidth );
    if( grabVertical )
      editorRect.height = Math.max( clientArea.height, minimumHeight );
    switch( horizontalAlignment ) {
      case SWT.RIGHT:
        editorRect.x += clientArea.width - editorRect.width;
      break;
      case SWT.LEFT:
        // do nothing - clientArea.x is the right answer
      break;
      default:
        // default is CENTER
        editorRect.x += ( clientArea.width - editorRect.width ) / 2;
    }
    switch( verticalAlignment ) {
      case SWT.BOTTOM:
        editorRect.y += clientArea.height - editorRect.height;
      break;
      case SWT.TOP:
        // do nothing - clientArea.y is the right answer
      break;
      default:
        // default is CENTER
        editorRect.y += ( clientArea.height - editorRect.height ) / 2;
    }
    return editorRect;
  }

  /**
   * Removes all associations between the Editor and the underlying composite.
   * The composite and the editor Control are <b>not</b> disposed.
   */
  public void dispose() {
    if( parent != null && !parent.isDisposed() ) {
      for( int i = 0; i < EVENTS.length; i++ ) {
        parent.removeListener( EVENTS[ i ], controlListener );
      }
      ScrollBar hBar = parent.getHorizontalBar();
      ScrollBar vBar = parent.getVerticalBar();
      if( hBar != null ) {
        hBar.removeListener( SWT.Selection, scrollbarListener );
      }
      if( vBar != null ) {
        vBar.removeListener( SWT.Selection, scrollbarListener );
      }
    }
    parent = null;
    editor = null;
    hadFocus = false;
    controlListener = null;
    scrollbarListener = null;
  }

  /**
   * Returns the Control that is displayed above the composite being edited.
   *
   * @return the Control that is displayed above the composite being edited
   */
  public Control getEditor() {
    return editor;
  }

  /**
   * Lays out the control within the underlying composite. This method should be
   * called after changing one or more fields to force the Editor to resize.
   */
  public void layout() {
    if( editor == null || editor.isDisposed() )
      return;
    if( editor.getVisible() ) {
      hadFocus = editor.isFocusControl();
    } // this doesn't work because
    // resizing the column takes the focus away
    // before we get here
    editor.setBounds( computeBounds() );
    if( hadFocus ) {
      if( editor == null || editor.isDisposed() )
        return;
      editor.setFocus();
    }
  }

  void scroll( Event e ) {
    if( editor == null || editor.isDisposed() )
      return;
    layout();
  }

  /**
   * Specify the Control that is to be displayed.
   * <p>
   * Note: The Control provided as the editor <b>must</b> be created with its
   * parent being the Composite specified in the ControlEditor constructor.
   *
   * @param editor the Control that is displayed above the composite being
   *          edited
   */
  public void setEditor( Control editor ) {
    if( editor == null ) {
      // this is the case where the caller is setting the editor to be blank
      // set all the values accordingly
      this.editor = null;
      return;
    }
    this.editor = editor;
    layout();
    if( this.editor == null || this.editor.isDisposed() )
      return;
    editor.setVisible( true );
  }
}
