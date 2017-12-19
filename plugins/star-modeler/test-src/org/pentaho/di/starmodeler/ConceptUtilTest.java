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

package org.pentaho.di.starmodeler;

import org.junit.Test;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.pms.schema.concept.DefaultPropertyID;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ConceptUtilTest {
  private String locale = Locale.UK.toString();

  @Test
  public void testGetDescription() throws Exception {
    final Concept concept = mock( Concept.class );
    final LocalizedString localizedString = mock( LocalizedString.class );
    final String result = "result";
    when( localizedString.getLocalizedString( eq( locale ) ) ).thenReturn( result );
    when( concept.getProperty( anyString() ) ).thenReturn( localizedString );

    final String description = ConceptUtil.getDescription( concept, locale );

    verify( localizedString, times( 1 ) ).getLocalizedString( eq( locale ) );
    assertEquals( result, description );
  }

  @Test
  public void testGetName() throws Exception {
    final Concept concept = mock( Concept.class );
    final LocalizedString localizedString = mock( LocalizedString.class );
    final String result = "result";
    when( localizedString.getLocalizedString( eq( locale ) ) ).thenReturn( result );
    when( concept.getProperty( anyString() ) ).thenReturn( localizedString );

    final String name = ConceptUtil.getName( concept, locale );

    verify( localizedString, times( 1 ) ).getLocalizedString( eq( locale ) );
    assertEquals( result, name );
  }

  @Test
  public void testGetTableTypeConcept() throws Exception {
    final Concept concept = mock( Concept.class );
    TableType tableType = ConceptUtil.getTableType( concept );
    assertEquals( TableType.OTHER, tableType );

    final TableType result = TableType.FACT;
    when( concept.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( result );

    tableType = ConceptUtil.getTableType( concept );
    assertEquals( result, tableType );
  }

  @Test
  public void testGetTableTypeString() throws Exception {
    TableType tableType = ConceptUtil.getTableType( "" );
    assertEquals( TableType.OTHER, tableType );

    final TableType result = TableType.FACT;

    tableType = ConceptUtil.getTableType( result.name() );
    assertEquals( result, tableType );
  }

  @Test
  public void testGetString() throws Exception {
    final Concept concept = mock( Concept.class );
    final String id = "id";
    final String result = "result";
    when( concept.getProperty( eq( id ) ) ).thenReturn( result );

    final String string = ConceptUtil.getString( concept, id );
    assertEquals( result, string );
  }

  @Test
  public void testFindFirstKeyColumn() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );

    assertNull( ConceptUtil.findFirstKeyColumn( logicalTable ) );

    final LogicalColumn logicalColumn = mock( LogicalColumn.class );
    final LogicalColumn logicalColumnKey1 = mock( LogicalColumn.class );
    final LogicalColumn logicalColumnKey2 = mock( LogicalColumn.class );
    when( logicalColumnKey1.getFieldType() ).thenReturn( FieldType.KEY );
    when( logicalColumnKey2.getFieldType() ).thenReturn( FieldType.KEY );
    when( logicalTable.getLogicalColumns() ).thenReturn( new LinkedList<LogicalColumn>() {
      {
        add( logicalColumn );
        add( logicalColumnKey1 );
        add( logicalColumnKey2 );
      }
    } );

    final LogicalColumn firstKeyColumn = ConceptUtil.findFirstKeyColumn( logicalTable );
    assertEquals( logicalColumnKey1, firstKeyColumn );
  }

  @Test
  public void testGetDimensionType() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );
    final DimensionType dtDate = DimensionType.DATE;
    when( logicalTable.getProperty( DefaultIDs.LOGICAL_TABLE_DIMENSION_TYPE ) ).thenReturn( dtDate.name() );

    final DimensionType dimensionType = ConceptUtil.getDimensionType( logicalTable );
    assertEquals( dtDate, dimensionType );
  }

  @Test
  public void testGetAttributeType() throws Exception {
    final LogicalColumn logicalColumn = mock( LogicalColumn.class );
    final AttributeType attribute = AttributeType.ATTRIBUTE;
    when( logicalColumn.getProperty( DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE ) ).thenReturn( attribute.name() );

    final AttributeType attributeType = ConceptUtil.getAttributeType( logicalColumn );
    assertEquals( attribute, attributeType );
  }

  @Test
  public void testFindLogicalColumn() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );
    final AttributeType attribute = AttributeType.ATTRIBUTE;

    final LogicalColumn logicalColumn1 = mock( LogicalColumn.class );
    when( logicalColumn1.getProperty( DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE ) ).thenReturn( attribute.name() );
    final LogicalColumn logicalColumn2 = mock( LogicalColumn.class );
    when( logicalColumn2.getProperty( DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE ) ).thenReturn( attribute.name() );
    when( logicalTable.getLogicalColumns() ).thenReturn( new LinkedList<LogicalColumn>() {
      {
        add( logicalColumn1 );
        add( logicalColumn2 );
      }
    } );

    assertNull( ConceptUtil.findLogicalColumn( logicalTable, AttributeType.ATTRIBUTE_HISTORICAL ) );

    final LogicalColumn result = ConceptUtil.findLogicalColumn( logicalTable, attribute );
    assertEquals( logicalColumn1, result );

  }

  @Test
  public void testFindLogicalColumns() throws Exception {
    final LogicalTable logicalTable = mock( LogicalTable.class );
    final AttributeType attribute = AttributeType.ATTRIBUTE;

    final LogicalColumn logicalColumn1 = mock( LogicalColumn.class );
    when( logicalColumn1.getProperty( DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE ) ).thenReturn( attribute.name() );
    final LogicalColumn logicalColumn2 = mock( LogicalColumn.class );
    when( logicalColumn2.getProperty( DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE ) ).thenReturn( attribute.name() );
    when( logicalTable.getLogicalColumns() ).thenReturn( new LinkedList<LogicalColumn>() {
      {
        add( logicalColumn1 );
        add( logicalColumn2 );
      }
    } );

    assertTrue( ConceptUtil.findLogicalColumns( logicalTable, AttributeType.ATTRIBUTE_HISTORICAL ).isEmpty() );

    final List<LogicalColumn> logicalColumns = ConceptUtil.findLogicalColumns( logicalTable, attribute );
    assertEquals( 2, logicalColumns.size() );
    assertEquals( logicalColumn1, logicalColumns.get( 0 ) );
    assertEquals( logicalColumn2, logicalColumns.get( 1 ) );
  }

  @Test
  public void testFindLogicalTables() throws Exception {
    final LogicalModel logicalModel = mock( LogicalModel.class );
    final TableType dimension = TableType.DIMENSION;

    final LogicalTable logicalTable1 = mock( LogicalTable.class );
    when( logicalTable1.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( dimension );
    final LogicalTable logicalTable2 = mock( LogicalTable.class );
    when( logicalTable2.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.FACT );
    final LogicalTable logicalTable3 = mock( LogicalTable.class );
    when( logicalTable3.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( dimension );
    when( logicalModel.getLogicalTables() ).thenReturn( new LinkedList<LogicalTable>() {
      {
        add( logicalTable1 );
        add( logicalTable2 );
        add( logicalTable3 );
      }
    } );

    final List<LogicalTable> logicalTables = ConceptUtil.findLogicalTables( logicalModel, dimension );
    assertEquals( 2, logicalTables.size() );
    assertEquals( logicalTable1, logicalTables.get( 0 ) );
    assertEquals( logicalTable3, logicalTables.get( 1 ) );
  }

  @Test
  public void testIndexOfFactTable() throws Exception {
    final LogicalModel logicalModel = mock( LogicalModel.class );

    final LogicalTable logicalTable1 = mock( LogicalTable.class );
    when( logicalTable1.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.DIMENSION );
    final LogicalTable logicalTable2 = mock( LogicalTable.class );
    when( logicalTable2.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.FACT );
    final LogicalTable logicalTable3 = mock( LogicalTable.class );
    when( logicalTable3.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.DIMENSION );
    when( logicalModel.getLogicalTables() ).thenReturn( new LinkedList<LogicalTable>() {
      {
        add( logicalTable1 );
        add( logicalTable2 );
        add( logicalTable3 );
      }
    } );

    final int indexOfFactTable = ConceptUtil.indexOfFactTable( logicalModel );
    assertEquals( 1, indexOfFactTable );
  }

  @Test
  public void testFindDimensionWithName() throws Exception {
    final LogicalModel logicalModel = mock( LogicalModel.class );
    final String dn = "dn";

    final LogicalTable logicalTable1 = mock( LogicalTable.class );
    when( logicalTable1.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.DIMENSION );
    when( logicalTable1.getProperty( eq( Concept.NAME_PROPERTY ) ) )
        .thenReturn( new LocalizedString( locale, "wrong name" ) );
    final LogicalTable logicalTable2 = mock( LogicalTable.class );
    when( logicalTable2.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.FACT );
    final LogicalTable logicalTable3 = mock( LogicalTable.class );
    when( logicalTable3.getProperty( eq( DefaultPropertyID.TABLE_TYPE.getId() ) ) ).thenReturn( TableType.DIMENSION );
    when( logicalTable3.getProperty( eq( Concept.NAME_PROPERTY ) ) ).thenReturn( new LocalizedString( locale, dn ) );
    when( logicalModel.getLogicalTables() ).thenReturn( new LinkedList<LogicalTable>() {
      {
        add( logicalTable1 );
        add( logicalTable2 );
        add( logicalTable3 );
      }
    } );

    assertNull( ConceptUtil.findDimensionWithName( logicalModel, dn, "other_locale" ) );
    assertNull( ConceptUtil.findDimensionWithName( logicalModel, "dn2", locale ) );

    final LogicalTable dimensionWithName = ConceptUtil.findDimensionWithName( logicalModel, dn, locale );
    assertEquals( logicalTable3, dimensionWithName );
  }

  @Test
  public void testGetDataType() throws Exception {
    assertEquals( DataType.UNKNOWN, ConceptUtil.getDataType( "" ) );
    assertEquals( DataType.BINARY, ConceptUtil.getDataType( DataType.BINARY.name() ) );
  }
}
