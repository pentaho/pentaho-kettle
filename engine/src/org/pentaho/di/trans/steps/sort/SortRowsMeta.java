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

package org.pentaho.di.trans.steps.sort;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 02-jun-2003
 */
public class SortRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SortRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** order by which fields? */
    private String  fieldName[];

    /** false : descending, true=ascending */
    private boolean ascending[];

    /** false : case insensitive, true=case sensitive */
    private boolean caseSensitive[];

    /** Directory to store the temp files */
    private String  directory;

    /** Temp files prefix... */
    private String  prefix;

    /** The sort size: number of rows sorted and kept in memory */
    private String  sortSize;
    
    /** The free memory limit in percentages in case we don't use the sort size */
    private String  freeMemoryLimit;

    /** only pass unique rows to the output stream(s) */
    private boolean onlyPassingUniqueRows;

    /**
     * Compress files: if set to true, temporary files are compressed, thus reducing I/O at the cost of slightly higher
     * CPU usage
     */
    private boolean compressFiles;
    
    /** The variable to use to set the compressFiles option boolean */
    private String  compressFilesVariable;

    public SortRowsMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the ascending.
     */
    public boolean[] getAscending()
    {
        return ascending;
    }

    /**
     * @param ascending The ascending to set.
     */
    public void setAscending(boolean[] ascending)
    {
        this.ascending = ascending;
    }

    /**
     * @return Returns the directory.
     */
    public String getDirectory()
    {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setDirectory(String directory)
    {
        this.directory = directory;
    }

    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }

    /**
     * @return Returns the prefix.
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * @param prefix The prefix to set.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields]; // order by
        ascending = new boolean[nrfields];
        caseSensitive = new boolean[nrfields];
    }

    public Object clone()
    {
        SortRowsMeta retval = (SortRowsMeta) super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.ascending[i] = ascending[i];
            retval.caseSensitive[i] = caseSensitive[i];
        }

        return retval;
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            directory = XMLHandler.getTagValue(stepnode, "directory");
            prefix = XMLHandler.getTagValue(stepnode, "prefix");
            sortSize = XMLHandler.getTagValue(stepnode, "sort_size");
            freeMemoryLimit = XMLHandler.getTagValue(stepnode, "free_memory");
            compressFiles = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "compress"));
            compressFilesVariable = XMLHandler.getTagValue(stepnode, "compress_variable");
            onlyPassingUniqueRows = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "unique_rows") );

            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nrfields = XMLHandler.countNodes(fields, "field");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

                fieldName[i] = XMLHandler.getTagValue(fnode, "name");
                String asc = XMLHandler.getTagValue(fnode, "ascending");
                ascending[i] = "Y".equalsIgnoreCase(asc);
                String sens = XMLHandler.getTagValue(fnode, "case_sensitive");
                caseSensitive[i] = Const.isEmpty(sens) || "Y".equalsIgnoreCase(sens);
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }

    public void setDefault()
    {
        directory = "%%java.io.tmpdir%%";
        prefix = "out";
        sortSize = "1000000";
        freeMemoryLimit = null;
        compressFiles = false;
        compressFilesVariable = null;
        onlyPassingUniqueRows = false;

        int nrfields = 0;

        allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i;
            caseSensitive[i] = true;
        }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer(256);

        retval.append("      ").append(XMLHandler.addTagValue("directory", directory));
        retval.append("      ").append(XMLHandler.addTagValue("prefix", prefix));
        retval.append("      ").append(XMLHandler.addTagValue("sort_size", sortSize));
        retval.append("      ").append(XMLHandler.addTagValue("free_memory", freeMemoryLimit));
        retval.append("      ").append(XMLHandler.addTagValue("compress", compressFiles));
        retval.append("      ").append(XMLHandler.addTagValue("compress_variable", compressFilesVariable));
        retval.append("      ").append(XMLHandler.addTagValue("unique_rows", onlyPassingUniqueRows));

        retval.append("    <fields>").append(Const.CR);
        for (int i = 0; i < fieldName.length; i++)
        {
            retval.append("      <field>").append(Const.CR);
            retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i]));
            retval.append("        ").append(XMLHandler.addTagValue("ascending", ascending[i]));
            retval.append("        ").append(XMLHandler.addTagValue("case_sensitive", caseSensitive[i]));
            retval.append("      </field>").append(Const.CR);
        }
        retval.append("    </fields>").append(Const.CR);

        return retval.toString();
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            directory = rep.getStepAttributeString(id_step, "directory");
            prefix = rep.getStepAttributeString(id_step, "prefix");
            int sortSizeInt = (int) rep.getStepAttributeInteger(id_step, "sort_size");
            if (sortSizeInt>0) {
            	sortSize = Integer.toString(sortSizeInt); // For backward compatibility
            }
            else {
            	sortSize = rep.getStepAttributeString(id_step, "sort_size");
            }
            freeMemoryLimit = rep.getStepAttributeString(id_step, "free_memory");
            
            compressFiles = rep.getStepAttributeBoolean(id_step, "compress");
            compressFilesVariable = rep.getStepAttributeString(id_step, "compress_variable");
            
            onlyPassingUniqueRows = rep.getStepAttributeBoolean(id_step, "unique_rows");

            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
                ascending[i] = rep.getStepAttributeBoolean(id_step, i, "field_ascending");
                caseSensitive[i] = rep.getStepAttributeBoolean(id_step, i, "field_case_sensitive", true);
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "directory", directory);
            rep.saveStepAttribute(id_transformation, id_step, "prefix", prefix);
            rep.saveStepAttribute(id_transformation, id_step, "sort_size", sortSize);
            rep.saveStepAttribute(id_transformation, id_step, "free_memory", freeMemoryLimit);
            rep.saveStepAttribute(id_transformation, id_step, "compress", compressFiles);
            rep.saveStepAttribute(id_transformation, id_step, "compress_variable", compressFilesVariable);
            rep.saveStepAttribute(id_transformation, id_step, "unique_rows", onlyPassingUniqueRows);

            for (int i = 0; i < fieldName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "field_ascending", ascending[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "field_case_sensitive", caseSensitive[i]);
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }
    
    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // Set the sorted properties: ascending/descending
        for (int i=0;i<fieldName.length;i++)
        {
            int idx = inputRowMeta.indexOfValue(fieldName[i]);
            if (idx>=0)
            {
                ValueMetaInterface valueMeta = inputRowMeta.getValueMeta(idx);
                valueMeta.setSortedDescending(!ascending[i]);
                valueMeta.setCaseInsensitive(!caseSensitive[i]);
                
                // Also see if lazy conversion is active on these key fields.
                // If so we want to automatically convert them to the normal storage type.
                // This will improve performance, see also: PDI-346
                // 
                valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
                valueMeta.setStorageMetadata(null);
            }
        }
        
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;

        if (prev != null && prev.size() > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.FieldsReceived", "" + prev.size()),
                    stepMeta);
            remarks.add(cr);

            String error_message = "";
            boolean error_found = false;

            // Starting from selected fields in ...
            for (int i = 0; i < fieldName.length; i++)
            {
                int idx = prev.indexOfValue(fieldName[i]);
                if (idx < 0)
                {
                    error_message += "\t\t" + fieldName[i] + Const.CR;
                    error_found = true;
                }
            }
            if (error_found)
            {
                error_message = BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.SortKeysNotFound", error_message);

                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length > 0)
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.AllSortKeysFound"), stepMeta);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.NoSortKeysEntered"), stepMeta);
                    remarks.add(cr);
                }
            }

            // Check the sort directory
            String realDirectory = transMeta.environmentSubstitute(directory);

            File f = new File(realDirectory);
            if (f.exists())
            {
                if (f.isDirectory())
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.DirectoryExists", realDirectory),
                            stepMeta);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.ExistsButNoDirectory",
                            realDirectory), stepMeta);
                    remarks.add(cr);
                }
            }
            else
            {
                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.DirectoryNotExists", realDirectory),
                        stepMeta);
                remarks.add(cr);
            }
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.NoFields"), stepMeta);
            remarks.add(cr);
        }

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.ExpectedInputOk"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortRowsMeta.CheckResult.ExpectedInputError"), stepMeta);
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new SortRows(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new SortRowsData();
    }

    /**
     * @return Returns the sortSize.
     */
    public String getSortSize()
    {
        return sortSize;
    }

    /**
     * @param sortSize The sortSize to set.
     */
    public void setSortSize(String sortSize)
    {
        this.sortSize = sortSize;
    }

    /**
     * @return Returns whether temporary files should be compressed
     */
    public boolean getCompressFiles()
    {
        return compressFiles;

    }

    /**
     * @param compressFiles Whether to compress temporary files created during sorting
     */
    public void setCompressFiles(boolean compressFiles)
    {
        this.compressFiles = compressFiles;
    }

    /**
     * @return the onlyPassingUniqueRows
     */
    public boolean isOnlyPassingUniqueRows()
    {
        return onlyPassingUniqueRows;
    }

    /**
     * @param onlyPassingUniqueRows the onlyPassingUniqueRows to set
     */
    public void setOnlyPassingUniqueRows(boolean onlyPassingUniqueRows)
    {
        this.onlyPassingUniqueRows = onlyPassingUniqueRows;
    }

	/**
	 * @return the compressFilesVariable
	 */
	public String getCompressFilesVariable() {
		return compressFilesVariable;
	}

	/**
	 * @param compressFilesVariable the compressFilesVariable to set
	 */
	public void setCompressFilesVariable(String compressFilesVariable) {
		this.compressFilesVariable = compressFilesVariable;
	}

	/**
	 * @return the caseSensitive
	 */
	public boolean[] getCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * @param caseSensitive the caseSensitive to set
	 */
	public void setCaseSensitive(boolean[] caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	/**
	 * @return the freeMemoryLimit
	 */
	public String getFreeMemoryLimit() {
		return freeMemoryLimit;
	}

	/**
	 * @param freeMemoryLimit the freeMemoryLimit to set
	 */
	public void setFreeMemoryLimit(String freeMemoryLimit) {
		this.freeMemoryLimit = freeMemoryLimit;
	}
	
  @Override
  public StepMetaInjectionInterface getStepMetaInjectionInterface() {
    return new SortRowsMetaInjection(this);
  }
}