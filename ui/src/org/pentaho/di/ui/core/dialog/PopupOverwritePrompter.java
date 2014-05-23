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
