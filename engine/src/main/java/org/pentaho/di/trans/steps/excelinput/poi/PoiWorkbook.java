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

package org.pentaho.di.trans.steps.excelinput.poi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.vfs.KettleVFS;

public class PoiWorkbook implements KWorkbook {

  private LogChannelInterface log;

  private Workbook workbook;
  private String filename;
  private String encoding;
  // for PDI-10251 we need direct access to streams
  private InputStream internalIS;
  private NPOIFSFileSystem npoifs;
  private OPCPackage opcpkg;

  public PoiWorkbook( String filename, String encoding ) throws KettleException {
    this.filename = filename;
    this.encoding = encoding;
    this.log = KettleLogStore.getLogChannelInterfaceFactory().create( this );
    try {
      FileObject fileObject = KettleVFS.getFileObject( filename );
      if ( fileObject instanceof LocalFile ) {
        // This supposedly shaves off a little bit of memory usage by allowing POI to randomly access data in the file
        //
        String localFilename = KettleVFS.getFilename( fileObject );
        File excelFile = new File( localFilename );
        try {
          npoifs = new NPOIFSFileSystem( excelFile );
          workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create( npoifs );
        } catch ( Exception ofe ) {
          try {
            opcpkg = OPCPackage.open( excelFile );
            workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create( opcpkg );
          } catch ( Exception ex ) {
            workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create( excelFile );
          }
        }
      } else {
        internalIS = KettleVFS.getInputStream( filename );
        workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create( internalIS );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public PoiWorkbook( InputStream inputStream, String encoding ) throws KettleException {
    this.encoding = encoding;

    try {
      workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create( inputStream );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void close() {
    try {
      if ( internalIS != null ) {
        internalIS.close();
      }
      if ( npoifs != null ) {
        npoifs.close();
      }
      if ( opcpkg != null ) {
        //We should not save change in xls because it is input step.
        opcpkg.revert();
      }
    } catch ( IOException ex ) {
      log.logError( "Could not close workbook", ex );
    }
  }

  @Override
  public KSheet getSheet( String sheetName ) {
    Sheet sheet = workbook.getSheet( sheetName );
    if ( sheet == null ) {
      return null;
    }
    return new PoiSheet( sheet );
  }

  public String[] getSheetNames() {
    int nrSheets = workbook.getNumberOfSheets();
    String[] names = new String[nrSheets];
    for ( int i = 0; i < nrSheets; i++ ) {
      names[i] = workbook.getSheetName( i );
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
    return workbook.getNumberOfSheets();
  }

  public KSheet getSheet( int sheetNr ) {
    Sheet sheet = workbook.getSheetAt( sheetNr );
    if ( sheet == null ) {
      return null;
    }
    return new PoiSheet( sheet );
  }

  public String getSheetName( int sheetNr ) {
    Sheet sheet = (Sheet) getSheet( sheetNr );
    if ( sheet == null ) {
      return null;
    }
    return sheet.getSheetName();
  }
}
