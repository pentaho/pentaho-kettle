/***** BEGIN LICENSE BLOCK *****
Version: MPL 1.1/GPL 2.0/LGPL 2.1

The contents of this project are subject to the Mozilla Public License Version
1.1 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Mozilla Corporation Metrics ETL for AMO

The Initial Developer of the Original Code is
Daniel Einspanjer deinspanjer@mozilla.com
Portions created by the Initial Developer are Copyright (C) 2008
the Initial Developer. All Rights Reserved.

Contributor(s):

Alternatively, the contents of this file may be used under the terms of
either the GNU General Public License Version 2 or later (the "GPL"), or
the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
in which case the provisions of the GPL or the LGPL are applicable instead
of those above. If you wish to allow use of your version of this file only
under the terms of either the GPL or the LGPL, and not to allow others to
use your version of this file under the terms of the MPL, indicate your
decision by deleting the provisions above and replace them with the notice
and other provisions required by the LGPL or the GPL. If you do not delete
the provisions above, a recipient may use your version of this file under
the terms of any one of the MPL, the GPL or the LGPL.

***** END LICENSE BLOCK *****/

package org.pentaho.di.trans.steps.userdefinedjavaclass;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.codehaus.janino.ClassBodyEvaluator;
import org.codehaus.janino.CompileException;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.Parser.ParseException;
import org.codehaus.janino.Scanner.ScanException;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef.ClassType;
import org.w3c.dom.Node;

public class UserDefinedJavaClassMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = UserDefinedJavaClassMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
    public enum ElementNames
    {
        class_type, class_name, class_source, definitions, definition,
        fields, field, field_name, field_type, field_length, field_precision,
        clear_result_fields,
        
        info_steps, info_step, info_,
        target_steps, target_step, target_,
        
        step_tag,
        step_name,
        step_description, 
        
        usage_parameters, usage_parameter, parameter_tag, parameter_value, parameter_description,
    }
    

    private List<FieldInfo>               fields      = new ArrayList<FieldInfo>();
    private List<UserDefinedJavaClassDef> definitions = new ArrayList<UserDefinedJavaClassDef>();
    public Class<TransformClassBase> cookedTransformClass;
    public final List<Exception> cookErrors = new ArrayList<Exception>(0);
    
    private boolean clearingResultFields;
    
    private boolean changed;
    
    private List<StepDefinition> infoStepDefinitions;
    private List<StepDefinition> targetStepDefinitions;

    private List<UsageParameter> usageParameters;
    
    public static class FieldInfo
    {
        public final String name;
        public final int    type;
        public final int    length;
        public final int    precision;

        public FieldInfo(String name, int type, int length, int precision)
        {
            super();
            this.name = name;
            this.type = type;
            this.length = length;
            this.precision = precision;
        }
    }

    public UserDefinedJavaClassMeta()
    {
        super();
        changed=true;
        infoStepDefinitions=new ArrayList<StepDefinition>();
        targetStepDefinitions=new ArrayList<StepDefinition>();
        usageParameters=new ArrayList<UsageParameter>();
    }

    private Class<?> cookClass(UserDefinedJavaClassDef def) throws CompileException, ParseException, ScanException, IOException, RuntimeException {
    	
        ClassBodyEvaluator cbe = new ClassBodyEvaluator();
        cbe.setClassName(def.getClassName());

        StringReader sr;
        if (def.isTransformClass())
        {
            cbe.setExtendedType(TransformClassBase.class);
            sr = new StringReader(def.getTransformedSource());
        }
        else
        {
            sr = new StringReader(def.getSource());
        }

        cbe.setDefaultImports(new String[] {
                "org.pentaho.di.trans.steps.userdefinedjavaclass.*",
                "org.pentaho.di.trans.step.*",
                "org.pentaho.di.core.row.*",
                "org.pentaho.di.core.*",
                "org.pentaho.di.core.exception.*"
        });
        cbe.cook(new Scanner(null, sr));

        return cbe.getClazz();
    }

    @SuppressWarnings("unchecked")
    public void cookClasses()
    {
        cookErrors.clear();
        for (UserDefinedJavaClassDef def : getDefinitions())
        {
            if (def.isActive())
            {
                try
                {
                    Class<?> cookedClass = cookClass(def);
                    if (def.isTransformClass())
                    {
                        cookedTransformClass = (Class<TransformClassBase>)cookedClass;
                    }
                }
                catch (Exception e)
                {
                    CompileException exception = new CompileException(e.getMessage(), null);
                    exception.setStackTrace(new StackTraceElement[] {});
                    cookErrors.add(exception);
                }
            }
        }
        changed=false;
    }

    public TransformClassBase newChildInstance(UserDefinedJavaClass parent, UserDefinedJavaClassMeta meta, UserDefinedJavaClassData data)
    {
        if (!checkClassCookings(parent.getLogChannel())) {
        	return null;
        }

        try
        {
            return cookedTransformClass.getConstructor(UserDefinedJavaClass.class, UserDefinedJavaClassMeta.class, UserDefinedJavaClassData.class).newInstance(parent, meta, data);
        }
        catch (Exception e)
        {
            KettleException kettleException = new KettleException(e.getMessage());
            kettleException.setStackTrace(new StackTraceElement[] {});
            cookErrors.add(kettleException);
            return null;
        }
    }

    public List<FieldInfo> getFieldInfo()
    {
        return Collections.unmodifiableList(fields);
    }

    public void replaceFields(List<FieldInfo> fields)
    {
        this.fields = fields;
        changed=true;
    }

    public List<UserDefinedJavaClassDef> getDefinitions()
    {
        return Collections.unmodifiableList(definitions);
    }

    public void replaceDefinitions(List<UserDefinedJavaClassDef> definitions)
    {
        this.definitions.clear();
        this.definitions.addAll(definitions);
        changed=true;
    }

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode);
    }

    public Object clone()
    {
        return super.clone();
    }

    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            Node definitionsNode = XMLHandler.getSubNode(stepnode, ElementNames.definitions.name());
            int nrDefinitions = XMLHandler.countNodes(definitionsNode, ElementNames.definition.name());

            for (int i = 0; i < nrDefinitions; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(definitionsNode, ElementNames.definition.name(), i);
                definitions.add(new UserDefinedJavaClassDef(ClassType.valueOf(XMLHandler.getTagValue(fnode, ElementNames.class_type.name())),
                        XMLHandler.getTagValue(fnode, ElementNames.class_name.name()), XMLHandler.getTagValue(fnode, ElementNames.class_source.name())));
            }

            Node fieldsNode = XMLHandler.getSubNode(stepnode, ElementNames.fields.name());
            int nrfields = XMLHandler.countNodes(fieldsNode, ElementNames.field.name());

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fieldsNode, ElementNames.field.name(), i); //$NON-NLS-1$
                fields.add(new FieldInfo(
                        XMLHandler.getTagValue(fnode, ElementNames.field_name.name()),
                        ValueMeta.getType(XMLHandler.getTagValue(fnode, ElementNames.field_type.name())),
                        Const.toInt(XMLHandler.getTagValue(fnode, ElementNames.field_length.name()), -1),
                        Const.toInt(XMLHandler.getTagValue(fnode, ElementNames.field_precision.name()), -1)));
            }
            
            infoStepDefinitions.clear();
            Node infosNode = XMLHandler.getSubNode(stepnode, ElementNames.info_steps.name());
            int nrInfos = XMLHandler.countNodes(infosNode, ElementNames.info_step.name());
            for (int i = 0; i < nrInfos; i++) {
            	Node infoNode = XMLHandler.getSubNodeByNr(infosNode, ElementNames.info_step.name(), i);
            	StepDefinition stepDefinition = new StepDefinition();
            	stepDefinition.tag = XMLHandler.getTagValue(infoNode, ElementNames.step_tag.name());
            	stepDefinition.stepName = XMLHandler.getTagValue(infoNode, ElementNames.step_name.name());
            	stepDefinition.description = XMLHandler.getTagValue(infoNode, ElementNames.step_description.name());
            	infoStepDefinitions.add(stepDefinition);
            }

            targetStepDefinitions.clear();
            Node targetsNode = XMLHandler.getSubNode(stepnode, ElementNames.target_steps.name());
            int nrTargets = XMLHandler.countNodes(targetsNode, ElementNames.target_step.name());
            for (int i = 0; i < nrTargets; i++) {
            	Node targetNode = XMLHandler.getSubNodeByNr(targetsNode, ElementNames.target_step.name(), i);
            	StepDefinition stepDefinition = new StepDefinition();
            	stepDefinition.tag = XMLHandler.getTagValue(targetNode, ElementNames.step_tag.name());
            	stepDefinition.stepName = XMLHandler.getTagValue(targetNode, ElementNames.step_name.name());
            	stepDefinition.description = XMLHandler.getTagValue(targetNode, ElementNames.step_description.name());
            	targetStepDefinitions.add(stepDefinition);
            }

            usageParameters.clear();
            Node parametersNode = XMLHandler.getSubNode(stepnode, ElementNames.usage_parameters.name());
            int nrParameters = XMLHandler.countNodes(parametersNode, ElementNames.usage_parameter.name());
            for (int i = 0; i < nrParameters; i++) {
            	Node parameterNode = XMLHandler.getSubNodeByNr(parametersNode, ElementNames.usage_parameter.name(), i);
            	UsageParameter usageParameter = new UsageParameter();
            	usageParameter.tag = XMLHandler.getTagValue(parameterNode, ElementNames.parameter_tag.name());
            	usageParameter.value = XMLHandler.getTagValue(parameterNode, ElementNames.parameter_value.name());
            	usageParameter.description = XMLHandler.getTagValue(parameterNode, ElementNames.parameter_description.name());
            	usageParameters.add(usageParameter);
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
        }
    }

    public void setDefault()
    {
        definitions.add(new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, BaseMessages.getString(PKG, "UserDefinedJavaClass.Script1"),
                "//AddDefaultCode" + Const.CR + Const.CR));
    }
    
    private boolean checkClassCookings(LogChannelInterface logChannel) {
    	boolean ok = cookedTransformClass!=null  && cookErrors.size() == 0;
        if (changed)
        {
            cookClasses();
            if (cookedTransformClass==null) {
	            if (cookErrors.size() > 0) {
	                logChannel.logDebug(BaseMessages.getString(PKG, "UserDefinedJavaClass.Exception.CookingError", cookErrors.get(0)));
	            }
                ok = false;
            } else {
            	ok = true;
            }
        }	
        return ok;
    }
    
    @Override
    public StepIOMetaInterface getStepIOMeta() {
        if (!checkClassCookings(getLog())) {
        	return super.getStepIOMeta();
        }

        try
        {
            Method getStepIOMeta = cookedTransformClass.getMethod("getStepIOMeta", UserDefinedJavaClassMeta.class);
            if (getStepIOMeta != null)
            {
                StepIOMetaInterface stepIoMeta =(StepIOMetaInterface)getStepIOMeta.invoke(null, this);
                if (stepIoMeta==null) {
                	return super.getStepIOMeta();
                }
                else 
                {
                	return stepIoMeta;
                }
            }
            else
            {
                return super.getStepIOMeta();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return super.getStepIOMeta();
        }
    }
    
	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StepDefinition stepDefinition : infoStepDefinitions) {
			stepDefinition.stepMeta = StepMeta.findStep(steps, stepDefinition.stepName);
		}
		for (StepDefinition stepDefinition : targetStepDefinitions) {
			stepDefinition.stepMeta = StepMeta.findStep(steps, stepDefinition.stepName);
		}
	}

    public void getFields(RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
            throws KettleStepException
    {
    	if (!checkClassCookings(getLog())) {
    		if (cookErrors.size()>0) {
    			throw new KettleStepException("Error initializing UserDefinedJavaClass to get fields: ", cookErrors.get(0));
    		} else {
    			return;
    		}
    	}

        try
        {
            Method getFieldsMethod = cookedTransformClass.getMethod("getFields", boolean.class, RowMetaInterface.class, String.class, RowMetaInterface[].class, StepMeta.class, VariableSpace.class, List.class);
            getFieldsMethod.invoke(null, clearingResultFields, row, originStepname, info, nextStep, space, fields);
        }
        catch (Exception e)
        {
            throw new KettleStepException("Error executing UserDefinedJavaClass.getFields(): ", e);
        }
    }

    public String getXML()
    {
        StringBuilder retval = new StringBuilder(300);

        retval.append(String.format("\n    <%s>", ElementNames.definitions.name()));
        for (UserDefinedJavaClassDef def : definitions)
        {
            retval.append(String.format("\n        <%s>", ElementNames.definition.name())); //$NON-NLS-1$
            retval.append("\n        ").append(XMLHandler.addTagValue(ElementNames.class_type.name(), def.getClassType().name()));
            retval.append("\n        ").append(XMLHandler.addTagValue(ElementNames.class_name.name(), def.getClassName()));
            retval.append("\n        ").append(XMLHandler.openTag(ElementNames.class_source.name()));
            retval.append(XMLHandler.buildCDATA(def.getSource())).append(XMLHandler.closeTag(ElementNames.class_source.name()));
            retval.append(String.format("\n        </%s>", ElementNames.definition.name())); //$NON-NLS-1$
        }
        retval.append(String.format("\n    </%s>", ElementNames.definitions.name()));

        retval.append(String.format("\n    <%s>", ElementNames.fields.name()));
        for (FieldInfo fi : fields)
        {
            retval.append(String.format("\n        <%s>", ElementNames.field.name())); //$NON-NLS-1$
            retval.append("\n        ").append(XMLHandler.addTagValue(ElementNames.field_name.name(), fi.name)); //$NON-NLS-1$
            retval.append("\n        ").append(XMLHandler.addTagValue(ElementNames.field_type.name(), ValueMeta.getTypeDesc(fi.type))); //$NON-NLS-1$
            retval.append("\n        ").append(XMLHandler.addTagValue(ElementNames.field_length.name(), fi.length)); //$NON-NLS-1$
            retval.append("\n        ").append(XMLHandler.addTagValue(ElementNames.field_precision.name(), fi.precision)); //$NON-NLS-1$
            retval.append(String.format("\n        </%s>", ElementNames.field.name())); //$NON-NLS-1$
        }
        retval.append(String.format("\n    </%s>", ElementNames.fields.name()));
        retval.append(XMLHandler.addTagValue(ElementNames.clear_result_fields.name(), clearingResultFields));

        // Add the XML for the info step definitions...
        //
    	retval.append(XMLHandler.openTag(ElementNames.info_steps.name()));
        for (StepDefinition stepDefinition : infoStepDefinitions) {
        	retval.append(XMLHandler.openTag(ElementNames.info_step.name()));
        	retval.append(XMLHandler.addTagValue(ElementNames.step_tag.name(), stepDefinition.tag));
        	retval.append(XMLHandler.addTagValue(ElementNames.step_name.name(), stepDefinition.stepMeta!=null ? stepDefinition.stepMeta.getName() : null));
        	retval.append(XMLHandler.addTagValue(ElementNames.step_description.name(), stepDefinition.description));
        	retval.append(XMLHandler.closeTag(ElementNames.info_step.name()));
        }
    	retval.append(XMLHandler.closeTag(ElementNames.info_steps.name()));

        // Add the XML for the target step definitions...
        //
    	retval.append(XMLHandler.openTag(ElementNames.target_steps.name()));
        for (StepDefinition stepDefinition : targetStepDefinitions) {
        	retval.append(XMLHandler.openTag(ElementNames.target_step.name()));
        	retval.append(XMLHandler.addTagValue(ElementNames.step_tag.name(), stepDefinition.tag));
        	retval.append(XMLHandler.addTagValue(ElementNames.step_name.name(), stepDefinition.stepMeta!=null ? stepDefinition.stepMeta.getName() : null));
        	retval.append(XMLHandler.addTagValue(ElementNames.step_description.name(), stepDefinition.description));
        	retval.append(XMLHandler.closeTag(ElementNames.target_step.name()));
        }
    	retval.append(XMLHandler.closeTag(ElementNames.target_steps.name()));

    	retval.append(XMLHandler.openTag(ElementNames.usage_parameters.name()));
        for (UsageParameter usageParameter : usageParameters) {
        	retval.append(XMLHandler.openTag(ElementNames.usage_parameter.name()));
        	retval.append(XMLHandler.addTagValue(ElementNames.parameter_tag.name(), usageParameter.tag));
        	retval.append(XMLHandler.addTagValue(ElementNames.parameter_value.name(), usageParameter.value));
        	retval.append(XMLHandler.addTagValue(ElementNames.parameter_description.name(), usageParameter.description));
        	retval.append(XMLHandler.closeTag(ElementNames.usage_parameter.name()));
        }
    	retval.append(XMLHandler.closeTag(ElementNames.usage_parameters.name()));

        return retval.toString();
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            int nrScripts = rep.countNrStepAttributes(id_step, ElementNames.class_name.name()); //$NON-NLS-1$
            for (int i = 0; i < nrScripts; i++)
            {
                definitions.add(new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.valueOf(rep
                        .getStepAttributeString(id_step, i, ElementNames.class_type.name())), rep.getStepAttributeString(id_step, i, ElementNames.class_name.name()), rep
                        .getStepAttributeString(id_step, i, ElementNames.class_source.name())));

            }

            int nrfields = rep.countNrStepAttributes(id_step, ElementNames.field_name.name()); //$NON-NLS-1$
            for (int i = 0; i < nrfields; i++)
            {
                fields.add(new FieldInfo(
                rep.getStepAttributeString(id_step, i, ElementNames.field_name.name()), //$NON-NLS-1$
                ValueMeta.getType(rep.getStepAttributeString(id_step, i, ElementNames.field_type.name())), //$NON-NLS-1$
                (int) rep.getStepAttributeInteger(id_step, i, ElementNames.field_length.name()), //$NON-NLS-1$
                (int) rep.getStepAttributeInteger(id_step, i, ElementNames.field_precision.name()))); //$NON-NLS-1$
            }
            
            clearingResultFields = rep.getStepAttributeBoolean(id_step, ElementNames.clear_result_fields.name());
            
            int nrInfos = rep.countNrStepAttributes(id_step, ElementNames.info_.name()+ElementNames.step_name.name()); //$NON-NLS-1$
            for (int i=0;i<nrInfos;i++) {
            	StepDefinition stepDefinition = new StepDefinition();
            	stepDefinition.tag = rep.getStepAttributeString(id_step, i, ElementNames.info_.name()+ElementNames.step_tag.name());
            	stepDefinition.stepName = rep.getStepAttributeString(id_step, i, ElementNames.info_.name()+ElementNames.step_name.name());
            	stepDefinition.description = rep.getStepAttributeString(id_step, i, ElementNames.info_.name()+ElementNames.step_description.name());
            	infoStepDefinitions.add(stepDefinition);
            }
            int nrTargets = rep.countNrStepAttributes(id_step, ElementNames.target_.name()+ElementNames.step_name.name()); //$NON-NLS-1$
            for (int i=0;i<nrTargets;i++) {
            	StepDefinition stepDefinition = new StepDefinition();
            	stepDefinition.tag = rep.getStepAttributeString(id_step, i, ElementNames.target_.name()+ElementNames.step_tag.name());
            	stepDefinition.stepName = rep.getStepAttributeString(id_step, i, ElementNames.target_.name()+ElementNames.step_name.name());
            	stepDefinition.description = rep.getStepAttributeString(id_step, i, ElementNames.target_.name()+ElementNames.step_description.name());
            	targetStepDefinitions.add(stepDefinition);
            }
            
            int nrParameters = rep.countNrStepAttributes(id_step, ElementNames.parameter_tag.name()); //$NON-NLS-1$
            for (int i=0;i<nrParameters;i++) {
            	UsageParameter usageParameter = new UsageParameter();
            	usageParameter.tag = rep.getStepAttributeString(id_step, i, ElementNames.parameter_tag.name());
            	usageParameter.value = rep.getStepAttributeString(id_step, i, ElementNames.parameter_value.name());
            	usageParameter.description = rep.getStepAttributeString(id_step, i, ElementNames.parameter_description.name());
            	usageParameters.add(usageParameter);
            }
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.Exception.UnexpectedErrorInReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {

            for (int i = 0; i < definitions.size(); i++)
            {
                UserDefinedJavaClassDef def = definitions.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.class_name.name(), def.getClassName());
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.class_source.name(), def.getSource());
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.class_type.name(), def.getClassType().name());
            }

            for (int i = 0; i < fields.size(); i++)
            {
                FieldInfo fi = fields.get(i);
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.field_name.name(), fi.name); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.field_type.name(), ValueMeta.getTypeDesc(fi.type)); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.field_length.name(), fi.length); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.field_precision.name(), fi.precision); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, ElementNames.clear_result_fields.name(), clearingResultFields); //$NON-NLS-1$
            
            for (int i=0;i<infoStepDefinitions.size();i++) {
            	StepDefinition stepDefinition = infoStepDefinitions.get(i);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.info_.name()+ElementNames.step_tag.name(), stepDefinition.tag);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.info_.name()+ElementNames.step_name.name(), stepDefinition.stepMeta!=null ? stepDefinition.stepMeta.getName() : null);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.info_.name()+ElementNames.step_description.name(), stepDefinition.description);
            }
            for (int i=0;i<targetStepDefinitions.size();i++) {
            	StepDefinition stepDefinition = targetStepDefinitions.get(i);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.target_.name()+ElementNames.step_tag.name(), stepDefinition.tag);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.target_.name()+ElementNames.step_name.name(), stepDefinition.stepMeta!=null ? stepDefinition.stepMeta.getName() : null);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.target_.name()+ElementNames.step_description.name(), stepDefinition.description);
            }
            
            for (int i=0;i<usageParameters.size();i++) {
            	UsageParameter usageParameter = usageParameters.get(i);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.parameter_tag.name(), usageParameter.tag);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.parameter_value.name(), usageParameter.value);
            	rep.saveStepAttribute(id_transformation, id_step, i, ElementNames.parameter_description.name(), usageParameter.description);
            }
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[],
            RowMetaInterface info)
    {
        CheckResult cr;

        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.CheckResult.ConnectedStepOK2"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "UserDefinedJavaClassMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
            remarks.add(cr);
        }
    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        UserDefinedJavaClass userDefinedJavaClass = new UserDefinedJavaClass(stepMeta, stepDataInterface, cnr, transMeta, trans);
        if (trans.hasHaltedSteps())
        {
            return null;
        }
        
        return userDefinedJavaClass;
    }

    public StepDataInterface getStepData()
    {
        return new UserDefinedJavaClassData();
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }

	/**
	 * @return the clearingResultFields
	 */
	public boolean isClearingResultFields() {
		return clearingResultFields;
	}

	/**
	 * @param clearingResultFields the clearingResultFields to set
	 */
	public void setClearingResultFields(boolean clearingResultFields) {
		this.clearingResultFields = clearingResultFields;
	}

	/**
	 * @return the infoStepDefinitions
	 */
	public List<StepDefinition> getInfoStepDefinitions() {
		return infoStepDefinitions;
	}

	/**
	 * @param infoStepDefinitions the infoStepDefinitions to set
	 */
	public void setInfoStepDefinitions(List<StepDefinition> infoStepDefinitions) {
		this.infoStepDefinitions = infoStepDefinitions;
	}

	/**
	 * @return the targetStepDefinitions
	 */
	public List<StepDefinition> getTargetStepDefinitions() {
		return targetStepDefinitions;
	}

	/**
	 * @param targetStepDefinitions the targetStepDefinitions to set
	 */
	public void setTargetStepDefinitions(List<StepDefinition> targetStepDefinitions) {
		this.targetStepDefinitions = targetStepDefinitions;
	}
	
	@Override
	public boolean excludeFromRowLayoutVerification() {
		return true;
	}

	/**
	 * @return the usageParameters
	 */
	public List<UsageParameter> getUsageParameters() {
		return usageParameters;
	}

	/**
	 * @param usageParameters the usageParameters to set
	 */
	public void setUsageParameters(List<UsageParameter> usageParameters) {
		this.usageParameters = usageParameters;
	}
}

