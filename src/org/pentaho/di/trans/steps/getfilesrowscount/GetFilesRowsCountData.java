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
 

package org.pentaho.di.trans.steps.getfilesrowscount;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.commons.vfs.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 06-Sept-2007
 */
public class GetFilesRowsCountData extends BaseStepData implements StepDataInterface 
{
	public String thisline;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public Object[] previousRow;

	public List<FileObject>     files;
	public boolean              last_file;
	public FileObject           file;
	public long                 filenr;
	public long					filesnr;
	
	public InputStream fr;
	public InputStreamReader isr;
	public BufferedInputStream is;
    public Document            document;
    public Node                section;
    public String              itemElement;
    public int                 itemCount;
    public int                 itemPosition;
    public long                rownr;
    public int fileFormatType;
    public StringBuffer lineStringBuffer;

	/**
	 * 
	 */
	public GetFilesRowsCountData()
	{
		super();
		previousRow = null;
		thisline=null;
		previousRow=null;
		filenr = 0;
		
		fr=null;
		is=null;
		lineStringBuffer = new StringBuffer(256);
	}
}