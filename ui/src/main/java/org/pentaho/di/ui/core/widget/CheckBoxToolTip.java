/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.widget;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.pentaho.di.ui.core.gui.GUIResource;

public class CheckBoxToolTip extends ToolTip {
  private String message = "";
  private String title;
  private Image image;
  private String checkBoxMessage;
  private boolean visible;

  private List<CheckBoxToolTipListener> listeners;

  private Display display;
  protected Rectangle checkBoxBounds;

  private boolean showingScrollBars;

  public CheckBoxToolTip( Control control ) {
    super( control, ToolTip.RECREATE, true );
    image = control.getDisplay().getSystemImage( SWT.ICON_INFORMATION );
    listeners = new ArrayList<CheckBoxToolTipListener>();
    visible = false;
    display = control.getDisplay();

    super.setRespectMonitorBounds( true );
    super.setRespectDisplayBounds( true );
    super.setHideDelay( 50000 );
    super.setPopupDelay( 0 );
    super.setHideOnMouseDown( false );
  }

  protected Composite createToolTipContentArea( Event event, Composite parent ) {
    Composite composite = new Composite( parent, SWT.NONE );
    FormLayout compLayout = new FormLayout();
    compLayout.marginHeight = 5;
    compLayout.marginWidth = 5;
    composite.setLayout( compLayout );

    composite.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );

    Label imageLabel = new Label( composite, SWT.NONE );
    imageLabel.setImage( image );
    imageLabel.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    FormData fdImageLabel = new FormData();
    fdImageLabel.left = new FormAttachment( 0, 0 );
    fdImageLabel.top = new FormAttachment( 0, 0 );
    imageLabel.setLayoutData( fdImageLabel );

    Label titleLabel = new Label( composite, SWT.LEFT );
    titleLabel.setText( title );
    titleLabel.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    titleLabel.setFont( GUIResource.getInstance().getFontBold() );
    FormData fdTitleLabel = new FormData();
    fdTitleLabel.left = new FormAttachment( imageLabel, 20 );
    fdTitleLabel.top = new FormAttachment( 0, 0 );
    titleLabel.setLayoutData( fdTitleLabel );

    Label line = new Label( composite, SWT.SEPARATOR | SWT.HORIZONTAL );
    line.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    FormData fdLine = new FormData();
    fdLine.left = new FormAttachment( imageLabel, 5 );
    fdLine.right = new FormAttachment( 100, -5 );
    fdLine.top = new FormAttachment( titleLabel, 5 );
    line.setLayoutData( fdLine );

    // Text messageLabel = new Text(composite, SWT.LEFT | ( showingScrollBars ? SWT.H_SCROLL | SWT.V_SCROLL : SWT.NONE )
    // );
    /*
     * Text messageLabel = new Text(composite, SWT.SINGLE | SWT.LEFT); messageLabel.setText(message);
     * messageLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND)); FormData fdMessageLabel = new
     * FormData(); fdMessageLabel.left = new FormAttachment(imageLabel, 20); fdMessageLabel.top = new
     * FormAttachment(line, 5); if (showingScrollBars) { fdMessageLabel.right = new FormAttachment(imageLabel, 500);
     * fdMessageLabel.bottom= new FormAttachment(line, 400); } messageLabel.setLayoutData(fdMessageLabel);
     */
    Label messageLabel = new Label( composite, SWT.LEFT );
    messageLabel.setText( message );
    messageLabel.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    FormData fdMessageLabel = new FormData();
    fdMessageLabel.left = new FormAttachment( imageLabel, 20 );
    fdMessageLabel.top = new FormAttachment( line, 5 );
    messageLabel.setLayoutData( fdMessageLabel );

    final Button disable = new Button( composite, SWT.CHECK );
    disable.setText( checkBoxMessage );
    disable.setBackground( display.getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );
    disable.setSelection( false );
    FormData fdDisable = new FormData();
    fdDisable.left = new FormAttachment( 0, 0 );
    fdDisable.top = new FormAttachment( messageLabel, 20 );
    fdDisable.bottom = new FormAttachment( 100, 0 );
    disable.setLayoutData( fdDisable );
    disable.addSelectionListener( new SelectionAdapter() {

      public void widgetSelected( SelectionEvent e ) {
        for ( CheckBoxToolTipListener listener : listeners ) {
          listener.checkBoxSelected( false );
        }
        hide();
      }

    } );
    disable.addPaintListener( new PaintListener() {

      public void paintControl( PaintEvent arg0 ) {
        checkBoxBounds = disable.getBounds();
      }

    } );

    composite.layout();
    checkBoxBounds = disable.getBounds();

    return composite;
  }

  public void show( Point location ) {
    super.show( location );
    visible = true;
  }

  public void hide() {
    visible = false;
    super.hide();
  }

  public void addCheckBoxToolTipListener( CheckBoxToolTipListener listener ) {
    listeners.add( listener );
  }

  protected void afterHideToolTip( Event event ) {
    super.afterHideToolTip( event );
    visible = false;
  }

  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param message
   *          the message to set
   */
  public void setMessage( String message ) {
    this.message = message;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle( String title ) {
    this.title = title;
    try {
      Method setText = super.getClass().getMethod( "setText", String.class );
      setText.invoke( this, title );
    } catch ( NoSuchMethodException e ) {
      // For Webspoon: No need to call method if it doesn't exist.
    } catch ( InvocationTargetException | IllegalAccessException e )  {
      e.printStackTrace();
    }
  }

  /**
   * @return the image
   */
  public Image getImage() {
    return image;
  }

  /**
   * @param image
   *          the image to set
   */
  public void setImage( Image image ) {
    this.image = image;
  }

  /**
   * @return the checkBoxMessage
   */
  public String getCheckBoxMessage() {
    return checkBoxMessage;
  }

  /**
   * @param checkBoxMessage
   *          the checkBoxMessage to set
   */
  public void setCheckBoxMessage( String checkBoxMessage ) {
    this.checkBoxMessage = checkBoxMessage;
  }

  /**
   * @return the visible
   */
  public boolean isVisible() {
    return visible;
  }

  /**
   * @param visible
   *          the visible to set
   */
  public void setVisible( boolean visible ) {
    this.visible = visible;
  }

  /**
   * @return the checkBoxBounds
   */
  public Rectangle getCheckBoxBounds() {
    return checkBoxBounds;
  }

  /**
   * @param checkBoxBounds
   *          the checkBoxBounds to set
   */
  public void setCheckBoxBounds( Rectangle checkBoxBounds ) {
    this.checkBoxBounds = checkBoxBounds;
  }

  /**
   * @return the showingScrollBars
   */
  public boolean isShowingScrollBars() {
    return showingScrollBars;
  }

  /**
   * @param showingScrollBars
   *          the showingScrollBars to set
   */
  public void setShowingScrollBars( boolean showingScrollBars ) {
    this.showingScrollBars = showingScrollBars;
  }
}
