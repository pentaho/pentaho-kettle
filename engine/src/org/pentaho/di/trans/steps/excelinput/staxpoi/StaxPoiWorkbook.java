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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;

public class StaxPoiWorkbook implements KWorkbook {

  private XSSFReader reader;

  // maintain the mapping of the sheet name to its ID
  private Map<String, String> sheetNameIDMap;

  // mapping of the sheet object with its ID/Name
  private Map<String, StaxPoiSheet> openSheetsMap;

  public StaxPoiWorkbook() {
    openSheetsMap = new HashMap<String, StaxPoiSheet>();
  }

  public StaxPoiWorkbook( String filename, String encoding ) throws KettleException {
    this();
    try {
      OPCPackage pkg = OPCPackage.open( filename );
      openFile( pkg, encoding );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }

  }

  public StaxPoiWorkbook( InputStream inputStream, String encoding ) throws KettleException {
    this();
    try {
      OPCPackage pkg = OPCPackage.open( inputStream );
      openFile( pkg, encoding );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private void openFile( OPCPackage pkg, String encoding ) throws KettleException {
    try {
      reader = new XSSFReader( pkg );
      sheetNameIDMap = new HashMap<String, String>();
      List<String> sheetList = new ArrayList<String>();
      InputStream workbookData;
      workbookData = reader.getWorkbookData();
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader workbookReader = factory.createXMLStreamReader( workbookData );
      while ( workbookReader.hasNext() ) {
        if ( workbookReader.next() == XMLStreamConstants.START_ELEMENT
          && workbookReader.getLocalName().equals( "sheet" ) ) {
          String sheetName = workbookReader.getAttributeValue( null, "name" );
          String sheetID =
            workbookReader.getAttributeValue(
              "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id" );
          sheetList.add( sheetName );
          sheetNameIDMap.put( sheetName, sheetID );
        }
      }
      workbookData.close();
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @Override
  /**
   * return the same sheet if it already is created otherwise instantiate a new one
   */
  public KSheet getSheet( String sheetName ) {
    String sheetID = sheetNameIDMap.get( sheetName );
    StaxPoiSheet sheet = openSheetsMap.get( sheetID );
    if ( openSheetsMap.get( sheetID ) == null ) {
      sheet = new StaxPoiSheet( reader, sheetName, sheetID );
      openSheetsMap.put( sheetID, sheet );
    }
    return sheet;
  }

  @Override
  public String[] getSheetNames() {
    String[] sheets = new String[sheetNameIDMap.size()];
    return sheetNameIDMap.keySet().toArray( sheets );
  }

  @Override
  public void close() {
    // close all the sheets
    for ( StaxPoiSheet sheet : openSheetsMap.values() ) {
      try {
        sheet.close();
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public int getNumberOfSheets() {
    return sheetNameIDMap.size();
  }

  @Override
  public KSheet getSheet( int sheetNr ) {
    for ( Map.Entry<String, String> entry : sheetNameIDMap.entrySet() ) {
      String sheetName = entry.getKey();
      String sheetID = entry.getValue();
      if ( sheetID.endsWith( Integer.toString( sheetNr + 1 ) ) ) {
        return getSheet( sheetName );
      }
    }
    return null;
  }

  @Override
  public String getSheetName( int sheetNr ) {
    for ( Map.Entry<String, String> entry : sheetNameIDMap.entrySet() ) {
      String sheetName = entry.getKey();
      String sheetID = entry.getValue();
      if ( sheetID.endsWith( Integer.toString( sheetNr ) ) ) {
        return sheetName;
      }
    }
    return null;
  }

}
