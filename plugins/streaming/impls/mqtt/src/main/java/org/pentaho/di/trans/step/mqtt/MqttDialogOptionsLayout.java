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
package org.pentaho.di.trans.step.mqtt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepOption;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.pentaho.di.ui.trans.step.BaseStreamingDialog.INPUT_WIDTH;

class MqttDialogOptionsLayout {

  private static final Class<?> PKG = MqttDialogOptionsLayout.class;

  private final PropsUI props;
  private final CTabFolder wTabFolder;
  private final ModifyListener lsMod;
  private final TransMeta transMeta;
  private final List<StepOption> options;

  private TableView optionsTable;

  MqttDialogOptionsLayout(
    PropsUI props, CTabFolder wTabFolder, ModifyListener lsMod, TransMeta transMeta,
    List<StepOption> options ) {
    checkNotNull( props );
    checkNotNull( wTabFolder );
    checkNotNull( lsMod );
    checkNotNull( transMeta );

    this.props = props;
    this.wTabFolder = wTabFolder;
    this.lsMod = lsMod;
    this.transMeta = transMeta;
    options = Optional.ofNullable( options ).orElse( emptyList() );
    this.options = options;
  }

  void buildTab() {
    CTabItem wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "MQTTDialog.Options.Tab" ) );

    Composite wOptionsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOptionsComp );
    FormLayout optionsLayout = new FormLayout();
    optionsLayout.marginHeight = 15;
    optionsLayout.marginWidth = 15;
    wOptionsComp.setLayout( optionsLayout );

    FormData fdOptionsComp = new FormData();
    fdOptionsComp.left = new FormAttachment( 0, 0 );
    fdOptionsComp.top = new FormAttachment( 0, 0 );
    fdOptionsComp.right = new FormAttachment( 100, 0 );
    wOptionsComp.setLayoutData( fdOptionsComp );

    ColumnInfo[] columns = getColumns();

    int fieldCount = 1;

    optionsTable = new TableView(
      transMeta,
      wOptionsComp,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      fieldCount,
      false,
      lsMod,
      props,
      false
    );

    optionsTable.setSortable( false );
    optionsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 215 );
      table.getColumn( 2 ).setWidth( 215 );
    } );

    populateTable();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( 0, 0 );
    fdData.right = new FormAttachment( 100, -10 );
    fdData.width = INPUT_WIDTH + 10;

    // resize the columns to fit the data in them
    stream( optionsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    // don't let any rows get deleted or added (this does not affect the read-only state of the cells)
    optionsTable.setReadonly( true );
    optionsTable.setLayoutData( fdData );

    wOptionsComp.layout();
    wOptionsTab.setControl( wOptionsComp );
  }

  private ColumnInfo[] getColumns() {
    ColumnInfo optionName = new ColumnInfo( BaseMessages.getString( PKG, "MQTTDialog.Options.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );
    ColumnInfo optionValue = new ColumnInfo( BaseMessages.getString( PKG, "MQTTDialog.Options.Column.Value" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );
    optionValue.setUsingVariables( true );

    return new ColumnInfo[] { optionName, optionValue };
  }

  private void populateTable() {
    optionsTable.clearAll();
    options
      .forEach( option -> optionsTable.add( option.getText(), option.getValue() ) );
    optionsTable.remove( 0 );
  }

  List<StepOption> retrieveOptions() {
    IntStream.range( 0, optionsTable.getItemCount() )
      .mapToObj( i -> optionsTable.getItem( i ) )
      .forEach( item ->
        options.forEach( option -> {
          if ( option.getText().equals( item[ 0 ] ) ) {
            option.setValue( item[ 1 ] );
          }
        } )
      );
    return options;
  }
}
