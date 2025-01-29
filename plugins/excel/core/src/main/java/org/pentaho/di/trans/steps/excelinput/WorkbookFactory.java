/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.excelinput;

import java.io.InputStream;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.steps.excelinput.jxl.XLSWorkbook;
import org.pentaho.di.trans.steps.excelinput.ods.OdfWorkbook;
import org.pentaho.di.trans.steps.excelinput.poi.PoiWorkbook;
import org.pentaho.di.trans.steps.excelinput.staxpoi.StaxPoiWorkbook;

public class WorkbookFactory {

  public static KWorkbook getWorkbook( Bowl bowl, SpreadSheetType type, String filename, String encoding )
    throws KettleException {
    return getWorkbook( bowl, type, filename, encoding, null );
  }

  public static KWorkbook getWorkbook( Bowl bowl, SpreadSheetType type, String filename, String encoding, String password )
    throws KettleException {
    switch ( type ) {
      case JXL:
        return new XLSWorkbook( bowl, filename, encoding );
      case POI:
        return new PoiWorkbook( bowl, filename, encoding, password ); // encoding is not used, perhaps detected automatically?
      case SAX_POI:
        return new StaxPoiWorkbook( filename, encoding );
      case ODS:
        return new OdfWorkbook( bowl, filename, encoding ); // encoding is not used, perhaps detected automatically?
      default:
        throw new KettleException( "Sorry, spreadsheet type " + type.getDescription() + " is not yet supported" );
    }

  }

  // Not Dead Code:  Used by pdi-google-docs-plugin
  public static KWorkbook getWorkbook( SpreadSheetType type, InputStream inputStream, String encoding )
    throws KettleException {
    switch ( type ) {
      case JXL:
        return new XLSWorkbook( inputStream, encoding );
      case POI:
        return new PoiWorkbook( inputStream, encoding ); // encoding is not used, perhaps detected automatically?
      case SAX_POI:
        return new StaxPoiWorkbook( inputStream, encoding );
      case ODS:
        return new OdfWorkbook( inputStream, encoding ); // encoding is not used, perhaps detected automatically?
      default:
        throw new KettleException( "Sorry, spreadsheet type " + type.getDescription() + " is not yet supported" );
    }

  }
}
