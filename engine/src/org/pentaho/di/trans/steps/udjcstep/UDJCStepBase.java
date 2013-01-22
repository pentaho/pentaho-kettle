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

package org.pentaho.di.trans.steps.udjcstep;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.pentaho.di.trans.steps.udjcstep.UDJCStepMetaBase.FieldInfo;
import org.pentaho.di.trans.steps.userdefinedjavaclass.FieldHelper;
import org.pentaho.di.trans.steps.userdefinedjavaclass.StepDefinition;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UsageParameter;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassData;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;

public class UDJCStepBase extends BaseStep implements StepInterface
{
	private static Class<?> PKG = UserDefinedJavaClassMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	protected final UDJCStepBase parent;
    protected final UDJCStepMetaBase meta;
    protected final UserDefinedJavaClassData data;
	
    public UDJCStepBase(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
        parent = this;
        meta = (UDJCStepMetaBase)(stepMeta.getStepMetaInterface());
        data = (UserDefinedJavaClassData) stepDataInterface;

        try
        {
            data.inputRowMeta = getTransMeta().getPrevStepFields(getStepMeta()).clone();
            data.outputRowMeta = getTransMeta().getThisStepFields(getStepMeta(), null, data.inputRowMeta.clone());
            
            data.parameterMap = new HashMap<String, String>();
            for (UsageParameter par : meta.getUsageParameters()) {
            	if (par.tag!=null && par.value!=null) {
            		data.parameterMap.put(par.tag, par.value);
            	}
            }
            
            data.infoMap = new HashMap<String, String>();
            for (StepDefinition stepDefinition : meta.getInfoStepDefinitions()) {
            	if (stepDefinition.tag!=null && stepDefinition.stepMeta!=null && stepDefinition.stepMeta.getName()!=null) {
            		data.infoMap.put(stepDefinition.tag, stepDefinition.stepMeta.getName());
            	}
            }

            data.targetMap = new HashMap<String, String>();
            for (StepDefinition stepDefinition : meta.getTargetStepDefinitions()) {
            	if (stepDefinition.tag!=null && stepDefinition.stepMeta!=null && stepDefinition.stepMeta.getName()!=null) {
            		data.targetMap.put(stepDefinition.tag, stepDefinition.stepMeta.getName());
            	}
            }
        }
        catch (KettleStepException e)
        {
            e.printStackTrace();
            //throw e;
        }
    }
    
    public boolean initImpl(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface)
    {
        return super.init(stepMetaInterface, stepDataInterface);
    }
    
    public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface)
    {
        if(!super.init(stepMetaInterface, stepDataInterface)) {
        	return false;
        }
        
        try
        {
            data.inputRowMeta = getTransMeta().getPrevStepFields(getStepMeta()).clone();
            data.outputRowMeta = getTransMeta().getThisStepFields(getStepMeta(), null, data.inputRowMeta.clone());
            
            data.parameterMap = new HashMap<String, String>();
            for (UsageParameter par : meta.getUsageParameters()) {
            	if (par.tag!=null && par.value!=null) {
            		data.parameterMap.put(par.tag, par.value);
            	}
            }
            
            data.infoMap = new HashMap<String, String>();
            for (StepDefinition stepDefinition : meta.getInfoStepDefinitions()) {
            	if (stepDefinition.tag!=null && stepDefinition.stepMeta!=null && stepDefinition.stepMeta.getName()!=null) {
            		data.infoMap.put(stepDefinition.tag, stepDefinition.stepMeta.getName());
            	}
            }

            data.targetMap = new HashMap<String, String>();
            for (StepDefinition stepDefinition : meta.getTargetStepDefinitions()) {
            	if (stepDefinition.tag!=null && stepDefinition.stepMeta!=null && stepDefinition.stepMeta.getName()!=null) {
            		data.targetMap.put(stepDefinition.tag, stepDefinition.stepMeta.getName());
            	}
            }
            return true;
        }
        catch (KettleStepException e)
        {
            e.printStackTrace();
            return false;
        }    	
    }
    
        public static String[] getInfoSteps()
        {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public static void getFields(boolean clearResultFields, RowMetaInterface row, String originStepname, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, List<?> fields)
        throws KettleStepException
        {
        	if (clearResultFields) {
        		row.clear();
        	}
            for (FieldInfo fi : (List<FieldInfo>)fields)
            {
                ValueMetaInterface v;
                v = new ValueMeta(fi.name, fi.type);
                v.setLength(fi.length);
                v.setPrecision(fi.precision);
                v.setOrigin(originStepname);
                row.addValueMeta(v);
            }
        }
        
        public static StepIOMetaInterface getStepIOMeta(UDJCStepMetaBase meta) {
        	StepIOMetaInterface ioMeta = new StepIOMeta(true, true, true, false, true, true);
        	
        	for (StepDefinition stepDefinition : meta.getInfoStepDefinitions()) {
        		ioMeta.addStream(new Stream(StreamType.INFO, stepDefinition.stepMeta, stepDefinition.description, StreamIcon.INFO, null));
        	}
        	for (StepDefinition stepDefinition : meta.getTargetStepDefinitions()) {
        		ioMeta.addStream(new Stream(StreamType.TARGET, stepDefinition.stepMeta, stepDefinition.description, StreamIcon.TARGET, null));
        	}
        	
        	return ioMeta;
        }
        
        public String getParameter(String tag) {
        	if (tag==null) return null;
        	return environmentSubstitute(data.parameterMap.get(tag));
        }

        public RowSet findInfoRowSet(String tag) throws KettleException {
        	if (tag==null) return null;
        	String stepname = data.infoMap.get(tag);
        	if (Const.isEmpty(stepname)) {
        		throw new KettleException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindInfoStepNameForTag", tag));
        	}
        	RowSet rowSet = findInputRowSet(stepname);
        	if (rowSet==null) {
        		throw new KettleException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindInfoRowSetForStep", stepname));
        	}
        	return rowSet;
        }
        
        public RowSet findTargetRowSet(String tag) throws KettleException {
        	if (tag==null) return null;
        	String stepname = data.targetMap.get(tag);
        	if (Const.isEmpty(stepname)) {
        		throw new KettleException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindTargetStepNameForTag", tag));
        	}
        	RowSet rowSet = findOutputRowSet(stepname);
        	if (rowSet==null) {
        		throw new KettleException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindTargetRowSetForStep", stepname));
        	}
        	return rowSet;
        }

    	private final Map<String, FieldHelper> inFieldHelpers = new HashMap<String, FieldHelper>();
    	private final Map<String, FieldHelper> infoFieldHelpers = new HashMap<String, FieldHelper>();
    	private final Map<String, FieldHelper> outFieldHelpers = new HashMap<String, FieldHelper>();
        public enum Fields { In, Out, Info; }
        
        public FieldHelper get(Fields type, String name) throws KettleStepException
        {
        	FieldHelper fh;
        	switch (type) {
        		case In:
        			fh = inFieldHelpers.get(name);
        			if (fh == null) {
        				try {
    						fh = new FieldHelper(data.inputRowMeta, name);
    					} catch (IllegalArgumentException e) {
    						throw new KettleStepException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindFieldHelper", type.name(), name));
    					}
    					inFieldHelpers.put(name, fh);
        			}
        			break;
        		case Out:
        			fh = outFieldHelpers.get(name);
        			if (fh == null) {
        				try {
    						fh = new FieldHelper(data.outputRowMeta, name);
    					} catch (IllegalArgumentException e) {
    						throw new KettleStepException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindFieldHelper", type.name(), name));
    					}
    					outFieldHelpers.put(name, fh);
        			}
        			break;
        		case Info:
        			fh = infoFieldHelpers.get(name);
        			if (fh == null) {
        				RowMetaInterface rmi = getTransMeta().getPrevInfoFields(getStepname());
        				try {
    						fh = new FieldHelper(rmi, name);
    					} catch (IllegalArgumentException e) {
    						throw new KettleStepException(BaseMessages.getString(PKG, "TransformClassBase.Exception.UnableToFindFieldHelper", type.name(), name));
    					}
    					infoFieldHelpers.put(name, fh);
        			}
        			break;
        		default:
        			throw new KettleStepException(BaseMessages.getString(PKG, "TransformClassBase.Exception.InvalidFieldsType", type.name(), name));    	}
        	return fh;
        }
        
        public Object[] createOutputRow(Object[] inputRow, int outputRowSize)
        {
        	if (meta.isClearingResultFields()) 
        		return RowDataUtil.allocateRowData(outputRowSize);
        	else
        		return RowDataUtil.createResizedCopy(inputRow, outputRowSize);
        }
    
}