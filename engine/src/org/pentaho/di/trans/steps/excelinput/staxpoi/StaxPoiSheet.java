/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KSheet;

public class StaxPoiSheet implements KSheet {

  private String sheetName;
  private InputStream sheetStream;
  private XMLStreamReader sheetReader;

  // hold the pointer to the current row so that access to the next row in the stream is quick and easy
  private int currentRow;
  private List<String> headerRow;
  private int numRows;
  private int numCols;

  // variable to hold the shared strings table
  private SharedStringsTable sst;

  public StaxPoiSheet( XSSFReader reader, String sheetName, String sheetID ) {
    this.sheetName = sheetName;
    try {
      sst = reader.getSharedStringsTable();
      sheetStream = reader.getSheet( sheetID );
      XMLInputFactory factory = XMLInputFactory.newInstance();
      sheetReader = factory.createXMLStreamReader( sheetStream );
      headerRow = new ArrayList<String>();
      while ( sheetReader.hasNext() ) {
        int event = sheetReader.next();
        if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "dimension" ) ) {
          String dim = sheetReader.getAttributeValue( null, "ref" ).split( ":" )[1];
          numRows = StaxUtil.extractRowNumber( dim );
          numCols = StaxUtil.extractColumnNumber( dim );
        }
        if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "row" ) ) {
          currentRow = Integer.parseInt( sheetReader.getAttributeValue( null, "r" ) );
          // calculate the number of columns in the header row
          while ( sheetReader.hasNext() ) {
            event = sheetReader.next();
            if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( "row" ) ) {
              // if the row has ended, break the inner while loop
              break;
            }
            if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "c" ) ) {
              String attributeValue = sheetReader.getAttributeValue( null, "t" );
              if ( attributeValue != null && attributeValue.equals( "s" ) ) {
                // only if the type of the cell is string, we continue
                while ( sheetReader.hasNext() ) {
                  event = sheetReader.next();
                  if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "v" ) ) {
                    int idx = Integer.parseInt( sheetReader.getElementText() );
                    String content = new XSSFRichTextString( sst.getEntryAt( idx ) ).toString();
                    headerRow.add( content );
                    break;
                  }
                }
              } else {
                break;
              }
            }
          }
          // we have parsed the header row
          break;
        }
      }
      // numCols = headerRow.size();
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new RuntimeException( e.getMessage() );
    }
  }

  @Override
  public KCell[] getRow( int rownr ) {

    // convert 0 based index to 1 based
    rownr += 1;
    try {
      while ( sheetReader.hasNext() ) {
        int event = sheetReader.next();
        if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "row" ) ) {
          String rowIndicator = sheetReader.getAttributeValue( null, "r" );
          currentRow = Integer.parseInt( rowIndicator );
          if ( currentRow < rownr ) {
            continue;
          }

          KCell[] cells = new StaxPoiCell[numCols];
          for ( int i = 0; i < numCols; i++ ) {
            // go to the "c" <cell> tag
            while ( sheetReader.hasNext() ) {
              if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "c" ) ) {
                break;
              }
              event = sheetReader.next();
            }
            String cellLocation = sheetReader.getAttributeValue( null, "r" );
            int columnIndex = StaxUtil.extractColumnNumber( cellLocation ) - 1;
            String cellType = sheetReader.getAttributeValue( null, "t" );

            // go to the "v" <value> tag
            while ( sheetReader.hasNext() ) {
              event = sheetReader.next();
              if ( event == XMLStreamConstants.START_ELEMENT && sheetReader.getLocalName().equals( "v" ) ) {
                break;
              }
              if ( event == XMLStreamConstants.END_ELEMENT && sheetReader.getLocalName().equals( "c" ) ) {
                // we have encountered an empty/incomplete row, so we set the max rows to current row number
                // TODO: accept empty row is option is check and go till the end of the xml (need to detect the end)
                numRows = currentRow;
                return new KCell[] {};
              }
            }
            String content = null;
            if ( cellType != null && cellType.equals( "s" ) ) {
              int idx = Integer.parseInt( sheetReader.getElementText() );
              content = new XSSFRichTextString( sst.getEntryAt( idx ) ).toString();
            } else {
              content = sheetReader.getElementText();
            }
            cells[columnIndex] = new StaxPoiCell( content, currentRow );
          }
          return cells;
        }
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
    numRows = currentRow;
    return new KCell[] {};
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
    if ( rownr == 0 && colnr < numCols ) {
      // only possible to return header
      return new StaxPoiCell( headerRow.get( colnr ), rownr );
    }
    return null;
    // throw new RuntimeException("getCell(col, row) is not supported yet");
  }

  public void close() throws IOException, XMLStreamException {
    sheetStream.close();
    sheetReader.close();
  }
}
