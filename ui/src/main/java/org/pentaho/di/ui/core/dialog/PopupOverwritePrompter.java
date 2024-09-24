/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.core.dialog;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;

public class PopupOverwritePrompter implements OverwritePrompter {
  private final Shell shell;
  private final Props props;

  public PopupOverwritePrompter( Shell shell, Props props ) {
    this.shell = shell;
    this.props = props;
  }

  @Override
  public boolean overwritePrompt( String message, String rememberText, String rememberPropertyName ) {
    Object[] res =
        messageDialogWithToggle( "Warning", null, message, Const.WARNING, new String[] {
          BaseMessages.getString( Spoon.class, "System.Button.Yes" ),
          BaseMessages.getString( Spoon.class, "System.Button.No" ) }, 1, rememberText, !"Y".equalsIgnoreCase( props
            .getProperty( rememberPropertyName ) ) );
    int idx = ( (Integer) res[0] ).intValue();
    boolean overwrite = ( ( idx & 0xFF ) == 0 );
    boolean toggleState = ( (Boolean) res[1] ).booleanValue();
    props.setProperty( rememberPropertyName, ( !toggleState ) ? "Y" : "N" );
    return overwrite;
  }

  public Object[] messageDialogWithToggle( String dialogTitle, Object image, String message, int dialogImageType,
      String[] buttonLabels, int defaultIndex, String toggleMessage, boolean toggleState ) {
    return GUIResource.getInstance().messageDialogWithToggle( shell, dialogTitle, (Image) image, message,
        dialogImageType, buttonLabels, defaultIndex, toggleMessage, toggleState );
  }
}
