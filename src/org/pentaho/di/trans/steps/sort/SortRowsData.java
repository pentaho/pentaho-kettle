 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.sort;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 */
public class SortRowsData extends BaseStepData implements StepDataInterface
{
	public List<FileObject> files;
	public List<Object[]>   buffer;
    public int              getBufferIndex;

	public List<InputStream> fis;
    public List<GZIPInputStream> gzis;
    public List<DataInputStream> dis;
	public List<Object[]> rowbuffer;
    public List<Integer> bufferSizes;

    // To store rows and file references
    public List<RowTempFile> tempRows;

	public int     fieldnrs[];      // the corresponding field numbers;
    public FileObject fil;
    public RowMetaInterface outputRowMeta;
	public int sortSize;
	public boolean compressFiles;
	public boolean[] convertKeysToNative;

	public Comparator<RowTempFile> comparator;
	
	public int freeCounter;
	public int freeMemoryPct;
	public int minSortSize;
	public int freeMemoryPctLimit;
	public int memoryReporting;
	
	
	/**
	 * 
	 */
	public SortRowsData()
	{
		super();
		
		files= new ArrayList<FileObject>();
		fis  = new ArrayList<InputStream>();
        gzis  = new ArrayList<GZIPInputStream>();
        dis = new ArrayList<DataInputStream>();
        bufferSizes = new ArrayList<Integer>();
	}

}
