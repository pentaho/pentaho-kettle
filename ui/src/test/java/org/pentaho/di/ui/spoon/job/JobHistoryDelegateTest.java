/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.ui.spoon.job;

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

public class JobHistoryDelegateTest {

  @Test
  public void getColumnMappings() {
    TableView view = mock( TableView.class );
    doReturn( getColumnInfo() ).when( view ).getColumns();

    JobHistoryDelegate.JobHistoryLogTab model = mock( JobHistoryDelegate.JobHistoryLogTab.class );
    setInternalState( model, "logDisplayTableView", view );
    setInternalState( model, "logTableFields", getLogTableFields() );

    JobHistoryDelegate history = new JobHistoryDelegate( mock( Spoon.class ), mock( JobGraph.class ) );
    Map<String, Integer> map = history.getColumnMappings( model );

    assertEquals( 0, (int) map.get( "COLUMN_1" ) );
    assertEquals( 1, (int) map.get( "COLUMN_2" ) );
    assertEquals( 2, (int) map.get( "COLUMN_3" ) );
    assertEquals( 4, (int) map.get( "COLUMN_5" ) );
    assertEquals( 5, (int) map.get( "COLUMN_6" ) );
  }

  @Test
  public void getValueMetaForStringColumn() {
    JobHistoryDelegate history = new JobHistoryDelegate( mock( Spoon.class ), mock( JobGraph.class ) );
    ValueMetaInterface
      valueMeta = history.getValueMetaForColumn( getColumnInfo(), new LogTableField( "COLUMN 2", "COLUMN_2", null ) );

    assertEquals( "COLUMN_2", valueMeta.getName() );
    assertThat( valueMeta, instanceOf( ValueMetaString.class ) );
  }

  @Test
  public void getValueMetaForIntegerColumn() {
    JobHistoryDelegate history = new JobHistoryDelegate( mock( Spoon.class ), mock( JobGraph.class ) );
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
