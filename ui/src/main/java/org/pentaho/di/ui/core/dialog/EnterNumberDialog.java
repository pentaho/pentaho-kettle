/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.FormDataBuilder;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;

/**
 * This dialog allows you to enter a number.
 *
 * @author Matt
 * @since 19-06-2003
 */
public class EnterNumberDialog extends Dialog {
  private static Class<?> PKG = EnterNumberDialog.class; // for i18n purposes, needed by Translator2!!

  private Label wlNumber, wlCheckbox;
  protected Text wNumber;
  private FormData fdlNumber, fdNumber, fdlCheckbox, fdCheckbox;

  protected Button wOK, wCancel, wCheckbox;
  private Listener lsOK, lsCancel;
  private boolean hideCancelButton;

  protected Shell shell;
  private SelectionAdapter lsDef;

  protected int samples;
  private String shellText;
  private String lineText;
  private String checkboxLabel;
  private PropsUI props;

  private int width;

  /**
   * @deprecated Use the CT without the <i>Props</i> parameter (at 2nd position)
   */
  @Deprecated
  public EnterNumberDialog( Shell parent, PropsUI props, int samples, String shellText, String lineText ) {
    super( parent, SWT.NONE );
    this.props = props;
    this.samples = samples;
    this.shellText = shellText;
    this.lineText = lineText;
  }

  public EnterNumberDialog( Shell parent, int samples, String shellText, String lineText ) {
    this( parent, samples, shellText, lineText, null );
  }

  public EnterNumberDialog( Shell parent, int samples, String shellText, String lineText, final String checkboxLabel,
                            final int width ) {
    this( parent, samples, shellText, lineText, checkboxLabel );
    this.width = width;
  }

  public EnterNumberDialog( Shell parent, int samples, String shellText, String lineText, final String checkboxLabel ) {
    super( parent, SWT.NONE );
    this.props = PropsUI.getInstance();
    this.samples = samples;
    this.shellText = shellText;
    this.lineText = lineText;
    this.checkboxLabel = checkboxLabel;
  }

  public int open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.SHEET );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = BaseDialog.MARGIN_SIZE;
    formLayout.marginHeight = BaseDialog.MARGIN_SIZE;

    shell.setLayout( formLayout );
    shell.setText( shellText );

    int length = Const.LENGTH;

    // From step line
    wlNumber = new Label( shell, SWT.NONE );
    wlNumber.setText( lineText );
    props.setLook( wlNumber );
    fdlNumber = new FormData();
    fdlNumber.left = new FormAttachment( 0, 0 );
    fdlNumber.top = new FormAttachment( 0, 0 );
    wlNumber.setLayoutData( fdlNumber );
    wNumber = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wNumber.setText( "100" );
    props.setLook( wNumber );
    fdNumber = new FormData();
    fdNumber.left = new FormAttachment( 0, 0 );
    fdNumber.top = new FormAttachment( wlNumber, BaseDialog.LABEL_SPACING );
    fdNumber.right = new FormAttachment( 100, 0 );
    wNumber.setLayoutData( fdNumber );

    Control lastControl = wNumber;
    if ( StringUtils.isNotBlank( checkboxLabel ) ) {
      wCheckbox = new Button( shell, SWT.CHECK );
      props.setLook( wCheckbox );
      fdCheckbox = new FormData();
      fdCheckbox.left = new FormAttachment( 0, 0 );
      fdCheckbox.top = new FormAttachment( wNumber, BaseDialog.ELEMENT_SPACING );
      fdCheckbox.width = ConstUI.CHECKBOX_WIDTH;
      wCheckbox.setLayoutData( fdCheckbox );

      wlCheckbox = new Label( shell, SWT.LEFT );
      wlCheckbox.addMouseListener( new MouseAdapter() {
        @Override public void mouseDown( MouseEvent mouseEvent ) {
          // toggle the checkbox when the label is clicked
          wCheckbox.setSelection( !wCheckbox.getSelection() );
        }
      } );
      wlCheckbox.setText( checkboxLabel );
      props.setLook( wlCheckbox );
      fdlCheckbox = new FormData();
      fdlCheckbox.left = new FormAttachment( wCheckbox, 0 );
      fdlCheckbox.top = new FormAttachment( wCheckbox, 0, SWT.CENTER );
      wlCheckbox.setLayoutData( fdlCheckbox );
      lastControl = wlCheckbox;
    }

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    if ( !hideCancelButton ) {
      wCancel = new Button( shell, SWT.PUSH );
      wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    }

    wCancel.setLayoutData( new FormDataBuilder().top(
      lastControl, BaseDialog.ELEMENT_SPACING * 2 ).right( 100, 0 ).result() );
    wOK.setLayoutData( new FormDataBuilder().top( lastControl, BaseDialog.ELEMENT_SPACING * 2 ).right(
      wCancel, Const.isOSX() ? 0 : -BaseDialog.LABEL_SPACING ).result() );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    if ( !hideCancelButton ) {
      lsCancel = new Listener() {
        public void handleEvent( Event e ) {
          cancel();
        }
      };
    }
    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };
    wNumber.addSelectionListener( lsDef );

    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();

    shell.pack();

    if ( this.width > 0 ) {
      final int height = shell.computeSize( this.width, SWT.DEFAULT ).y;
      // for some reason the actual width and minimum width are smaller than what is requested - add the
      // SHELL_WIDTH_OFFSET to get the desired size
      shell.setMinimumSize( this.width + BaseDialog.SHELL_WIDTH_OFFSET, height );
      shell.setSize( this.width + BaseDialog.SHELL_WIDTH_OFFSET, height );
    }

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return samples;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void getData() {
    wNumber.setText( Integer.toString( samples ) );
    wNumber.selectAll();
  }

  private void cancel() {
    samples = -1;
    dispose();
  }

  protected void ok() {
    try {
      samples = Integer.parseInt( wNumber.getText() );
      dispose();
    } catch ( Exception e ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setMessage( BaseMessages.getString( PKG, "Dialog.Error.EnterInteger" ) );
      mb.setText( BaseMessages.getString( PKG, "Dialog.Error.Header" ) );
      mb.open();
      wNumber.selectAll();
    }
  }

  public void setHideCancel( boolean hideCancel ) {
    hideCancelButton = hideCancel;
  }
}
