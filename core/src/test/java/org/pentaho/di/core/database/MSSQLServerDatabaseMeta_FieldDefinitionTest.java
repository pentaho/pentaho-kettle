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

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

public class MSSQLServerDatabaseMeta_FieldDefinitionTest {
  private MSSQLServerDatabaseMeta dbMeta;

  private static final String DEFAULT_TABLE_NAME = "table";

  private static final String STRING_INT = "INT";
  private static final String STRING_BIGINT = "BIGINT";
  private static final String STRING_DECIMAL = "DECIMAL";
  private static final String STRING_FLOAT = "FLOAT";
  private static final String STRING_VARCHAR = "VARCHAR";
  private static final String STRING_TEXT = "TEXT";


  @Before
  public void init() {
    dbMeta = new MSSQLServerDatabaseMeta();
  }


  @Test
  public void numberType_ZeroLength_ZeroPrecision() {
    ValueMetaInterface valueMeta =
      new MetaInterfaceBuilder( ValueMetaInterface.TYPE_NUMBER )
        .length( 0 )
        .precision( 0 )
        .build();

    assertEquals( STRING_INT, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );
  }

  @Test
  public void numberType_LessThanNineLength_ZeroPrecision() {
    ValueMetaInterface valueMeta =
      new MetaInterfaceBuilder( ValueMetaInterface.TYPE_NUMBER )
        .length( 5 )
        .precision( 0 )
        .build();

    assertEquals( STRING_INT, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );
  }


  @Test
  public void numberType_MoreThanNineLessThanEighteenLength_ZeroPrecision() {
    ValueMetaInterface valueMeta =
      new MetaInterfaceBuilder( ValueMetaInterface.TYPE_NUMBER )
        .length( 17 )
        .precision( 0 )
        .build();

    assertEquals( STRING_BIGINT, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );
  }

  @Test
  public void numberType_MoreThanEighteenLength_ZeroPrecision() {
    ValueMetaInterface valueMeta =
      new MetaInterfaceBuilder( ValueMetaInterface.TYPE_NUMBER )
        .length( 19 )
        .precision( 0 )
        .build();

    final String expected =
      STRING_DECIMAL + "(" + valueMeta.getLength() + "," + valueMeta.getPrecision() + ")";

    assertEquals( expected, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );
  }

  @Test
  public void numberType_NonZeroLength_NonZeroPrecision() {
    ValueMetaInterface valueMeta =
      new MetaInterfaceBuilder( ValueMetaInterface.TYPE_NUMBER )
        .length( 5 )
        .precision( 5 )
        .build();

    final String expected = STRING_DECIMAL + "(" + valueMeta.getLength() + "," + valueMeta.getPrecision() + ")";

    assertEquals( expected, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );
  }

  @Test
  public void numberType_ZeroLength_NonZeroPrecision() {
    ValueMetaInterface valueMeta = new MetaInterfaceBuilder( ValueMetaInterface.TYPE_NUMBER )
      .length( 0 )
      .precision( 5 )
      .build();

    final String definition = dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false );

    // There is actually returned FLOAT(53), where 53 is hardcoded string,
    // but we don't wanna tie to it, so checking type only.
    assertTrue( definition.contains( STRING_FLOAT ) );
  }

  @Test
  public void stringType_ZeroLength() {
    ValueMetaInterface valueMeta = new MetaInterfaceBuilder( ValueMetaInterface.TYPE_STRING )
      .length( 0 )
      .build();

    final String definition = dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false );

    // There is actually returned VARCHAR(100), where 100 is hardcoded string,
    // but we don't wanna tie to it, so checking type only.
    assertTrue( definition.contains( STRING_VARCHAR ) );
  }

  @Test
  public void stringType_NonZeroLength() {
    ValueMetaInterface valueMeta = new MetaInterfaceBuilder( ValueMetaInterface.TYPE_STRING )
      .length( 50 )
      .build();

    final String expected = STRING_VARCHAR + "(" + valueMeta.getLength() + ")";

    assertEquals( expected, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );

  }

  @Test
  public void stringType_TenThousandLength() {
    ValueMetaInterface valueMeta = new MetaInterfaceBuilder( ValueMetaInterface.TYPE_STRING )
      .length( 10_000 )
      .build();


    assertEquals( STRING_TEXT, dbMeta.getFieldDefinition( valueMeta, null, null, false, false, false ) );

  }

  private static class MetaInterfaceBuilder {
    private final ValueMetaInterface meta;

    public MetaInterfaceBuilder( Integer type ) {
      this( type, DEFAULT_TABLE_NAME );
    }

    public MetaInterfaceBuilder( Integer type, String name ) {
      switch ( type ) {
        case ValueMetaInterface.TYPE_NUMBER:
          meta = new ValueMetaNumber( name );
          break;
        case ValueMetaInterface.TYPE_STRING:
          meta = new ValueMetaString( name );
          break;
        default:
          meta = new ValueMetaNone( name );
      }
    }

    public MetaInterfaceBuilder length( int length ) {
      meta.setLength( length );
      return this;
    }

    public MetaInterfaceBuilder precision( int precision ) {
      meta.setPrecision( precision );
      return this;
    }

    public ValueMetaInterface build() {
      return meta;
    }
  }
}
