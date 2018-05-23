/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.trans.step.common;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.ui.core.dialog.BaseDialog;
import org.pentaho.di.ui.core.dialog.BaseMessageDialog;
import org.pentaho.di.ui.core.widget.TableView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An interface providing functionality for any step dialog that has the "get fields" capability.
 */
public interface GetFieldsCapableStepDialog<StepMetaType extends BaseStepMeta> {
  Class<?> PKG = GetFieldsCapableStepDialog.class; // for i18n purposes, needed by Translator2!!

  LogChannel logger = new LogChannel( GetFieldsCapableStepDialog.class );

  /**
   * Returns the {@link Shell} of the parent control.
   *
   * @return the {@link Shell} of the parent control
   */
  Shell getParent();

  /**
   * Returns the {@link Shell} of this dialog.
   *
   * @return the {@link Shell} of this dialog
   */
  Shell getShell();

  /**
   * Returns an array of incoming field names, or an empty array, if fields cannot be fetched, for some reason.
   *
   * @param meta the {@link StepMetaType}
   * @return an array of incoming field names, or an empty array, if fields cannot be fetched, for some reason.
   */
  String[] getFieldNames( final StepMetaType meta );

  /**
   * @return the {@link TableView} containing step fields
   */
  TableView getFieldsTable();

  default TableItem findTableItem( final String fieldName ) {
    for ( int i = 0; i < getFieldsTable().table.getItemCount(); i++ ) {
      final TableItem item = getFieldsTable().table.getItem( i );
      int fieldNameIndex = getFieldsTable().hasIndexColumn() ? 1 : 0;
      final String itemFieldName = item.getText( fieldNameIndex );
      if ( itemFieldName != null && itemFieldName.equalsIgnoreCase( fieldName ) ) {
        return item;
      }
    }
    return null;
  }

  default List<String> getNewFieldNames( final String[] incomingFieldNames ) {
    // get names of all the fields within the fields table, in lower case for a case-insensitive comparison
    final Set<String> fieldNamesInTableLowerCase = new HashSet();
    for ( int i = 0; i < getFieldsTable().table.getItemCount(); i++ ) {
      final TableItem item = getFieldsTable().table.getItem( i );
      int fieldNameIndex = getFieldsTable().hasIndexColumn() ? 1 : 0;
      fieldNamesInTableLowerCase.add( item.getText( fieldNameIndex ).toLowerCase() );
    }
    final List<String> newFieldNames = Arrays.asList( incomingFieldNames ).stream().filter(
      fieldName -> !fieldNamesInTableLowerCase.contains( fieldName.toLowerCase() ) ).collect( Collectors.toList() );
    return newFieldNames;
  }

  default void getFields( final StepMetaType meta ) {

    final String[] incomingFieldNames = getFieldNames( meta );
    final List<String> newFieldNames = getNewFieldNames( incomingFieldNames );

    if ( newFieldNames != null && newFieldNames.size() > 0 ) {
      // we have new incoming fields
      final int nrNonEmptyFields = getFieldsTable().nrNonEmpty();
      // are any fields already populated in the fields table?
      if ( nrNonEmptyFields > 0 ) {
        final FieldSelectionDialog fieldSelectDialog = new FieldSelectionDialog( this, newFieldNames.size() );
        fieldSelectDialog.open();
      } else {
        // no fields are populated yet, go straight to "sample data" dialog
        openGetFieldsSampleDataDialog();
      }
    } else {
      // we have no new fields
      final BaseDialog errorDlg = new BaseMessageDialog( getShell(),
        BaseMessages.getString( PKG, "System.GetFields.NoNewFields.Title" ),
        BaseMessages.getString( PKG, "System.GetFields.NoNewFields.Message" ) );
      // if there are no incoming fields at all, we leave the OK button handler as-is and simply dispose the dialog;
      // if there are some incoming fields, we overwrite the OK button handler to show the GetFieldsSampleDataDialog
      if ( incomingFieldNames != null && incomingFieldNames.length > 0 ) {
        final Map<String, Listener> buttons = new HashMap();
        buttons.put( BaseMessages.getString( PKG, "System.Button.OK" ), event -> {
          errorDlg.dispose();
          openGetFieldsSampleDataDialog();
        } );
        errorDlg.setButtons( buttons );
      }
      errorDlg.open();
    }
  }

  default void openGetFieldsSampleDataDialog() {
    final GetFieldsSampleDataDialog dlg = new GetFieldsSampleDataDialog( getShell(), this, true );
    dlg.open();
  }

  String loadFieldsImpl( final StepMetaType meta, final int samples, final boolean reloadAllFields );

  default Map<String, List<String>> getFieldValues() {
    getFieldsTable().nrNonEmpty();
    final Map<String, List<String>> rowValues = new HashMap<>();
    for ( int i = 0; i < getFieldsTable().table.getItemCount(); i++ ) {
      final TableItem item = getFieldsTable().table.getItem( i );
      int startIndex = getFieldsTable().hasIndexColumn() ? 1 : 0;
      final String fieldName = item.getText( startIndex );
      if ( StringUtils.isBlank( fieldName ) ) {
        continue;
      }
      final List<String> values = new ArrayList();
      for ( int j = startIndex; j < getFieldsTable().getColumns().length; j++ ) {
        values.add( item.getText( j ) );
      }
      rowValues.put( fieldName, values );
    }
    return rowValues;
  }

  default List<String> repopulateFields( final StepMetaType meta, final Map<String, List<String>> previousFieldValues,
                                         final boolean reloadAllFields ) {
    final List<String> userDefinedFields = new ArrayList();
    final String[] fieldNames = getFieldNames( meta );
    for ( final String fieldName : fieldNames ) {
      final TableItem item = new TableItem( getFieldsTable().table, SWT.NONE );
      // remove the corresponding item, that way, all that remains is fields that are not incoming, but rather may
      // have been entered manually by the user
      final List<String> values = previousFieldValues.remove( fieldName );
      int columnIndexOffset = getFieldsTable().hasIndexColumn() ? 1 : 0;
      int columnIndex = 0;
      if ( !reloadAllFields && values != null ) {
        for ( final String value : values ) {
          item.setText( columnIndex++ + columnIndexOffset, value );
        }
      } else {
        userDefinedFields.add( fieldName );
        item.setText( columnIndexOffset, fieldName );
      }
    }
    return userDefinedFields;
  }

  default void loadRemainingFields( final Map<String, List<String>> previousFieldValues ) {
    final Iterator<List<String>> remainigValues = previousFieldValues.values().iterator();
    while ( remainigValues.hasNext() ) {
      final List<String> values = remainigValues.next();
      if ( values != null ) {
        final TableItem item = new TableItem( getFieldsTable().table, SWT.NONE );
        int columnIndexOffset = getFieldsTable().hasIndexColumn() ? 1 : 0;
        int columnIndex = 0;
        for ( final String value : values ) {
          item.setText( columnIndex++ + columnIndexOffset, value );
        }
      }
    }
  }

  default String loadFields( final StepMetaType meta, final int samples, final boolean reloadAllFields ) {
    // fields loading might rely on specific order, and since we allow users to enter fields manually, order is not
    // guaranteed; we therefore need to ensure that fields are properly ordered within the fields table

    // cache the fields currently present in the fields table
    final Map<String, List<String>> fieldValues = getFieldValues();

    // clear the table
    getFieldsTable().removeAll();

    // ...and reorder the table items, keeping track of user defined fields
    final List<String> userDefinedFields = repopulateFields( meta, fieldValues, reloadAllFields );

    // are there any other fields left that the user may have entered manually? If we are not clearing and reloading,
    // we should preserve those
    if ( !reloadAllFields ) {
      loadRemainingFields( fieldValues );
    }

    populateMeta( meta );
    final String message = loadFieldsImpl( meta, samples, reloadAllFields );
    if ( message != null ) {
      if ( reloadAllFields ) {
        getFieldsTable().removeAll();
      }
      // OK, what's the result of our search?
      getData( meta, false, reloadAllFields, userDefinedFields );
      getFieldsTable().removeEmptyRows();
      getFieldsTable().setRowNums();
      getFieldsTable().optWidth( true );
    }
    return message;
  }

  void getData( final StepMetaType inputMeta, final boolean copyStepname, final boolean reloadAllFields,
                final List<String> newFieldNames );

  default StepMetaType getPopulatedMeta() {
    final StepMetaType newMeta = getNewMetaInstance();
    populateMeta( newMeta );
    return newMeta;
  }

  void populateMeta( final StepMetaType meta );

  StepMetaType getNewMetaInstance();
}
