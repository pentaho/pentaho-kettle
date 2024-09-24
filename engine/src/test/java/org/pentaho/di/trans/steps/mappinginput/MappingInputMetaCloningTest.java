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

package org.pentaho.di.trans.steps.mappinginput;

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.NonZeroIntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;
/**
 * @author Andrey Khayrutdinov
 */
public class MappingInputMetaCloningTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester<MappingInputMeta> loadSaveTester;

  @Test
  public void clonesCorrectly() throws Exception {
    MappingInputMeta meta = new MappingInputMeta();
    meta.setFieldName( new String[] { "f1", "f2" } );
    meta.setFieldType( new int[] { ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.TYPE_STRING } );
    meta.setFieldLength( new int[] { 1, 2 } );
    meta.setFieldPrecision( new int[] { 3, 4 } );
    meta.setChanged();
    meta.setValueRenames( Collections.singletonList( new MappingValueRename( "f1", "r1" ) ) );

    Object clone = meta.clone();
    if ( !EqualsBuilder.reflectionEquals( meta, clone ) ) {
      String template = ""
        + "clone() is expected to handle all values.\n"
        + "\tOriginal object:\n"
        + "%s\n"
        + "\tCloned object:\n"
        + "%s";
      fail( String.format( template, ToStringBuilder.reflectionToString( meta ),
        ToStringBuilder.reflectionToString( clone ) ) );
    }
  }

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    PluginRegistry.init( false );
    List<String> attributes =
      Arrays.asList( "selectingAndSortingUnspecifiedFields", "fieldName", "fieldType", "fieldLength",
        "fieldPrecision" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "selectingAndSortingUnspecifiedFields", "isSelectingAndSortingUnspecifiedFields" );
        put( "fieldName", "getFieldName" );
        put( "fieldType", "getFieldType" );
        put( "fieldLength", "getFieldLength" );
        put( "fieldPrecision", "getFieldPrecision" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "selectingAndSortingUnspecifiedFields", "setSelectingAndSortingUnspecifiedFields" );
        put( "fieldName", "setFieldName" );
        put( "fieldType", "setFieldType" );
        put( "fieldLength", "setFieldLength" );
        put( "fieldPrecision", "setFieldPrecision" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 5 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "fieldName", stringArrayLoadSaveValidator );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    typeValidatorMap.put( int[].class.getCanonicalName(),
      new PrimitiveIntArrayLoadSaveValidator( new NonZeroIntLoadSaveValidator( 6 ), 5 ) );

    loadSaveTester = new LoadSaveTester<MappingInputMeta>( MappingInputMeta.class, attributes, getterMap,
      setterMap, attrValidatorMap, typeValidatorMap );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

}
