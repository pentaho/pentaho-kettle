/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
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


package org.pentaho.di.trans.steps.rssinput;

import it.sauronsoftware.feed4j.bean.Feed;

import java.util.Date;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Samatar Hassan
 * @since 13-10-2007
 */
public class RssInputData extends BaseStepData implements StepDataInterface 
{
	public String thisline, nextline, lastline;
	public Object[] previousRow;
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public int    nr_repeats;
	
	public NumberFormat         nf;
	public DecimalFormat        df;
	public DecimalFormatSymbols dfs;
	public SimpleDateFormat     daf;
	public DateFormatSymbols    dafs;
	public Date readfromdatevalide;

    public long                rownr;
    public int                 urlnr;
    public int                 urlsize;
    public String			   currenturl;
    public boolean			   last_url;
    public int				   itemssize;
    public int				   itemsnr;
    
    public  String 			   PLUGIN_VERSION;
    
    public int 				   indexOfUrlField;
    public int 				   totalpreviousfields;
    public int 				   nrInputFields;
    public Feed 			   feed;
    public Object[]		       readrow;

	/**
	 * 
	 */
	public RssInputData()
	{
		super();

		thisline=null;
		nextline=null;
		nf = NumberFormat.getInstance();
		df = (DecimalFormat)nf;
		dfs=new DecimalFormatSymbols();
		daf = new SimpleDateFormat();
		dafs= new DateFormatSymbols();

		nr_repeats=0;
		previousRow=null;
		readfromdatevalide= new Date();
		
		PLUGIN_VERSION="1.1";
		
		indexOfUrlField=-1;
		totalpreviousfields=0;
		urlsize=0;
		currenturl=null;
		last_url=false;
		itemssize=0;
		itemsnr=0;
		feed=null;
		readrow=null;
	}
}