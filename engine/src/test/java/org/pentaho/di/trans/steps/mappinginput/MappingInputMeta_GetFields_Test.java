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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mapping.MappingValueRename;


import java.util.Arrays;
import java.util.List;

import java.util.Collections;
import static org.junit.Assert.assertEquals;

/**
 * @author Andrey Khayrutdinov
 */
@RunWith( Parameterized.class )
public class MappingInputMeta_GetFields_Test {

  @BeforeClass
  public static void setUpBeforeClass() throws KettlePluginException {
    ValueMetaPluginType.getInstance().searchPlugins();
  }

  @Parameterized.Parameters
  public static List<Object[]> getData() {
    return Arrays.asList(
      simpleRename(),
      renameAndSort(),
      noRenames(),
      noInputRowMeta(),
      alreadyRenamed()
    );
  }

  private static Object[] createCaseData( RowMeta inputRowMeta, List<MappingValueRename> renames, String[] fields,
                                          boolean sortUnspecified, String[] expectedOutputFields ) {
    return new Object[] { inputRowMeta, renames, fields, sortUnspecified, expectedOutputFields };
  }

  private static Object[] createCaseData( RowMeta inputRowMeta, List<MappingValueRename> renames, String[] fields,
                                          String[] expectedOutputFields ) {
    return createCaseData( inputRowMeta, renames, fields, false, expectedOutputFields );
  }


  private static RowMeta createRowMeta( String... fields ) {
    RowMeta meta = new RowMeta();
    for ( String field : fields ) {
      meta.addValueMeta( new ValueMetaString( field ) );
    }
    return meta;
  }

  private static Object[] simpleRename() {
    RowMeta inputRowMeta = createRowMeta( "field1", "field2" );
    List<MappingValueRename> renames = Collections.singletonList( new MappingValueRename( "field2", "renamed" ) );
    String[] fields = new String[] { "field1", "renamed" };
    String[] expected = new String[] { "field1", "renamed" };

    return createCaseData( inputRowMeta, renames, fields, expected );
  }

  private static Object[] renameAndSort() {
    RowMeta inputRowMeta = createRowMeta( "field1", "field2", "2", "1" );
    List<MappingValueRename> renames = Collections.singletonList( new MappingValueRename( "field2", "renamed" ) );
    String[] fields = new String[] { "field1", "renamed" };
    String[] expected = new String[] { "field1", "renamed", "1", "2" };

    return createCaseData( inputRowMeta, renames, fields, true, expected );
  }

  private static Object[] noRenames() {
    RowMeta inputRowMeta = createRowMeta( "field1", "field2" );
    String[] fields = new String[] { "field1", "field2" };
    String[] expected = new String[] { "field1", "field2" };

    return createCaseData( inputRowMeta, null, fields, expected );
  }

  private static Object[] noInputRowMeta() {
    String[] fields = new String[] { "field1", "field2" };
    String[] expected = new String[] { "field1", "field2" };

    return createCaseData( null, null, fields, expected );
  }

  private static Object[] alreadyRenamed() {
    RowMeta inputRowMeta = createRowMeta( "field1", "renamed" );
    List<MappingValueRename> renames = Collections.singletonList( new MappingValueRename( "field2", "renamed" ) );
    String[] fields = new String[] { "field1", "renamed" };
    String[] expected = new String[] { "field1", "renamed" };

    return createCaseData( inputRowMeta, renames, fields, expected );
  }


  private final RowMeta inputRowMeta;
  private final List<MappingValueRename> renames;
  private final String[] fields;
  private final boolean sortUnspecified;
  private final String[] expectedOutputFields;

  public MappingInputMeta_GetFields_Test( RowMeta inputRowMeta, List<MappingValueRename> renames, String[] fields,
                                          boolean sortUnspecified, String[] expectedOutputFields ) {
    this.inputRowMeta = inputRowMeta;
    this.renames = renames;
    this.fields = fields;
    this.sortUnspecified = sortUnspecified;
    this.expectedOutputFields = expectedOutputFields;
  }

  @Test
  public void getFields() throws Exception {
    MappingInputMeta meta = new MappingInputMeta();
    meta.setInputRowMeta( inputRowMeta );
    meta.setValueRenames( renames );
    meta.allocate( fields.length );
    meta.setFieldName( fields );
    meta.setSelectingAndSortingUnspecifiedFields( sortUnspecified );

    RowMeta rowMeta = new RowMeta();
    meta.getFields( rowMeta, "origin", new RowMetaInterface[ 0 ], null, null, null, null );

    assertEquals( Arrays.toString( expectedOutputFields ), expectedOutputFields.length, rowMeta.size() );
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      assertEquals( String.format( "Element %d", i ), expectedOutputFields[ i ], rowMeta.getValueMeta( i ).getName() );
    }
  }
}
