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
 
package org.pentaho.di.trans.steps.blockingstep;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


public class BlockingStepData extends BaseStepData implements StepDataInterface
{
	public List<FileObject>        files;
	public List<Object[]>          buffer;
	public List<InputStream>       fis;
	public List<GZIPInputStream>   gzis;
	public List<DataInputStream>   dis;
	public List<Object[]>          rowbuffer;
	
    public RowMetaInterface outputRowMeta;

	public int        fieldnrs[];    // the corresponding field numbers;
    public FileObject fil;

    public BlockingStepData()
    {
        super();
        		
		buffer    = new ArrayList<Object[]>(BlockingStepMeta.CACHE_SIZE);
		files     = new ArrayList<FileObject>();
		fis       = new ArrayList<InputStream>();
		dis       = new ArrayList<DataInputStream>();
		gzis      = new ArrayList<GZIPInputStream>();
		rowbuffer = new ArrayList<Object[]>();
    }
}