/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.selectvalues;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta.SelectField;

public class SelectValuesMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String FIRST_FIELD = "FIRST_FIELD";

  private static final String SECOND_FIELD = "SECOND_FIELD";

  private SelectValuesMeta selectValuesMeta;

  @Before
  public void before() {
    selectValuesMeta = new SelectValuesMeta();
  }

  @Test
  public void loadSaveTest() throws KettleException {
    List<String> attributes = Arrays.asList( "selectFields", "deleteName" );

    SelectField selectField = new SelectField();
    selectField.setName( "TEST_NAME" );
    selectField.setRename( "TEST_RENAME" );
    selectField.setLength( 2 );
    selectField.setPrecision( 2 );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidatorTypeMap.put( SelectField[].class.getCanonicalName(), new ArrayLoadSaveValidator<SelectField>(
        new SelectFieldLoadSaveValidator( selectField ), 2 ) );

    LoadSaveTester<SelectValuesMeta> tester =
        new LoadSaveTester<>( SelectValuesMeta.class, attributes, new HashMap<String, String>(),
            new HashMap<String, String>(), new HashMap<String, FieldLoadSaveValidator<?>>(),
            fieldLoadSaveValidatorTypeMap );

    tester.testSerialization();
  }

  @Test
  public void setSelectName() {
    selectValuesMeta.setSelectName( new String[] { FIRST_FIELD, SECOND_FIELD } );
    assertArrayEquals( new String[] { FIRST_FIELD, SECOND_FIELD }, selectValuesMeta.getSelectName() );
  }

  @Test
  public void setSelectName_getOtherFields() {
    selectValuesMeta.setSelectName( new String[] { FIRST_FIELD, SECOND_FIELD } );
    assertArrayEquals( new String[] { null, null }, selectValuesMeta.getSelectRename() );
    assertArrayEquals( new int[] { SelectValuesMeta.UNDEFINED, SelectValuesMeta.UNDEFINED }, selectValuesMeta
        .getSelectLength() );
    assertArrayEquals( new int[] { SelectValuesMeta.UNDEFINED, SelectValuesMeta.UNDEFINED }, selectValuesMeta
        .getSelectPrecision() );
  }

  @Test
  public void setSelectName_smallerThanPrevious() {
    selectValuesMeta.setSelectName( new String[] { FIRST_FIELD, SECOND_FIELD } );
    selectValuesMeta.setSelectName( new String[] { FIRST_FIELD } );
    assertArrayEquals( new String[] { FIRST_FIELD }, selectValuesMeta.getSelectName() );
  }

  @Test
  public void getSelectName() {
    assertArrayEquals( new String[0], selectValuesMeta.getSelectName() );
  }

  @Test
  public void setSelectRename() {
    selectValuesMeta.setSelectRename( new String[] { FIRST_FIELD, SECOND_FIELD } );
    assertArrayEquals( new String[] { FIRST_FIELD, SECOND_FIELD }, selectValuesMeta.getSelectRename() );
  }

  @Test
  public void setSelectRename_getOtherFields() {
    selectValuesMeta.setSelectRename( new String[] { FIRST_FIELD, SECOND_FIELD } );
    assertArrayEquals( new String[] { null, null }, selectValuesMeta.getSelectName() );
    assertArrayEquals( new int[] { SelectValuesMeta.UNDEFINED, SelectValuesMeta.UNDEFINED }, selectValuesMeta
        .getSelectLength() );
    assertArrayEquals( new int[] { SelectValuesMeta.UNDEFINED, SelectValuesMeta.UNDEFINED }, selectValuesMeta
        .getSelectPrecision() );
  }

  @Test
  public void setSelectRename_smallerThanPrevious() {
    selectValuesMeta.setSelectRename( new String[] { FIRST_FIELD, SECOND_FIELD } );
    selectValuesMeta.setSelectRename( new String[] { FIRST_FIELD } );
    assertArrayEquals( new String[] { FIRST_FIELD, null }, selectValuesMeta.getSelectRename() );
  }

  @Test
  public void getSelectRename() {
    assertArrayEquals( new String[0], selectValuesMeta.getSelectRename() );
  }

  @Test
  public void setSelectLength() {
    selectValuesMeta.setSelectLength( new int[] { 1, 2 } );
    assertArrayEquals( new int[] { 1, 2 }, selectValuesMeta.getSelectLength() );
  }

  @Test
  public void setSelectLength_getOtherFields() {
    selectValuesMeta.setSelectLength( new int[] { 1, 2 } );
    assertArrayEquals( new String[] { null, null }, selectValuesMeta.getSelectName() );
    assertArrayEquals( new String[] { null, null }, selectValuesMeta.getSelectRename() );
    assertArrayEquals( new int[] { SelectValuesMeta.UNDEFINED, SelectValuesMeta.UNDEFINED }, selectValuesMeta
        .getSelectPrecision() );
  }

  @Test
  public void setSelectLength_smallerThanPrevious() {
    selectValuesMeta.setSelectLength( new int[] { 1, 2 } );
    selectValuesMeta.setSelectLength( new int[] { 1 } );
    assertArrayEquals( new int[] { 1, SelectValuesMeta.UNDEFINED }, selectValuesMeta.getSelectLength() );
  }

  @Test
  public void getSelectLength() {
    assertArrayEquals( new int[0], selectValuesMeta.getSelectLength() );
  }

  @Test
  public void setSelectPrecision() {
    selectValuesMeta.setSelectPrecision( new int[] { 1, 2 } );
    assertArrayEquals( new int[] { 1, 2 }, selectValuesMeta.getSelectPrecision() );
  }

  @Test
  public void setSelectPrecision_getOtherFields() {
    selectValuesMeta.setSelectPrecision( new int[] { 1, 2 } );
    assertArrayEquals( new String[] { null, null }, selectValuesMeta.getSelectName() );
    assertArrayEquals( new String[] { null, null }, selectValuesMeta.getSelectRename() );
    assertArrayEquals( new int[] { SelectValuesMeta.UNDEFINED, SelectValuesMeta.UNDEFINED }, selectValuesMeta
        .getSelectLength() );
  }

  @Test
  public void setSelectPrecision_smallerThanPrevious() {
    selectValuesMeta.setSelectPrecision( new int[] { 1, 2 } );
    selectValuesMeta.setSelectPrecision( new int[] { 1 } );
    assertArrayEquals( new int[] { 1, SelectValuesMeta.UNDEFINED }, selectValuesMeta.getSelectPrecision() );
  }

  @Test
  public void setSelectFieldsNull() {
    selectValuesMeta.setSelectFields( null );
    assertNotNull( selectValuesMeta.getSelectFields() );
    assertEquals( 0, selectValuesMeta.getSelectFields().length );
  }

  @Test
  public void setDeleteNameNull() {
    selectValuesMeta.setDeleteName( null );
    assertNotNull( selectValuesMeta.getDeleteName() );
    assertEquals( 0, selectValuesMeta.getDeleteName().length );
  }

  @Test
  public void setMetaNull() {
    selectValuesMeta.setMeta( null );
    assertNotNull( selectValuesMeta.getMeta() );
    assertEquals( 0, selectValuesMeta.getMeta().length );
  }

  @Test
  public void getSelectPrecision() {
    assertArrayEquals( new int[0], selectValuesMeta.getSelectPrecision() );
  }

  @Test
  public void testMetaDataFieldsRenameConflict() throws Exception {
    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "A" ) );
    rowMeta.addValueMeta( new ValueMetaString( "B" ) );

    SelectMetadataChange change = new SelectMetadataChange( selectValuesMeta );
    change.setName( "A" );
    change.setRename( "B" );
    selectValuesMeta.setMeta( new SelectMetadataChange[] { change } );

    selectValuesMeta.getMetadataFields( rowMeta, "select values", null );
    assertEquals( "rename conflict", "B_1", rowMeta.getValueMeta( 0 ).getName() );
  }

  public static class SelectFieldLoadSaveValidator implements FieldLoadSaveValidator<SelectField> {

    private final SelectField defaultValue;

    public SelectFieldLoadSaveValidator( SelectField defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public SelectField getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( SelectField testObject, Object actual ) {
      return EqualsBuilder.reflectionEquals( testObject, actual );
    }
  }
}
