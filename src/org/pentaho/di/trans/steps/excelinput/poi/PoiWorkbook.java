/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.vfs.KettleVFS;

public class PoiWorkbook implements KWorkbook {

  private Workbook workbook;
  private String filename;
  private String encoding;
  
  public PoiWorkbook(String filename, String encoding) throws KettleException {
    this.filename = filename;
    this.encoding = encoding;
    
    try {
      workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(KettleVFS.getInputStream(filename));
    } catch(Exception e) {
      throw new KettleException(e);
    }
  }
  
  public void close() {
    // not needed here
  }
  
  @Override
  public KSheet getSheet(String sheetName) {
    Sheet sheet = workbook.getSheet(sheetName);
    if (sheet==null) return null;
    return new PoiSheet(sheet);
  }
  
  public String[] getSheetNames() {
    int nrSheets = workbook.getNumberOfSheets();
    String[] names = new String[nrSheets];
    for (int i=0;i<nrSheets;i++) {
      names[i] = workbook.getSheetName(i);
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
  
  public KSheet getSheet(int sheetNr) {
    Sheet sheet = workbook.getSheetAt(sheetNr);
    if (sheet==null) return null;
    return new PoiSheet(sheet);
  }
}
