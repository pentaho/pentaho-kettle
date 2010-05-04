/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.trans.steps.infobrightoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.w3c.dom.Node;

import com.infobright.etl.model.DataFormat;


/**
 * Metadata for the Infobright loader.
 *
 * @author geoffrey.falk@infobright.com
 */
public class InfobrightLoaderMeta extends TableOutputMeta implements StepMetaInterface {

  private DataFormat dataFormat;
  private boolean rejectErrors = false;
  
  /**
   * Default constructor.
   */
  public InfobrightLoaderMeta()
  {
    super();
    setIgnoreErrors(false);
    setTruncateTable(false);
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStep(org.pentaho.di.trans.step.StepMeta, org.pentaho.di.trans.step.StepDataInterface, int, org.pentaho.di.trans.TransMeta, org.pentaho.di.trans.Trans)
   */
  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
  {
    InfobrightLoader loader = new InfobrightLoader(stepMeta, stepDataInterface, cnr, tr, trans);
    return loader;
  }
  
  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.StepMetaInterface#getStepData()
   */
  public StepDataInterface getStepData()
  {
    return new InfobrightLoaderData();
  }

  /** {@inheritDoc}
   * @see org.pentaho.di.trans.step.BaseStepMeta#clone()
   */
  public Object clone()
  {
    InfobrightLoaderMeta retval = (InfobrightLoaderMeta) super.clone();
    return retval;
  }

  public String getInfobrightProductType() {
    return dataFormat.getDisplayText();
  }

  public void setDataFormat(DataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }
  
  public void setDefault() {
    this.dataFormat = DataFormat.TXT_VARIABLE; // default for ICE
    // this.dataFormat = DataFormat.BINARY; // default for IEE
  }

  @Override
  public String getXML() {
    String ret = super.getXML();
    return ret + new String("    "+XMLHandler.addTagValue("data_format", dataFormat.toString()));
  }

  //@SuppressWarnings("unchecked")
  @Override
  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleXMLException {
    super.loadXML(stepnode, databases, counters);
    dataFormat = Enum.valueOf(DataFormat.class, XMLHandler.getTagValue(stepnode, "data_format"));
  }

  @Override
  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
	  super.readRep(rep, id_step, databases, counters);
	  dataFormat = Enum.valueOf(DataFormat.class, rep.getStepAttributeString(id_step, "data_format"));
  }

  @Override
  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
	  super.saveRep(rep, id_transformation, id_step);
	  rep.saveStepAttribute(id_transformation, id_step, "data_format", dataFormat.toString());
  }
    
  /** @return the rejectErrors */
  public boolean isRejectErrors() {
    return rejectErrors;
  }

  /** @param rejectErrors the rejectErrors to set. */
  public void setRejectErrors(boolean rejectErrors) {
    this.rejectErrors = rejectErrors;
  }
}
