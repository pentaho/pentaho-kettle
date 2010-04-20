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


package org.pentaho.di.trans.steps.olapinput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 2-jun-2003
 *
 */
public class OlapInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String mdx;
	private String username;
	private String password;
	private String olap4jUrl;
	private final String olap4jDriver = "org.olap4j.driver.xmla.XmlaOlap4jDriver";
	private String catalog;
	private OlapData data;
	
    private boolean variableReplacementActive;
    public OlapInputMeta()
	{
		super();
	}
	
    /**
     * @return Returns the variableReplacementActive.
     */
    public boolean isVariableReplacementActive()
    {
        return variableReplacementActive;
    }

    /**
     * @param variableReplacementActive The variableReplacementActive to set.
     */
    public void setVariableReplacementActive(boolean variableReplacementActive)
    {
        this.variableReplacementActive = variableReplacementActive;
    }

	
	
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		OlapInputMeta retval = (OlapInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
		    olap4jUrl                  = XMLHandler.getTagValue(stepnode, "url");
		    username                   = XMLHandler.getTagValue(stepnode, "username");
		    password                   = XMLHandler.getTagValue(stepnode, "password");
		    catalog                    = XMLHandler.getTagValue(stepnode, "catalog");
			mdx                        = XMLHandler.getTagValue(stepnode, "mdx");
		
            variableReplacementActive = "Y".equals(XMLHandler.getTagValue(stepnode, "variables_active"));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{

        olap4jUrl = "http://localhost:8080/pentaho/Xmla";
        username = "joe";
        password = "password";
        catalog = "SampleData";
		mdx        =  "SELECT NON EMPTY Hierarchize(Union(Crossjoin({[Measures].[Actual]}, Union(Crossjoin({[Region].[All Regions]}, \n {[Department].[All Departments]}), Crossjoin({[Region].[All Regions]}, [Department].[All Departments].Children))), Crossjoin({[Measures].[Actual]}, \n Union(Crossjoin([Region].[All Regions].Children, {[Department].[All Departments]}),  Crossjoin([Region].[All Regions].Children, [Department].[All Departments].Children))))) ON COLUMNS, \n NON EMPTY Hierarchize(Union({[Positions].[All Positions]}, [Positions].[All Positions].Children)) ON ROWS \n from [Quadrant Analysis]";
		variableReplacementActive=false;
		
	}
    
    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
    {
		
		RowMetaInterface add=null;
		
		try
		{
			initData(space);
			
			add = data.outputRowMeta;
		}
		catch(Exception dbe)
		{
			throw new KettleStepException("Unable to get query result for MDX query: "+Const.CR+mdx, dbe);
		}

		// Set the origin
		//
		for (int i=0;i<add.size();i++)
		{
			ValueMetaInterface v=add.getValueMeta(i);
			v.setOrigin(origin);
		}
		
		row.addRowMeta( add );
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
        retval.append("    "+XMLHandler.addTagValue("url",        olap4jUrl));
        retval.append("    "+XMLHandler.addTagValue("username",   username));
        retval.append("    "+XMLHandler.addTagValue("password",   password));
		retval.append("    "+XMLHandler.addTagValue("mdx",        mdx));
		retval.append("    "+XMLHandler.addTagValue("catalog",    catalog));
        retval.append("    "+XMLHandler.addTagValue("variables_active",   variableReplacementActive));
        
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
		    olap4jUrl                 =      rep.getStepAttributeString (id_step, "url");
		    username                  =      rep.getStepAttributeString (id_step, "username");
		    password                  =      rep.getStepAttributeString (id_step, "password");
			mdx                       =      rep.getStepAttributeString (id_step, "mdx");
			catalog                   =      rep.getStepAttributeString(id_step, "catalog");
            variableReplacementActive =      rep.getStepAttributeBoolean(id_step, "variables_active");
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{

            rep.saveStepAttribute(id_transformation, id_step, "url",              olap4jUrl);
            rep.saveStepAttribute(id_transformation, id_step, "username",         username);
            rep.saveStepAttribute(id_transformation, id_step, "password",         mdx);
            rep.saveStepAttribute(id_transformation, id_step, "catalog",            catalog);            
			rep.saveStepAttribute(id_transformation, id_step, "mdx",              mdx);
            rep.saveStepAttribute(id_transformation, id_step, "variables_active", variableReplacementActive);

		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        //TODO: perform tests to see if connection is valid
	    //	    CheckResult cr;
	    //	    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Please select or create a connection to use", stepMeta);
	    //	    remarks.add(cr);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		data = (OlapData) stepDataInterface;
		return new OlapInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new OlapData();
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) throws KettleStepException
	{
		// you can't really analyze the database impact since it runs on a remote XML/A server
	}
    
	public RowMeta createRowMeta(String[] headerValues, String[][] cellValues) {
		RowMeta outputRowMeta = new RowMeta();

        for (int i=0;i<cellValues[0].length;i++)
        {
            String name ="";
            if (Const.isEmpty(headerValues)) {
                name = "Column" + i;
            }
            else {
                name = headerValues[i];
            }

            ValueMetaInterface valueMeta=new ValueMeta(name,ValueMetaInterface.TYPE_STRING);

            outputRowMeta.addValueMeta(valueMeta);

        }
        return outputRowMeta;
	}

	/**
	 * @return the catalog
	 */
	public String getCatalog() {
		return catalog;
	}

	/**
	 * @param catalog the catalog to set
	 */
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
	
	public String getMdx() {
        return mdx;
    }

    public void setMdx(String mdx) {
        this.mdx = mdx;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOlap4jUrl() {
        return olap4jUrl;
    }

    public void setOlap4jUrl(String olap4jUrl) {
        this.olap4jUrl = olap4jUrl;
    }

    public String getOlap4jDriver() {
        return olap4jDriver;
    }

    public void initData(VariableSpace space) throws Exception {
    		String driver = this.getOlap4jDriver();
    		String url = space.environmentSubstitute(this.getOlap4jUrl());
    		String username = space.environmentSubstitute(this.getUsername());
    		String password = space.environmentSubstitute(this.getPassword());
    		
    		String mdx = this.getMdx();
            if(this.isVariableReplacementActive()) mdx = space.environmentSubstitute(this.getMdx());
            String catalog = space.environmentSubstitute(this.getCatalog());

//            mdx = "select NON EMPTY Hierarchize(Union(Crossjoin({[Measures].[Actual]}, Union(Crossjoin({[Region].[All Regions]},  {[Department].[All Departments]}), Crossjoin({[Region].[All Regions]}, [Department].[All Departments].Children))), Crossjoin({[Measures].[Actual]}, Union(Crossjoin([Region].[All Regions].Children, {[Department].[All Departments]}),  Crossjoin([Region].[All Regions].Children, [Department].[All Departments].Children))))) ON COLUMNS,NON EMPTY Hierarchize(Union({[Positions].[All Positions]}, [Positions].[All Positions].Children)) ON ROWS from [Quadrant Analysis]";
//            data.olapHelper = new OlapHelper(driver,"http://localhost:8080/pentaho/Xmla", "joe", "password","SampleData",mdx, this);
    		data.olapHelper = new OlapHelper(driver,url, username, password,catalog,mdx);
    		data.olapHelper.openQuery();
			data.olapHelper.createRectangularOutput();
    		data.outputRowMeta = this.createRowMeta(data.olapHelper.getHeaderValues(), data.olapHelper.getCellValues()).clone(); 
    		
    }
}
