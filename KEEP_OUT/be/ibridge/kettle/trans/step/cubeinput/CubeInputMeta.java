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


package be.ibridge.kettle.trans.step.cubeinput;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleFileException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
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
 
public class CubeInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private String filename;
	private int rowLimit;

	public CubeInputMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
	 * @return Returns the filename.
	 */
	public String getFilename()
	{
		return filename;
	}
	
	/**
	 * @param filename The filename to set.
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
	
	/**
	 * @param rowLimit The rowLimit to set.
	 */
	public void setRowLimit(int rowLimit)
	{
		this.rowLimit = rowLimit;
	}
	
	/**
	 * @return Returns the rowLimit.
	 */
	public int getRowLimit()
	{
		return rowLimit;
	}
	
	public Object clone()
	{
		CubeInputMeta retval = (CubeInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			filename  = XMLHandler.getTagValue(stepnode, "file", "name"); //$NON-NLS-1$ //$NON-NLS-2$
			rowLimit  = Const.toInt( XMLHandler.getTagValue(stepnode, "limit"), 0); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("CubeInputMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		filename = ""; //$NON-NLS-1$
		rowLimit   = 0;
	}
	
	public Row getFields(Row r, String name, Row info)
		throws KettleStepException
	{
		Row row;

        if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		GZIPInputStream fis = null;
		DataInputStream dis = null;
		try
		{
			File f = new File(filename);
			fis = new GZIPInputStream(new FileInputStream(f));
			dis = new DataInputStream(fis);
	
			Row add = getMetaData(dis);		
				
			if (add==null) return row;
			for (int i=0;i<add.size();i++)
			{
				Value v=add.getValue(i);
				v.setOrigin(name);
			}
			row.addRow(	add );
		}
		catch(KettleFileException kfe)
		{
			throw new KettleStepException(Messages.getString("CubeInputMeta.Exception.UnableToReadMetaData"), kfe); //$NON-NLS-1$
		}
		catch(IOException e)
		{
			throw new KettleStepException(Messages.getString("CubeInputMeta.Exception.ErrorOpeningOrReadingCubeFile"), e); //$NON-NLS-1$
		}
		finally
		{
			try
			{
				if (fis!=null) fis.close();
				if (dis!=null) dis.close();
			}
			catch(IOException ioe)
			{
				throw new KettleStepException(Messages.getString("CubeInputMeta.Exception.UnableToCloseCubeFile"), ioe); //$NON-NLS-1$
			}
		}
		
		return row;
	}
	
	public static final Row getMetaData(DataInputStream dis) throws KettleFileException
	{
		return new Row(dis);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    <file>"+Const.CR); //$NON-NLS-1$
		retval.append("      "+XMLHandler.addTagValue("name", filename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      </file>"+Const.CR); //$NON-NLS-1$
		retval.append("    "+XMLHandler.addTagValue("limit",    rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			filename         =      rep.getStepAttributeString (id_step, "file_name"); //$NON-NLS-1$
			rowLimit         = (int)rep.getStepAttributeInteger(id_step, "limit"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("CubeInputMeta.Exception.UnexpectedErrorWhileReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "file_name",   filename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "limit",       rowLimit); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			throw new KettleException(Messages.getString("CubeInputMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, Messages.getString("CubeInputMeta.CheckResult.FileSpecificationsNotChecked"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new CubeInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new CubeInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new CubeInputData();
	}

}
