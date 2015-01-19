/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.core.namedcluster.dialog;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.namedcluster.NamedClusterUIHelper;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

/**
 *
 * Dialog that allows you to edit the settings of a named cluster.
 *
 * @see <code>NamedCluster</code>
 *
 */
public class NamedClusterDialog extends Dialog {
  private static Class<?> PKG = NamedClusterDialog.class; // for i18n purposes, needed by Translator2!!

  private Shell shell;
  private PropsUI props;
  private Button wOK, wCancel;
  
  private int margin;

  private NamedCluster originalNamedCluster;
  private NamedCluster namedCluster;
  private boolean newClusterCheck = false;
  private String result;

  public NamedClusterDialog( Shell parent ) {
    super( parent );
    props = PropsUI.getInstance();
  }

  public NamedClusterDialog( Shell parent, NamedCluster namedCluster ) {
    super( parent );
    props = PropsUI.getInstance();
    
    this.namedCluster = namedCluster;
    this.originalNamedCluster = namedCluster.clone();
  }

  public NamedCluster getNamedCluster() {
    return namedCluster;
  }

  public void setNamedCluster( NamedCluster namedCluster ) {
    this.namedCluster = namedCluster;
    this.originalNamedCluster = namedCluster.clone();
  }  
  
  public boolean isNewClusterCheck() {
    return newClusterCheck;
  }

  public void setNewClusterCheck(boolean newClusterCheck) {
    this.newClusterCheck = newClusterCheck;
  }
  
  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }
  
  public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.CLOSE | SWT.MAX | SWT.MIN | SWT.ICON );
    shell.setSize( 480, 550 );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    margin = Const.MARGIN;
    
    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setText( BaseMessages.getString( PKG, "NamedClusterDialog.Shell.Title" ) );
    shell.setLayout( new FormLayout() );

    NamedClusterComposite namedClusterComposite = new NamedClusterComposite( shell, namedCluster, props );
    FormData fd = new FormData( 470, 480 );
    namedClusterComposite.setLayoutData( fd );
    
    // Buttons
    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    Button[] buttons = new Button[] { wOK, wCancel };
    BaseStepDialog.positionBottomButtons( shell, buttons, margin, null );

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

    //BaseStepDialog.setSize( shell );
    shell.open();
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
    result = namedCluster.getName();
    if ( StringUtils.isEmpty( result ) ) {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      mb.setText( BaseMessages.getString( PKG, "NamedClusterDialog.Error" ) );
      mb.setMessage( BaseMessages.getString( PKG, "NamedClusterDialog.ClusterNameMissing" ) );
      mb.open();
      return;
    } else if ( newClusterCheck || !originalNamedCluster.getName().equals( result ) ) {
      // check that the name does not already exist
      try {
        NamedCluster fetched = NamedClusterUIHelper.getNamedCluster( result );
        if ( fetched != null ) {
          // there already exists a cluster with the new name, ask the user
          MessageBox mb = new MessageBox( shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
          mb.setText( BaseMessages.getString( PKG, "NamedClusterDialog.Warning" ) );
          mb.setMessage( BaseMessages.getString( PKG, "NamedClusterDialog.ClusterNameExists", result ) );
          if ( SWT.NO == mb.open() ) {
            // do not exist dialog
            return;
          }
        }
      } catch ( MetaStoreException ignored ) {
        // the lookup failed, the cluster does not exist, move on to dispose
      }
    }
    dispose();
  }
  
}