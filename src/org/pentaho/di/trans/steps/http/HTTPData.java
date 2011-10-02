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
 
package org.pentaho.di.trans.steps.http;

import org.apache.commons.httpclient.NameValuePair;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * @author Matt
 * @since 24-jan-2005
 */
public class HTTPData extends BaseStepData implements StepDataInterface
{
	public int argnrs[];
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface inputRowMeta;
	public int indexOfUrlField;
	public String realUrl;
	public String realProxyHost;
	public int realProxyPort;
	public String realHttpLogin;
	public String realHttpPassword;
   public int header_parameters_nrs[];
   public boolean useHeaderParameters;
   public NameValuePair[] headerParameters; 
	
	public int realSocketTimeout;
	public int realConnectionTimeout;

	/**
	 * Default constructor. 
	 */
	public HTTPData()
	{
		super();
		indexOfUrlField=-1;
		realProxyHost=null;
		realProxyPort=8080;
		realHttpLogin=null;
		realHttpPassword=null;
	}
}