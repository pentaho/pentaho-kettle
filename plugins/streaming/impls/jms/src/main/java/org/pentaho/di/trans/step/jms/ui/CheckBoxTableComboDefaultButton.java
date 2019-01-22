/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.jms.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.JmsDelegate;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.BaseDialog;
import org.pentaho.di.ui.core.dialog.BaseMessageDialog;
import org.pentaho.di.ui.core.widget.CheckBoxTableCombo;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.pentaho.di.i18n.BaseMessages.getString;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class CheckBoxTableComboDefaultButton extends CheckBoxTableCombo {

  private Button wUseDefaultCheckBox;
  private JmsProvider.ConnectionType selectedConnectionType;
  private Composite parentComposite;
  protected boolean errorDialogProceed = true;
  private ConnectionForm connectionForm;

  private static final String JAVAX_SSL_TRUSTSTORE_PATH = "javax.net.ssl.trustStore";
  private static final String JAVAX_SSL_TRUSTSTORE_PASS = "javax.net.ssl.trustStorePassword";
  private static final String JAVAX_SSL_KEYSTORE_PATH = "javax.net.ssl.keyStore";
  private static final String JAVAX_SSL_KEYSTORE_PASS = "javax.net.ssl.keyStorePassword";
  private static final String HTTPS_CIPHER_SUITES = "https.cipherSuites";

  public CheckBoxTableComboDefaultButton( Composite parentComposite, PropsUI props, ModifyListener lsMod,
                                   TransMeta transMeta, Map<String, String> dataMap,
                                   Consumer<JmsProvider.ConnectionType> toggleVisibilityCallback,
                                   JmsDelegate delegate ) {
    super( parentComposite, props, lsMod, transMeta, dataMap,
      getString( PKG, "JmsDialog.Security.SSLButton" ),
      getString( PKG, "JmsDialog.Security.SSLTable" ),
      getString( PKG, "JmsDialog.Security.Column.Name" ),
      getString( PKG, "JmsDialog.Security.Column.Value" ),
      delegate.sslEnabled );

    this.parentComposite = parentComposite;
    wUseDefaultCheckBox = new Button( parentComposite, SWT.CHECK );
    wUseDefaultCheckBox.setText( getString( PKG, "JmsDialog.Security.SSL_USE_DEFAULT" ) );
    FormData fdCheckBox = new FormData();
    fdCheckBox.left = new FormAttachment( wCheckBox, 10 );
    wUseDefaultCheckBox.setLayoutData( fdCheckBox );
    wUseDefaultCheckBox.setSelection( delegate.sslUseDefaultContext );
    wUseDefaultCheckBox.setEnabled( delegate.sslEnabled );
    selectedConnectionType = JmsProvider.ConnectionType.valueOf( delegate.connectionType );

    for ( Listener l : wCheckBox.getListeners( SWT.Selection ) ) {
      if ( l instanceof SelectionListener ) {
        wCheckBox.removeSelectionListener( (SelectionListener) l );
      }
    }

    wCheckBox.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        boolean selection = ( (Button) selectionEvent.getSource() ).getSelection();
        boolean proceed = true;
        if ( selection ) {
          proceed = checkCommandLineArgs();
        }
        if ( proceed ) {
          wUseDefaultCheckBox.setEnabled( selection );
        } else {
          wCheckBox.setSelection( false );
          wUseDefaultCheckBox.setEnabled( false );
        }
        lsMod.modifyText( null );
        resetPropertyTableVisibility();
      }

      @Override public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        widgetSelected( selectionEvent );
      }
    } );

    wUseDefaultCheckBox.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        toggleVisibilityCallback.accept( selectedConnectionType );
        lsMod.modifyText( null );
        resetPropertyTableVisibility();
      }

      @Override public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        widgetSelected( selectionEvent );
      }
    } );
  }

  void setConnectionForm( ConnectionForm connectionForm ) {
    this.connectionForm = connectionForm;
  }

  boolean getUseDefaultSslContext() {
    return this.wUseDefaultCheckBox.getSelection();
  }

  /**
   * Method should be used to set the value of the currently selected connection type on the connection tab.
   *
   * @param type Selected connection type on the connection tab.
   */
  void setSelectedConnectionType( JmsProvider.ConnectionType type ) {
    selectedConnectionType = type;
  }

  JmsProvider.ConnectionType getSelectedConnectionType() {
    return selectedConnectionType;
  }

  public void resetPropertyTableVisibility() {
    propertiesTable.setEnabled( wCheckBox.getSelection()
      && ( selectedConnectionType.equals( JmsProvider.ConnectionType.WEBSPHERE )
      || !wUseDefaultCheckBox.getSelection() ) );
    propertiesTable.table.setEnabled( wCheckBox.getSelection()
      && ( selectedConnectionType.equals( JmsProvider.ConnectionType.WEBSPHERE )
      || !wUseDefaultCheckBox.getSelection() ) );
  }

  public void addSelectionListenerToSslCheckbox( SelectionListener l ) {
    wCheckBox.addSelectionListener( l );
  }

  protected boolean checkCommandLineArgs() {
    this.errorDialogProceed = true;
    if ( null != connectionForm && JmsProvider.ConnectionType.ACTIVEMQ.equals(
      JmsProvider.ConnectionType.valueOf( connectionForm.getConnectionType() ) ) ) {

      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      boolean showDialog = false;
      for ( String arg : runtimeMXBean.getInputArguments() ) {
        showDialog = arg.contains( JAVAX_SSL_KEYSTORE_PASS ) || arg.contains( JAVAX_SSL_KEYSTORE_PATH )
          || arg.contains( JAVAX_SSL_TRUSTSTORE_PASS ) || arg.contains( JAVAX_SSL_TRUSTSTORE_PATH )
          || arg.contains( HTTPS_CIPHER_SUITES );
        if ( showDialog ) {
          break;
        }
      }

      if ( showDialog ) {
        this.errorDialogProceed = false;

        final BaseDialog errorDlg = new BaseMessageDialog( parentComposite.getShell(),
          getString( PKG, "JmsDialog.Security.ACTIVEMQ_COMMAND_LINE_ARGS_MISMATCH_TITLE" ),
          getString( PKG, "JmsDialog.Security.ACTIVEMQ_COMMAND_LINE_ARGS_MISMATCH_MSG" ),
          350 );

        final Map<String, Listener> buttons = new HashMap();
        buttons
          .put( getString( PKG, "JmsDialog.Security.ACTIVEMQ_COMMAND_LINE_ARGS_MISMATCH_YES" ), event -> {
            errorDlg.dispose();
            this.errorDialogProceed = true;
          } );
        buttons
          .put( getString( PKG, "JmsDialog.Security.ACTIVEMQ_COMMAND_LINE_ARGS_MISMATCH_NO" ), event -> {
            errorDlg.dispose();
            this.errorDialogProceed = false;
          } );

        errorDlg.setButtons( buttons );
        errorDlg.open();
      }
    }
    return this.errorDialogProceed;
  }

}
