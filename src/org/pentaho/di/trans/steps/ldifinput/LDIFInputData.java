/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/


package org.pentaho.di.trans.steps.ldifinput;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipInputStream;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFRecord;

import org.apache.commons.vfs.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 24-May-2005
 */
public class LDIFInputData extends BaseStepData implements StepDataInterface
{
	public String thisline, nextline, lastline;
	//public Row    previousRow;
	public int    nr_repeats;
	
	public NumberFormat         nf;
	public DecimalFormat        df;
	public DecimalFormatSymbols dfs;
	public SimpleDateFormat     daf;
	public DateFormatSymbols    dafs;
	
	public FileInputList files;
	public boolean    last_file;
	public FileObject file;
	public int        filenr;
	
	public FileInputStream     fr;
	public ZipInputStream      zi;
	public BufferedInputStream is;
    public Document            document;
    public Node                section;
    public String              itemElement;
    public int                 itemCount;
    public int                 itemPosition;
    public long                rownr;
    public RowMetaInterface outputRowMeta;
	public Object[] previousRow;
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface convertRowMeta;
	public int nrInputFields;
	public LDAPAttribute[] attributes_LDIF;
	public LDIFRecord recordLDIF;
	public LDIF InputLDIF;
	public String multiValueSeparator;
    public int totalpreviousfields;
    public Object[] readrow;    
    public int indexOfFilenameField;
    
    public String filename;
	public String shortFilename;
	public String path;	
	public String extension;	
	public boolean hidden;	
	public Date lastModificationDateTime;	
	public String uriName;	
	public String rootUriName;	
	public long size;

	/**
	 * 
	 */
	public LDIFInputData()
	{
		super();
		nrInputFields=-1;
		thisline=null;
		nextline=null;
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();
		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();

		nr_repeats=0;
		filenr = 0;
		
		fr=null;
		zi=null;
		is=null;
		InputLDIF=null;
		recordLDIF=null;
		multiValueSeparator=",";
		totalpreviousfields=0;
		readrow=null;
		indexOfFilenameField=-1;
	}
}