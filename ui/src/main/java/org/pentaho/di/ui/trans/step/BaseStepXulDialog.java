/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.spoon.XulSpoonResourceBundle;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.util.HelpUtils;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulSettingsManager;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.swt.SwtBindingFactory;
import org.pentaho.ui.xul.swt.SwtXulRunner;

/**
 * User: nbaker Date: Jun 7, 2010
 */
public abstract class BaseStepXulDialog extends BaseStepGenericXulDialog {

  @Override
  public XulSettingsManager getSettingsManager() {
    return XulSpoonSettingsManager.getInstance();
  }

  @Override
  public ResourceBundle getResourceBundle() {
    return new XulSpoonResourceBundle( getClassForMessages() );
  }

  @Override
  public void clear() {
    // Nothing to do
  }

  @Override
  public boolean validate() {
    return true;
  }

  private static Class<?> PKG = StepInterface.class; // for i18n purposes, needed by Translator2!!

  protected Listener lsOK, lsGet, lsPreview, lsSQL, lsCreate, lsCancel;

  protected Listener lsResize;

  protected boolean changed, backupChanged;

  protected PropsUI props;

  protected static final int BUTTON_ALIGNMENT_CENTER = 0;

  protected static final int BUTTON_ALIGNMENT_LEFT = 1;

  protected static final int BUTTON_ALIGNMENT_RIGHT = 2;

  protected static int buttonAlignment = BUTTON_ALIGNMENT_CENTER;

  protected DatabaseDialog databaseDialog;

  protected Shell dialogShell;

  static {
    // Get the button alignment
    buttonAlignment = getButtonAlignment();
  }

  public BaseStepXulDialog( String xulFile, Shell parent, BaseStepMeta baseStepMeta, TransMeta transMeta,
    String stepname ) {

    super( xulFile, parent, baseStepMeta, transMeta, stepname );

    this.backupChanged = baseStepMeta.hasChanged();
    this.props = PropsUI.getInstance();

    try {
      initializeXul();
    } catch ( Exception e ) {
      e.printStackTrace();
      log.logError( "Error initializing (" + stepname + ") step dialog", e );
      throw new IllegalStateException( "Cannot load dialog due to error in initialization", e );
    }
  }

  protected void initializeXul() throws XulException {
    initializeXul( new KettleXulLoader(), new SwtBindingFactory(), new SwtXulRunner(), parent );
    dialogShell = (Shell) xulDialog.getRootObject();
  }

  public void setShellImage( Shell shell, StepMetaInterface stepMetaInterface ) {
    try {
      String id = PluginRegistry.getInstance().getPluginId( StepPluginType.class, stepMetaInterface );
      if ( getShell() != null && id != null ) {
        getShell().setImage( GUIResource.getInstance().getImagesSteps().get( id ).getAsBitmap( shell.getDisplay() ) );
      }
    } catch ( Throwable e ) {
      // Ignore
    }
  }

  public void dispose() {
    Shell shell = (Shell) this.xulDialog.getRootObject();

    if ( !shell.isDisposed() ) {
      WindowProperty winprop = new WindowProperty( shell );
      props.setScreen( winprop );
      ( (Composite) this.xulDialog.getManagedObject() ).dispose();
      shell.dispose();
    }
  }

  public Shell getShell() {
    return dialogShell;
  }

  /**
   * Set the shell size, based upon the previous time the geometry was saved in the Properties file.
   */
  public void setSize() {
    setSize( dialogShell );
  }

  /**
   * Returns the default alignment for the buttons. This is set in the LAF properties with the key
   * <code>Button_Position</code>. The valid values are:
   * <UL>
   * <LI><code>left</code>
   * <LI><code>center</code>
   * <LI><code>right</code>
   * </UL>
   * NOTE: if the alignment is not provided or contains an invalid value, <code>center</code> will be used as a default
   *
   * @return a constant which indicates the button alignment
   */
  protected static int getButtonAlignment() {
    String buttonAlign = BasePropertyHandler.getProperty( "Button_Position", "center" ).toLowerCase();
    if ( "center".equals( buttonAlign ) ) {
      return BUTTON_ALIGNMENT_CENTER;
    } else if ( "left".equals( buttonAlign ) ) {
      return BUTTON_ALIGNMENT_LEFT;
    } else {
      return BUTTON_ALIGNMENT_RIGHT;
    }
  }

  protected DatabaseDialog getDatabaseDialog( Shell shell ) {
    if ( databaseDialog == null ) {
      databaseDialog = new DatabaseDialog( shell );
    }
    return databaseDialog;
  }

  public void storeScreenSize() {
    props.setScreen( new WindowProperty( dialogShell ) );
  }

  public static void setSize( Shell shell ) {
    setSize( shell, -1, -1, true );
  }

  public static void setSize( Shell shell, int minWidth, int minHeight, boolean packIt ) {
    PropsUI props = PropsUI.getInstance();

    WindowProperty winprop = props.getScreen( shell.getText() );
    if ( winprop != null ) {
      winprop.setShell( shell, minWidth, minHeight );
    } else {
      if ( packIt ) {
        shell.pack();
      } else {
        shell.layout();
      }

      // OK, sometimes this produces dialogs that are waay too big.
      // Try to limit this a bit, m'kay?
      // Use the same algorithm by cheating :-)
      //
      winprop = new WindowProperty( shell );
      winprop.setShell( shell, minWidth, minHeight );

      // Now, as this is the first time it gets opened, try to put it in the middle of the screen...
      Rectangle shellBounds = shell.getBounds();
      Monitor monitor = shell.getDisplay().getPrimaryMonitor();
      if ( shell.getParent() != null ) {
        monitor = shell.getParent().getMonitor();
      }
      Rectangle monitorClientArea = monitor.getClientArea();

      int middleX = monitorClientArea.x + ( monitorClientArea.width - shellBounds.width ) / 2;
      int middleY = monitorClientArea.y + ( monitorClientArea.height - shellBounds.height ) / 2;

      shell.setLocation( middleX, middleY );
    }
  }

  /**
   * Gets unused fields from previous steps and inserts them as rows into a table view.
   *
   * @param row
   *          the input fields
   * @param tableView
   *          the table view to modify
   * @param keyColumn
   *          the column in the table view to match with the names of the fields, checks for existance if >0
   * @param nameColumn
   *          the column numbers in which the name should end up in
   * @param dataTypeColumn
   *          the target column numbers in which the data type should end up in
   * @param lengthColumn
   *          the length column where the length should end up in (if >0)
   * @param precisionColumn
   *          the length column where the precision should end up in (if >0)
   * @param listener
   *          A listener that you can use to do custom modifications to the inserted table item, based on a value from
   *          the provided row
   */
  public static final void getFieldsFromPrevious( RowMetaInterface row, XulTree tableView, int keyColumn,
    int[] nameColumn, int[] dataTypeColumn, int lengthColumn, int precisionColumn,
    TableItemInsertListener listener ) {
    if ( row == null || row.size() == 0 ) {
      return; // nothing to do
    }

    Table table = ( (TableViewer) tableView.getManagedObject() ).getTable();

    // get a list of all the non-empty keys (names)
    //
    List<String> keys = new ArrayList<String>();
    for ( int i = 0; i < table.getItemCount(); i++ ) {
      TableItem tableItem = table.getItem( i );
      String key = tableItem.getText( keyColumn );
      if ( !Utils.isEmpty( key ) && keys.indexOf( key ) < 0 ) {
        keys.add( key );
      }
    }

    int choice = 0;

    if ( keys.size() > 0 ) {
      // Ask what we should do with the existing data in the step.
      //
      Shell shell = ( (TableViewer) tableView.getManagedObject() ).getTable().getShell();
      MessageDialog md =
        new MessageDialog( shell,
          BaseMessages.getString( PKG, "BaseStepDialog.GetFieldsChoice.Title" ), // "Warning!"
          null,
          BaseMessages.getString( PKG, "BaseStepDialog.GetFieldsChoice.Message", "" + keys.size(), "" + row.size() ),
          MessageDialog.WARNING, new String[] {
            BaseMessages.getString( PKG, "BaseStepDialog.AddNew" ),
            BaseMessages.getString( PKG, "BaseStepDialog.Add" ),
            BaseMessages.getString( PKG, "BaseStepDialog.ClearAndAdd" ),
            BaseMessages.getString( PKG, "BaseStepDialog.Cancel" ), }, 0 );
      MessageDialog.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      int idx = md.open();
      choice = idx & 0xFF;
    }

    if ( choice == 3 || choice == 255 ) {
      return; // Cancel clicked
    }

    if ( choice == 2 ) {
      tableView.getRootChildren().removeAll();
    }

    for ( int i = 0; i < row.size(); i++ ) {
      ValueMetaInterface v = row.getValueMeta( i );

      boolean add = true;

      if ( choice == 0 ) { // hang on, see if it's not yet in the table view

        if ( keys.indexOf( v.getName() ) >= 0 ) {
          add = false;
        }
      }

      if ( add ) {
        XulTreeRow tRow = tableView.getRootChildren().addNewRow();

        for ( int c = 0; c < nameColumn.length; c++ ) {
          tRow.addCellText( nameColumn[c], Const.NVL( v.getName(), "" ) );
        }
        if ( dataTypeColumn != null ) {
          for ( int c = 0; c < dataTypeColumn.length; c++ ) {
            tRow.addCellText( dataTypeColumn[c], v.getTypeDesc() );
          }
        }
        if ( lengthColumn > 0 ) {
          if ( v.getLength() >= 0 ) {
            tRow.addCellText( lengthColumn, Integer.toString( v.getLength() ) );
          }
        }
        if ( precisionColumn > 0 ) {
          if ( v.getPrecision() >= 0 ) {
            tRow.addCellText( precisionColumn, Integer.toString( v.getPrecision() ) );
          }
        }

        if ( listener != null ) {
          if ( !listener.tableItemInserted( table.getItem( tRow.getParent().getParent().getChildNodes().indexOf(
            tRow.getParent() ) ), v ) ) {
            tRow.getParent().getParent().removeChild( tRow.getParent() );
          }
        }
      }
    }
    // tableView.removeEmptyRows();
    // tableView.setRowNums();
    // tableView.optWidth(true);
  }

  /**
   * Gets fields from previous steps and populate a ComboVar.
   *
   * @param comboVar
   *          the comboVar to populate
   * @param TransMeta
   *          the source transformation
   * @param StepMeta
   *          the source step
   */
  public static final void getFieldsFromPrevious( ComboVar comboVar, TransMeta transMeta, StepMeta stepMeta ) {
    String selectedField = null;
    int indexField = -1;
    try {
      RowMetaInterface r = transMeta.getPrevStepFields( stepMeta );
      selectedField = comboVar.getText();
      comboVar.removeAll();

      if ( r != null && !r.isEmpty() ) {
        r.getFieldNames();
        comboVar.setItems( r.getFieldNames() );
        indexField = r.indexOfValue( selectedField );
      }
      // Select value if possible...
      if ( indexField > -1 ) {
        comboVar.select( indexField );
      } else {
        if ( selectedField != null ) {
          comboVar.setText( selectedField );
        }
      }

    } catch ( KettleException ke ) {
      new ErrorDialog( comboVar.getShell(),
        BaseMessages.getString( PKG, "BaseStepDialog.FailedToGetFieldsPrevious.DialogTitle" ),
        BaseMessages.getString( PKG, "BaseStepDialog.FailedToGetFieldsPrevious.DialogMessage" ), ke );
    }
  }

  /**
   * Create a new field mapping between source and target steps.
   *
   * @param shell
   *          the shell of the parent window
   * @param sourceFields
   *          the source fields
   * @param targetFields
   *          the target fields
   * @param fieldMapping
   *          the list of source to target mappings to default to (can be empty but not null)
   * @throws KettleException
   *           in case something goes wrong during the field mapping
   */
  public static final void generateFieldMapping( Shell shell, RowMetaInterface sourceFields,
    RowMetaInterface targetFields, java.util.List<SourceToTargetMapping> fieldMapping ) throws KettleException {
    // Build the mapping: let the user decide!!
    String[] source = sourceFields.getFieldNames();
    for ( int i = 0; i < source.length; i++ ) {
      ValueMetaInterface v = sourceFields.getValueMeta( i );
      source[i] += EnterMappingDialog.STRING_ORIGIN_SEPARATOR + v.getOrigin() + ")";
    }
    String[] target = targetFields.getFieldNames();

    EnterMappingDialog dialog = new EnterMappingDialog( shell, source, target, fieldMapping );
    java.util.List<SourceToTargetMapping> newMapping = dialog.open();
    if ( newMapping != null ) {
      fieldMapping.clear();
      fieldMapping.addAll( newMapping );
    }
  }

  public static void getFieldsFromPrevious( RowMetaInterface row, XulTree tableView, List<Object> fields,
    StepTableDataObject field, TableItemInsertXulListener listener ) {
    if ( row == null || row.size() == 0 ) {
      return; // nothing to do
    }

    // get a list of all the non-empty keys (names)
    //
    List<String> keys = new ArrayList<String>();
    for ( Object entry : fields ) {
      keys.add( ( (StepTableDataObject) entry ).getName() );
    }

    int choice = 0;

    if ( keys.size() > 0 ) {
      // Ask what we should do with the existing data in the step.
      //
      Shell shell = ( (TableViewer) tableView.getManagedObject() ).getTable().getShell();
      MessageDialog md =
        new MessageDialog( shell,
          BaseMessages.getString( PKG, "BaseStepDialog.GetFieldsChoice.Title" ), // "Warning!"
          null,
          BaseMessages.getString( PKG, "BaseStepDialog.GetFieldsChoice.Message", "" + keys.size(), "" + row.size() ),
          MessageDialog.WARNING, new String[] {
            BaseMessages.getString( PKG, "BaseStepDialog.AddNew" ),
            BaseMessages.getString( PKG, "BaseStepDialog.Add" ),
            BaseMessages.getString( PKG, "BaseStepDialog.ClearAndAdd" ),
            BaseMessages.getString( PKG, "BaseStepDialog.Cancel" ), }, 0 );
      MessageDialog.setDefaultImage( GUIResource.getInstance().getImageSpoon() );
      int idx = md.open();
      choice = idx & 0xFF;
    }

    if ( choice == 3 || choice == 255 ) {
      return; // Cancel clicked
    }

    if ( choice == 2 ) {
      fields.clear();
    }

    for ( int i = 0; i < row.size(); i++ ) {
      ValueMetaInterface v = row.getValueMeta( i );

      if ( choice == 0 ) { // hang on, see if it's not yet in the table view

        if ( keys.indexOf( v.getName() ) >= 0 ) {
          continue;
        }
      }

      if ( listener != null && !listener.tableItemInsertedFor( v ) ) {
        continue;
      }

      StepTableDataObject newField = field.createNew( v );
      fields.add( newField );
    }
  }

  public void onHelp() {
    HelpUtils.openHelpDialog( dialogShell, getPlugin() );
  }

}
