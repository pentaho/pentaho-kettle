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

/* Modifications to original RegexEval step made by Daniel Einspanjer */

package org.pentaho.di.trans.steps.regexeval;

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
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

public class RegexEvalMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = RegexEvalMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private String  script;
    private String  matcher;
    private String  resultfieldname;
    private boolean usevar;

    private boolean allowcapturegroups;
    
    private boolean canoneq;
    private boolean caseinsensitive;
    private boolean comment;
    private boolean dotall;
    private boolean multiline;
    private boolean unicode;
    private boolean unix;

    private String  fieldName[];
    private int     fieldType[];
    private String  fieldFormat[];
    private String  fieldGroup[];
    private String  fieldDecimal[];
    private String  fieldCurrency[];
    private int     fieldLength[];
    private int     fieldPrecision[];
    private String  fieldNullIf[];
    private String  fieldIfNull[];
    private int     fieldTrimType[];

    public RegexEvalMeta()
    {
        super();
    }

    public Object clone()
    {
        RegexEvalMeta retval = (RegexEvalMeta) super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.fieldType[i] = fieldType[i];
            retval.fieldLength[i] = fieldLength[i];
            retval.fieldPrecision[i] = fieldPrecision[i];
            retval.fieldFormat[i] = fieldFormat[i];
            retval.fieldGroup[i] = fieldGroup[i];
            retval.fieldDecimal[i] = fieldDecimal[i];
            retval.fieldCurrency[i] = fieldCurrency[i];
            retval.fieldNullIf[i] = fieldNullIf[i];
            retval.fieldIfNull[i] = fieldIfNull[i];
            retval.fieldTrimType[i] = fieldTrimType[i];
        }

        return retval;
    }

    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields];
        fieldType = new int[nrfields];
        fieldFormat = new String[nrfields];
        fieldGroup = new String[nrfields];
        fieldDecimal = new String[nrfields];
        fieldCurrency = new String[nrfields];
        fieldLength = new int[nrfields];
        fieldPrecision = new int[nrfields];
        fieldNullIf = new String[nrfields];
        fieldIfNull = new String[nrfields];
        fieldTrimType = new int[nrfields];
    }

    public String getScript()
    {
        return script;
    }


    public String getRegexOptions()
    {
        StringBuilder options = new StringBuilder();
        
        if (isCaseInsensitiveFlagSet())
        {
                options.append("(?i)");
        }
        if (isCommentFlagSet())
        {
                options.append("(?x)");
        }
        if (isDotAllFlagSet())
        {
                options.append("(?s)");
        }
        if (isMultilineFlagSet())
        {
                options.append("(?m)");
        }
        if (isUnicodeFlagSet())
        {
                options.append("(?u)");
        }
        if (isUnixLineEndingsFlagSet())
        {
                options.append("(?d)");
        }
        return options.toString();
    }
    public void setScript(String script)
    {
        this.script = script;
    }

    public String getMatcher()
    {
        return matcher;
    }

    public void setMatcher(String matcher)
    {
        this.matcher = matcher;
    }

    public String getResultFieldName()
    {
        return resultfieldname;
    }

    public void setResultFieldName(String resultfieldname)
    {
        this.resultfieldname = resultfieldname;
    }

    public boolean isUseVariableInterpolationFlagSet()
    {
        return usevar;
    }

    public void setUseVariableInterpolationFlag(boolean usevar)
    {
        this.usevar = usevar;
    }

    public boolean isAllowCaptureGroupsFlagSet()
    {
        return allowcapturegroups;
    }
    
    public void setAllowCaptureGroupsFlag(boolean allowcapturegroups)
    {
        this.allowcapturegroups = allowcapturegroups;
    }
    
    public boolean isCanonicalEqualityFlagSet()
    {
        return canoneq;
    }

    public void setCanonicalEqualityFlag(boolean canoneq)
    {
        this.canoneq = canoneq;
    }

    public boolean isCaseInsensitiveFlagSet()
    {
        return caseinsensitive;
    }

    public void setCaseInsensitiveFlag(boolean caseinsensitive)
    {
        this.caseinsensitive = caseinsensitive;
    }

    public boolean isCommentFlagSet()
    {
        return comment;
    }

    public void setCommentFlag(boolean comment)
    {
        this.comment = comment;
    }

    public boolean isDotAllFlagSet()
    {
        return dotall;
    }

    public void setDotAllFlag(boolean dotall)
    {
        this.dotall = dotall;
    }

    public boolean isMultilineFlagSet()
    {
        return multiline;
    }

    public void setMultilineFlag(boolean multiline)
    {
        this.multiline = multiline;
    }

    public boolean isUnicodeFlagSet()
    {
        return unicode;
    }

    public void setUnicodeFlag(boolean unicode)
    {
        this.unicode = unicode;
    }

    public boolean isUnixLineEndingsFlagSet()
    {
        return unix;
    }

    public void setUnixLineEndingsFlag(boolean unix)
    {
        this.unix = unix;
    }

    public String[] getFieldName()
    {
        return fieldName;
    }

    public int[] getFieldType()
    {
        return fieldType;
    }

    public void setFieldType(int[] fieldType)
    {
        this.fieldType = fieldType;
    }

    public String[] getFieldFormat()
    {
        return fieldFormat;
    }

    public void setFieldFormat(String[] fieldFormat)
    {
        this.fieldFormat = fieldFormat;
    }

    public String[] getFieldGroup()
    {
        return fieldGroup;
    }

    public void setFieldGroup(String[] fieldGroup)
    {
        this.fieldGroup = fieldGroup;
    }

    public String[] getFieldDecimal()
    {
        return fieldDecimal;
    }

    public void setFieldDecimal(String[] fieldDecimal)
    {
        this.fieldDecimal = fieldDecimal;
    }

    public String[] getFieldCurrency()
    {
        return fieldCurrency;
    }

    public void setFieldCurrency(String[] fieldCurrency)
    {
        this.fieldCurrency = fieldCurrency;
    }

    public int[] getFieldLength()
    {
        return fieldLength;
    }

    public void setFieldLength(int[] fieldLength)
    {
        this.fieldLength = fieldLength;
    }

    public int[] getFieldPrecision()
    {
        return fieldPrecision;
    }

    public void setFieldPrecision(int[] fieldPrecision)
    {
        this.fieldPrecision = fieldPrecision;
    }

    public String[] getFieldNullIf()
    {
        return fieldNullIf;
    }

    public void setFieldNullIf(final String[] fieldNullIf)
    {
        this.fieldNullIf = fieldNullIf;
    }

    public String[] getFieldIfNull()
    {
        return fieldIfNull;
    }

    public void setFieldIfNull(final String[] fieldIfNull)
    {
        this.fieldIfNull = fieldIfNull;
    }

    public int[] getFieldTrimType()
    {
        return fieldTrimType;
    }

    public void setFieldTrimType(final int[] fieldTrimType)
    {
        this.fieldTrimType = fieldTrimType;
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
    {
        try
        {
            script = XMLHandler.getTagValue(stepnode, "script"); //$NON-NLS-1$
            matcher = XMLHandler.getTagValue(stepnode, "matcher"); //$NON-NLS-1$
            resultfieldname = XMLHandler.getTagValue(stepnode, "resultfieldname"); //$NON-NLS-1$
            usevar = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "usevar"));
            allowcapturegroups = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "allowcapturegroups"));
            canoneq = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "canoneq"));
            caseinsensitive = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "caseinsensitive"));
            comment = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "comment"));
            dotall = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "dotall"));
            multiline = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "multiline"));
            unicode = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "unicode"));
            unix = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "unix"));

            Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
            int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$

                fieldName[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
                final String stype = XMLHandler.getTagValue(fnode, "type"); //$NON-NLS-1$
                fieldFormat[i] = XMLHandler.getTagValue(fnode, "format"); //$NON-NLS-1$
                fieldGroup[i] = XMLHandler.getTagValue(fnode, "group"); //$NON-NLS-1$
                fieldDecimal[i] = XMLHandler.getTagValue(fnode, "decimal"); //$NON-NLS-1$
                fieldCurrency[i] = XMLHandler.getTagValue(fnode, "currency"); //$NON-NLS-1$
                final String slen = XMLHandler.getTagValue(fnode, "length"); //$NON-NLS-1$
                final String sprc = XMLHandler.getTagValue(fnode, "precision"); //$NON-NLS-1$
                fieldNullIf[i] = XMLHandler.getTagValue(fnode, "nullif"); //$NON-NLS-1$
                fieldIfNull[i] = XMLHandler.getTagValue(fnode, "ifnull"); //$NON-NLS-1$
                final String trim = XMLHandler.getTagValue(fnode, "trimtype"); //$NON-NLS-1$
                fieldType[i] = ValueMeta.getType(stype);
                fieldLength[i] = Const.toInt(slen, -1);
                fieldPrecision[i] = Const.toInt(sprc, -1);
                fieldTrimType[i] = ValueMeta.getTrimTypeByCode(trim);
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException(
                    BaseMessages.getString(PKG, "RegexEvalMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }

    public void setDefault()
    {
        script = ""; //$NON-NLS-1$
        matcher = "";
        resultfieldname = "result";
        usevar = false;
        allowcapturegroups = false;
        canoneq = false;
        caseinsensitive = false;
        comment = false;
        dotall = false;
        multiline = false;
        unicode = false;
        unix = false;

        allocate(0);
    }

    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep,
            VariableSpace space) throws KettleStepException
    {
        if (!Const.isEmpty(resultfieldname))
        {
            ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(resultfieldname), ValueMeta.TYPE_BOOLEAN);
            v.setOrigin(name);
            inputRowMeta.addValueMeta(v);
        }
        
        if (allowcapturegroups == true)
        {
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] != null && fieldName[i].length() != 0)
                {
                    int type = fieldType[i];
                    if (type == ValueMetaInterface.TYPE_NONE) type = ValueMetaInterface.TYPE_STRING;
                    ValueMetaInterface v = new ValueMeta(fieldName[i], type);
                    v.setLength(fieldLength[i]);
                    v.setPrecision(fieldPrecision[i]);
                    v.setOrigin(name);
                    v.setConversionMask(fieldFormat[i]);
                    v.setDecimalSymbol(fieldDecimal[i]);
                    v.setGroupingSymbol(fieldGroup[i]);
                    v.setCurrencySymbol(fieldCurrency[i]);
                    v.setTrimType(fieldTrimType[i]);
                    inputRowMeta.addValueMeta(v);
                }
            }
        }
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
            throws KettleXMLException
    {
        readData(stepnode, databases);
    }

    public String getXML()
    {
        StringBuilder retval = new StringBuilder();

        retval.append("    " + XMLHandler.openTag("script") + XMLHandler.buildCDATA(script) + XMLHandler.closeTag("script")); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("matcher", matcher)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("usevar", usevar));
        retval.append("    " + XMLHandler.addTagValue("allowcapturegroups", allowcapturegroups));
        retval.append("    " + XMLHandler.addTagValue("canoneq", canoneq));
        retval.append("    " + XMLHandler.addTagValue("caseinsensitive", caseinsensitive));
        retval.append("    " + XMLHandler.addTagValue("comment", comment));
        retval.append("    " + XMLHandler.addTagValue("dotall", dotall));
        retval.append("    " + XMLHandler.addTagValue("multiline", multiline));
        retval.append("    " + XMLHandler.addTagValue("unicode", unicode));
        retval.append("    " + XMLHandler.addTagValue("unix", unix));

        retval.append("    <fields>").append(Const.CR);
        for (int i = 0; i < fieldName.length; i++)
        {
            if (fieldName[i] != null && fieldName[i].length() != 0)
            {
                retval.append("      <field>").append(Const.CR);
                retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i]));
                retval.append("        ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(fieldType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("format", fieldFormat[i]));
                retval.append("        ").append(XMLHandler.addTagValue("group", fieldGroup[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("decimal", fieldDecimal[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("length", fieldLength[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("precision", fieldPrecision[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("nullif", fieldNullIf[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("        ").append(XMLHandler.addTagValue("ifnull", fieldIfNull[i])); //$NON-NLS-1$ //$NON-NLS-2$
                retval
                        .append("        ").append(XMLHandler.addTagValue("trimtype", ValueMeta.getTrimTypeCode(fieldTrimType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
                retval.append("      </field>").append(Const.CR);
            }
        }
        retval.append("    </fields>").append(Const.CR);

        return retval.toString();
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
            throws KettleException

    {
        try
        {
            script = rep.getStepAttributeString(id_step, "script"); //$NON-NLS-1$
            matcher = rep.getStepAttributeString(id_step, "matcher"); //$NON-NLS-1$
            resultfieldname = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
            usevar = rep.getStepAttributeBoolean(id_step, "usevar");
            allowcapturegroups = rep.getStepAttributeBoolean(id_step, "allowcapturegroups");
            canoneq = rep.getStepAttributeBoolean(id_step, "canoneq");
            caseinsensitive = rep.getStepAttributeBoolean(id_step, "caseinsensitive");
            comment = rep.getStepAttributeBoolean(id_step, "comment");
            multiline = rep.getStepAttributeBoolean(id_step, "multiline");
            dotall = rep.getStepAttributeBoolean(id_step, "dotall");
            unicode = rep.getStepAttributeBoolean(id_step, "unicode");
            unix = rep.getStepAttributeBoolean(id_step, "unix");

            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
                fieldType[i] = ValueMeta.getType(rep.getStepAttributeString(id_step, i, "field_type"));

                fieldFormat[i] = rep.getStepAttributeString(id_step, i, "field_format");
                fieldGroup[i] = rep.getStepAttributeString(id_step, i, "field_group"); //$NON-NLS-1$
                fieldDecimal[i] = rep.getStepAttributeString(id_step, i, "field_decimal"); //$NON-NLS-1$
                fieldLength[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_length"); //$NON-NLS-1$
                fieldPrecision[i] = (int) rep.getStepAttributeInteger(id_step, i, "field_precision"); //$NON-NLS-1$
                fieldNullIf[i] = rep.getStepAttributeString(id_step, i, "field_nullif"); //$NON-NLS-1$
                fieldIfNull[i] = rep.getStepAttributeString(id_step, i, "field_ifnull"); //$NON-NLS-1$
                fieldTrimType[i] = ValueMeta.getTrimTypeByCode(rep.getStepAttributeString(id_step, i, "field_trimtype"));  //$NON-NLS-1$
            }
        }
        catch (Exception e)
        {
            throw new KettleException(
                    BaseMessages.getString(PKG, "RegexEvalMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "script", script); //$NON-NLS-1$
            for (int i = 0; i < fieldName.length; i++)
            {
                if (fieldName[i] != null && fieldName[i].length() != 0)
                {
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
                    rep.saveStepAttribute(id_transformation, id_step, i,
                            "field_type", ValueMeta.getTypeDesc(fieldType[i])); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_format", fieldFormat[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_group", fieldGroup[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal", fieldDecimal[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_length", fieldLength[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_precision", fieldPrecision[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_nullif", fieldNullIf[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i, "field_ifnull", fieldIfNull[i]); //$NON-NLS-1$
                    rep.saveStepAttribute(id_transformation, id_step, i,
                            "field_trimtype", ValueMeta.getTrimTypeCode(fieldTrimType[i])); //$NON-NLS-1$
                }
            }

            rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "usevar", usevar);
            rep.saveStepAttribute(id_transformation, id_step, "allowcapturegroups", allowcapturegroups);
            rep.saveStepAttribute(id_transformation, id_step, "canoneq", canoneq);
            rep.saveStepAttribute(id_transformation, id_step, "caseinsensitive", caseinsensitive);
            rep.saveStepAttribute(id_transformation, id_step, "comment", comment);
            rep.saveStepAttribute(id_transformation, id_step, "dotall", dotall);
            rep.saveStepAttribute(id_transformation, id_step, "multiline", multiline);
            rep.saveStepAttribute(id_transformation, id_step, "unicode", unicode);
            rep.saveStepAttribute(id_transformation, id_step, "unix", unix);
            rep.saveStepAttribute(id_transformation, id_step, "matcher", matcher);
        }
        catch (Exception e)
        {
            throw new KettleException(
                    BaseMessages.getString(PKG, "RegexEvalMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta,
            RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {

        CheckResult cr;

        if (prev != null && prev.size() > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "RegexEvalMeta.CheckResult.ConnectedStepOK", String.valueOf(prev.size())), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, 
            		BaseMessages.getString(PKG, "RegexEvalMeta.CheckResult.NoInputReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

        // Check Field to evaluate
        if (!Const.isEmpty(matcher))
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, 
            		BaseMessages.getString(PKG, "RegexEvalMeta.CheckResult.MatcherOK"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, 
            		BaseMessages.getString(PKG, "RegexEvalMeta.CheckResult.NoMatcher"), stepMeta);
            remarks.add(cr);

        }

        // Check Result Field name
        if (!Const.isEmpty(resultfieldname))
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, 
            		BaseMessages.getString(PKG, "RegexEvalMeta.CheckResult.ResultFieldnameOK"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, 
            		BaseMessages.getString(PKG, "RegexEvalMeta.CheckResult.NoResultFieldname"), stepMeta);
            remarks.add(cr);
        }

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
            Trans trans)
    {
        return new RegexEval(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new RegexEvalData();
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
