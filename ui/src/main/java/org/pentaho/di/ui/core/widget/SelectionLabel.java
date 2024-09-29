/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2020 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

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
