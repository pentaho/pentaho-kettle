package org.pentaho.di.trans.steps.streamMerger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;


@Step(	
		id = "StreamMerger",
		image = "Merge.svg",
		name="Stream Merge",
		description = "Merge multiple streams with different schema into one unified schema, the fields data type has to match",
		categoryDescription=""
)
public class StreamMergerStepMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = org.pentaho.di.trans.steps.streamMerger.StreamMergerStepMeta.class; // for i18n purposes

    private ArrayList<String> stepsToMerge = new ArrayList<String>();

	public StreamMergerStepMeta() {
		super(); 
	}


    public boolean excludeFromRowLayoutVerification() {
        return true;
    }
	

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new StreamMergerStepDialog(shell, meta, transMeta, name);
	}


	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans disp) {
		return new StreamMergerStep(stepMeta, stepDataInterface, cnr, transMeta, disp);
	}


	public StepDataInterface getStepData() {
		return new StreamMergerStepData();
	}

	public void setDefault() {
		// intentionally empty
	}

    public String[] getStepsToMerge() {
        if (stepsToMerge == null) {
            return new String[0];
        } else {
            return stepsToMerge.toArray(new String[stepsToMerge.size()]);
        }

    }

    public int getNumberOfSteps() {
        if (stepsToMerge == null) {
            return 0;
        } else {
            return stepsToMerge.size();
        }
    }


    public void setStepsToMerge(String[] arrayOfSteps) {
        stepsToMerge = new ArrayList<String>();
        Collections.addAll(stepsToMerge, arrayOfSteps);
    }

	public Object clone() {
		Object retval = super.clone();
		return retval;
	}
	
	public String getXML() throws KettleValueException {
		StringBuilder xml = new StringBuilder();
        xml.append( "    <steps>" + Const.CR );
        for (String stepName : stepsToMerge) {
            xml.append( "      <step>" + Const.CR );
            xml.append( "        " + XMLHandler.addTagValue( "name", stepName ) );
            xml.append( "        </step>" + Const.CR );
        }
        xml.append("    </steps>" + Const.CR);
		return xml.toString();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {

        readData(stepnode);

	}

    private void readData( Node stepnode) throws KettleXMLException {
        try {
            //TODO put the strings in a config file or make constants in this file
            Node steps = XMLHandler.getSubNode( stepnode, "steps" );
            int nrsteps = XMLHandler.countNodes( steps, "step" );

            stepsToMerge.clear();

            // we need to add a stream for each step we want to merge to ensure it gets treated as an info stream
            for ( int i = 0; i < nrsteps; i++ ) {
                getStepIOMeta().addStream(
                        new Stream( StreamInterface.StreamType.INFO, null, "Streams to Merge", StreamIcon.INFO, null ) );
            }

            List<StreamInterface> infoStreams = getStepIOMeta().getInfoStreams();
            for ( int i = 0; i < nrsteps; i++ ) {
                Node fnode = XMLHandler.getSubNodeByNr( steps, "step", i );
                String name = XMLHandler.getTagValue(fnode, "name");
                stepsToMerge.add(name);
                infoStreams.get(i).setSubject(name);
            }
        } catch ( Exception e ) {
            throw new KettleXMLException( "Unable to load step info from XML", e );
        }
    }

	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {

		/*
		 * We don't have any input fields so we ingore inputRowMeta
		 */
        try {
            StreamMerger schemaMapping = new StreamMerger(info);  // compute the union of the info fields being passed in
            RowMetaInterface base = schemaMapping.getRowMeta();

            for ( int i = 0; i < base.size(); i++ ) {
                base.getValueMeta( i ).setOrigin( name );
            }
            inputRowMeta.mergeRowMeta(base);
        } catch (KettlePluginException e) {
            throw new KettleStepException("Kettle plugin exception trying to resolve fields");
        }
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore)  {
		
		CheckResult cr;

		// See if there are input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Found " + input.length + " input steps", stepMeta);
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input steps configured", stepMeta);
			remarks.add(cr);
		}	
	}

	@Override
	public void searchInfoAndTargetSteps( List<StepMeta> steps ) {
		for ( StreamInterface stream : getStepIOMeta().getInfoStreams() ) {
			stream.setStepMeta( StepMeta.findStep( steps, (String) stream.getSubject() ) );
		}
	}

    public void resetStepIoMeta() {
        // Do nothing, don't reset as there is no need to do this.
    }

	public void wipeStepIoMeta() {
		ioMeta = null;
	}


}
