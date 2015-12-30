package org.pentaho.di.trans.steps.datagrid;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.TransTestingUtil;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.StepMockUtil;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.reflect.FieldUtils.*;
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

    List<Object[]> actual = TransTestingUtil.execute( step, meta, data, 3, true );
    TransTestingUtil.assertResult( expected, actual );
  }

  private DataGrid createAndInitStep( DataGridMeta meta, DataGridData data ) {
    StepMockHelper<DataGridMeta, StepDataInterface> helper =
      StepMockUtil.getStepMockHelper( DataGridMeta.class, "DataGrid_EmptyStringVsNull_Test" );
    when( helper.stepMeta.getStepMetaInterface() ).thenReturn( meta );

    DataGrid step = new DataGrid( helper.stepMeta, data, 0, helper.transMeta, helper.trans );
    step.init( meta, data );
    return step;
  }
}
