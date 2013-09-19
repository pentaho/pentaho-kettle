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

package org.pentaho.di.trans.steps.jsoninput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar
 * @since 21-06-2010
 */
public class JsonInputData extends BaseStepData implements StepDataInterface 
{
	public Object[] previousRow;
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public int nr_repeats;
	
    public int nrInputFields;
    public int recordnr;
    public int nrrecords;
    public Object[] readrow;
    public int totalpreviousfields;
    
    
	/**
	 * The XML files to read
	 */
	public FileInputList files;

	public FileObject file;
	public int     filenr;
	
	public FileInputStream fr;
	public BufferedInputStream is;
    public String itemElement;
    public int itemCount;
    public int itemPosition;
    public long rownr;
    public int indexSourceField;
    
    RowMetaInterface      outputMeta;
    
	public String filename;
	public String shortFilename;
	public String path;	
	public String extension;	
	public boolean hidden;	
	public Date lastModificationDateTime;	
	public String uriName;	
	public String rootUriName;	
	public long size;
	
	public JsonReader jsonReader;	
	public List<NJSONArray> resultList;
	
	public String stringToParse;
	
	/**
	 * 
	 */
	public JsonInputData()
	{
		super();
		nr_repeats=0;
		previousRow=null;
		filenr = 0;
		
		fr=null;
		is=null;
		indexSourceField=-1;
		
		nrInputFields=-1;
		recordnr=0;
		nrrecords=0;

		readrow=null;
		totalpreviousfields=0;
	}

}
