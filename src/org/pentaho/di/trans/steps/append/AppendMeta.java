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

package org.pentaho.di.trans.steps.append;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;

/**
 * @author Sven Boden
 * @since  3-june-2007
 */
public class AppendMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = Append.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
    public AppendMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public Object clone()
	{
		AppendMeta retval = (AppendMeta)super.clone();

        return retval;
	}
	
	public String getXML()
	{
        StringBuilder retval = new StringBuilder();

        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
		retval.append(XMLHandler.addTagValue("head_name", infoStreams.get(0).getStepname()));	//$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("tail_name", infoStreams.get(1).getStepname()));	//$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            StreamInterface headStream = infoStreams.get(0);
            StreamInterface tailStream = infoStreams.get(1);
			headStream.setSubject( XMLHandler.getTagValue(stepnode, "head_name") );  //$NON-NLS-1$
			tailStream.setSubject( XMLHandler.getTagValue(stepnode, "tail_name") );  //$NON-NLS-1$			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "AppendMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
	}


	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            StreamInterface headStream = infoStreams.get(0);
            StreamInterface tailStream = infoStreams.get(1);
            headStream.setSubject( rep.getStepAttributeString (id_step, "head_name") );  //$NON-NLS-1$
            tailStream.setSubject( rep.getStepAttributeString (id_step, "tail_name") );  //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AppendMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            StreamInterface headStream = infoStreams.get(0);
            StreamInterface tailStream = infoStreams.get(1);
			rep.saveStepAttribute(id_transformation, id_step, "head_name", headStream.getStepname()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "tail_name", tailStream.getStepname()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "AppendMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
    @Override
    public void searchInfoAndTargetSteps(List<StepMeta> steps) {
      for (StreamInterface stream : getStepIOMeta().getInfoStreams()) {
        stream.setStepMeta(StepMeta.findStep(steps, (String) stream.getSubject()));
      }
    }

	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}
    
    public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // We don't have any input fields here in "r" as they are all info fields.
        // So we just take the info fields.
        //
        if (info!=null)
        {
        	if ( info.length > 0 && info[0]!=null) {            
                r.mergeRowMeta(info[0]);
        	}
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info)
	{
		CheckResult cr;
		
        List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
        StreamInterface headStream = infoStreams.get(0);
        StreamInterface tailStream = infoStreams.get(1);
		
		if (headStream.getStepname()!=null && tailStream.getStepname()!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AppendMeta.CheckResult.SourceStepsOK"), stepMeta);
			remarks.add(cr);
		}
		else if (headStream.getStepname()==null && tailStream.getStepname()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "AppendMeta.CheckResult.SourceStepsMissing"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "AppendMeta.CheckResult.OneSourceStepMissing"), stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new Append(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new AppendData();
	}
	
	   /**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta() {
        if (ioMeta==null) {

            ioMeta = new StepIOMeta(true, true, false, false, false, false);
        
            ioMeta.addStream( new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "AppendMeta.InfoStream.FirstStream.Description"), StreamIcon.INFO, null) );
            ioMeta.addStream( new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "AppendMeta.InfoStream.SecondStream.Description"), StreamIcon.INFO, null) );
        }
        
        return ioMeta;
    }

    @Override
    public void resetStepIoMeta() {
    }
    
    public TransformationType[] getSupportedTransformationTypes() {
      return new TransformationType[] { TransformationType.Normal, };
    }
}