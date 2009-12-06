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
 
package org.pentaho.di.trans.steps.javafilter;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
import org.w3c.dom.Node;




/**
 * Contains the meta-data for the java filter step: calculates conditions using Janino
 * 
 * Created on 30-oct-2009
 */
public class JavaFilterMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = JavaFilterMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** The formula calculations to be performed */
    private String condition;
    
    public JavaFilterMeta()
	{
		super(); // allocate BaseStepMeta
	}

    public String getCondition() {
		return condition;
	}
    
    public void setCondition(String condition) {
		this.condition = condition;
	}
     
    public void allocate(int nrCalcs)
    {
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,  Map<String, Counter> counters) throws KettleXMLException
	{
		StreamInterface[] targetStreams = getStepIOMeta().getTargetStreams();

		targetStreams[0].setStepname( XMLHandler.getTagValue(stepnode, "send_true_to") ); //$NON-NLS-1$
		targetStreams[1].setStepname( XMLHandler.getTagValue(stepnode, "send_false_to") ); //$NON-NLS-1$

		condition = XMLHandler.getTagValue(stepnode, "condition");
	}
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        StreamInterface[] targetStreams = getStepIOMeta().getTargetStreams();
		retval.append(XMLHandler.addTagValue("send_true_to", targetStreams[0].getStepname()));		 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("send_false_to", targetStreams[1].getStepname()));		 //$NON-NLS-1$
        
        retval.append(XMLHandler.addTagValue("condition", condition));

        return retval.toString();
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	JavaFilterMeta m = (JavaFilterMeta)obj;
            return (getXML() == m.getXML());
        }

        return false;
    }        
    
	public Object clone()
	{
		JavaFilterMeta retval = (JavaFilterMeta) super.clone();
		return retval;
	}

	public void setDefault()
	{
        condition = "true"; 
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
	{
		StreamInterface[] targetStreams = getStepIOMeta().getTargetStreams();

		targetStreams[0].setStepname( rep.getStepAttributeString (id_step, "send_true_to") );  //$NON-NLS-1$
		targetStreams[1].setStepname( rep.getStepAttributeString (id_step, "send_false_to") );  //$NON-NLS-1$

		condition = rep.getStepAttributeString(id_step, "condition");
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		StreamInterface[] targetStreams = getStepIOMeta().getTargetStreams();

		rep.saveStepAttribute(id_transformation, id_step, "send_true_to", targetStreams[0].getStepname()); //$NON-NLS-1$
		rep.saveStepAttribute(id_transformation, id_step, "send_false_to", targetStreams[1].getStepname()); //$NON-NLS-1$

		rep.saveStepAttribute(id_transformation, id_step, "condition", condition);
	}

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		
		StreamInterface[] targetStreams = getStepIOMeta().getTargetStreams();

		if (targetStreams[0].getStepname()!=null && targetStreams[1].getStepname()!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.BothTrueAndFalseStepSpecified"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		if (targetStreams[0].getStepname()==null && targetStreams[1].getStepname()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.NeitherTrueAndFalseStepSpecified"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.PlsSpecifyBothTrueAndFalseStep"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
				
		if ( targetStreams[0].getStepname()!=null )
		{
			int trueTargetIdx = Const.indexOfString(targetStreams[0].getStepname(), output);
			if ( trueTargetIdx < 0 )
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, 
						             BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.TargetStepInvalid", "true", targetStreams[0].getStepname() ), 
						             stepinfo);
				remarks.add(cr);
			}
		}

		if ( targetStreams[1].getStepname()!=null )
		{
			int falseTargetIdx = Const.indexOfString(targetStreams[1].getStepname(), output);
			if ( falseTargetIdx < 0 )
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, 
						             BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.TargetStepInvalid", "false", targetStreams[1].getStepname()), 
						             stepinfo);
				remarks.add(cr);
			}
		}
		
		if (Const.isEmpty(condition))
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.NoConditionSpecified"), stepinfo);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.ConditionSpecified"), stepinfo); //$NON-NLS-1$
		}
		remarks.add(cr);		
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.StepReceivingFields",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			// What fields are used in the condition?
			// TODO: verify condition, parse it
			// 
		}
		else
		{
			error_message=BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.CouldNotReadFieldsFromPreviousStep")+Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "JavaFilterMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new JavaFilter(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new JavaFilterData();
	}


	/**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta() {
    	if (ioMeta==null) {

    		ioMeta = new StepIOMeta(true, true, false, false);
    	
	    	ioMeta.addStream( new Stream(StreamType.TARGET, BaseMessages.getString(PKG, "JavaFilterMeta.InfoStream.True.Description")) );
	    	ioMeta.addStream( new Stream(StreamType.TARGET, BaseMessages.getString(PKG, "JavaFilterMeta.InfoStream.False.Description")) );
    	}
    	
    	return ioMeta;
    }
}
