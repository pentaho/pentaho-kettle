/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.excelinput.jxl;

import java.io.IOException;
import java.io.InputStream;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

public class XLSWorkbook implements KWorkbook {

  private LogChannelInterface log;

  private Workbook workbook;
  private String filename;
  private String encoding;
  private InputStream inputStream;

  public XLSWorkbook( String filename, String encoding ) throws KettleException {
    this.filename = filename;
    this.encoding = encoding;

    WorkbookSettings ws = new WorkbookSettings();
    if ( !Utils.isEmpty( encoding ) ) {
      ws.setEncoding( encoding );
    }
    try {
      inputStream = KettleVFS.getInputStream( filename );
      workbook = Workbook.getWorkbook( inputStream, ws );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public XLSWorkbook( InputStream inputStream, String encoding ) throws KettleException {
    this.encoding = encoding;

    WorkbookSettings ws = new WorkbookSettings();
    if ( !Utils.isEmpty( encoding ) ) {
      ws.setEncoding( encoding );
    }
    try {
      workbook = Workbook.getWorkbook( inputStream, ws );
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public void close() {
    if ( workbook != null ) {
      workbook.close();
      try {
        inputStream.close();
      } catch ( IOException e ) {
        log.logError( "Could not close workbook", e );
      }
    }
  }

  @Override
  public KSheet getSheet( String sheetName ) {
    Sheet sheet = workbook.getSheet( sheetName );
    if ( sheet == null ) {
      return null;
    }
    return new XLSSheet( sheet );
  }

  public String[] getSheetNames() {
    return workbook.getSheetNames();
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
    Sheet sheet = workbook.getSheet( sheetNr );
    if ( sheet == null ) {
      return null;
    }
    return new XLSSheet( sheet );
  }

  public String getSheetName( int sheetNr ) {
    Sheet sheet = workbook.getSheet( sheetNr );
    if ( sheet == null ) {
      return null;
    }
    return sheet.getName();
  }
}
