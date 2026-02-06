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


package org.pentaho.di.ui.core.dialog;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
  public static final char DISABLED = '\0';

  private Label wlFields;
  private TableView wFields;
  private FormData fdlFields, fdFields;

  private Button wOK, wCancel;
  private Listener lsOK, lsCancel;

  private Shell shell;
  private PropsUI props;

  private Map<String, String> kettleProperties;

  private PropertiesConfiguration properties = new PropertiesConfiguration();
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
      loadProperties();

      // These are the standard Kettle variables...
      //
      KettleVariablesList variablesList = KettleVariablesList.getInstance();

      // Add the standard variables to the properties if they are not in there already
      //
      for ( String key : variablesList.getDescriptionMap().keySet() ) {
        if ( Utils.isEmpty( (String) properties.getString( key ) ) ) {
          String defaultValue = variablesList.getDefaultValueMap().get( key );
          properties.setProperty( key, Const.NVL( defaultValue, "" ) );
        }
      }

      // Obtain and sort the list of keys...
      //
      List<String> keys = new ArrayList<String>();
      Iterator<String> keysEnum = properties.getKeys();
      while ( keysEnum.hasNext() ) {
        keys.add( keysEnum.next() );
      }
      Collections.sort( keys );

      // Populate the grid...
      //
      for ( int i = 0; i < keys.size(); i++ ) {
        String key = keys.get( i );
        String value = properties.getString( key, "" );
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
      properties.getKeys().forEachRemaining( previousKettlePropertiesKeys::add);

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
    kettleProperties = new HashMap<String, String>();

    int nr = wFields.nrNonEmpty();
    for ( int i = 0; i < nr; i++ ) {
      TableItem item = wFields.getNonEmpty( i );
      int pos = 1;
      String variable = item.getText( pos++ );
      String value = item.getText( pos++ );

      if ( !Utils.isEmpty( variable ) ) {
        properties.setProperty( variable, value );
        kettleProperties.put( variable, value );
      }
    }

    // NOTE that the way the old file was laid out, the file header ended up attached to the first key
    properties.getLayout().setHeaderComment( Const.getKettlePropertiesFileHeader() );
    
    // Save the properties file...
    //
    try ( FileOutputStream out = new FileOutputStream( getKettlePropertiesFilename() );
          Writer writer = new OutputStreamWriter( out, "UTF-8" ) ) {
      properties.write( writer );
    } catch ( Exception e ) {
      new ErrorDialog( shell,
        BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Title" ),
        BaseMessages.getString( PKG, "KettlePropertiesFileDialog.Exception.ErrorSavingData.Message" ), e );
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

  private void loadProperties() throws IOException {
    String filename = getKettlePropertiesFilename();
    try ( FileInputStream fis = new FileInputStream( filename );
          InputStreamReader reader = new java.io.InputStreamReader( fis, "UTF-8" ) ) {
      properties.read( reader );
    } catch( ConfigurationException e ) {
      throw new IOException( "Unable to load kettle properties from file: " + filename, e );
    }
  }

}
