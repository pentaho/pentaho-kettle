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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 * This dialog allows you to enter a (single line) String.
 *
 * @author Matt
 * @since 21-11-2004
 */
public class EnterStringDialog extends Dialog {
  private static Class<?> PKG = EnterStringDialog.class; // for i18n purposes, needed by Translator2!!

  private Label wlString;

  private Text wString;

  private TextVar wStringVar;

  private TransMeta transMeta;

  private boolean allowVariables;

  private FormData fdlString, fdString;

  private Button wOK, wCancel;

  private Listener lsOK, lsCancel;

  private Shell shell;

  private SelectionAdapter lsDef;

  private String string;

  private String shellText;

  private String lineText;

  private PropsUI props;

  private boolean mandatory;

  /**
   * This constructs without allowing for variable substitution. This constructor allows for backwards compatibility for
   * objects that wish to create this object without variable substitution.
   *
   * @param parent
   *          Parent gui object
   * @param string
   *          The string to display in the dialog
   * @param shellText
   * @param lineText
   */
  public EnterStringDialog( Shell parent, String string, String shellText, String lineText ) {
    this( parent, string, shellText, lineText, false, null );
  }

  /**
   * Constructs with the ability to use environmental variable substitution.
   *
   * @param parent
   *          Parent gui object
   * @param string
   *          The string to display in the dialog
   * @param shellText
   * @param lineText
   * @param allowVariables
   *          Indicates to allow environmental substitution
   * @param transMeta
   *          This object has the has the environmental variables
   */
  public EnterStringDialog( Shell parent, String string, String shellText, String lineText,
    boolean allowVariables, TransMeta transMeta ) {
    super( parent, SWT.NONE );
    this.props = PropsUI.getInstance();
    this.string = string;
    this.shellText = shellText;
    this.lineText = lineText;
    this.allowVariables = allowVariables;
    this.transMeta = transMeta;
  }

  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    Control lastControl = null;

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL | SWT.SHEET );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );
    shell.setText( shellText );

    int margin = Const.MARGIN;

    // The String line...
    wlString = new Label( shell, SWT.NONE );
    wlString.setText( lineText );
    props.setLook( wlString );
    fdlString = new FormData();
    fdlString.left = new FormAttachment( 0, 0 );
    fdlString.top = new FormAttachment( 0, margin );
    wlString.setLayoutData( fdlString );
    if ( allowVariables ) {
      wStringVar = new TextVar( transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      wStringVar.setText( string );
      props.setLook( wStringVar );
      lastControl = wStringVar;
    } else {
      wString = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
      wString.setText( string );
      props.setLook( wString );
      lastControl = wString;
    }

    fdString = new FormData();
    fdString.left = new FormAttachment( 0, 0 );
    fdString.top = new FormAttachment( wlString, margin );
    fdString.right = new FormAttachment( 100, -margin );

    if ( allowVariables ) {
      wStringVar.setLayoutData( fdString );
      wStringVar.addModifyListener( new ModifyListener() {
        public void modifyText( ModifyEvent arg0 ) {
          setFlags();
        }
      } );
    } else {
      wString.setLayoutData( fdString );
      wString.addModifyListener( new ModifyListener() {
        public void modifyText( ModifyEvent arg0 ) {
          setFlags();
        }
      } );
    }

    // Some buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, lastControl );

    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    if ( allowVariables ) {
      wStringVar.addSelectionListener( lsDef );
    } else {
      wString.addSelectionListener( lsDef );
    }

    // Detect [X] or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();

    shell.pack();

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return string;
  }

  protected void setFlags() {
    String string = ( allowVariables ? wStringVar.getText() : wString.getText() );
    boolean enabled = !mandatory || !Utils.isEmpty( string );
    wOK.setEnabled( enabled );
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  public void getData() {
    if ( allowVariables ) {
      wStringVar.setText( Const.NVL( string, "" ) );
      wStringVar.selectAll();
    } else {
      wString.setText( Const.NVL( string, "" ) );
      wString.selectAll();
    }

    setFlags();
  }

  private void cancel() {
    string = null;
    dispose();
  }

  private void ok() {
    string = ( allowVariables ? wStringVar.getText() : wString.getText() );
    dispose();
  }

  /**
   * @return the manditory
   */
  @Deprecated
  public boolean isManditory() {
    return this.isMandatory();
  }

  /**
   * @return the mandatory
   */
  public boolean isMandatory() {
    return mandatory;
  }

  /**
   * @param manditory
   *          the manditory to set
   */
  @Deprecated
  public void setManditory( boolean manditory ) {
    this.setMandatory( manditory );
  }

  /**
   * @param mandatory
   *          the manditory to set
   */
  public void setMandatory( boolean mandatory ) {
    this.mandatory = mandatory;
  }
}
