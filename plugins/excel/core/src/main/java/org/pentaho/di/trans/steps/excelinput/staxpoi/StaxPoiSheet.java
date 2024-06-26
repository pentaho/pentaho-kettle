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

/**
 * Author = Shailesh Ahuja
 */

package org.pentaho.di.trans.steps.excelinput.staxpoi;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTXf;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.xml.XMLParserFactoryProducer;

/**
 * Streaming reader for XLSX sheets.<br>
 * Rows should only be accessed sequentially: random access will severely impact performance.<br>
 */
public class StaxPoiSheet implements KSheet {

  // set to UTC for coherence with PoiSheet
  private static final TimeZone DATE_TZ = TimeZone.getTimeZone( "UTC" );

  private static final String ATTRIBUTE_T = "t";

  private static final String TAG_C = "c";
  private static final String TAG_IS = "is";
  private static final String TAG_ROW = "row";
  private static final String TAG_SHEET_DATA = "sheetData";
  private static final String TAG_T = "t";
  private static final String TAG_V = "v";

  private final String sheetName;
  private final String sheetId;

  private final XSSFReader xssfReader;
  private InputStream sheetStream;
  private XMLStreamReader sheetReader;

  // hold the pointer to the current row so that access to the next row in the stream is quick and easy
  private int currentRow;
  private List<String> headerRow;
  private int numRows;
  private int numCols;
  private boolean maxColsNumberDefined = true;
  // 1-based first non-empty row
  private int firstRow;
  private KCell[] currentRowCells;

  // full shared strings table
  private SharedStrings sst;
  // custom styles
  private StylesTable styles;

  public StaxPoiSheet( XSSFReader reader, String sheetName, String sheetID )
      throws InvalidFormatException, IOException, XMLStreamException {
    this.sheetName = sheetName;
    xssfReader = reader;
    sheetId = sheetID;
    sst = reader.getSharedStringsTable();
    styles = reader.getStylesTable();
    sheetStream = reader.getSheet( sheetID );
    XMLInputFactory factory = XMLParserFactoryProducer.createSecureXMLInputFactory();
    sheetReader = factory.createXMLStreamReader( sheetStream );
    headerRow = new ArrayList<>();
    while ( sheetReader.hasNext() ) {
      int event = sheetReader.next();
      if ( event == XMLStreamConstants.START_ELEMENT ) {
        if ( sheetReader.getLocalName().equals( "dimension" ) ) {
          String dim = sheetReader.getAttributeValue( null, "ref" );
          // empty sheets have dimension with no range
          if ( StringUtils.contains( dim, ':' ) ) {
            dim = dim.split( ":" )[ 1 ];
            numRows = StaxUtil.extractRowNumber( dim );
            numCols = StaxUtil.extractColumnNumber( dim );
          } else {
            maxColsNumberDefined = false;
            numCols = StaxUtil.MAX_COLUMNS;
            numRows = StaxUtil.MAX_ROWS;
          }
        } else if ( sheetReader.getLocalName().equals( TAG_ROW ) ) {
          currentRow = Integer.parseInt( sheetReader.getAttributeValue( null, "r" ) );
          firstRow = currentRow;

          // calculate the number of columns in the header row
          while ( sheetReader.hasNext() ) {
            event = sheetReader.next();
            if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( TAG_ROW ) ) {
              // if the row has ended, break the inner while loop
              break;
            }
            if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( TAG_C ) ) {
              String attributeValue = sheetReader.getAttributeValue( null, ATTRIBUTE_T );
              if ( attributeValue != null ) {
                if ( attributeValue.equals( "s" ) ) {
                  // if the type of the cell is string, we continue
                  while ( sheetReader.hasNext() ) {
                    event = sheetReader.next();
                    if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( TAG_V ) ) {
                      int idx = Integer.parseInt( sheetReader.getElementText() );
                      if (sst != null && sst.getCount() > 0) {
                        String content = new XSSFRichTextString(sst.getItemAt(idx).getString()).toString();
                        headerRow.add( content );
                      }
                      break;
                    }
                  }
                } else if ( attributeValue.equals( "inlineStr" ) ) {
                  // if the type of the cell is string, we continue
                  while ( sheetReader.hasNext() ) {
                    event = sheetReader.next();
                    if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( TAG_IS ) ) {
                      while ( sheetReader.hasNext() ) {
                        event = sheetReader.next();
                        if ( event == XMLStreamConstants.CHARACTERS ) {
                          String content = new XSSFRichTextString( sheetReader.getText() ).toString();
                          headerRow.add( content );
                          break;
                        }
                        if ( event == XMLStreamConstants.END_ELEMENT ) {
                          if ( sheetReader.getLocalName().equals( TAG_T ) ) {
                            // If "t" ended, this is a 'blank' cell
                            headerRow.add( "" );
                            break;
                          } else if ( sheetReader.getLocalName().equals( TAG_IS ) ) {
                            // If "is" ended, this is a 'null' cell
                            headerRow.add( null );
                            break;
                          }
                        }
                      }
                      break;
                    }
                  }
                }
              } else {
                // Most probably a 'null' cell, but let's make sure...
                if ( sheetReader.hasNext() ) {
                  event = sheetReader.next();
                  if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( TAG_C ) ) {
                    // Yes, it is a 'null' cell!
                    headerRow.add( null );
                    continue;
                  }
                }
                break;
              }
            }
          }
          // we have parsed the header row
          break;
        }
      }
    }
  }

  boolean isMaxColsNumberDefined() {
    return maxColsNumberDefined;
  }

  @Override
  public KCell[] getRow( int rownr ) {
    // xlsx raw row numbers are 1-based index, KSheet is 0-based

    // Don't check the upper limit as not all rows may have been read!
    // If it's found that the row does not exist, the exception will be thrown at the end of this method.
    if ( rownr < 0 ) {
      // KSheet requires out of bounds here
      throw new ArrayIndexOutOfBoundsException( rownr );
    }
    if ( rownr + 1 < firstRow ) {
      // before first non-empty row
      return new KCell[0];
    }
    if ( rownr > 0 && currentRow == rownr + 1 ) {
      if ( currentRowCells != null ) {
        return currentRowCells;
      }
      // The case when the table contains the empty row(s) before the header
      // but at the same time user wants to read starting from 0 row
      return new KCell[0];
    }
    try {
      if ( currentRow >= rownr + 1 ) {
        // allow random access per api despite performance hit
        resetSheetReader();
      }
      while ( sheetReader.hasNext() ) {
        int event = sheetReader.next();
        if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( TAG_ROW ) ) {
          String rowIndicator = sheetReader.getAttributeValue( null, "r" );
          currentRow = Integer.parseInt( rowIndicator );
          if ( currentRow < rownr + 1 ) {
            continue;
          }
          currentRowCells = parseRow();
          return currentRowCells;
        }
        if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( TAG_SHEET_DATA ) ) {
          // There're no more columns, no need to continue to read
          break;
        }
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }

    // We've read all document rows, let's update the final count.
    numRows = currentRow;

    // And, as this was an invalid row to ask for, throw the proper exception!
    throw new ArrayIndexOutOfBoundsException( rownr );
  }

  private KCell[] parseRow() throws XMLStreamException {
    List<StaxPoiCell> cells;
    if ( isMaxColsNumberDefined() ) {
      cells = new ArrayList<>( numCols );
    } else {
      cells = new ArrayList<>();
    }

    int undefinedColIndex = 0;
    for ( int i = 0; i < numCols; i++ ) {
      // go to the "c" cell tag
      while ( sheetReader.hasNext() ) {
        int event = sheetReader.next();
        if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( TAG_C ) ) {
          break;
        }
        if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( TAG_ROW ) ) {
          // premature end of row, returning what we have
          return cells.toArray( new StaxPoiCell[cells.size()] );
        }
      }
      // We're on the "c" cell tag
      String cellLocation = sheetReader.getAttributeValue( null, "r" );
      int columnIndex = StaxUtil.extractColumnNumber( cellLocation ) - 1;

      String cellType = sheetReader.getAttributeValue( null, ATTRIBUTE_T );
      String cellStyle = sheetReader.getAttributeValue( null, "s" );

      boolean isFormula = false;
      String content = null;
      // get value tag
      while ( sheetReader.hasNext() ) {
        int event = sheetReader.next();
        if ( event == XMLStreamConstants.START_ELEMENT ) {
          if ( sheetReader.getLocalName().equals( TAG_V ) ) {
            // read content as string
            if ( cellType != null && cellType.equals( "s" ) ) {
              int idx = Integer.parseInt( sheetReader.getElementText() );
              if (sst != null && sst.getCount() > 0) {
                content = new XSSFRichTextString(sst.getItemAt(idx).getString()).toString();
              }
            } else {
              content = sheetReader.getElementText();
            }
          } else if ( sheetReader.getLocalName().equals( TAG_IS ) ) {
            while ( sheetReader.hasNext() ) {
              event = sheetReader.next();
              if ( event == XMLStreamConstants.CHARACTERS ) {
                content = new XSSFRichTextString( sheetReader.getText() ).toString();
                break;
              }
              if ( event == XMLStreamConstants.END_ELEMENT ) {
                if ( sheetReader.getLocalName().equals( TAG_T ) ) {
                  // If "t" ended, this is a 'blank' cell
                  content = "";
                  break;
                } else if ( sheetReader.getLocalName().equals( TAG_IS ) ) {
                  // If "is" ended, this is a 'null' cell

                  content = null;
                  break;
                }
              }
            }
          } else if ( sheetReader.getLocalName().equals( "f" ) ) {
            isFormula = true;
          }
        }
        if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( TAG_C ) ) {
          break;
        }
      }
      if ( content != null ) {
        KCellType kcType = getCellType( cellType, cellStyle, isFormula );
        setCells( cells, undefinedColIndex, columnIndex, new StaxPoiCell( parseValue( kcType, content ), kcType, currentRow ) );
      } else {
        // else let cell be null
        setCells( cells, undefinedColIndex, columnIndex, null );
      }
        undefinedColIndex = columnIndex + 1;
    }
    return cells.toArray( new StaxPoiCell[cells.size()] );
  }

  private static void setCells( List<StaxPoiCell> cellsArray, int firstUndefinedColIndex, int foundColIndex, StaxPoiCell cell ) {
    // fill all cells before found with Null
    for ( int index = firstUndefinedColIndex; index < foundColIndex; index++ ) {
      cellsArray.add( null );
    }
    // add the found Cell
    cellsArray.add( cell );
  }

  @Override
  public String getName() {
    return sheetName;
  }

  @Override
  public int getRows() {
    return numRows;
  }

  @Override
  public KCell getCell( int colnr, int rownr ) {
    if ( rownr == 0 && colnr < headerRow.size() ) {
      // only possible to return header
      return new StaxPoiCell( headerRow.get( colnr ), rownr );
    }
    // if random access this will be very expensive
    KCell[] row = getRow( rownr );
    if ( row != null && rownr < row.length ) {
      return row[colnr];
    }
    return null;
  }

  private KCellType getCellType( String cellType, String cellStyle, boolean isFormula ) {
    // numeric type can be implicit or 'n'
    if ( cellType == null || cellType.equals( "n" ) ) {
      // the only difference between date and numeric is the cell format
      if ( isDateCell( cellStyle ) ) {
        return isFormula ? KCellType.DATE_FORMULA : KCellType.DATE;
      }
      return isFormula ? KCellType.NUMBER_FORMULA : KCellType.NUMBER;
    }
    switch ( cellType ) {
      case "s":
        return KCellType.LABEL;
      case "b":
        return isFormula ? KCellType.BOOLEAN_FORMULA : KCellType.BOOLEAN;
      case "e":
        // error
        return KCellType.EMPTY;
      case "str":
      default:
        return KCellType.STRING_FORMULA;
    }
  }

  @VisibleForTesting
  protected boolean isDateCell( String cellStyle ) {
    if ( cellStyle != null ) {
      int styleIdx = Integer.parseInt( cellStyle );
      CTXf cellXf = styles.getCellXfAt( styleIdx );
      if ( cellXf != null ) {
        // need id for builtin types, format if custom
        short formatId = (short) cellXf.getNumFmtId();
        String format = styles.getNumberFormatAt( formatId );
        return DateUtil.isADateFormat( formatId, format );
      }
    }
    return false;
  }

  private Object parseValue( KCellType type, String vContent ) {
    if ( vContent == null ) {
      return null;
    }
    try {
      switch ( type ) {
        case NUMBER:
        case NUMBER_FORMULA:
          return Double.parseDouble( vContent );
        case BOOLEAN:
        case BOOLEAN_FORMULA:
          return vContent.equals( "1" );
        case DATE:
        case DATE_FORMULA:
          Double xlDate = Double.parseDouble( vContent );
          return DateUtil.getJavaDate( xlDate, DATE_TZ );
        case LABEL:
        case STRING_FORMULA:
        case EMPTY:
        default:
          return vContent;
      }
    } catch ( Exception e ) {
      return vContent;
    }
  }

  private void resetSheetReader() throws IOException, XMLStreamException, InvalidFormatException {
    sheetReader.close();
    sheetStream.close();
    sheetStream = xssfReader.getSheet( sheetId );
    XMLInputFactory factory = XMLParserFactoryProducer.createSecureXMLInputFactory();
    sheetReader = factory.createXMLStreamReader( sheetStream );
  }

  public void close() throws IOException, XMLStreamException {
    sheetReader.close();
    sheetStream.close();
  }
}
