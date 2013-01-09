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

package org.pentaho.di.trans.steps.addxml;

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
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;





/**
 * This class knows how to handle the MetaData for the XML output step
 * 
 * @since 14-jan-2006
 *
 */

public class AddXMLMeta extends BaseStepMeta  implements StepMetaInterface
{
	private static Class<?> PKG = AddXMLMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** The base name of the output file */

    /** Flag: ommit the XML Header*/
    private  boolean omitXMLheader;
    
    /** Flag: omit null elements from the xml result */
    private  boolean omitNullValues;

    /** The encoding to use for reading: null or empty string means system default encoding */
    private String encoding;

    /** The name value containing the resulting XML fragment */
    private String valueName;

    /** The name of the repeating row XML element */
    private String rootNode;

    /* THE FIELD SPECIFICATIONS ... */
    
    /** The output fields */
    private  XMLField outputFields[];


    public AddXMLMeta()
    {
        super(); // allocate BaseStepMeta
    }
    
    
    
    /**
     * @return Returns the zipped.
     */
    public boolean isOmitXMLheader()
    {
        return omitXMLheader;
    }



    /**
     * @param omitXMLheader The omit XML header flag to set.
     */
    public void setOmitXMLheader(boolean omitXMLheader)
    {
        this.omitXMLheader = omitXMLheader;
    }



    public void setOmitNullValues(boolean omitNullValues) {
      
          this.omitNullValues = omitNullValues;
        
    }



    public boolean isOmitNullValues() {
      
          return omitNullValues;
        
    }



    /**
     * @return Returns the outputFields.
     */
    public XMLField[] getOutputFields()
    {
        return outputFields;
    }
    
    /**
     * @param outputFields The outputFields to set.
     */
    public void setOutputFields(XMLField[] outputFields)
    {
        this.outputFields = outputFields;
    }
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
        readData(stepnode);
    }

    public void allocate(int nrfields)
    {
        outputFields = new XMLField[nrfields];
    }
    
    public Object clone()
    {
        AddXMLMeta retval = (AddXMLMeta)super.clone();
        int nrfields=outputFields.length;
        
        retval.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            retval.outputFields[i] = (XMLField) outputFields[i].clone();
        }
        
        return retval;
    }
    
    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            encoding         = XMLHandler.getTagValue(stepnode, "encoding"); //$NON-NLS-1$
            valueName      = XMLHandler.getTagValue(stepnode, "valueName"); //$NON-NLS-1$
            rootNode    = XMLHandler.getTagValue(stepnode, "xml_repeat_element"); //$NON-NLS-1$

            omitXMLheader    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "omitXMLheader"));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            omitNullValues    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "file", "omitNullValues"));   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            
            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields= XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
    
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
            
                outputFields[i] = new XMLField();
                outputFields[i].setFieldName( XMLHandler.getTagValue(fnode, "name") ); //$NON-NLS-1$
                outputFields[i].setElementName( XMLHandler.getTagValue(fnode, "element") ); //$NON-NLS-1$
                outputFields[i].setType( XMLHandler.getTagValue(fnode, "type") ); //$NON-NLS-1$
                outputFields[i].setFormat( XMLHandler.getTagValue(fnode, "format") ); //$NON-NLS-1$
                outputFields[i].setCurrencySymbol( XMLHandler.getTagValue(fnode, "currency") ); //$NON-NLS-1$
                outputFields[i].setDecimalSymbol( XMLHandler.getTagValue(fnode, "decimal") ); //$NON-NLS-1$
                outputFields[i].setGroupingSymbol( XMLHandler.getTagValue(fnode, "group") ); //$NON-NLS-1$
                outputFields[i].setNullString( XMLHandler.getTagValue(fnode, "nullif") ); //$NON-NLS-1$
                outputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) ); //$NON-NLS-1$
                outputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) ); //$NON-NLS-1$
                outputFields[i].setAttribute( "Y".equalsIgnoreCase( XMLHandler.getTagValue(fnode, "attribute") ) ); //$NON-NLS-1$ //$NON-NLS-2$
                outputFields[i].setAttributeParentName( XMLHandler.getTagValue(fnode, "attributeParentName") ); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e); //$NON-NLS-1$
        }
    }

    public void setDefault()
    {
        omitXMLheader    = true;
        omitNullValues   = false;
        encoding         = Const.XML_ENCODING;
        
        valueName        = "xmlvaluename"; //$NON-NLS-1$
        rootNode         = "Row"; //$NON-NLS-1$


        int nrfields=0;
        
        allocate(nrfields);
                    
        for (int i=0;i<nrfields;i++)
        {
            outputFields[i] = new XMLField();

            outputFields[i].setFieldName( "field"+i ); //$NON-NLS-1$
            outputFields[i].setElementName( "field"+i ); //$NON-NLS-1$
            outputFields[i].setType( "Number" ); //$NON-NLS-1$
            outputFields[i].setFormat( " 0,000,000.00;-0,000,000.00" ); //$NON-NLS-1$
            outputFields[i].setCurrencySymbol( "" ); //$NON-NLS-1$
            outputFields[i].setDecimalSymbol( "," ); //$NON-NLS-1$
            outputFields[i].setGroupingSymbol(  "." ); //$NON-NLS-1$
            outputFields[i].setNullString( "" ); //$NON-NLS-1$
            outputFields[i].setLength( -1 );
            outputFields[i].setPrecision( -1 );
            outputFields[i].setAttribute( false );
            outputFields[i].setElementName( "field"+i ); //$NON-NLS-1$ 
        }
    }
    
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        
    	ValueMetaInterface v=new ValueMeta(this.getValueName(), ValueMetaInterface.TYPE_STRING);
        v.setOrigin(name);
        row.addValueMeta( v );
    }

    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        
        retval.append("    ").append(XMLHandler.addTagValue("encoding",  encoding)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("valueName",  valueName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("xml_repeat_element",  rootNode)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("    <file>").append(Const.CR); //$NON-NLS-1$
        retval.append("      ").append(XMLHandler.addTagValue("omitXMLheader", omitXMLheader)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("omitNullValues", omitNullValues)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    </file>").append(Const.CR); //$NON-NLS-1$
        retval.append("    <fields>").append(Const.CR); //$NON-NLS-1$
        for (int i=0;i<outputFields.length;i++)
        {
            XMLField field = outputFields[i];
            
            if (field.getFieldName()!=null && field.getFieldName().length()!=0)
            {
                retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
                retval.append("        ").append(XMLHandler.addTagValue("name",      field.getFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("element",   field.getElementName())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("type",      field.getTypeDesc())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("format",    field.getFormat())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("currency",  field.getCurrencySymbol())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("decimal",   field.getDecimalSymbol())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("group",     field.getGroupingSymbol())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("nullif",    field.getNullString())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("length",    field.getLength())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("precision", field.getPrecision())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("attribute", field.isAttribute())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("attributeParentName",   field.getAttributeParentName())); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
            }
        }
        retval.append("    </fields>"+Const.CR); //$NON-NLS-1$

        return retval.toString();
    }
    
    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
        try
        {
            encoding        =      rep.getStepAttributeString (id_step, "encoding"); //$NON-NLS-1$
            valueName     =      rep.getStepAttributeString (id_step, "valueName"); //$NON-NLS-1$
            rootNode   =      rep.getStepAttributeString (id_step, "xml_repeat_element"); //$NON-NLS-1$
            
            omitXMLheader          =      rep.getStepAttributeBoolean(id_step, "omitXMLheader"); //$NON-NLS-1$
            omitNullValues         =      rep.getStepAttributeBoolean(id_step, "omitNullValues"); //$NON-NLS-1$
    
            int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
            
            allocate(nrfields);
            
            for (int i=0;i<nrfields;i++)
            {
                outputFields[i] = new XMLField();

                outputFields[i].setFieldName(       rep.getStepAttributeString (id_step, i, "field_name") ); //$NON-NLS-1$
                outputFields[i].setElementName(     rep.getStepAttributeString (id_step, i, "field_element") ); //$NON-NLS-1$
                outputFields[i].setType(            rep.getStepAttributeString (id_step, i, "field_type") ); //$NON-NLS-1$
                outputFields[i].setFormat(          rep.getStepAttributeString (id_step, i, "field_format") ); //$NON-NLS-1$
                outputFields[i].setCurrencySymbol(  rep.getStepAttributeString (id_step, i, "field_currency") ); //$NON-NLS-1$
                outputFields[i].setDecimalSymbol(   rep.getStepAttributeString (id_step, i, "field_decimal") ); //$NON-NLS-1$
                outputFields[i].setGroupingSymbol(  rep.getStepAttributeString (id_step, i, "field_group") ); //$NON-NLS-1$
                outputFields[i].setNullString(      rep.getStepAttributeString (id_step, i, "field_nullif") ); //$NON-NLS-1$
                outputFields[i].setLength(     (int)rep.getStepAttributeInteger(id_step, i, "field_length") ); //$NON-NLS-1$
                outputFields[i].setPrecision(  (int)rep.getStepAttributeInteger(id_step, i, "field_precision") ); //$NON-NLS-1$
                outputFields[i].setAttribute(       rep.getStepAttributeBoolean(id_step, i, "field_attribute") ); //$NON-NLS-1$
                outputFields[i].setAttributeParentName(   rep.getStepAttributeString(id_step, i, "field_attributeName") ); //$NON-NLS-1$
            }       
        }
        catch(Exception e)
        {
            throw new KettleException("Unexpected error reading step information from the repository", e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "encoding",           encoding); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "valueName",          valueName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "xml_repeat_element", rootNode); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "omitXMLheader",        omitXMLheader); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "omitNullValues",        omitNullValues); //$NON-NLS-1$
            
            for (int i=0;i<outputFields.length;i++)
            {
                XMLField field = outputFields[i];
                
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      field.getFieldName()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_element",   field.getElementName()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_type",      field.getTypeDesc()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_format",    field.getFormat()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",  field.getCurrencySymbol()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",   field.getDecimalSymbol()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_group",     field.getGroupingSymbol()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif",    field.getNullString()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_length",    field.getLength()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", field.getPrecision()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_attribute", field.isAttribute()); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "field_attributeName", field.getAttributeParentName()); //$NON-NLS-1$
            }
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {

    	CheckResult cr;
        // TODO - add checks for empty fieldnames 
        
        // Check output fields
        if (prev!=null && prev.size()>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AddXMLMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
            
            String  error_message="";
            boolean error_found=false;
            
            // Starting from selected fields in ...
            for (int i=0;i<outputFields.length;i++)
            {
                int idx = prev.indexOfValue(outputFields[i].getFieldName());
                if (idx<0)
                {
                    error_message+="\t\t"+outputFields[i].getFieldName()+Const.CR; //$NON-NLS-1$
                    error_found=true;
                } 
            }
            if (error_found) 
            {
                error_message=BaseMessages.getString(PKG, "AddXMLMeta.CheckResult.FieldsNotFound", error_message); //$NON-NLS-1$
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AddXMLMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
        
        // See if we have input streams leading to this step!
        if (input.length>0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AddXMLMeta.CheckResult.ExpectedInputOk"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AddXMLMeta.CheckResult.ExpectedInputError"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        
        cr = new CheckResult(CheckResult.TYPE_RESULT_COMMENT, BaseMessages.getString(PKG, "AddXMLMeta.CheckResult.FilesNotChecked"), stepMeta); //$NON-NLS-1$
        remarks.add(cr);
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new AddXML(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new AddXMLData();
    }



    public String getEncoding()
    {
        return encoding;
    }


    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }


    /**
     * @return Returns the rootNode.
     */
    public String getRootNode()
    {
        return rootNode;
    }

    /**
     * @param rootNode The root node to set.
     */
    public void setRootNode(String rootNode)
    {
        this.rootNode = rootNode;
    }

    
    public String getValueName() {
        return valueName;
    }


    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
    
}
