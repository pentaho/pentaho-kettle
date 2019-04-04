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

package org.pentaho.di.ui.spoon.trans;

import org.junit.Test;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.powermock.reflect.Whitebox.setInternalState;

public class TransHistoryDelegateTest {

  @Test
  public void getColumnMappings() {
    TableView view = mock( TableView.class );
    doReturn( getColumnInfo() ).when( view ).getColumns();

    TransHistoryDelegate.TransHistoryLogTab model = mock( TransHistoryDelegate.TransHistoryLogTab.class );
    setInternalState( model, "logDisplayTableView", view );
    setInternalState( model, "logTableFields", getLogTableFields() );

    TransHistoryDelegate history = new TransHistoryDelegate( mock( Spoon.class ), mock( TransGraph.class ) );
    Map<String, Integer> map = history.getColumnMappings( model );

    assertEquals( 0, (int) map.get( "COLUMN_1" ) );
    assertEquals( 1, (int) map.get( "COLUMN_2" ) );
    assertEquals( 2, (int) map.get( "COLUMN_3" ) );
    assertEquals( 4, (int) map.get( "COLUMN_5" ) );
    assertEquals( 5, (int) map.get( "COLUMN_6" ) );
  }

  @Test
  public void getValueMetaForStringColumn() {
    TransHistoryDelegate history = new TransHistoryDelegate( mock( Spoon.class ), mock( TransGraph.class ) );
    ValueMetaInterface valueMeta = history.getValueMetaForColumn( getColumnInfo(), new LogTableField( "COLUMN 2", "COLUMN_2", null ) );

    assertEquals( "COLUMN_2", valueMeta.getName() );
    assertThat( valueMeta, instanceOf( ValueMetaString.class ) );
  }

  @Test
  public void getValueMetaForIntegerColumn() {
    TransHistoryDelegate history = new TransHistoryDelegate( mock( Spoon.class ), mock( TransGraph.class ) );
    ValueMetaInterface valueMeta = history.getValueMetaForColumn( getColumnInfo(), new LogTableField( "COLUMN 5", "COLUMN_5", null ) );

    assertEquals( "COLUMN_5", valueMeta.getName() );
    assertThat( valueMeta, instanceOf( ValueMetaInteger.class ) );
  }

  private ColumnInfo[] getColumnInfo() {
    ColumnInfo[] colinf = new ColumnInfo[] {
      new ColumnInfo( "COLUMN_1", ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( "COLUMN_2", ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( "COLUMN_3", ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( "COLUMN_5", ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( "COLUMN_6", ColumnInfo.COLUMN_TYPE_TEXT )
    };

    colinf[1].setValueMeta( new ValueMetaString( "COLUMN_2" ) );
    colinf[3].setValueMeta( new ValueMetaInteger( "COLUMN_5" ) );

    return colinf;
  }

  private List<LogTableField> getLogTableFields() {
    return Arrays.asList(
      new LogTableField( "COLUMN 1", "COLUMN_1", null ),
      new LogTableField( "COLUMN 2", "COLUMN_2", null ),
      new LogTableField( "COLUMN 3", "COLUMN_3", null ),
      new LogTableField( "COLUMN 4", "COLUMN_4", null ),
      new LogTableField( "COLUMN 5", "COLUMN_5", null ),
      new LogTableField( "COLUMN 6", "COLUMN_6", null ),
      new LogTableField( "COLUMN 7", "COLUMN_7", null )
    );
  }
}
