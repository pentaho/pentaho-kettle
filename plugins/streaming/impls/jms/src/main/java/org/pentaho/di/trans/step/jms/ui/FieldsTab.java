/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.jms.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.jms.JmsConsumerMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Arrays.stream;
import static org.pentaho.di.trans.step.jms.JmsConstants.PKG;

public class FieldsTab {

  public static final String STRING_DESC = ValueMetaBase.getTypeDesc( ValueMetaInterface.TYPE_STRING );

  private CTabFolder wTabFolder;
  private PropsUI props;
  TableView fieldsTable;
  private VariableSpace transMeta;
  private ModifyListener lsMod;
  private String message;
  private String destination;
  private String messageId;
  private String jmsTimestamp;
  private String jmsRedelivered;

  public FieldsTab(
    CTabFolder wTabFolder, PropsUI props, VariableSpace transMeta, ModifyListener lsMod, JmsConsumerMeta jmsMeta ) {
    this.wTabFolder = wTabFolder;
    this.props = props;
    this.transMeta = transMeta;
    this.lsMod = lsMod;
    this.message = jmsMeta.messageField;
    this.destination = jmsMeta.destinationField;
    this.messageId = jmsMeta.messageId;
    this.jmsTimestamp = jmsMeta.jmsTimestamp;
    this.jmsRedelivered = jmsMeta.jmsRedelivered;
  }

  public void buildFieldsTab() {
    checkArgument( wTabFolder.getItemCount() > 0 );

    CTabItem wFieldsTab = new CTabItem( wTabFolder, SWT.NONE, wTabFolder.getItemCount() - 1 );
    wFieldsTab.setText( BaseMessages.getString( PKG, "JmsConsumerDialog.FieldsTab" ) );

    Composite wFieldsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wFieldsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wFieldsComp.setLayout( fieldsLayout );

    FormData fieldsFormData = new FormData();
    fieldsFormData.left = new FormAttachment( 0, 0 );
    fieldsFormData.top = new FormAttachment( wFieldsComp, 0 );
    fieldsFormData.right = new FormAttachment( 100, 0 );
    fieldsFormData.bottom = new FormAttachment( 100, 0 );
    wFieldsComp.setLayoutData( fieldsFormData );

    buildFieldTable( wFieldsComp, wFieldsComp );

    wFieldsComp.layout();
    wFieldsTab.setControl( wFieldsComp );
  }

  private void buildFieldTable( Composite parentWidget, Control relativePosition ) {
    ColumnInfo[] columns = getFieldColumns();

    fieldsTable = new TableView(
      transMeta,
      parentWidget,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      5,
      true,
      lsMod,
      props,
      false
    );

    fieldsTable.setSortable( false );
    fieldsTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 147 );
      table.getColumn( 2 ).setWidth( 147 );
      table.getColumn( 3 ).setWidth( 147 );
    } );

    populateFieldData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( relativePosition, 5 );
    fdData.right = new FormAttachment( 100, 0 );

    // resize the columns to fit the data in them
    stream( fieldsTable.getTable().getColumns() ).forEach( column -> {
      if ( column.getWidth() > 0 ) {
        // don't pack anything with a 0 width, it will resize it to make it visible (like the index column)
        column.setWidth( 120 );
      }
    } );

    // don't let any rows get deleted or added (this does not affect the read-only state of the cells)
    fieldsTable.setReadonly( true );
    fieldsTable.setLayoutData( fdData );
  }

  private void populateFieldData() {
    TableItem messageItem = fieldsTable.getTable().getItem( 0 );
    messageItem.setText( 1, BaseMessages.getString( PKG, "JmsConsumerDialog.InputName.Message" ) );
    messageItem.setText( 2, message );
    messageItem.setText( 3, STRING_DESC );

    TableItem topicItem = fieldsTable.getTable().getItem( 1 );
    topicItem.setText( 1, BaseMessages.getString( PKG, "JmsConsumerDialog.InputName.Destination" ) );
    topicItem.setText( 2, destination );
    topicItem.setText( 3, STRING_DESC );

    TableItem messageIdItem = fieldsTable.getTable().getItem( 2 );
    messageIdItem.setText( 1, BaseMessages.getString( PKG, "JmsConsumerDialog.InputName.MessageId" ) );
    messageIdItem.setText( 2, messageId );
    messageIdItem.setText( 3, STRING_DESC );

    TableItem jmsTimestampIdItem = fieldsTable.getTable().getItem( 3 );
    jmsTimestampIdItem.setText( 1, BaseMessages.getString( PKG, "JmsConsumerDialog.InputName.JMSTimestamp" ) );
    jmsTimestampIdItem.setText( 2, jmsTimestamp );
    jmsTimestampIdItem.setText( 3, STRING_DESC );

    TableItem jmsRedeliveredItem = fieldsTable.getTable().getItem( 4 );
    jmsRedeliveredItem.setText( 1, BaseMessages.getString( PKG, "JmsConsumerDialog.InputName.JMSRedelivered" ) );
    jmsRedeliveredItem.setText( 2, jmsRedelivered );
    jmsRedeliveredItem.setText( 3, STRING_DESC );
  }

  private ColumnInfo[] getFieldColumns() {
    ColumnInfo referenceName = new ColumnInfo( BaseMessages.getString( PKG, "JmsConsumerDialog.Column.Ref" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );

    ColumnInfo name = new ColumnInfo( BaseMessages.getString( PKG, "JmsConsumerDialog.Column.Name" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, false );

    ColumnInfo type = new ColumnInfo( BaseMessages.getString( PKG, "JmsConsumerDialog.Column.Type" ),
      ColumnInfo.COLUMN_TYPE_TEXT, false, true );

    return new ColumnInfo[] { referenceName, name, type };
  }
}
