/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.ssh;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

import com.trilead.ssh2.Connection;


/**
 * @author Samatar
 * @since 03-Juin-2008
 *
 */
public class SSHData extends BaseStepData implements StepDataInterface
{
	public int indexOfCommand;
	public Connection conn;
	public boolean wroteOneRow;
	public String commands;
	public int nrInputFields;
	public int nrOutputFields;
	
	// Output fields
	public String stdOutField;
	public String stdTypeField;
	
    public RowMetaInterface outputRowMeta;

	/**
	 * 
	 */
	public SSHData()
	{
		super();
		this.indexOfCommand=-1;
		this.conn=null;
		this.wroteOneRow=false;
		this.commands=null;
		this.stdOutField=null;
		this.stdTypeField=null;
	}

}
