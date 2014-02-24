package org.pentaho.di.trans.steps.update;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.step.utils.RowMetaUtils;

public class UpdateSQLTest extends TestCase {

  @Test
  public void testRowsTransform() {

    String[] keyLookup = new String[] { "Name" };
    String[] keyStream = new String[] { "FirstName" };
    String[] updateLookup = new String[] { "SecondName", "PostAddress", "ZIP" };
    String[] updateStream = new String[] { "SurName", "Address", "ZIP" };

    RowMetaInterface prev = new RowMeta();
    prev.addValueMeta( new ValueMetaString( keyStream[0] ) );
    prev.addValueMeta( new ValueMetaString( updateStream[0] ) );
    prev.addValueMeta( new ValueMetaString( updateStream[1] ) );
    prev.addValueMeta( new ValueMetaString( updateStream[2] ) );

    try {
      RowMetaInterface result =
          RowMetaUtils.getRowMetaForUpdate( prev, keyLookup, keyStream, updateLookup, updateStream );

      ValueMetaInterface vmi = result.getValueMeta( 0 );
      assertEquals( vmi.getName(), keyLookup[0] );
      assertEquals( prev.getValueMeta( 0 ).getName(), keyStream[0] );

      assertEquals( result.getValueMeta( 1 ).getName(), updateLookup[0] );
      assertEquals( prev.getValueMeta( 1 ).getName(), updateStream[0] );

      assertEquals( result.getValueMeta( 2 ).getName(), updateLookup[1] );
      assertEquals( prev.getValueMeta( 2 ).getName(), updateStream[1] );

      assertEquals( result.getValueMeta( 3 ).getName(), updateStream[2] );
      assertEquals( prev.getValueMeta( 3 ).getName(), updateStream[2] );
    } catch ( Exception ex ) {
      ex.printStackTrace();
      fail();
    }
  }

}
