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


package org.pentaho.di.trans.steps.rssoutput;

import  java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;
import org.dom4j.Document;
import org.dom4j.Element;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;


/**
 * @author Samatar
 * @since 4-Nov-2007
 */
public class RssOutputData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface inputRowMeta;
	public RowMetaInterface outputRowMeta;
	List<SyndEntry> entries = new ArrayList<SyndEntry>();
	SyndFeed feed;
	private static final String DATE_FORMAT = "yyyy-MM-dd H:mm:ss";
	DateFormat dateParser;
	public int indexOfFieldchanneltitle;
	public int indexOfFieldchanneldescription;
	public int indexOfFieldchannellink;
	public int indexOfFielditemtitle;
	public int indexOfFielditemdescription;
	public int indexOfFieldchannelpubdate;
	public int indexOfFieldchannellanguage;
	public int indexOfFieldchannelcopyright;
	public int indexOfFieldchannelauthor;
	
	
	public int indexOfFieldchannelimagetitle;
	public int indexOfFieldchannelimagelink;
	public int indexOfFieldchannelimageurl;
	public int indexOfFieldchannelimagedescription;
	public int indexOfFielditemlink;
	public int indexOfFielditempubdate;
	public int indexOfFielditemauthor;
	public int indexOfFielditempointx;
	public int indexOfFielditempointy;
	
	public String channeltitlevalue;
	public String channeldescriptionvalue;
	public String channellinkvalue;
    
	public Date  channelpubdatevalue;
	public String  channellanguagevalue;
	public String  channelcopyrightvalue;
	public String  channelauthorvalue;
	public String  channelimagetitlevalue;
	public String  channelimagelinkvalue;
	public String  channelimageurlvalue;
	public String  channelimagedescriptionvalue;
	
	public String filename;
	public int indexOfFieldfilename;
	
	public int    customchannels[]; 
	public int    customitems[]; 
	public int    valuecustomchannels[];   
	public int    valuecustomItems[];  
	public Document document;
	public Element rssElement;
	public Element channel;
	
	public Element itemtag;
    
	public RssOutputData()
	{
		super();
		feed =null;
		dateParser = new SimpleDateFormat(DATE_FORMAT);
		indexOfFieldchanneltitle=-1;
		indexOfFieldchanneldescription=-1;
		indexOfFieldchannellink=-1;
		indexOfFielditemtitle=-1;
		indexOfFieldchannelpubdate=-1;
		indexOfFieldchannellanguage=-1;
		indexOfFieldchannelcopyright=-1;
		indexOfFieldchannelauthor=-1;
		
		indexOfFieldchannelimagetitle=-1;
		indexOfFieldchannelimagelink=-1;
		indexOfFieldchannelimageurl=-1;
		indexOfFieldchannelimagedescription=-1;
		
		indexOfFielditemlink=-1;
		indexOfFielditemtitle=-1;
		indexOfFielditempubdate=-1;
		indexOfFielditemdescription=-1;
		indexOfFielditemauthor=-1;
		indexOfFielditempointx=-1;
		indexOfFielditempointy=-1;
		
		// Channel values
		channeltitlevalue=null;
		channeldescriptionvalue=null;
		channellinkvalue=null;
		
		channelpubdatevalue=null;;
		channellanguagevalue=null;
		channelcopyrightvalue=null;
		channelauthorvalue=null;
		channelimagetitlevalue=null;
		channelimagelinkvalue=null;
	    channelimageurlvalue=null;
		channelimagedescriptionvalue=null;
		
		filename=null;
		indexOfFieldfilename=-1;
		
		document=null;
		rssElement=null;
		channel=null;
		itemtag=null;

	}

}
