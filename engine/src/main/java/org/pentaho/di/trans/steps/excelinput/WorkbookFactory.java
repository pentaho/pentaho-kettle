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

package org.pentaho.di.trans.steps.excelinput;

import java.io.InputStream;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.steps.excelinput.jxl.XLSWorkbook;
import org.pentaho.di.trans.steps.excelinput.ods.OdfWorkbook;
import org.pentaho.di.trans.steps.excelinput.poi.PoiWorkbook;
import org.pentaho.di.trans.steps.excelinput.staxpoi.StaxPoiWorkbook;

public class WorkbookFactory {

  public static KWorkbook getWorkbook( SpreadSheetType type, String filename, String encoding ) throws KettleException {
    switch ( type ) {
      case JXL:
        return new XLSWorkbook( filename, encoding );
      case POI:
        return new PoiWorkbook( filename, encoding ); // encoding is not used, perhaps detected automatically?
      case SAX_POI:
        return new StaxPoiWorkbook( filename, encoding );
      case ODS:
        return new OdfWorkbook( filename, encoding ); // encoding is not used, perhaps detected automatically?
      default:
        throw new KettleException( "Sorry, spreadsheet type " + type.getDescription() + " is not yet supported" );
    }

  }

  public static KWorkbook getWorkbook( SpreadSheetType type, InputStream inputStream, String encoding ) throws KettleException {
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
