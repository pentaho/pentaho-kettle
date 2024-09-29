/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput.staxpoi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

public class StaxPoiSheetTest {

  private static final String BP_SHEET = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
    + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
    + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
    + "%s"
    + "</worksheet>";

  private static final String SHEET_DATE_NO_V = String.format( BP_SHEET,
    " <dimension ref=\"A1:A3\"/>"
      + " <sheetData>"
      + "   <row r=\"1\" spans=\"1:1\">"
      + "     <c r=\"A1\" s=\"1\" t=\"s\"><v>0</v></c>"
      + "   </row>"
      + "   <row r=\"2\" spans=\"1:1\">"
      + "     <c r=\"A2\" s=\"2\"><v>42248</v></c>"
      + "   </row>"
      + "   <row r=\"3\" spans=\"1:1\">"
      + "     <c r=\"A3\" s=\"2\"/>"
      + "   </row>"
      + " </sheetData>" );

  private static final String SHEET_1 = String.format( BP_SHEET,
    " <dimension ref=\"B2:F5\"/>"
      + " <sheetData>"
      + "  <row r=\"2\" spans=\"2:6\"><c r=\"B2\" t=\"s\"><v>0</v></c><c r=\"C2\" t=\"s\"><v>1</v></c>"
      + "    <c r=\"D2\" t=\"s\"><v>2</v></c><c r=\"E2\" t=\"s\"><v>3</v></c><c r=\"F2\" t=\"s\"><v>4</v></c></row>"
      + "  <row r=\"3\" spans=\"2:6\"><c r=\"B3\" t=\"s\"><v>5</v></c><c r=\"C3\" s=\"1\"><v>40428</v></c>"
      + "    <c r=\"D3\"><v>75</v></c><c r=\"E3\" t=\"b\"><v>1</v></c><c r=\"F3\"><f>D3</f><v>75</v></c></row>"
      + "  <row r=\"4\" spans=\"2:6\"><c r=\"B4\" t=\"s\"><v>6</v></c><c r=\"C4\" s=\"1\"><v>40429</v></c>"
      + "    <c r=\"D4\"><v>42</v></c><c r=\"E4\" t=\"b\"><v>0</v></c><c r=\"F4\"><f>F3+D4</f><v>117</v></c></row>"
      + "  <row r=\"5\" spans=\"2:6\"><c r=\"B5\" t=\"s\"><v>7</v></c><c r=\"C5\" s=\"1\"><v>40430</v></c>"
      + "    <c r=\"D5\"><v>93</v></c><c r=\"E5\" t=\"b\"><v>1</v></c><c r=\"F5\"><f>F4+D5</f><v>210</v></c></row>"
      + " </sheetData>" );

  private static final String SHEET_EMPTY = String.format( BP_SHEET, "<dimension ref=\"A1\"/><sheetData/>" );

  private static final String SHEET_INLINE_STRINGS = String.format( BP_SHEET,
    "<dimension ref=\"A1:B3\"/>"
      + "<sheetViews>"
      + "<sheetView tabSelected=\"1\" workbookViewId=\"0\" rightToLeft=\"false\">"
      + "<selection activeCell=\"C5\" sqref=\"C5\"/>"
      + "</sheetView>"
      + "</sheetViews>"
      + "<sheetFormatPr defaultRowHeight=\"15\"/>"
      + "<sheetData>"
      + "<row outlineLevel=\"0\" r=\"1\">"
      + "<c r=\"A1\" s=\"0\" t=\"inlineStr\"><is><t>Test1</t></is></c>"
      + "<c r=\"B1\" s=\"0\" t=\"inlineStr\"><is><t>Test2</t></is></c>"
      + "</row>"
      + "<row outlineLevel=\"0\" r=\"2\">"
      + "<c r=\"A2\" s=\"0\" t=\"inlineStr\"><is><t>value 1 1</t></is></c>"
      + "<c r=\"B2\" s=\"0\" t=\"inlineStr\"><is><t>value 2 1</t></is></c>"
      + "</row>"
      + "<row outlineLevel=\"0\" r=\"3\">"
      + "<c r=\"A3\" s=\"0\" t=\"inlineStr\"><is><t>value 1 2</t></is></c>"
      + "<c r=\"B3\" s=\"0\" t=\"inlineStr\"><is><t>value 2 2</t></is></c>"
      + "</row>"
      + "</sheetData>" );

  private static final String SHEET_NO_USED_RANGE_SPECIFIED = String.format( BP_SHEET,
    "<dimension ref=\"A1\" />"
      + "<sheetViews>"
      + "<sheetView tabSelected=\"1\" workbookViewId=\"0\">"
      + "<selection/>"
      + "</sheetView>"
      + "</sheetViews>"
      + "<sheetFormatPr defaultRowHeight=\"12.750000\" customHeight=\"true\"/>"
      + "<sheetData>"
      + "<row r=\"2\">"
      + "<c r=\"A2\" s=\"9\" t=\"s\">"
      + "<v>0</v>"
      + "</c><c r=\"B2\" s=\"9\" t=\"s\">"
      + "<v>0</v>"
      + "</c><c r=\"C2\" s=\"9\" t=\"s\">"
      + "<v>1</v>"
      + "</c><c r=\"D2\" s=\"9\" t=\"s\">"
      + "<v>2</v>"
      + "</c><c r=\"E2\" s=\"9\" t=\"s\">"
      + "<v>3</v>"
      + "</c>"
      + "</row>"
      + "<row r=\"3\">"
      + "<c r=\"A3\" s=\"11\" t=\"s\">"
      + "<v>4</v>"
      + "</c><c r=\"B3\" s=\"11\" t=\"s\">"
      + "<v>4</v>"
      + "</c><c r=\"C3\" s=\"11\" t=\"s\">"
      + "<v>5</v>"
      + "</c><c r=\"D3\" s=\"12\">"
      + "<v>2623</v>"
      + "</c><c r=\"E3\" s=\"11\" t=\"s\">"
      + "<v>6</v>"
      + "</c>"
      + "</row>"
      + "</sheetData>" );

  @Test
  public void testNullDateCell() throws Exception {
    // cell had null value instead of being null
    final String sheetId = "1";
    final String sheetName = "Sheet 1";
    XSSFReader reader = mockXSSFReader( sheetId, SHEET_DATE_NO_V,
      mockSharedStringsTable( "Some Date" ),
      mockStylesTable(
        Collections.singletonMap( 2, 165 ),
        Collections.singletonMap( 165, "M/D/YYYY" ) ) );
    StaxPoiSheet spSheet = spy( new StaxPoiSheet( reader, sheetName, sheetId ) );
    doReturn( true ).when( spSheet ).isDateCell( any() );
    KCell cell = spSheet.getRow( 1 )[ 0 ];
    assertNotNull( cell );
    assertEquals( KCellType.DATE, cell.getType() );
    cell = spSheet.getRow( 2 )[ 0 ];
    assertNull( "cell must be null", cell );
  }

  @Test( expected = ArrayIndexOutOfBoundsException.class )
  public void testEmptySheet_row0() throws Exception {
    XSSFReader reader = mockXSSFReader( "sheet1", SHEET_EMPTY,
      mock( SharedStringsTable.class ),
      mock( StylesTable.class ) );

    StaxPoiSheet sheet = new StaxPoiSheet( reader, "empty", "sheet1" );
    sheet.getRow( 0 );

    fail( "An exception should have been thrown!" );
  }

  @Test( expected = ArrayIndexOutOfBoundsException.class )
  public void testEmptySheet_row1() throws Exception {
    XSSFReader reader = mockXSSFReader( "sheet1", SHEET_EMPTY,
      mock( SharedStringsTable.class ),
      mock( StylesTable.class ) );

    StaxPoiSheet sheet = new StaxPoiSheet( reader, "empty", "sheet1" );
    sheet.getRow( 1 );

    fail( "An exception should have been thrown!" );
  }

  @Test
  public void testReadSameRow() throws Exception {
    KSheet sheet1 = getSampleSheet();
    KCell[] row = sheet1.getRow( 3 );
    assertEquals( "Two", row[ 1 ].getValue() );
    row = sheet1.getRow( 3 );
    assertEquals( "Two", row[ 1 ].getValue() );
  }

  @Test
  public void testReadRowRA() throws Exception {
    KSheet sheet1 = getSampleSheet();
    KCell[] row = sheet1.getRow( 4 );
    assertEquals( "Three", row[ 1 ].getValue() );
    row = sheet1.getRow( 2 );
    assertEquals( "One", row[ 1 ].getValue() );
  }

  @Test
  public void testReadEmptyRow() throws Exception {
    KSheet sheet1 = getSampleSheet();
    KCell[] row = sheet1.getRow( 0 );
    assertEquals( "empty row expected", 0, row.length );
  }

  @Test
  public void testReadCells() throws Exception {
    KSheet sheet = getSampleSheet();

    KCell cell = sheet.getCell( 1, 2 );
    assertEquals( "One", cell.getValue() );
    assertEquals( KCellType.LABEL, cell.getType() );

    cell = sheet.getCell( 2, 2 );
    assertEquals( KCellType.DATE, cell.getType() );
    assertEquals( new Date( 1283817600000L ), cell.getValue() );

    cell = sheet.getCell( 1, 3 );
    assertEquals( "Two", cell.getValue() );
    assertEquals( KCellType.LABEL, cell.getType() );
  }

  @Test
  public void testReadData() throws Exception {
    KSheet sheet1 = getSampleSheet();
    assertEquals( 5, sheet1.getRows() );

    KCell[] row = sheet1.getRow( 2 );
    assertEquals( KCellType.LABEL, row[ 1 ].getType() );
    assertEquals( "One", row[ 1 ].getValue() );
    assertEquals( KCellType.DATE, row[ 2 ].getType() );
    assertEquals( new Date( 1283817600000L ), row[ 2 ].getValue() );
    assertEquals( KCellType.NUMBER, row[ 3 ].getType() );
    assertEquals( Double.valueOf( "75" ), row[ 3 ].getValue() );
    assertEquals( KCellType.BOOLEAN, row[ 4 ].getType() );
    assertEquals( Boolean.TRUE, row[ 4 ].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[ 5 ].getType() );
    assertEquals( Double.valueOf( "75" ), row[ 5 ].getValue() );

    row = sheet1.getRow( 3 );
    assertEquals( KCellType.LABEL, row[ 1 ].getType() );
    assertEquals( "Two", row[ 1 ].getValue() );
    assertEquals( KCellType.DATE, row[ 2 ].getType() );
    assertEquals( new Date( 1283904000000L ), row[ 2 ].getValue() );
    assertEquals( KCellType.NUMBER, row[ 3 ].getType() );
    assertEquals( Double.valueOf( "42" ), row[ 3 ].getValue() );
    assertEquals( KCellType.BOOLEAN, row[ 4 ].getType() );
    assertEquals( Boolean.FALSE, row[ 4 ].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[ 5 ].getType() );
    assertEquals( Double.valueOf( "117" ), row[ 5 ].getValue() );

    row = sheet1.getRow( 4 );
    assertEquals( KCellType.LABEL, row[ 1 ].getType() );
    assertEquals( "Three", row[ 1 ].getValue() );
    assertEquals( KCellType.DATE, row[ 2 ].getType() );
    assertEquals( new Date( 1283990400000L ), row[ 2 ].getValue() );
    assertEquals( KCellType.NUMBER, row[ 3 ].getType() );
    assertEquals( Double.valueOf( "93" ), row[ 3 ].getValue() );
    assertEquals( KCellType.BOOLEAN, row[ 4 ].getType() );
    assertEquals( Boolean.TRUE, row[ 4 ].getValue() );
    assertEquals( KCellType.NUMBER_FORMULA, row[ 5 ].getType() );
    assertEquals( Double.valueOf( "210" ), row[ 5 ].getValue() );

    try {
      row = sheet1.getRow( 5 );
      fail( "No out of bounds exception thrown when expected" );
    } catch ( ArrayIndexOutOfBoundsException e ) {
      // OK!
    }
  }

  private StaxPoiSheet getSampleSheet() throws Exception {
    String sheetId = "sheet1";
    XSSFReader reader = mockXSSFReader( sheetId, SHEET_1,
      mockSharedStringsTable(
        "Col1Label", "Col2Date", "Col3Number", "Col4Boolean", "Col5NumFormula", "One", "Two", "Three" ),
      mockStylesTable( Collections.singletonMap( 1, 14 ), Collections.<Integer, String>emptyMap() ) );
    return new StaxPoiSheet( reader, "Sheet 1", sheetId );
  }

  private XSSFReader mockXSSFReader( final String sheetId,
                                     final String sheetContent, final SharedStrings sst, final StylesTable styles )
    throws Exception {
    XSSFReader reader = mock( XSSFReader.class );
    when( reader.getSharedStringsTable() ).thenReturn( sst );
    when( reader.getStylesTable() ).thenReturn( styles );
    when( reader.getSheet( sheetId ) ).thenAnswer( new Answer<InputStream>() {
      public InputStream answer( InvocationOnMock invocation ) throws Throwable {
        return IOUtils.toInputStream( sheetContent, "UTF-8" );
      }
    } );
    return reader;
  }

  private StylesTable mockStylesTable( final Map<Integer, Integer> styleToNumFmtId,
                                       final Map<Integer, String> numFmts ) {
    StylesTable styles = mock( StylesTable.class );
    when( styles.getCellXfAt( any( Integer.class ) ) ).then( new Answer<CTXf>() {
      public CTXf answer( InvocationOnMock invocation ) throws Throwable {
        int style = (int) invocation.getArguments()[ 0 ];
        Integer numFmtId = styleToNumFmtId.get( style );
        if ( numFmtId != null ) {
          CTXf ctxf = CTXf.Factory.newInstance();
          ctxf.setNumFmtId( numFmtId );
          return ctxf;
        } else {
          return null;
        }
      }
    } );
    when( styles.getNumberFormatAt( any( Short.class ) ) ).then( new Answer<String>() {
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return numFmts.get( invocation.getArguments()[ 0 ] );
      }
    } );
    return styles;
  }

  private SharedStringsTable mockSharedStringsTable( String... strings ) {
    SharedStringsTable sst = new SharedStringsTable();
    for (String str : strings) {
      sst.addSharedStringItem(new XSSFRichTextString(str));
    }
    return sst;
  }

  @Test
  public void testInlineString() throws Exception {
    final String sheetId = "1";
    final String sheetName = "Sheet 1";
    XSSFReader reader = mockXSSFReader( sheetId, SHEET_INLINE_STRINGS,
      mock( SharedStringsTable.class ),
      mock( StylesTable.class ) );
    StaxPoiSheet spSheet = new StaxPoiSheet( reader, sheetName, sheetId );
    KCell[] rowCells = spSheet.getRow( 0 );
    assertEquals( "Test1", rowCells[ 0 ].getValue() );
    assertEquals( KCellType.STRING_FORMULA, rowCells[ 0 ].getType() );
    assertEquals( "Test2", rowCells[ 1 ].getValue() );
    assertEquals( KCellType.STRING_FORMULA, rowCells[ 1 ].getType() );
    rowCells = spSheet.getRow( 1 );
    assertEquals( "value 1 1", rowCells[ 0 ].getValue() );
    assertEquals( KCellType.STRING_FORMULA, rowCells[ 0 ].getType() );
    assertEquals( "value 2 1", rowCells[ 1 ].getValue() );
    assertEquals( KCellType.STRING_FORMULA, rowCells[ 1 ].getType() );
    rowCells = spSheet.getRow( 2 );
    assertEquals( "value 1 2", rowCells[ 0 ].getValue() );
    assertEquals( KCellType.STRING_FORMULA, rowCells[ 0 ].getType() );
    assertEquals( "value 2 2", rowCells[ 1 ].getValue() );
    assertEquals( KCellType.STRING_FORMULA, rowCells[ 1 ].getType() );
  }

  // The row and column bounds of all cells in the worksheet are specified in ref attribute of Dimension tag in sheet
  // xml
  // But ref can be present as range: <dimension ref="A1:C2"/> or as just one start cell: <dimension ref="A1"/>.
  // Below tests to validate correct work for such cases
  @Test
  public void testNoUsedRangeSpecified() throws Exception {
    final String sheetId = "1";
    final String sheetName = "Sheet 1";
    SharedStringsTable sharedStringsTableMock =
      mockSharedStringsTable( "Report ID", "Report ID", "Approval Status", "Total Report Amount", "Policy",
        "ReportIdValue_1", "ReportIdValue_1", "ApprovalStatusValue_1", "PolicyValue_1" );
    XSSFReader reader =
      mockXSSFReader( sheetId, SHEET_NO_USED_RANGE_SPECIFIED, sharedStringsTableMock, mock( StylesTable.class ) );
    StaxPoiSheet spSheet = new StaxPoiSheet( reader, sheetName, sheetId );
    // The first row is empty - it should have empty rowCells
    KCell[] rowCells = spSheet.getRow( 0 );
    assertEquals( 0, rowCells.length );
    // The second row - is the header - just skip it
    rowCells = spSheet.getRow( 1 );
    assertEquals( 0, rowCells.length );
    // The row3 - is the first row with data - validating it
    rowCells = spSheet.getRow( 2 );
    assertEquals( KCellType.LABEL, rowCells[ 0 ].getType() );
    assertEquals( "ReportIdValue_1", rowCells[ 0 ].getValue() );
    assertEquals( KCellType.LABEL, rowCells[ 1 ].getType() );
    assertEquals( "ReportIdValue_1", rowCells[ 1 ].getValue() );
    assertEquals( KCellType.LABEL, rowCells[ 2 ].getType() );
    assertEquals( "ApprovalStatusValue_1", rowCells[ 2 ].getValue() );
    assertEquals( KCellType.NUMBER, rowCells[ 3 ].getType() );
    assertEquals( 2623.0, rowCells[ 3 ].getValue() );
    assertEquals( KCellType.LABEL, rowCells[ 4 ].getType() );
    assertEquals( "PolicyValue_1", rowCells[ 4 ].getValue() );
  }

}
