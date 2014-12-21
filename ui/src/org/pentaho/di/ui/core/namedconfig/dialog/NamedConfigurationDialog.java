/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.namedconfig.dialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

/**
 *
 * Dialog that allows you to edit the settings of a named configuration.
 *
 * @see <code>NamedConfiguration</code>
 * @author Matt
 * @since 18-05-2003
 *
 */
public class NamedConfigurationDialog extends Dialog {
  private static Class<?> PKG = NamedConfigurationDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private PropsUI props;
  private Button wOK, wCancel;
  
  private int margin;

  private NamedConfiguration namedConfiguration;
  private String result;

  public NamedConfigurationDialog( Shell parent ) {
    super( parent );
    props = PropsUI.getInstance();
    namedConfiguration = new NamedConfiguration();
  }

  public NamedConfigurationDialog( Shell parent, NamedConfiguration namedConfiguration ) {
    super( parent );
    props = PropsUI.getInstance();
    this.namedConfiguration = namedConfiguration;
  }

  public NamedConfiguration getNamedConfiguration() {
    return namedConfiguration;
  }

  public void setNamedConfiguration(NamedConfiguration namedConfiguration) {
    this.namedConfiguration = namedConfiguration;
  }  
  
  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }
  
  public String open() {
    Shell parent = getParent();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSlave() );

    margin = Const.MARGIN;
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "NamedConfiguarationDialog.Shell.Title" ) );
    shell.setLayout( formLayout );
    
    new NamedConfigurationComposite( shell, namedConfiguration, props );

    // First, add the buttons...

    // Buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    Button[] buttons = new Button[] { wOK, wCancel };
    BaseStepDialog.positionBottomButtons( shell, buttons, margin, null );

    // The rest stays above the buttons...

    // Add listeners
    wOK.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    } );
    wCancel.addListener( SWT.Selection, new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    } );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    BaseStepDialog.setSize( shell );

    shell.open();
    Display display = parent.getDisplay();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return result;
  }
  
  private void cancel() {
    result = null;
    dispose();
  }

  public void ok() {
    result = namedConfiguration.getName();
    dispose();
  }
  
}