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

package org.pentaho.di.ui.trans.step.common;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.ui.core.widget.TableView;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class GetFieldsCapableStepDialogTest {

  private GetFieldsCapableStepDialog getFieldsCapableStepDialog;
  private TableView tableView;
  private Table table;
  private TableItem tableItem;

  @Before
  public void before() {
    getFieldsCapableStepDialog = spy( GetFieldsCapableStepDialogTestImplementation.class );
    tableView = mock( TableView.class );
    table = mock( Table.class );

    when( getFieldsCapableStepDialog.getFieldsTable() ).thenReturn( tableView );
    when( tableView.getTable() ).thenReturn( table );
  }

  @Test
  public void testGetNewFieldNamesEmpty() {
    String[] incomingFields = new String[] {};
    when( table.getItemCount() ).thenReturn( 0 );
    List<String> newFieldNames = getFieldsCapableStepDialog.getNewFieldNames( incomingFields );
    assertTrue( newFieldNames.isEmpty() );
  }

  @Test
  public void testGetNewFieldNamesWithValues() {
    String[] incomingFields = new String[] { "a", "b" };
    tableItem = mock( TableItem.class );

    when( tableView.hasIndexColumn() ).thenReturn( false );
    when( tableView.getTable().getItemCount() ).thenReturn( 1 );
    when( tableView.getTable().getItem( 0 ) ).thenReturn( tableItem );
    when( tableItem.getText( 0 ) ).thenReturn( "a" );

    List<String> newFieldNames = getFieldsCapableStepDialog.getNewFieldNames( incomingFields );

    assertEquals( 1, newFieldNames.size() );
    assertEquals( "b", newFieldNames.get( 0 ) );
  }

  /*
   * Workaround to uses Mock.spy with interface that have default methods.
   * This feature are only available on version 2+ of Mockito. ( Stay in beta since 2015)
   */
  static class GetFieldsCapableStepDialogTestImplementation implements GetFieldsCapableStepDialog {

    @Override
    public Shell getParent() {
      return null;
    }

    @Override
    public Shell getShell() {
      return null;
    }

    @Override
    public String[] getFieldNames( BaseStepMeta meta ) {
      return new String[ 0 ];
    }

    @Override
    public TableView getFieldsTable() {
      return null;
    }

    @Override
    public String loadFieldsImpl( BaseStepMeta meta, int samples ) {
      return null;
    }

    @Override
    public void populateMeta( BaseStepMeta meta ) {

    }

    @Override
    public BaseStepMeta getNewMetaInstance() {
      return null;
    }

    @Override
    public TransMeta getTransMeta() {
      return null;
    }

    @Override
    public void getData( BaseStepMeta inputMeta, boolean copyStepName, boolean reloadAllFields, Set newFieldNames ) {

    }
  }


}
