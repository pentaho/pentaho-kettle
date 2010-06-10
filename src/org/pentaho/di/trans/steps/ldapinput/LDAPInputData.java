/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
 

package org.pentaho.di.trans.steps.ldapinput;

import java.util.HashSet;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;


/**
 * @author Samatar Hassan
 * @since 21-09-2007
 */
public class LDAPInputData extends BaseStepData implements StepDataInterface 
{
	public String thisline;
	public RowMetaInterface outputRowMeta;
	public RowMetaInterface convertRowMeta;
	public Object[] previousRow;
	public int    nr_repeats;

    public long   rownr;
    
    public InitialLdapContext ctx;
    public NamingEnumeration<SearchResult> results;
    public String multi_valuedFieldSeparator;

    public int nrfields;
    
    public String searchbase;
    public String filter;
    public SearchControls controls;
    public byte[] cookie;
    public boolean pagingSet;
    public int pageSize;
    
    public HashSet<String> attributesBinary;
    
	public LDAPInputData()
	{
		super();
		previousRow = null;
		thisline=null;

		nr_repeats=0;
		previousRow=null;
		ctx=null;
		multi_valuedFieldSeparator=null;
		results=null;
		nrfields=0;
		searchbase=null;
		filter=null;
		controls=null;
		cookie=null;
		pagingSet=false;
		pageSize=-1;
	}
	
   
}