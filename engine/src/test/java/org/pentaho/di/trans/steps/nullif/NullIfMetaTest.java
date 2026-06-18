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


package org.pentaho.di.trans.steps.nullif;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.nullif.NullIfMeta.Field;
import static org.junit.Assert.assertEquals;

public class NullIfMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {

    List<String> attributes = Arrays.asList( "fields" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "fields", "getFields" );
      }
    };
    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "fields", "setFields" );
      }
    };
    Field field = new Field();
    field.setFieldName( "fieldName" );
    field.setFieldValue( "fieldValue" );
    FieldLoadSaveValidator<Field[]> fieldArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<Field>( new NullIfFieldLoadSaveValidator( field ), 5 );
    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    typeValidatorMap.put( Field[].class.getCanonicalName(), fieldArrayLoadSaveValidator );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fields", fieldArrayLoadSaveValidator );

    loadSaveTester =
        new LoadSaveTester( NullIfMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

  @Test
  public void setFieldValueTest() {
    Field field = new Field();
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" );
    field.setFieldValue( "theValue" );
    assertEquals( "theValue", field.getFieldValue() );
  }

  @Test
  public void setFieldValueNullTest() {
    Field field = new Field();
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "N" );
    field.setFieldValue( null );
    assertEquals( null, field.getFieldValue() );
  }

  @Test
  public void setFieldValueNullWithEmptyStringsDiffersFromNullTest() {
    Field field = new Field();
    System.setProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL, "Y" );
    field.setFieldValue( null );
    assertEquals( "", field.getFieldValue() );
  }

  public static class NullIfFieldLoadSaveValidator implements FieldLoadSaveValidator<Field> {

    private final Field defaultValue;

    public NullIfFieldLoadSaveValidator( Field defaultValue ) {
      this.defaultValue = defaultValue;
    }

    @Override
    public Field getTestObject() {
      return defaultValue;
    }

    @Override
    public boolean validateTestObject( Field testObject, Object actual ) {
      return EqualsBuilder.reflectionEquals( testObject, actual );
    }
  }

}
