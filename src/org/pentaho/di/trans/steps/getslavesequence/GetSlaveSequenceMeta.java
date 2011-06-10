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
 
package org.pentaho.di.trans.steps.getslavesequence;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Meta data for the Add Sequence step.
 * 
 * Created on 13-may-2003
 */
public class GetSlaveSequenceMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = GetSlaveSequenceMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String       valuename;	
	private String       slaveServerName;
  private String       sequenceName;
    
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode, databases);
	}
	
	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
		try
		{
			valuename    = XMLHandler.getTagValue(stepnode, "valuename"); //$NON-NLS-1$
      slaveServerName = XMLHandler.getTagValue(stepnode, "slave"); //$NON-NLS-1$
			sequenceName = XMLHandler.getTagValue(stepnode, "seqname"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "GetSequenceMeta.Exception.ErrorLoadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault() {
		valuename = "id"; //$NON-NLS-1$
    slaveServerName = "slave server name"; //$NON-NLS-1$
    sequenceName = "Slave Sequence Name -- To be configured"; //$NON-NLS-1$
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		ValueMetaInterface v=new ValueMeta(valuename, ValueMetaInterface.TYPE_INTEGER);
		v.setOrigin(name);
		row.addValueMeta( v );
	}

	public String getXML()
	{
    StringBuffer retval = new StringBuffer(300);
		
		retval.append("      ").append(XMLHandler.addTagValue("valuename", valuename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("slave", slaveServerName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("seqname", sequenceName)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			valuename       =   rep.getStepAttributeString (id_step, "valuename"); //$NON-NLS-1$
      slaveServerName =   rep.getStepAttributeString (id_step, "slave"); //$NON-NLS-1$
      sequenceName    =   rep.getStepAttributeString (id_step, "seqname"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "GetSequenceMeta.Exception.UnableToReadStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "valuename", valuename); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "slave", slaveServerName); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "seqname", sequenceName); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "GetSequenceMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}


	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GetSequenceMeta.CheckResult.StepIsReceving.Title"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GetSequenceMeta.CheckResult.NoInputReceived.Title"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new GetSlaveSequence(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new GetSlaveSequenceData();
	}

  /**
   * @return the valuename
   */
  public String getValuename() {
    return valuename;
  }

  /**
   * @param valuename the valuename to set
   */
  public void setValuename(String valuename) {
    this.valuename = valuename;
  }

  /**
   * @return the slaveServerName
   */
  public String getSlaveServerName() {
    return slaveServerName;
  }

  /**
   * @param slaveServerName the slaveServerName to set
   */
  public void setSlaveServerName(String slaveServerName) {
    this.slaveServerName = slaveServerName;
  }

  /**
   * @return the sequenceName
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * @param sequenceName the sequenceName to set
   */
  public void setSequenceName(String sequenceName) {
    this.sequenceName = sequenceName;
  }

	
}