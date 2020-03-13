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
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.FieldDisabledListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Allows the user to edit the kettle.properties file.
 *
 * @author Matt
 *
 */
public class KettlePropertiesFileDialog extends Dialog {
  private static Class<?> PKG = KettlePropertiesFileDialog.class; // for i18n purposes, needed by Translator2!!

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;

  private Shell shell;
  private PropsUI props;

  private Map<String, String> kettleProperties;

  private Set<String> previousKettlePropertiesKeys;

  /**
   * Constructs a new dialog
   *
   * @param parent
   *          The parent shell to link to
   * @param style
   *          The style in which we want to draw this shell.
   * @param strings
   *          The list of rows to change.
   */
  public KettlePropertiesFileDialog( Shell parent, int style ) {
    super( parent, style );
    props = PropsUI.getInstance();
    kettleProperties = null;
  }

  public Map<String, String> open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX );
    shell.setImage( GUIResource.getInstance().getImageTransGraph() );
    props.setLook( shell );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Title" ) );

    int margin = Const.MARGIN;

    // Message line
    //
    wlFields = new Label( shell, SWT.NONE );
    wlFields.setText( BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Message" ) );
    props.setLook( wlFields );
    fdlFields = new FormData();
    fdlFields.left = new FormAttachment( 0, 0 );
    fdlFields.top = new FormAttachment( 0, margin );
    wlFields.setLayoutData( fdlFields );

    int FieldsRows = 0;

    ColumnInfo[] colinf =
      new ColumnInfo[] {
        new ColumnInfo(
          BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Name.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Value.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, false ),
        new ColumnInfo(
          BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Description.Label" ),
          ColumnInfo.COLUMN_TYPE_TEXT, false, true ), };
    colinf[2].setDisabledListener( new FieldDisabledListener() {
      public boolean isFieldDisabled( int rowNr ) {
        return false;
      }
    } );

    wFields =
      new TableView(
        Variables.getADefaultVariableSpace(), shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf,
        FieldsRows, null, props );

    wFields.setReadonly( false );

    fdFields = new FormData();
    fdFields.left = new FormAttachment( 0, 0 );
    fdFields.top = new FormAttachment( wlFields, 30 );
    fdFields.right = new FormAttachment( 100, 0 );
    fdFields.bottom = new FormAttachment( 100, -50 );
    wFields.setLayoutData( fdFields );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );

    BaseStepDialog.positionBottomButtons( shell, new Button[] { wOK, wCancel }, margin, wFields );

    // Add listeners
    lsOK = new Listener() {
      public void handleEvent( Event e ) {
        ok();
      }
    };
    lsCancel = new Listener() {
      public void handleEvent( Event e ) {
        cancel();
      }
    };

    wOK.addListener( SWT.Selection, lsOK );
    wCancel.addListener( SWT.Selection, lsCancel );

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener( new ShellAdapter() {
      public void shellClosed( ShellEvent e ) {
        cancel();
      }
    } );

    getData();

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return kettleProperties;
  }

  public void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    try {
      // Load the Kettle properties file...
      //
      Properties properties = EnvUtil.readProperties( getKettlePropertiesFilename() );

      // These are the standard Kettle variables...
      //
      KettleVariablesList variablesList = KettleVariablesList.getInstance();

      // Add the standard variables to the properties if they are not in there already
      //
      for ( String key : variablesList.getDescriptionMap().keySet() ) {
        if ( Utils.isEmpty( (String) properties.get( key ) ) ) {
          String defaultValue = variablesList.getDefaultValueMap().get( key );
          properties.put( key, Const.NVL( defaultValue, "" ) );
        }
      }

      // Obtain and sort the list of keys...
      //
      List<String> keys = new ArrayList<String>();
      Enumeration<Object> keysEnum = properties.keys();
      while ( keysEnum.hasMoreElements() ) {
        keys.add( (String) keysEnum.nextElement() );
      }
      Collections.sort( keys );

      // Populate the grid...
      //
      for ( int i = 0; i < keys.size(); i++ ) {
        String key = keys.get( i );
        String value = properties.getProperty( key, "" );
        String description = Const.NVL( variablesList.getDescriptionMap().get( key ), "" );

        TableItem item = new TableItem( wFields.table, SWT.NONE );
        item.setBackground( 3, GUIResource.getInstance().getColorLightGray() );

        int pos = 1;
        item.setText( pos++, key );
        item.setText( pos++, value );
        item.setText( pos++, description );
      }

      wFields.removeEmptyRows();
      wFields.setRowNums();
      wFields.optWidth( true );

      //saves the properties keys at the moment this method was called
      previousKettlePropertiesKeys = new HashSet<>();
      previousKettlePropertiesKeys.addAll( Arrays.asList( properties.keySet().toArray( new String[ 0 ] ) ) );

    } catch ( Exception e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Exception.ErrorLoadingData.Title" ),
        BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Exception.ErrorLoadingData.Message" ), e );
    }
  }

  private String getKettlePropertiesFilename() {
    return Const.getKettlePropertiesFilename();
  }

  private void cancel() {
    kettleProperties = null;
    dispose();
  }

  private void ok() {
    Properties properties = new Properties();
    kettleProperties = new HashMap<String, String>();

    int nr = wFields.nrNonEmpty();
    for ( int i = 0; i < nr; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      int pos = 1;
      String variable = item.getText( pos++ );
      String value = item.getText( pos++ );

      if ( !Utils.isEmpty( variable ) ) {
        properties.put( variable, value );
        kettleProperties.put( variable, value );
      }
    }

    // Save the properties file...
    //
    FileOutputStream out = null;
    try {
      out = new FileOutputStream( getKettlePropertiesFilename() );
      properties.store( out, Const.getKettlePropertiesFileHeader() );
    } catch ( Exception e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Title" ),
        BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Message" ), e );
    } finally {
      try {
        out.close();
      } catch ( IOException e ) {
        LogChannel.GENERAL.logError( BaseMessages.getString(
          PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Message", Const.KETTLE_PROPERTIES,
          getKettlePropertiesFilename() ), e );
      }
    }

    if ( previousKettlePropertiesKeys != null ) {
      for ( String originalKey : previousKettlePropertiesKeys ) {
        if ( !kettleProperties.containsKey( originalKey ) ) {
          EnvUtil.clearSystemProperty( originalKey );
        }
      }
    }

    dispose();
  }
}
