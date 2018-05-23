/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * A base dialog class containing a body and a configurable button panel.
 */
public abstract class BaseDialog extends Dialog {

  public static final int MARGIN_SIZE = 15;
  public static final int LABEL_SPACING = 5;
  public static final int ELEMENT_SPACING = 10;
  public static final int MEDIUM_FIELD = 250;
  public static final int MEDIUM_SMALL_FIELD = 150;
  public static final int SMALL_FIELD = 50;
  public static final int SHELL_WIDTH_OFFSET = 16;
  public static final int VAR_ICON_WIDTH = GUIResource.getInstance().getImageVariable().getBounds().width;
  public static final int VAR_ICON_HEIGHT = GUIResource.getInstance().getImageVariable().getBounds().height;

  protected Map<String, Listener> buttons = new HashMap();

  protected Shell shell;

  protected PropsUI props;
  protected int width = -1;
  protected String title;

  private int footerTopPadding = BaseDialog.ELEMENT_SPACING * 4;

  public BaseDialog( final Shell shell ) {
    this( shell, null, -1 );
  }

  public BaseDialog( final Shell shell, final String title, final int width ) {
    super( shell, SWT.NONE );
    this.props = PropsUI.getInstance();
    this.title = title;
    this.width = width;
  }

  /**
   * Returns a {@link org.eclipse.swt.events.SelectionAdapter} that is used to "submit" the dialog.
   */
  private Display prepareLayout() {

    // Prep the parent shell and the dialog shell
    final Shell parent = getParent();
    final Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );
    props.setLook( shell );
    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      @Override
      public void shellClosed( ShellEvent e ) {
        dispose();
      }
    } );

    final FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = MARGIN_SIZE;
    formLayout.marginHeight = MARGIN_SIZE;

    shell.setLayout( formLayout );
    shell.setText( this.title );
    return display;
  }

  /**
   * Returns the last element in the body - the one to which the buttons should be attached.
   *
   * @return
   */
  protected abstract Control buildBody();

  public int open() {
    final Display display = prepareLayout();

    final Control lastBodyElement = buildBody();
    buildFooter( lastBodyElement );

    open( display );

    return 1;
  }

  private void open( final Display display ) {
    shell.pack();
    if ( width > 0 ) {
      final int height = shell.computeSize( width, SWT.DEFAULT ).y;
      // for some reason the actual width and minimum width are smaller than what is requested - add the
      // SHELL_WIDTH_OFFSET to get the desired size
      shell.setMinimumSize( width + SHELL_WIDTH_OFFSET, height );
      shell.setSize( width + SHELL_WIDTH_OFFSET, height );
    }

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  protected void buildFooter( final Control anchorElement ) {

    final Button[] buttonArr = new Button[ buttons == null ? 0 : buttons.size() ];
    int index = 0;
    if ( buttons != null ) {
      for ( final String buttonName : buttons.keySet() ) {
        final Button button = new Button( shell, SWT.PUSH );
        button.setText( buttonName );
        final Listener listener = buttons.get( buttonName );
        if ( listener != null ) {
          button.addListener( SWT.Selection, listener );
        } else {
          // fall back on simply closing the dialog
          button.addListener( SWT.Selection, event -> {
            dispose();
          } );
        }
        buttonArr[ index++ ] = button;
      }
    }

    // traverse the buttons backwards to position them to the right
    Button previousButton = null;
    for ( int i = buttonArr.length - 1; i >= 0; i-- ) {
      final Button button = buttonArr[ i ];
      if ( previousButton == null ) {
        button.setLayoutData( new FormDataBuilder().top(
          anchorElement, footerTopPadding ).right( 100, 0 ).result() );
      } else {
        button.setLayoutData( new FormDataBuilder().top( anchorElement, footerTopPadding ).right(
          previousButton, Const.isOSX() ? 0 : -BaseDialog.LABEL_SPACING ).result() );
      }
      previousButton = button;
    }
  }

  public void setFooterTopPadding( final int footerTopPadding ) {
    this.footerTopPadding = footerTopPadding;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void setButtons( final  Map<String, Listener> buttons ) {
    this.buttons = buttons;
  }
}
