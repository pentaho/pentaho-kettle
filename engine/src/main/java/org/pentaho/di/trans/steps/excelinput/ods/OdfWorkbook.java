/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.excelinput.ods;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.odfdom.doc.OdfDocument;
import org.odftoolkit.odfdom.doc.OdfSpreadsheetDocument;
import org.odftoolkit.odfdom.doc.table.OdfTable;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.vfs.KettleVFS;

public class OdfWorkbook implements KWorkbook {

  private String filename;
  private String encoding;
  private OdfDocument document;
  private Map<String, OdfSheet> openSheetsMap = new HashMap<String, OdfSheet>();

  public OdfWorkbook( String filename, String encoding ) throws KettleException {
    this.filename = filename;
    this.encoding = encoding;

    try {
      document = OdfSpreadsheetDocument.loadDocument( KettleVFS.getInputStream( filename ) );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public OdfWorkbook( InputStream inputStream, String encoding ) throws KettleException {
    this.encoding = encoding;

    try {
      document = OdfSpreadsheetDocument.loadDocument( inputStream );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void close() {
    if ( document != null ) {
      document.close();
    }
  }

  @Override
  public KSheet getSheet( String sheetName ) {
    OdfSheet sheet = openSheetsMap.get( sheetName );
    if ( sheet == null ) {
      OdfTable table = document.getTableByName( sheetName );
      if ( table == null ) {
        return null;
      } else {
        sheet = new OdfSheet( table );
        openSheetsMap.put( sheetName, sheet );
      }
    }
    return sheet;
  }

  public String[] getSheetNames() {
    List<OdfTable> list = document.getTableList();
    int nrSheets = list.size();
    String[] names = new String[nrSheets];
    for ( int i = 0; i < nrSheets; i++ ) {
      names[i] = list.get( i ).getTableName();
    }
    return names;
  }

  public String getFilename() {
    return filename;
  }

  public String getEncoding() {
    return encoding;
  }

  public int getNumberOfSheets() {
    return document.getTableList().size();
  }

  public KSheet getSheet( int sheetNr ) {
    OdfTable table = document.getTableList().get( sheetNr );
    if ( table == null ) {
      return null;
    }
    return new OdfSheet( table );
  }

  public String getSheetName( int sheetNr ) {
    OdfTable table = document.getTableList().get( sheetNr );
    if ( table == null ) {
      return null;
    }
    return table.getTableName();
  }
}
