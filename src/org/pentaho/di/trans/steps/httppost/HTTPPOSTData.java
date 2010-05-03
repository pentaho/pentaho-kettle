 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Samatar Hassan and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Samatar Hassan.
 * The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.httppost;

import org.apache.commons.httpclient.NameValuePair;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;



/**
 * @author Matt
 * @since 24-jan-2005
 *
 */
public class HTTPPOSTData extends BaseStepData implements StepDataInterface
{
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface inputRowMeta;
	public String realEncoding;
	public int header_parameters_nrs[];
	public int body_parameters_nrs[];
	public int query_parameters_nrs[];
	public int indexOfUrlField;
	public String realUrl;
	public NameValuePair[] headerParameters; 
	public NameValuePair[] bodyParameters; 
	public NameValuePair[] queryParameters;
	public boolean useHeaderParameters;
	public boolean contentTypeHeaderOverwrite;
	public boolean useBodyParameters;
	public boolean useQueryParameters;
	public int indexOfRequestEntity;
	
	public String realProxyHost;
	public int realProxyPort;
	public String realHttpLogin;
	public String realHttpPassword;

	/**
	 * 
	 */
	public HTTPPOSTData()
	{
		super();
		indexOfUrlField=-1;
		useHeaderParameters=false;
		contentTypeHeaderOverwrite=false;
		useBodyParameters=false;
		useQueryParameters=false;
		indexOfRequestEntity=-1;
		realEncoding=null;
		realProxyHost=null;
		realProxyPort=8080;
		realHttpLogin=null;
		realHttpPassword=null;
	}

}
