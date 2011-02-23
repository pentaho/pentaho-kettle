package org.pentaho.di.trans.steps.excelinput.jxl;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.vfs.KettleVFS;

public class XLSWorkbook implements KWorkbook {

  private Workbook workbook;
  private String filename;
  private String encoding;
  
  public XLSWorkbook(String filename, String encoding) throws KettleException {
    this.filename = filename;
    this.encoding = encoding;
    
    WorkbookSettings ws = new WorkbookSettings();
    if (!Const.isEmpty(encoding))
    {
        ws.setEncoding(encoding);
    }
    try {
      workbook = Workbook.getWorkbook(KettleVFS.getInputStream(filename), ws);
    } catch(Exception e) {
      throw new KettleException(e);
    }
  }
  
  public void close() {
    workbook.close();
  }
  
  public KSheet getSheet(String sheetName) {
    Sheet sheet = workbook.getSheet(sheetName);
    if (sheet==null) return null;
    return new XLSSheet(sheet);
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
  
  public KSheet getSheet(int sheetNr) {
    Sheet sheet = workbook.getSheet(sheetNr);
    if (sheet==null) return null;
    return new XLSSheet(sheet);
  }
}
