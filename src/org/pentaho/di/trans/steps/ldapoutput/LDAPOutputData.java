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
 

package org.pentaho.di.trans.steps.ldapoutput;

import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.ldapinput.LDAPConnection;


/**
 * @author Samatar Hassan
 * @since 21-09-2007
 */
public class LDAPOutputData extends BaseStepData implements StepDataInterface 
{
    public LDAPConnection connection;
    public int indexOfDNField;
    public int[] fieldStream;
    public String[] fieldsAttribute;
    public int nrfields;
    public int nrfieldsToUpdate;
    public String separator;
    public String[] attributes;
    public String[] attributesToUpdate;
    
    public int[] fieldStreamToUpdate;
    public String[] fieldsAttributeToUpdate;
    
	public LDAPOutputData()
	{
		super();
		this.indexOfDNField=-1;
		this.nrfields=0;
		this.separator=null;
		this.fieldStreamToUpdate=null;
		this.fieldsAttributeToUpdate=null;
		this.attributesToUpdate=null;
		this.nrfieldsToUpdate=0;
	}
	
   
}