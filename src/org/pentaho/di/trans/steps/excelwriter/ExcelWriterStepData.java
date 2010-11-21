//
// Excel Writer plugin for Pentaho PDI a.k.a. Kettle
// 
// Copyright (C) 2010 Slawomir Chodnicki
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

package org.pentaho.di.trans.steps.excelwriter;

import org.apache.commons.vfs.FileObject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class ExcelWriterStepData extends BaseStepData implements StepDataInterface {

	public RowMetaInterface outputRowMeta;
	public int splitnr;
	public int datalines;
	public String realSheetname;
	public String realTemplateSheetName;
	public boolean firstFileOpened;
	public FileObject file;
	public int posX;
	public int posY;
	public Sheet sheet;
	public Workbook wb;
	public int[] fieldnrs;
	public RowMetaInterface inputRowMeta;
	public int[] commentfieldnrs;
	public int[] commentauthorfieldnrs;
	public int startingCol = 0;
	public int startingRow = 0;
	public boolean shiftExistingCells = false;
	public boolean createNewFile = false;
	public boolean createNewSheet = false;
	public String realTemplateFileName;
	public String realStartingCell;
	public String realPassword;
	public String realProtectedBy;
	public int[] linkfieldnrs;
	private CellStyle[] cellStyleCache;  
	private CellStyle[] cellLinkStyleCache;
		
    public ExcelWriterStepData(){
		super();
	}
    
    public void clearStyleCache(int nrFields){
    	cellStyleCache = new CellStyle[nrFields];	
    	cellLinkStyleCache = new CellStyle[nrFields];
    }
    
    public void cacheStyle(int fieldNr, CellStyle style){
    	cellStyleCache[fieldNr] = style;
    }
    
    public void cacheLinkStyle(int fieldNr, CellStyle style){
    	cellLinkStyleCache[fieldNr] = style;
    }
    
    public CellStyle getCachedStyle(int fieldNr){
    	return cellStyleCache[fieldNr];
    }

    public CellStyle getCachedLinkStyle(int fieldNr){
    	return cellLinkStyleCache[fieldNr];
    }

}
	
