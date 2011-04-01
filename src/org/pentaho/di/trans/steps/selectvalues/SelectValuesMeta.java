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

package org.pentaho.di.trans.steps.selectvalues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.KettleAttributeInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.lineage.FieldnameLineage;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInjectionMetaEntry;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Meta Data class for the Select Values Step.
 * 
 * Created on 02-jun-2003
 */
public class SelectValuesMeta extends BaseStepMeta implements StepMetaInterface, StepMetaInjectionInterface
{
	private static Class<?> PKG = SelectValuesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	// SELECT mode
    /** Select: Name of the selected field */
	private String selectName[];

	/** Select: Rename to ... */
	private String selectRename[];

	/** Select: length of field */
	private int selectLength[];

	/** Select: Precision of field (for numbers) */
	private int selectPrecision[];

	/**
	 * Select: flag to indicate that the non-selected fields should also be
	 * taken along, ordered by fieldname
	 */
	private boolean selectingAndSortingUnspecifiedFields;
	
	// DE-SELECT mode
	/** Names of the fields to be removed!  */
	private String deleteName[]; 
	
	// META-DATA mode
	private SelectMetadataChange[] meta;
	
	public SelectValuesMeta()
	{
		super(); // allocate BaseStepMeta
	}

    /**
     * @return Returns the deleteName.
     */
    public String[] getDeleteName()
    {
        return deleteName;
    }
    
    /**
     * @param deleteName The deleteName to set.
     */
    public void setDeleteName(String[] deleteName)
    {
        this.deleteName = deleteName;
    }
    
    /**
     * @return Returns the selectLength.
     */
    public int[] getSelectLength()
    {
        return selectLength;
    }
    
    /**
     * @param selectLength The selectLength to set.
     */
    public void setSelectLength(int[] selectLength)
    {
        this.selectLength = selectLength;
    }
    
    /**
     * @return Returns the selectName.
     */
    public String[] getSelectName()
    {
        return selectName;
    }
    
    /**
     * @param selectName The selectName to set.
     */
    public void setSelectName(String[] selectName)
    {
        this.selectName = selectName;
    }
    
    /**
     * @return Returns the selectPrecision.
     */
    public int[] getSelectPrecision()
    {
        return selectPrecision;
    }
    
    /**
     * @param selectPrecision The selectPrecision to set.
     */
    public void setSelectPrecision(int[] selectPrecision)
    {
        this.selectPrecision = selectPrecision;
    }
    
    /**
     * @return Returns the selectRename.
     */
    public String[] getSelectRename()
    {
        return selectRename;
    }
    
    /**
     * @param selectRename The selectRename to set.
     */
    public void setSelectRename(String[] selectRename)
    {
        this.selectRename = selectRename;
    }
    
    
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrFields, int nrRemove, int nrMeta)
	{
		allocateSelect(nrFields);
		allocateRemove(nrRemove);
		allocateMeta(nrMeta);
	}
	
	private void allocateSelect(int nrFields) {
      selectName      = new String[nrFields];
      selectRename    = new String[nrFields];
      selectLength    = new int   [nrFields];
      selectPrecision = new int   [nrFields];
	}
	
	private void allocateRemove(int nrRemove) {
      deleteName      = new String[nrRemove];
	}

	private void allocateMeta(int nrMeta) {
      meta            = new SelectMetadataChange[nrMeta];
    }
	

	public Object clone()
	{
		SelectValuesMeta retval = (SelectValuesMeta)super.clone();

		int nrfields = selectName.length;
		int nrremove = deleteName.length;
		int nrmeta   = meta.length;
		
		retval.allocate(nrfields, nrremove, nrmeta);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.selectName     [i] = selectName[i];
			retval.selectRename   [i] = selectRename[i];
			retval.selectLength   [i] = selectLength[i];
			retval.selectPrecision[i] = selectPrecision[i];
		}

		for (int i=0;i<nrremove;i++)
		{
			retval.deleteName     [i] = deleteName[i];
		}
		
		for (int i=0;i<nrmeta;i++)
		{
			retval.getMeta()[i] = meta[i].clone();
		}

		return retval;
	}
	
	private void readData(Node step) throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(step, "fields"); //$NON-NLS-1$
	
			int nrfields   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			int nrremove   = XMLHandler.countNodes(fields, "remove"); //$NON-NLS-1$
			int nrmeta     = XMLHandler.countNodes(fields, SelectMetadataChange.XML_TAG); //$NON-NLS-1$
			allocate(nrfields, nrremove, nrmeta);
			
			for (int i=0;i<nrfields;i++)
			{
				Node line = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				selectName     [i] = XMLHandler.getTagValue(line, "name"); //$NON-NLS-1$
				selectRename   [i] = XMLHandler.getTagValue(line, "rename"); //$NON-NLS-1$
				selectLength   [i] = Const.toInt(XMLHandler.getTagValue(line, "length"), -2); //$NON-NtagLS-1$
				selectPrecision[i] = Const.toInt(XMLHandler.getTagValue(line, "precision"), -2); //$NON-NLS-1$
			}
			selectingAndSortingUnspecifiedFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(fields, "select_unspecified"));
	
			for (int i=0;i<nrremove;i++)
			{
				Node line = XMLHandler.getSubNodeByNr(fields, "remove", i); //$NON-NLS-1$
				deleteName     [i] = XMLHandler.getTagValue(line, "name"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrmeta;i++)
			{
				Node metaNode = XMLHandler.getSubNodeByNr(fields, SelectMetadataChange.XML_TAG, i); //$NON-NLS-1$
				meta[i] = new SelectMetadataChange(this);
				meta[i].loadXML(metaNode);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "SelectValuesMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		allocate(0, 0, 0);
	}
	
	public void getSelectFields(RowMetaInterface inputRowMeta, String name) throws KettleStepException
	{
		RowMetaInterface row;
		
		if (selectName!=null && selectName.length>0)  // SELECT values
		{
			// 0. Start with an empty row
			// 1. Keep only the selected values
			// 2. Rename the selected values
			// 3. Keep the order in which they are specified... (not the input order!)
			//
			
			row=new RowMeta();
			for (int i=0;i<selectName.length;i++)
			{
				ValueMetaInterface v = inputRowMeta.searchValueMeta(selectName[i]);
				
				if (v!=null)  // We found the value
				{
					v = v.clone();
					// Do we need to rename ?
					if (!v.getName().equals(selectRename[i]) && selectRename[i]!=null && selectRename[i].length()>0)
					{
						v.setName(selectRename[i]);
						v.setOrigin(name);
					}
					if (selectLength[i]!=-2   ) { v.setLength(selectLength[i]);       v.setOrigin(name); } 
					if (selectPrecision[i]!=-2) { v.setPrecision(selectPrecision[i]); v.setOrigin(name); }
					
					// Add to the resulting row!
					row.addValueMeta(v);
				}
			}
			
			if (selectingAndSortingUnspecifiedFields) {
				// Select the unspecified fields.
				// Sort the fields
				// Add them after the specified fields...
				//
				List<String> extra = new ArrayList<String>();
				for (int i=0;i<inputRowMeta.size();i++) {
					String fieldName = inputRowMeta.getValueMeta(i).getName();
					if (Const.indexOfString(fieldName, selectName)<0) {
						extra.add(fieldName);
					}
				}
				Collections.sort(extra);
				for (String fieldName : extra) {
					ValueMetaInterface extraValue = inputRowMeta.searchValueMeta(fieldName);
					row.addValueMeta(extraValue);
				}
			}

            // OK, now remove all from r and re-add row:
            inputRowMeta.clear();
            inputRowMeta.addRowMeta(row);
		}
	}
	
	public void getDeleteFields(RowMetaInterface inputRowMeta) throws KettleStepException
	{
		if (deleteName!=null && deleteName.length>0)  // DESELECT values from the stream...
		{
			for (int i=0;i<deleteName.length;i++)
			{
				try
                {
                    inputRowMeta.removeValueMeta(deleteName[i]);
                }
                catch (KettleValueException e)
                {
                    throw new KettleStepException(e);
                }
			}
		}
	}
	
	public void getMetadataFields(RowMetaInterface inputRowMeta, String name)
	{
		if (meta!=null && meta.length>0) // METADATA mode: change the meta-data of the values mentioned...
		{
			for (int i=0;i<meta.length;i++)
			{
				SelectMetadataChange metaChange = meta[i];
				
				int idx = inputRowMeta.indexOfValue(metaChange.getName());
				if (idx>=0)  // We found the value
				{
					// This is the value we need to change:
					ValueMetaInterface v = inputRowMeta.getValueMeta(idx);
					
					// Do we need to rename ?
					if (!v.getName().equals(metaChange.getRename()) && !Const.isEmpty(metaChange.getRename()))
					{
						v.setName(metaChange.getRename());
						v.setOrigin(name);
					}
					// Change the type?
					if (metaChange.getType()!=ValueMetaInterface.TYPE_NONE && v.getType()!=metaChange.getType())
					{
						v.setType(metaChange.getType());
						
						// This also moves the data to normal storage type
						//
						v.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
					}
					if (metaChange.getLength()     != -2) { v.setLength(metaChange.getLength());       v.setOrigin(name); } 
					if (metaChange.getPrecision()  != -2) { v.setPrecision(metaChange.getPrecision()); v.setOrigin(name); }
					if (metaChange.getStorageType() >= 0) { v.setStorageType(metaChange.getStorageType()); v.setOrigin(name); }
					if (!Const.isEmpty(metaChange.getConversionMask())) 
					{ 
						v.setConversionMask(metaChange.getConversionMask());
						v.setOrigin(name);
					}

					v.setDateFormatLenient(metaChange.isDateFormatLenient());
					
					if (!Const.isEmpty(metaChange.getEncoding())) 
					{ 
					  v.setStringEncoding(metaChange.getEncoding());
					  v.setOrigin(name);
					}
					if (!Const.isEmpty(metaChange.getDecimalSymbol())) 
					{ 
						v.setDecimalSymbol(metaChange.getDecimalSymbol());
						v.setOrigin(name);
					}
					if (!Const.isEmpty(metaChange.getGroupingSymbol())) 
					{ 
						v.setGroupingSymbol(metaChange.getGroupingSymbol());
						v.setOrigin(name);
					}
					if (!Const.isEmpty(metaChange.getCurrencySymbol())) 
					{ 
						v.setCurrencySymbol(metaChange.getCurrencySymbol());
						v.setOrigin(name);
					}
				}
			}
		}
	}

	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
	    RowMetaInterface rowMeta = inputRowMeta.clone();
	    inputRowMeta.clear();
	    inputRowMeta.addRowMeta(rowMeta);
	    
        getSelectFields(inputRowMeta, name);
		getDeleteFields(inputRowMeta);
		getMetadataFields(inputRowMeta, name);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<selectName.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_NAME"),      selectName[i])); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_RENAME"),    selectRename[i])); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_LENGTH"),    selectLength[i])); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("FIELD_PRECISION"), selectPrecision[i])); //$NON-NLS-1$
			retval.append("      </field>"); //$NON-NLS-1$
		}
		retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("SELECT_UNSPECIFIED"), selectingAndSortingUnspecifiedFields)); //$NON-NLS-1$
		for (int i=0;i<deleteName.length;i++)
		{
			retval.append("      <remove>"); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue(getXmlCode("REMOVE_NAME"),      deleteName[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </remove>"); //$NON-NLS-1$
		}
		for (int i=0;i<meta.length;i++)
		{
			retval.append(meta[i].getXML());
		}
		retval.append("    </fields>"); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			int nrfields = rep.countNrStepAttributes(id_step, getRepCode("FIELD_NAME")); //$NON-NLS-1$
			int nrremove = rep.countNrStepAttributes(id_step, getRepCode("REMOVE_NAME")); //$NON-NLS-1$
			int nrmeta   = rep.countNrStepAttributes(id_step, getRepCode("META_NAME")); //$NON-NLS-1$
			
			allocate(nrfields, nrremove, nrmeta);
	
			for (int i=0;i<nrfields;i++)
			{
				selectName[i]      =      rep.getStepAttributeString (id_step, i, getRepCode("FIELD_NAME")); //$NON-NLS-1$
				selectRename[i]    =      rep.getStepAttributeString (id_step, i, getRepCode("FIELD_RENAME")); //$NON-NLS-1$
				selectLength[i]    = (int)rep.getStepAttributeInteger(id_step, i, getRepCode("FIELD_LENGTH")); //$NON-NLS-1$
				selectPrecision[i] = (int)rep.getStepAttributeInteger(id_step, i, getRepCode("FIELD_PRECISION")); //$NON-NLS-1$
			}
			selectingAndSortingUnspecifiedFields = rep.getStepAttributeBoolean(id_step, getRepCode("SELECT_UNSPECIFIED"));
			
			for (int i=0;i<nrremove;i++)
			{
				deleteName[i]      =      rep.getStepAttributeString(id_step, i, getRepCode("REMOVE_NAME")); //$NON-NLS-1$
			}

			for (int i=0;i<nrmeta;i++)
			{
				meta[i] = new SelectMetadataChange(this);
				meta[i].setName(rep.getStepAttributeString (id_step, i, getRepCode("META_NAME"))); //$NON-NLS-1$
				meta[i].setRename(rep.getStepAttributeString (id_step, i, getRepCode("META_RENAME"))); //$NON-NLS-1$
				meta[i].setType((int)rep.getStepAttributeInteger(id_step, i, getRepCode("META_TYPE"))); //$NON-NLS-1$
				meta[i].setLength((int)rep.getStepAttributeInteger(id_step, i, getRepCode("META_LENGTH"))); //$NON-NLS-1$
				meta[i].setPrecision((int)rep.getStepAttributeInteger(id_step, i, getRepCode("META_PRECISION"))); //$NON-NLS-1$
				meta[i].setStorageType(ValueMeta.getStorageType(rep.getStepAttributeString (id_step, i, getRepCode("META_STORAGE_TYPE")))); //$NON-NLS-1$ 
				meta[i].setConversionMask(rep.getStepAttributeString (id_step, i, getRepCode("META_CONVERSION_MASK"))); //$NON-NLS-1$
				meta[i].setDateFormatLenient(Boolean.parseBoolean(rep.getStepAttributeString (id_step, i, getRepCode("META_DATE_FORMAT_LENIENT")))); //$NON-NLS-1$
				meta[i].setEncoding(rep.getStepAttributeString (id_step, i, getRepCode("META_ENCODING"))); //$NON-NLS-1$
				meta[i].setDecimalSymbol(rep.getStepAttributeString (id_step, i, getRepCode("META_DECIMAL"))); //$NON-NLS-1$
				meta[i].setGroupingSymbol(rep.getStepAttributeString (id_step, i, getRepCode("META_GROUPING"))); //$NON-NLS-1$
				meta[i].setCurrencySymbol(rep.getStepAttributeString (id_step, i, getRepCode("META_CURRENCY"))); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SelectValuesMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			for (int i=0;i<selectName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_NAME"),      selectName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_RENAME"),    selectRename[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_LENGTH"),    selectLength[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("FIELD_PRECISION"), selectPrecision[i]); //$NON-NLS-1$
			}
			rep.saveStepAttribute(id_transformation, id_step, getRepCode("SELECT_UNSPECIFIED"), selectingAndSortingUnspecifiedFields); //$NON-NLS-1$
	
			for (int i=0;i<deleteName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("REMOVE_NAME"),      deleteName[i]); //$NON-NLS-1$
			}
	
			for (int i=0;i<meta.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_NAME"),            meta[i].getName()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_RENAME"),          meta[i].getRename()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_TYPE"),            meta[i].getType()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_LENGTH"),          meta[i].getLength()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_PRECISION"),       meta[i].getPrecision()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_STORAGE_TYPE"),    ValueMeta.getStorageTypeCode(meta[i].getStorageType())); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_CONVERSION_MASK"), meta[i].getConversionMask()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_DATE_FORMAT_LENIENT"), Boolean.toString(meta[i].isDateFormatLenient())); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_DECIMAL"),  meta[i].getDecimalSymbol()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_GROUPING"), meta[i].getGroupingSymbol()); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, getRepCode("META_CURRENCY"), meta[i].getCurrencySymbol()); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SelectValuesMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}

	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.StepReceivingFields",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);

			/*
			 * Take care of the normal SELECT fields...
			 */
			String  error_message=""; //$NON-NLS-1$
			boolean error_found=false;
			
			// Starting from selected fields in ...
			for (int i=0;i< this.selectName.length;i++)
			{
				int idx = prev.indexOfValue(selectName[i]);
				if (idx<0)
				{
					error_message+="\t\t"+selectName[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.SelectedFieldsNotFound")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.AllSelectedFieldsFound"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			
			if (this.selectName.length>0)
			{
				// Starting from prev...
				for (int i=0;i<prev.size();i++)
				{
					ValueMetaInterface pv = prev.getValueMeta(i);
					int idx = Const.indexOfString(pv.getName(), selectName);
					if (idx<0) 
					{
						error_message+="\t\t"+pv.getName()+" ("+pv.getTypeDesc()+")"+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						error_found=true;
					} 
				}
				if (error_found) 
				{
					error_message=BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.FieldsNotFound")+Const.CR+Const.CR+error_message; //$NON-NLS-1$
	
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_COMMENT, error_message, stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.AllSelectedFieldsFound2"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
				}
			}

			/*
			 * How about the DE-SELECT (remove) fields...
			 */
		
			error_message=""; //$NON-NLS-1$
			error_found=false;
			
			// Starting from selected fields in ...
			for (int i=0;i< this.deleteName.length;i++)
			{
				int idx = prev.indexOfValue(deleteName[i]);
				if (idx<0)
				{
					error_message+="\t\t"+deleteName[i]+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.DeSelectedFieldsNotFound")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.AllDeSelectedFieldsFound"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}

			/*
			 * How about the Meta-fields...?
			 */
			error_message=""; //$NON-NLS-1$
			error_found=false;
			
			// Starting from selected fields in ...
			for (int i=0;i< this.meta.length;i++)
			{
				int idx = prev.indexOfValue(this.meta[i].getName());
				if (idx<0)
				{
					error_message+="\t\t"+this.meta[i].getName()+Const.CR; //$NON-NLS-1$
					error_found=true;
				} 
			}
			if (error_found) 
			{
				error_message=BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.MetadataFieldsNotFound")+Const.CR+Const.CR+error_message; //$NON-NLS-1$

				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.AllMetadataFieldsFound"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.FieldsNotFound2"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		
		// Check for doubles in the selected fields...
		int cnt[] = new int[selectName.length];
		boolean error_found = false;
		String error_message=""; //$NON-NLS-1$
		
		for (int i=0;i<selectName.length;i++)
		{
			cnt[i]=0;
			for (int j=0;j<selectName.length;j++)
			{
				if (selectName[i].equals(selectName[j])) cnt[i]++;
			}
			
			if (cnt[i]>1)
			{
				if (!error_found) // first time...
				{
					error_message=BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.DuplicateFieldsSpecified")+Const.CR; //$NON-NLS-1$
				}
				else
				{
					error_found=true;
				}
				error_message+=BaseMessages.getString(PKG, "SelectValuesMeta.CheckResult.OccurentRow",i+" : "+selectName[i]+"  ("+cnt[i])+Const.CR; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				error_found=true;
			}
		}
		if (error_found)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SelectValues(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SelectValuesData();
	}

	/**
	 * @return the selectingAndSortingUnspecifiedFields
	 */
	public boolean isSelectingAndSortingUnspecifiedFields() {
		return selectingAndSortingUnspecifiedFields;
	}

	/**
	 * @param selectingAndSortingUnspecifiedFields the selectingAndSortingUnspecifiedFields to set
	 */
	public void setSelectingAndSortingUnspecifiedFields(boolean selectingAndSortingUnspecifiedFields) {
		this.selectingAndSortingUnspecifiedFields = selectingAndSortingUnspecifiedFields;
	}

	/**
	 * @return the meta
	 */
	public SelectMetadataChange[] getMeta() {
		return meta;
	}

	/**
	 * @param meta the meta to set
	 */
	public void setMeta(SelectMetadataChange[] meta) {
		this.meta = meta;
	}
	
	public boolean supportsErrorHandling() {
	   return true;
	}
	   
	/**
	 * We will describe in which way the field names change between input and output in this step.
	 * 
	 * @return The list of field name lineage objects
	 */
	public List<FieldnameLineage> getFieldnameLineage() {
		List<FieldnameLineage> lineages = new ArrayList<FieldnameLineage>();
		
		// Select values...
		//
		for (int i=0;i<getSelectName().length;i++) {
			String input = getSelectName()[i];
			String output = getSelectRename()[i];
			
			// See if the select tab renames a column!
			//
			if (!Const.isEmpty(output) && !input.equalsIgnoreCase(output)) {
				// Yes, add it to the list
				//
				lineages.add(new FieldnameLineage(input, output));
			}
		}
		
		// Metadata
		//
		for (int i=0;i<getMeta().length;i++) {
			String input = getMeta()[i].getName();
			String output = getMeta()[i].getRename();

			// See if the select tab renames a column!
			//
			if (!Const.isEmpty(output) && !input.equalsIgnoreCase(output)) {
				// See if the input is not the output of a row in the Select tab
				//
				int idx = Const.indexOfString(input, getSelectRename());
				
				if (idx<0) {
					// nothing special, add it to the list
					//
					lineages.add(new FieldnameLineage(input, output));
				} else {
					// Modify the existing field name lineage entry
					//
					FieldnameLineage lineage = FieldnameLineage.findFieldnameLineageWithInput(lineages, input);
					lineage.setOutputFieldname(output);
				}
			}
		}
		
		return lineages;
	}
	
	public StepMetaInjectionInterface getStepMetaInjectionInterface() {
	  return this;
	}
	
    /**
     * Describe the metadata attributes that can be injected into this step metadata object.
     */
    public List<StepInjectionMetaEntry> getStepInjectionMetadataEntries() {
      return getStepInjectionMetadataEntries(PKG);
    }

    public void injectStepMetadataEntries(List<StepInjectionMetaEntry> metadata) {
      for (StepInjectionMetaEntry entry : metadata) {
        KettleAttributeInterface attr = findAttribute(entry.getKey());
        
        // Set top level attributes...
        //
        if (entry.getValueType()!=ValueMetaInterface.TYPE_NONE) {
          if (entry.getKey().equals("SELECT_UNSPECIFIED")) { selectingAndSortingUnspecifiedFields = (Boolean) entry.getValue(); } else
          {
            throw new RuntimeException("Unhandled metadata injection of attribute: "+attr.toString()+" - "+attr.getDescription());
          }
        } else {
          // The data sets...
          //
          if (attr.getKey().equals("FIELDS")) {
            List<StepInjectionMetaEntry> selectFields = entry.getDetails();
            allocateSelect(selectFields.size());
            for (int row=0;row<selectFields.size();row++) {
              StepInjectionMetaEntry selectField = selectFields.get(row);

              List<StepInjectionMetaEntry> fieldAttributes = selectField.getDetails();
              for (int i=0;i<fieldAttributes.size();i++) {
                StepInjectionMetaEntry fieldAttribute = fieldAttributes.get(i);
                KettleAttributeInterface fieldAttr = findAttribute(fieldAttribute.getKey());

                String attributeValue = (String)fieldAttribute.getValue();
                if (fieldAttr.getKey().equals("FIELD_NAME")) { getSelectName()[row] = attributeValue; } else
                if (fieldAttr.getKey().equals("FIELD_RENAME")) { getSelectRename()[row] = attributeValue; } else
                if (fieldAttr.getKey().equals("FIELD_LENGTH")) { getSelectLength()[row] = attributeValue==null ? -1 : Integer.parseInt(attributeValue); } else
                if (fieldAttr.getKey().equals("FIELD_PRECISION")) { getSelectPrecision()[row] = attributeValue==null ? -1 : Integer.parseInt(attributeValue); } else
                {
                  throw new RuntimeException("Unhandled metadata injection of attribute: "+fieldAttr.toString()+" - "+fieldAttr.getDescription());
                }
              }
            }
          } else if (attr.getKey().equals("REMOVES")) {
            List<StepInjectionMetaEntry> removeFields = entry.getDetails();
            allocateRemove(removeFields.size());
            for (int row=0;row<removeFields.size();row++) {
              StepInjectionMetaEntry removeField = removeFields.get(row);

              List<StepInjectionMetaEntry> fieldAttributes = removeField.getDetails();
              for (int i=0;i<fieldAttributes.size();i++) {
                StepInjectionMetaEntry fieldAttribute = fieldAttributes.get(i);
                KettleAttributeInterface fieldAttr = findAttribute(fieldAttribute.getKey());
                String attributeValue = (String)fieldAttribute.getValue();
                
                if (fieldAttr.getKey().equals("REMOVE_NAME")) { getDeleteName()[row] = attributeValue; } else
                {
                  throw new RuntimeException("Unhandled metadata injection of attribute: "+fieldAttr.toString()+" - "+fieldAttr.getDescription());
                }
              }
            }
          } else if (attr.getKey().equals("METAS")) { 
            List<StepInjectionMetaEntry> metaFields = entry.getDetails();
            allocateMeta(metaFields.size());
            for (int row=0;row<metaFields.size();row++) {
              StepInjectionMetaEntry metaField = metaFields.get(row);
              SelectMetadataChange metaChange = new SelectMetadataChange(this);
              List<StepInjectionMetaEntry> fieldAttributes = metaField.getDetails();
              for (int i=0;i<fieldAttributes.size();i++) {
                StepInjectionMetaEntry fieldAttribute = fieldAttributes.get(i);
                KettleAttributeInterface fieldAttr = findAttribute(fieldAttribute.getKey());
                String attributeValue = (String)fieldAttribute.getValue();
                
                if (fieldAttr.getKey().equals("META_NAME")) { metaChange.setName(attributeValue); } else
                if (fieldAttr.getKey().equals("META_RENAME")) { metaChange.setRename(attributeValue); } else
                if (fieldAttr.getKey().equals("META_TYPE")) { metaChange.setType(ValueMeta.getType(attributeValue)); } else
                if (fieldAttr.getKey().equals("META_CONVERSION_MASK")) { metaChange.setConversionMask(attributeValue); } else
                if (fieldAttr.getKey().equals("META_LENGTH")) { metaChange.setLength(attributeValue==null ? -1 : Integer.parseInt(attributeValue)); } else
                if (fieldAttr.getKey().equals("META_PRECISION")) { metaChange.setPrecision(attributeValue==null ? -1 : Integer.parseInt(attributeValue)); } else
                if (fieldAttr.getKey().equals("META_CURRENCY")) { metaChange.setCurrencySymbol(attributeValue); } else
                if (fieldAttr.getKey().equals("META_DECIMAL")) { metaChange.setDecimalSymbol(attributeValue); } else
                if (fieldAttr.getKey().equals("META_GROUPING")) { metaChange.setGroupingSymbol(attributeValue); } else
                if (fieldAttr.getKey().equals("META_STORAGE_TYPE")) { metaChange.setStorageType(ValueMeta.getStorageType(attributeValue)); } else
                if (fieldAttr.getKey().equals("META_DATE_FORMAT_LENIENT")) { metaChange.setDateFormatLenient(ValueMeta.convertStringToBoolean(attributeValue)==null ? false : true); } else 
                if (fieldAttr.getKey().equals("META_ENCODING")) { metaChange.setEncoding(attributeValue); } else 
                {
                  throw new RuntimeException("Unhandled metadata injection of attribute: "+fieldAttr.getKey()+" - "+fieldAttr.getDescription());
                }
              }
              meta[row] = metaChange;
            }
            break;
          }
        }
      }
    }
}