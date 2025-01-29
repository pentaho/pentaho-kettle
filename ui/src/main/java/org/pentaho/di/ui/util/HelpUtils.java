/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ShowHelpDialog;
import org.pentaho.di.ui.core.gui.GUIResource;

public class HelpUtils {

  private static final Class<?> PKG = HelpUtils.class;

  public static Button createHelpButton( final Composite parent, final String title, final PluginInterface plugin ) {
    Button button = newButton( parent );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        openHelpDialog( parent.getShell(), plugin );
      }
    } );
    return button;
  }

  public static Button createHelpButton( final Composite parent, final String title, final String url,
      final String header ) {
    Button button = newButton( parent );
    button.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent arg0 ) {
        openHelpDialog( parent.getShell(), title, url, header );
      }
    } );
    return button;
  }

  private static Button newButton( final Composite parent ) {
    Button button = new Button( parent, SWT.PUSH );
    button.setImage( GUIResource.getInstance().getImageHelpWeb() );
    button.setText( BaseMessages.getString( PKG, "System.Button.Help" ) );
    button.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.Help" ) );
    FormData fdButton = new FormData();
    fdButton.left = new FormAttachment( 0, 0 );
    fdButton.bottom = new FormAttachment( 100, 0 );
    button.setLayoutData( fdButton );
    return button;
  }

  public static String getHelpDialogTitle( PluginInterface plugin ) {
    if ( plugin == null ) {
      return "";
    }
    String msgKey = "";
    // TODO currently support only Step and JobEntry - extend if required.
    if ( plugin.getPluginType().equals( StepPluginType.class ) ) {
      msgKey = "System.ShowHelpDialog.StepPluginType.Title";
    } else {
      msgKey = "System.ShowHelpDialog.JobEntryPluginType.Title";
    }
    return BaseMessages.getString( PKG, msgKey, plugin.getName() );
  }

  public static boolean isPluginDocumented( PluginInterface plugin ) {
    if ( plugin == null ) {
      return false;
    }
    return !StringUtil.isEmpty( plugin.getDocumentationUrl() );
  }

  public static ShowHelpDialog openHelpDialog( Shell shell, PluginInterface plugin ) {
    if ( shell == null || plugin == null ) {
      return null;
    }
    if ( isPluginDocumented( plugin ) ) {
      return openHelpDialog( shell, getHelpDialogTitle( plugin ), plugin.getDocumentationUrl(),
          plugin.getName()  );
    } else {
      MessageBox mb = new MessageBox( shell, SWT.OK | SWT.ICON_ERROR );
      String msgKey = "";
      // TODO currently support only Step and JobEntry - extend if required.
      if ( plugin.getPluginType().equals( StepPluginType.class ) ) {
        msgKey = "System.ShowHelpDialog.Step.HelpIsNotAvailable";
      } else {
        msgKey = "System.ShowHelpDialog.JobEntry.HelpIsNotAvailable";
      }
      mb.setMessage( BaseMessages.getString( PKG, msgKey, plugin.getName() ) );
      mb.setText( BaseMessages.getString( PKG, "System.Dialog.Error.Title" ) );
      mb.open();
    }
    return null;
  }

  public static ShowHelpDialog openHelpDialog( Shell shell, String dialogTitle, String url, String header ) {
    ShowHelpDialog helpDlg = new ShowHelpDialog( shell, dialogTitle, url, header );
    helpDlg.open();
    return helpDlg;
  }

  public static ShowHelpDialog openHelpDialog( Shell shell, String dialogTitle, String url ) {
    ShowHelpDialog helpDlg = new ShowHelpDialog( shell, dialogTitle, url );
    helpDlg.open();
    return helpDlg;
  }
}
