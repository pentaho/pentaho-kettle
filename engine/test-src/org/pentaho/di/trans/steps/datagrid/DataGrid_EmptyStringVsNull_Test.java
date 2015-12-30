package org.pentaho.di.trans.steps.datagrid;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.reflect.FieldUtils.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class DataGrid_EmptyStringVsNull_Test {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }


  @Test
  public void emptyAndNullsAreNotDifferent() throws Exception {
    List<Object[]> expected = asList(
      new Object[] { "", "", null },
      new Object[] { null, "", null },
      new Object[] { null, "", null }
    );
    doTestEmptyStringVsNull( false, expected );
  }


  @Test
  public void emptyAndNullsAreDifferent() throws Exception {
    List<Object[]> expected = asList(
      new Object[] { "", "", null },
      new Object[] { "", "", null },
      new Object[] { null, "", null }
    );
    doTestEmptyStringVsNull( true, expected );
  }


  private void doTestEmptyStringVsNull( boolean diffProperty, List<Object[]> expected ) throws Exception {
    final String fieldName = "EMPTY_STRING_AND_NULL_ARE_DIFFERENT";

    final Field metaBaseField = getField( ValueMetaBase.class, fieldName );
    final Object metaBaseValue = readStaticField( metaBaseField );

    final Field metaField = getField( ValueMeta.class, fieldName );
    final Object metaValue = readStaticField( metaField );

    final Field modifiers = getField( Field.class, "modifiers", true );
    writeField( modifiers, metaBaseField, metaBaseField.getModifiers() & ~Modifier.FINAL, true );
    writeField( modifiers, metaField, metaField.getModifiers() & ~Modifier.FINAL, true );

    Field.setAccessible( new AccessibleObject[] { metaBaseField, metaField }, true );
    writeStaticField( metaBaseField, diffProperty );
    writeStaticField( metaField, diffProperty );

    try {
      executeAndAssertResults( expected );
    } finally {
      writeStaticField( metaBaseField, metaBaseValue );
      writeStaticField( metaField, metaValue );

      writeField( modifiers, metaBaseField, metaBaseField.getModifiers() & Modifier.FINAL, true );
      writeField( modifiers, metaField, metaField.getModifiers() & Modifier.FINAL, true );
    }
  }

  private void executeAndAssertResults( List<Object[]> expected ) throws Exception {
    final String stringType = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_STRING );
    final String numberType = ValueMetaFactory.getValueMetaName( ValueMetaInterface.TYPE_NUMBER );

    DataGridMeta meta = new DataGridMeta();
    meta.allocate( 3 );
    meta.setFieldName( new String[] { "string", "string-setEmpty", "number" } );
    meta.setFieldType( new String[] { stringType, stringType, numberType } );
    meta.setEmptyString( new boolean[] { false, true, false } );

    List<List<String>> dataRows = asList(
      asList( " ", " ", " " ),
      asList( "", "", "" ),
      asList( (String) null, null, null )
    );
    meta.setDataLines( dataRows );

    DataGridData data = new DataGridData();
    DataGrid step = createAndInitStep( meta, data );

    List<Object[]> actual = execute( step, meta, data, 3 );
    assertResult( expected, actual );
  }

  private DataGrid createAndInitStep( DataGridMeta meta, DataGridData data ) {
    StepMockHelper<DataGridMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( DataGridMeta.class, "DataGrid_EmptyStringVsNull_Test" );
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    DataGrid step = new DataGrid( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }

  private List<Object[]> execute( DataGrid step,
                                  DataGridMeta meta,
                                  DataGridData data,
                                  int expectedRowsAmount ) throws Exception {
    RowSet output = new BlockingRowSet( expectedRowsAmount );
    step.setOutputRowSets( singletonList( output ) );

    int i = 0;
    List<Object[]> result = new ArrayList<>( expectedRowsAmount );
    while ( step.processRow( meta, data ) && i < expectedRowsAmount ) {
      Object[] row = output.getRowImmediate();
      assertNotNull( Integer.toString( i ), row );
      result.add( row );

      i++;
    }
    assertEquals( "The amount of executions should be equal to expected", expectedRowsAmount, i );
    assertTrue( output.isDone() );

    return result;
  }

  private static void assertResult( List<Object[]> expected, List<Object[]> actual ) {
    assertEquals( expected.size(), actual.size() );
    for ( int i = 0; i < expected.size(); i++ ) {
      Object[] expectedRow = expected.get( i );
      Object[] actualRow = actual.get( i );
      assertRow( i, expectedRow, actualRow );
    }
  }

  private static void assertRow( int index, Object[] expected, Object[] actual ) {
    assertNotNull( actual );

    boolean sizeCondition = expected.length <= actual.length;
    if ( !sizeCondition ) {
      fail(
        String.format( "Row [%d]: expected.length=[%d]; actual.length=[%d]", index, expected.length, actual.length ) );
    }

    int i = 0;
    while ( i < expected.length ) {
      assertEquals( expected[ i ], actual[ i ] );
      i++;
    }
    while ( i < actual.length ) {
      assertNull( actual[ i ] );
      i++;
    }
  }
}
