/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.ValueMetaInterface;
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
    PluginRegistry.init( true );
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
