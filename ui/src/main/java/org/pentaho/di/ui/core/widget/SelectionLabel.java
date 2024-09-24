/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


public class SelectionLabel extends Composite {
  private static final int HEIGHT_HINT = 28;
  private MouseAdapter exitAction;
  private String labelText;

  public SelectionLabel( Composite parent, int style, String text, MouseAdapter exitAction ) {
    super( parent, style );
    this.exitAction = exitAction;
    this.labelText = text;
    init( text );
  }

  private void init( String text ) {
    this.setLayout( new GridLayout( 2, false ) );
    this.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    Label label = new Label( this, SWT.BEGINNING );
    label.setText( text );
    label.setToolTipText( text );
    label.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    Label button = new Label( this, SWT.FILL );
    button.setText( " \u274C " );
    button.setToolTipText( "Remove " + text );
    GridData xGridData = new GridData( SWT.END, SWT.CENTER, false, false );
    xGridData.minimumWidth = 25;
    button.setLayoutData( xGridData );
    button.setLocation( label.getLocation() );
    button.setCursor( new Cursor( Display.getDefault(), SWT.CURSOR_HAND ) );
    button.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW ) );
    button.addMouseListener( exitAction );
  }

  public String getLabelText() {
    return labelText;
  }

  /**
   * This will return a hint in case a parent Composite needs to resize
   * before this Composite is drawn
   * @return
   */
  public int getHeight() {
    if ( this.getSize().y == 0 ) {
      return HEIGHT_HINT;
    } else {
      return this.getSize().y;
    }
  }
}
