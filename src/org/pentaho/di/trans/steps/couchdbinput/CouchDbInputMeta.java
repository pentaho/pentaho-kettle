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

package org.pentaho.di.trans.steps.couchdbinput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
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
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Created on 8-apr-2011
 * @author matt
 * @since 4.2.0-M1
 */
@Step(id = "CouchDbInput", image = "couchdb-input.png", name = "CouchDb Input", description="Reads from a Couch DB view", categoryDescription="Big Data")
public class CouchDbInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = CouchDbInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private String hostname;
	private String port;
  private String dbName;
  private String designDocument;
  private String viewName;
  
  private String authenticationUser;
  private String authenticationPassword;
  
	public CouchDbInputMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		CouchDbInputMeta retval = (CouchDbInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			hostname  = XMLHandler.getTagValue(stepnode, "hostname"); //$NON-NLS-1$ //$NON-NLS-2$
      port = XMLHandler.getTagValue(stepnode, "port"); //$NON-NLS-1$ //$NON-NLS-2$
      dbName = XMLHandler.getTagValue(stepnode, "db_name"); //$NON-NLS-1$
      designDocument  = XMLHandler.getTagValue(stepnode, "design_document"); //$NON-NLS-1$
			viewName  = XMLHandler.getTagValue(stepnode, "view_name"); //$NON-NLS-1$
      authenticationUser = XMLHandler.getTagValue(stepnode, "auth_user"); //$NON-NLS-1$
      authenticationPassword = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "auth_password")); //$NON-NLS-1$ 
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "CouchDbInputMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		hostname = "localhost"; //$NON-NLS-1$
    port = "5984"; //$NON-NLS-1$
    dbName = "db";  //$NON-NLS-1$
    viewName = "design-document/view-name";  //$NON-NLS-1$
	}
	
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
		throws KettleStepException
	{
    ValueMetaInterface idValueMeta = new ValueMeta("json", ValueMetaInterface.TYPE_STRING);
    idValueMeta.setOrigin(origin);
    rowMeta.addValueMeta(idValueMeta);
	}
	
	public String getXML()
	{
    StringBuffer retval = new StringBuffer(300);
		
		retval.append("    ").append(XMLHandler.addTagValue("hostname", hostname)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("port", port)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("db_name", dbName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("design_document", designDocument)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("view_name", viewName)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("    ").append(XMLHandler.addTagValue("auth_user", authenticationUser));
    retval.append("    ").append(XMLHandler.addTagValue("auth_password", Encr.encryptPasswordIfNotUsingVariables(authenticationPassword)));

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String,Counter> counters)
		throws KettleException
	{
		try
		{
			hostname      = rep.getStepAttributeString (id_step, "hostname"); //$NON-NLS-1$
      port          = rep.getStepAttributeString (id_step, "port"); //$NON-NLS-1$
      dbName        = rep.getStepAttributeString (id_step, "db_name"); //$NON-NLS-1$
      designDocument = rep.getStepAttributeString (id_step, "design_document"); //$NON-NLS-1$
      viewName    = rep.getStepAttributeString (id_step, "view_name"); //$NON-NLS-1$
      authenticationUser = rep.getStepAttributeString(id_step, "auth_user");
      authenticationPassword = Encr.decryptPasswordOptionallyEncrypted(rep.getStepAttributeString(id_step, "auth_password"));
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CouchDbInputMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
      rep.saveStepAttribute(id_transformation, id_step, "hostname", hostname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "port", port); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "db_name", dbName); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "design_document", designDocument); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "view_name", viewName); //$NON-NLS-1$
      rep.saveStepAttribute(id_transformation, id_step, "auth_user", authenticationUser);
      rep.saveStepAttribute(id_transformation, id_step, "auth_password", Encr.encryptPasswordIfNotUsingVariables(authenticationPassword));
		}
		catch(KettleException e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "CouchDbInputMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new CouchDbInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new CouchDbInputData();
	}

  @Override
  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
    // TODO add checks
  }

  /**
   * @return the hostname
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @param hostname the hostname to set
   */
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  /**
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port the port to set
   */
  public void setPort(String port) {
    this.port = port;
  }

  /**
   * @return the dbName
   */
  public String getDbName() {
    return dbName;
  }

  /**
   * @param dbName the dbName to set
   */
  public void setDbName(String dbName) {
    this.dbName = dbName;
  }

  /**
   * @return the authenticationUser
   */
  public String getAuthenticationUser() {
    return authenticationUser;
  }

  /**
   * @param authenticationUser the authenticationUser to set
   */
  public void setAuthenticationUser(String authenticationUser) {
    this.authenticationUser = authenticationUser;
  }

  /**
   * @return the authenticationPassword
   */
  public String getAuthenticationPassword() {
    return authenticationPassword;
  }

  /**
   * @param authenticationPassword the authenticationPassword to set
   */
  public void setAuthenticationPassword(String authenticationPassword) {
    this.authenticationPassword = authenticationPassword;
  }


  /**
   * @return the viewName
   */
  public String getViewName() {
    return viewName;
  }

  /**
   * @param viewName the viewName to set
   */
  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  /**
   * @return the designDocument
   */
  public String getDesignDocument() {
    return designDocument;
  }

  /**
   * @param designDocument the designDocument to set
   */
  public void setDesignDocument(String designDocument) {
    this.designDocument = designDocument;
  }  
  
}
