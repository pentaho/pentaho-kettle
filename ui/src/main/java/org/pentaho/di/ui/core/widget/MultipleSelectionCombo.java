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
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Display;

public class MultipleSelectionCombo extends Composite {
  Button arrow;
  Text displayText = null;
  String[] comboItems;
  int[] comboSelection;
  Shell floatShell = null;
  List list = null;

  public MultipleSelectionCombo( Composite parent, int style ) {
    super( parent, style );
    comboItems = new String[]{};
    comboSelection = new int[]{};
    init();
  }

  private void init() {
    GridLayout layout = new GridLayout( 2, false );
    layout.marginBottom = 0;
    layout.marginTop = 0;
    layout.marginLeft = 0;
    layout.marginRight = 0;
    layout.marginWidth = 0;
    layout.marginHeight = 0;
    setLayout( layout );
    displayText = new Text( this, SWT.SINGLE );
    displayText.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

    arrow = new Button( this, SWT.ARROW | SWT.DOWN );
    arrow.setBackground( Display.getCurrent().getSystemColor( SWT.COLOR_BLUE ) );
    arrow.setSize( 25, 25 );
    arrow.setLocation( displayText.getLocation() );
    arrow.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseDown( MouseEvent event ) {
        super.mouseDown( event );
        if ( floatShell == null || floatShell.isDisposed() ) {
          initFloatShell();
        } else {
          closeShellAndUpdate();
        }
      }
    } );
  }

  private void initFloatShell() {
    Point p = displayText.getParent().toDisplay( displayText.getLocation() );
    Point size = displayText.getSize();
    Rectangle shellRect = new Rectangle( p.x, p.y + size.y, size.x, 0 );
    floatShell = new Shell( MultipleSelectionCombo.this.getShell(),
            SWT.NO_TRIM );

    GridLayout gl = new GridLayout();
    gl.marginBottom = 2;
    gl.marginTop = 2;
    gl.marginRight = 0;
    gl.marginLeft = 0;
    gl.marginWidth = 0;
    gl.marginHeight = 0;
    floatShell.setLayout( gl );

    list = new List( floatShell, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL
            | SWT.V_SCROLL );
    for ( String value : comboItems ) {
      list.add( value );
    }

    GridData gd = new GridData( GridData.FILL_BOTH );
    list.setLayoutData( gd );
    floatShell.setSize( shellRect.width, 100 );
    floatShell.setLocation( shellRect.x, shellRect.y );
    list.addMouseListener( new MouseAdapter() {
      @Override
      public void mouseUp( MouseEvent event ) {
        super.mouseUp( event );
        comboSelection = list.getSelectionIndices();
        displayText();
      }
    } );

    floatShell.open();
  }

  private void closeShellAndUpdate() {
    if ( floatShell != null && !floatShell.isDisposed() ) {
      comboSelection = list.getSelectionIndices();
      displayText();
      floatShell.dispose();
    }
  }

  private void displayText() {
    if ( comboSelection != null && comboSelection.length > 0 ) {
      StringBuilder sb = new StringBuilder();
      for ( int i = 0; i < comboSelection.length; i++ ) {
        if ( i > 0 ) {
          sb.append( "," );
        }
        sb.append( comboItems[comboSelection[i]] );
      }
      displayText.setText( sb.toString() );
    }
  }

  public void setText( String text ) {
    displayText.setText( text );
  }

  public String getText() {
    return displayText.getText();
  }

  public Text getTextWidget() {
    return displayText;
  }

  public void setItems( String[] items ) {
    this.comboItems = items;
  }
}
