/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.util;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.SimpleMessageDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.FieldDisabledListener;
import org.pentaho.di.ui.core.widget.TableView;

public class ParameterTableHelper {
  private static final int PARAM_COL_1 = 1;
  private static final int PARAM_COL_2 = 2;
  private static final int PARAM_COL_3 = 3;

  private int paramRowNum = -1;
  private int paramColNum = -1;

  private TableView parameterTableView;

  private FieldDisabledListener varDisabledListener;
  private FieldDisabledListener fieldDisabledListener;
  private FieldDisabledListener inputDisabledListener;

  public void setUpDisabledListeners() {
    varDisabledListener = rowNr -> {
      paramRowNum = rowNr;
      paramColNum = PARAM_COL_1;
      return false;
    };
    fieldDisabledListener = new FieldDisabledListener() {
      @Override
      public boolean isFieldDisabled( String value ) {
        return !Utils.isEmpty( value );
      }

      public boolean isFieldDisabled( int rowNr ) {
        paramRowNum = rowNr;
        paramColNum = PARAM_COL_2;

        String field = parameterTableView.table.getItem( rowNr ).getText( PARAM_COL_3 );
        return !Utils.isEmpty( field );
      }
    };
    inputDisabledListener = new FieldDisabledListener() {
      @Override
      public boolean isFieldDisabled( String value ) {
        return !Utils.isEmpty( value );
      }

      public boolean isFieldDisabled( int rowNr ) {
        paramRowNum = rowNr;
        paramColNum = PARAM_COL_3;

        String field = parameterTableView.table.getItem( rowNr ).getText( PARAM_COL_2 );
        return !Utils.isEmpty( field );
      }
    };
  }

  public void checkTableOnMod( ModifyEvent modifyEvent ) {
    String value = "";
    if ( paramColNum == PARAM_COL_3 && modifyEvent.widget instanceof Text ) {
      Text text = (Text) modifyEvent.widget;
      value = text.getText();
      parameterTableView.table.getItem( paramRowNum ).setBackground( PARAM_COL_2,
          fieldDisabledListener.isFieldDisabled( value ) ? GUIResource.getInstance().getColorLightGray()
            : GUIResource.getInstance().getColorWhite() );
    } else if ( paramColNum  == PARAM_COL_2 ) {
      if ( modifyEvent.widget instanceof CCombo ) {
        CCombo cCombo = (CCombo) modifyEvent.widget;
        value = cCombo.getText();
      } else if ( modifyEvent.widget instanceof Text ) {
        Text text = (Text) modifyEvent.widget;
        value = text.getText();
      }
      parameterTableView.table.getItem( paramRowNum ).setBackground( PARAM_COL_3,
          inputDisabledListener.isFieldDisabled( value ) ? GUIResource.getInstance().getColorLightGray()
            : GUIResource.getInstance().getColorWhite() );
    }
  }

  public void checkTableOnOpen( TableItem tableItem, int i ) {
    if ( fieldDisabledListener.isFieldDisabled( i ) ) {
      tableItem.setBackground( PARAM_COL_2, GUIResource.getInstance().getColorLightGray() );
    }
    if ( inputDisabledListener.isFieldDisabled( i ) ) {
      tableItem.setBackground( PARAM_COL_3, GUIResource.getInstance().getColorLightGray() );
    }
  }

  public void setParameterTableView( TableView parameterTableView ) {
    this.parameterTableView = parameterTableView;
  }

  public FieldDisabledListener getFieldDisabledListener() {
    return fieldDisabledListener;
  }

  public FieldDisabledListener getInputDisabledListener() {
    return inputDisabledListener;
  }

  public FieldDisabledListener getVarDisabledListener() {
    return varDisabledListener;
  }

  /**
   * If user has not entered a parameter name, but has entered a value/variable, OR
   * if user has entered a parameter name, but not entered a value/variable.
   * If either is true, present error dialog and return true, else return false.
   * @return true if conditions above are met, false otherwise
   */
  public boolean checkParams( Shell shell ) {
    if ( parameterTableView != null ) {
      for ( int i = 0; i < parameterTableView.getItemCount(); i++ ) {
        String[] params = parameterTableView.getItem( i );
        if ( Utils.isEmpty( params[ 0 ] ) && ( !Utils.isEmpty( params[ 1 ] ) || !Utils.isEmpty( params[ 2 ] ) ) ) {
          new SimpleMessageDialog( shell, BaseMessages.getString( "Dialog.Parameters.Missing.Parameter.Title" ),
            BaseMessages.getString( "Dialog.Parameters.Missing.Parameter.Message" ), MessageDialog.ERROR,
            BaseMessages.getString( "System.Button.OK" ), 350, SimpleMessageDialog.BUTTON_WIDTH ).open();
          return true;
        } else if ( !Utils.isEmpty( params[ 0 ] ) && Utils.isEmpty( params[ 1 ] ) && Utils.isEmpty( params[ 2 ] ) ) {
          new SimpleMessageDialog( shell, BaseMessages.getString( "Dialog.Parameters.Missing.Value.Title" ),
            BaseMessages.getString( "Dialog.Parameters.Missing.Value.Message" ), MessageDialog.ERROR,
            BaseMessages.getString( "System.Button.OK" ), 350, SimpleMessageDialog.BUTTON_WIDTH ).open();
          return true;
        }
      }
    }
    return false;
  }
}
