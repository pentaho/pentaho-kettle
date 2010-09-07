package org.pentaho.di.trans.steps.excelinput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.trans.steps.excelinput.jxl.XLSWorkbook;
import org.pentaho.di.trans.steps.excelinput.ods.OdfWorkbook;
import org.pentaho.di.trans.steps.excelinput.poi.PoiWorkbook;

public class WorkbookFactory {
  
  public static KWorkbook getWorkbook(SpreadSheetType type, String filename, String encoding) throws KettleException {
    switch(type) {
    case JXL: return new XLSWorkbook(filename, encoding);
    case POI: return new PoiWorkbook(filename, encoding); // encoding is not used, perhaps detected automatically?
    case ODS: return new OdfWorkbook(filename, encoding); // encoding is not used, perhaps detected automatically?
    default: throw new KettleException("Sorry, spreadsheet type "+type.getDescription()+" is not yet supported");
    }
    
  }
}
