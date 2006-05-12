 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/


package be.ibridge.kettle.trans.step.xbaseinput;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XBase;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/*
 * Created on 2-jun-2003
 *
 */
 
public class XBaseInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String 	dbfFileName;
	private int 	rowLimit;
	private boolean rowNrAdded;
	private String  rowNrField;
	
	public XBaseInputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the dbfFileName.
     */
    public String getDbfFileName()
    {
        return dbfFileName;
    }
    
    /**
     * @param dbfFileName The dbfFileName to set.
     */
    public void setDbfFileName(String dbfFileName)
    {
        this.dbfFileName = dbfFileName;
    }
    
    /**
     * @return Returns the rowLimit.
     */
    public int getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(int rowLimit)
    {
        this.rowLimit = rowLimit;
    }
    
    /**
     * @return Returns the rowNrField.
     */
    public String getRowNrField()
    {
        return rowNrField;
    }
    
    /**
     * @param rowNrField The rowNrField to set.
     */
    public void setRowNrField(String rowNrField)
    {
        this.rowNrField = rowNrField;
    }
    
    /**
     * @return Returns the rowNrAdded.
     */
    public boolean isRowNrAdded()
    {
        return rowNrAdded;
    }
    
    /**
     * @param rowNrAdded The rowNrAdded to set.
     */
    public void setRowNrAdded(boolean rowNrAdded)
    {
        this.rowNrAdded = rowNrAdded;
    }

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		XBaseInputMeta retval = (XBaseInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			dbfFileName              = XMLHandler.getTagValue(stepnode, "file_dbf"); //$NON-NLS-1$
			rowLimit              = Const.toInt(XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
			rowNrAdded             = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "add_rownr")); //$NON-NLS-1$ //$NON-NLS-2$
			rowNrField           = XMLHandler.getTagValue(stepnode, "field_rownr"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("XBaseInputMeta.Exception.UnableToReadStepInformationFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		dbfFileName    = null;
		rowLimit    = 0;
		rowNrAdded   = false;
		rowNrField = null;
	}
	
	public Row getFields(Row r, String name, Row info)
		throws KettleStepException
	{
		Row row;
				
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		XBase xbi = new XBase(StringUtil.environmentSubstitute(dbfFileName));
		// System.out.println("File version: "+xbi.getVersionInfo());
		try
		{
            xbi.open();
			Row add = xbi.getFields();
			for (int i=0;i<add.size();i++)
			{
				Value v=add.getValue(i);
				v.setOrigin(name);
			}
			row.addRow(	add );
		}
		catch(KettleException ke)
	    {
			throw new KettleStepException(Messages.getString("XBaseInputMeta.Exception.UnableToReadMetaDataFromXBaseFile"), ke); //$NON-NLS-1$
	    }
        finally
        {
            xbi.close();
        }
	    
	    if (rowNrAdded && rowNrField!=null && rowNrField.length()>0)
	    {
	    	Value rnr = new Value(rowNrField, Value.VALUE_TYPE_INTEGER);
	    	rnr.setOrigin(name);
	    	row.addValue(rnr);
	    }
		
		return row;
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("file_dbf",    dbfFileName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("limit",       rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("add_rownr",   rowNrAdded)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("field_rownr", rowNrField)); //$NON-NLS-1$ //$NON-NLS-2$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			dbfFileName              =      rep.getStepAttributeString (id_step, "file_dbf"); //$NON-NLS-1$
			rowLimit              = (int)rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
			rowNrAdded             =      rep.getStepAttributeBoolean(id_step, "add_rownr"); //$NON-NLS-1$
			rowNrField           =      rep.getStepAttributeString (id_step, "field_rownr"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XBaseInputMeta.Exception.UnexpectedErrorReadingMetaDataFromRepository"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_dbf",        dbfFileName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "add_rownr",       rowNrAdded); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "field_rownr",     rowNrField); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("XBaseInputMeta.Exception.UnableToSaveMetaDataToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		if (dbfFileName==null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XBaseInputMeta.Remark.PleaseSelectFileToUse"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XBaseInputMeta.Remark.FileToUseIsSpecified"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);

            XBase xbi = new XBase(StringUtil.environmentSubstitute(dbfFileName));
            try
            {
                xbi.open();
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("XBaseInputMeta.Remark.FileExistsAndCanBeOpened"), stepinfo); //$NON-NLS-1$
                remarks.add(cr);
                
                Row r = xbi.getFields();
            
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, r.size()+Messages.getString("XBaseInputMeta.Remark.OutputFieldsCouldBeDetermined"), stepinfo); //$NON-NLS-1$
                remarks.add(cr);
            }
            catch(KettleException ke)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("XBaseInputMeta.Remark.NoFieldsCouldBeFoundInFileBecauseOfError")+Const.CR+ke.getMessage(), stepinfo); //$NON-NLS-1$
                remarks.add(cr);
            }
            finally
            {
                xbi.close();
            }
        }

		
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new XBaseInputDialog(shell, info, transMeta, name);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new XBaseInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new XBaseInputData();
	}


}
