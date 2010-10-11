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
 
package org.pentaho.di.trans.step;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.CheckResultSourceInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceHolderInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.shared.SharedObjectBase;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.StepPlugin;
import org.pentaho.di.trans.TransMeta;
import org.w3c.dom.Node;



/**
 * This class contains everything that is needed to define a step.
 * 
 * @since 27-mei-2003
 * @author Matt
 *
 */
public class StepMeta extends SharedObjectBase implements Cloneable, Comparable<StepMeta>, GUIPositionInterface, SharedObjectInterface, 
                                                          CheckResultSourceInterface, ResourceExportInterface, ResourceHolderInterface
{
	public static final String XML_TAG = "step";

	public static final Object STRING_ID_MAPPING        = "Mapping";
	public static final Object STRING_ID_MAPPING_INPUT  = "MappingInput";
	public static final Object STRING_ID_MAPPING_OUTPUT = "MappingOutput";
    
    private String        stepid;   // --> StepPlugin.id
	private String        stepname;
	private StepMetaInterface stepMetaInterface;
	private boolean       selected;
	private boolean       distributes;
	private int           copies;
	private Point         location;
	private boolean       drawstep;
	private String        description;
	private boolean       terminator;
	
    private StepPartitioningMeta stepPartitioningMeta;
    private StepPartitioningMeta targetStepPartitioningMeta;
    
    private ClusterSchema        clusterSchema;
    private String               clusterSchemaName; // temporary to resolve later.
    
    private StepErrorMeta stepErrorMeta;
    
	// OK, we need to explain to this running step that we expect input from remote steps.
	// This only happens when the previous step "repartitions". (previous step has different
	// partitioning method than this one)
	//
	// So here we go, let's create List members for the remote input and output step
	//
	
	/** These are the remote input steps to read from, one per host:port combination */
	private List<RemoteStep> remoteInputSteps;

	/** These are the remote output steps to write to, one per host:port combination */
	private List<RemoteStep> remoteOutputSteps;
	
	private long id;

    
    /**
     * @param stepid The ID of the step: this is derived information, you can also use the constructor without stepid.
     *               This constructor will be deprecated soon.
     * @param stepname The name of the new step
     * @param stepMetaInterface The step metadata interface to use (TextFileInputMeta, etc)
     */
    public StepMeta(String stepid, String stepname, StepMetaInterface stepMetaInterface)
    {
        this(stepname, stepMetaInterface);
        if (this.stepid==null) this.stepid = stepid;
    }
    
    /**
     * @param stepname The name of the new step
     * @param stepMetaInterface The step metadata interface to use (TextFileInputMeta, etc)
     */
	public StepMeta(String stepname, StepMetaInterface stepMetaInterface)
	{
        if (stepMetaInterface!=null)
        {
            this.stepid = StepLoader.getInstance().getStepPluginID(stepMetaInterface);
        }
		this.stepname          = stepname;
		this.stepMetaInterface = stepMetaInterface;
        
		selected    = false;
		distributes  = true;
		copies      = 1;
		location    = new Point(0,0);
		drawstep    = false;
		description = null;
        stepPartitioningMeta = new StepPartitioningMeta();
        // targetStepPartitioningMeta = new StepPartitioningMeta();
        
        clusterSchema = null; // non selected by default.

        remoteInputSteps = new ArrayList<RemoteStep>();
        remoteOutputSteps = new ArrayList<RemoteStep>();
	}
        
	public StepMeta()
	{
		this((String)null, (String)null, (StepMetaInterface)null);
	}

	public String getXML() throws KettleException
	{
		StringBuffer retval=new StringBuffer(200); //$NON-NLS-1$
		
		retval.append("  <").append(XML_TAG).append('>').append(Const.CR); //$NON-NLS-1$
		retval.append("    ").append(XMLHandler.addTagValue("name",        getName()) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("type",        getStepID()) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("description", description) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("distribute",  distributes) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("copies",      copies) ); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append( stepPartitioningMeta.getXML() );
        if (targetStepPartitioningMeta!=null) {
        	retval.append( XMLHandler.openTag("target_step_partitioning")).append(targetStepPartitioningMeta.getXML()).append( XMLHandler.closeTag("target_step_partitioning"));
        }

        retval.append( stepMetaInterface.getXML() );
        retval.append("     ").append(XMLHandler.addTagValue("cluster_schema", clusterSchema==null?"":clusterSchema.getName()));
        
        retval.append(" <remotesteps>");
        // Output the remote input steps
        List<RemoteStep> inputSteps = new ArrayList<RemoteStep>(remoteInputSteps);
        Collections.sort(inputSteps); // sort alphabetically, making it easier to compare XML files
        retval.append("   <input>");
        for (RemoteStep remoteStep : inputSteps) {
        	retval.append("      ").append(remoteStep.getXML()).append(Const.CR);
        }
        retval.append("   </input>");

        // Output the remote output steps
        List<RemoteStep> outputSteps = new ArrayList<RemoteStep>(remoteOutputSteps);
        Collections.sort(outputSteps); // sort alphabetically, making it easier to compare XML files
        retval.append("   <output>");
        for (RemoteStep remoteStep : outputSteps) {
        	retval.append("      ").append(remoteStep.getXML()).append(Const.CR);
        }
        retval.append("   </output>");
        retval.append(" </remotesteps>");
        
		retval.append("    <GUI>").append(Const.CR); //$NON-NLS-1$
		retval.append("      <xloc>").append(location.x).append("</xloc>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      <yloc>").append(location.y).append("</yloc>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      <draw>").append((drawstep?"Y":"N")).append("</draw>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		retval.append("      </GUI>").append(Const.CR); //$NON-NLS-1$
		retval.append("    </"+XML_TAG+">").append(Const.CR).append(Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}

    
	/**
	 * Read the step data from XML
	 * 
	 * @param stepnode The XML step node.
	 * @param databases A list of databases
	 * @param counters A map with all defined counters.
	 * 
	 */
	public StepMeta(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
        this();
        LogWriter log = LogWriter.getInstance();
		StepLoader steploader = StepLoader.getInstance();

		try
		{
			stepname = XMLHandler.getTagValue(stepnode, "name"); //$NON-NLS-1$
			stepid   = XMLHandler.getTagValue(stepnode, "type"); //$NON-NLS-1$
	
			log.logDebug("StepMeta()", Messages.getString("StepMeta.Log.LookingForTheRightStepNode",stepname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	
			// Create a new StepMetaInterface object...
			StepPlugin sp = steploader.findStepPluginWithID(stepid);
            if (sp!=null)
            {
                stepMetaInterface = BaseStep.getStepInfo(sp, steploader);
                stepid=sp.getID()[0]; // revert to the default in case we loaded an alternate version
            }
            else
            {
                throw new KettleStepLoaderException(Messages.getString("StepMeta.Exception.UnableToLoadClass",stepid)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
			
			// Load the specifics from XML...
			if (stepMetaInterface!=null)
			{
				stepMetaInterface.loadXML(stepnode, databases, counters);
			}
			log.logDebug("StepMeta()", Messages.getString("StepMeta.Log.SpecificLoadedStep",stepname)); //$NON-NLS-1$ //$NON-NLS-2$
			
			/* Handle info general to all step types...*/
			description    = XMLHandler.getTagValue(stepnode, "description"); //$NON-NLS-1$
			copies         = Const.toInt(XMLHandler.getTagValue(stepnode, "copies"), 1); //$NON-NLS-1$
			String sdistri = XMLHandler.getTagValue(stepnode, "distribute"); //$NON-NLS-1$
			distributes     = "Y".equalsIgnoreCase(sdistri); //$NON-NLS-1$
			if (sdistri==null) distributes=true; // default=distribute
	
			// Handle GUI information: location & drawstep?
			String xloc, yloc;
			int x,y;
			xloc=XMLHandler.getTagValue(stepnode, "GUI", "xloc"); //$NON-NLS-1$ //$NON-NLS-2$
			yloc=XMLHandler.getTagValue(stepnode, "GUI", "yloc"); //$NON-NLS-1$ //$NON-NLS-2$
			try{ x=Integer.parseInt(xloc); } catch(Exception e) { x=0; }
			try{ y=Integer.parseInt(yloc); } catch(Exception e) { y=0; }
			location=new Point(x,y);
			
			drawstep = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "GUI", "draw")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            
            // The partitioning information?
			//
            Node partNode = XMLHandler.getSubNode(stepnode, "partitioning");
            stepPartitioningMeta = new StepPartitioningMeta(partNode);
            
            // Target partitioning information?
            //
            Node targetPartNode = XMLHandler.getSubNode(stepnode, "target_step_partitioning");
            partNode = XMLHandler.getSubNode(targetPartNode, "partitioning");
            if (partNode!=null) {
            	targetStepPartitioningMeta = new StepPartitioningMeta(partNode);
            }
            
            clusterSchemaName = XMLHandler.getTagValue(stepnode, "cluster_schema"); // resolve to clusterSchema later

            // The remote input and output steps...
            Node remotestepsNode = XMLHandler.getSubNode(stepnode, "remotesteps");
            Node inputNode = XMLHandler.getSubNode(remotestepsNode, "input");
            int nrInput = XMLHandler.countNodes(inputNode, RemoteStep.XML_TAG);
            for (int i=0;i<nrInput;i++) {
            	remoteInputSteps.add( new RemoteStep( XMLHandler.getSubNodeByNr(inputNode, RemoteStep.XML_TAG, i) ) );
            	
            }
            Node outputNode = XMLHandler.getSubNode(remotestepsNode, "output");
            int nrOutput = XMLHandler.countNodes(outputNode, RemoteStep.XML_TAG);
            for (int i=0;i<nrOutput;i++) {
            	remoteOutputSteps.add( new RemoteStep( XMLHandler.getSubNodeByNr(outputNode, RemoteStep.XML_TAG, i) ) );
            }
            
			log.logDebug("StepMeta()", Messages.getString("StepMeta.Log.EndOfReadXML")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("StepMeta.Exception.UnableToLoadStepInfo")+e.toString(), e); //$NON-NLS-1$
		}
	}
    
    /**
     * Resolves the name of the cluster loaded from XML/Repository to the correct clusterSchema object
     * @param clusterSchemas The list of clusterSchemas to reference.
     */
    public void setClusterSchemaAfterLoading(List<ClusterSchema> clusterSchemas)
    {
        if (clusterSchemaName==null) return;
        for (ClusterSchema look : clusterSchemas)
        {
            if (look.getName().equals(clusterSchemaName)) clusterSchema=look;
        }
    }

	public long getID()
	{
		return id;
	}
	
	public void setID(long id)
	{
		this.id = id;
	}
	
	/**
	 * See wether or not the step is drawn on the canvas.
	 * 
	 * @return True if the step is drawn on the canvas.
	 */
	public boolean isDrawn()
	{
		return drawstep;
	}
	/**
	 * See wether or not the step is drawn on the canvas.
	 * Same as isDrawn(), but needed for findMethod(StepMeta, drawstep)
	 * called by StringSearcher.findMetaData(). Otherwise findMethod() returns
	 * org.pentaho.di.trans.step.StepMeta.drawStep() instead of isDrawn().
	 * @return True if the step is drawn on the canvas.
	 */
	public boolean isDrawStep()
	{
		return drawstep;
	}
	
	/**
	 * Sets the draw attribute of the step so that it will be drawn on the canvas.
	 *  
	 * @param draw True if you want the step to show itself on the canvas, False if you don't.
	 */
	public void setDraw(boolean draw)
	{
		drawstep=draw;
		setChanged();
	}
	
	/**
	 * Sets the number of parallel copies that this step will be launched with.
	 *  
	 * @param c The number of copies.
	 */
	public void setCopies(int c)
	{
		if (copies!=c) setChanged();
		copies=c;
	}
	
	/**
	 * Get the number of copies to start of a step.
	 * This takes into account the partitioning logic.
	 * @return the number of step copies to start.
	 */
	public int getCopies()
	{
		// If the step is partitioned, that's going to determine the number of copies, nothing else...
		//
        if (isPartitioned() && getStepPartitioningMeta().getPartitionSchema()!=null)
        {
            List<String> partitionIDs = getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
            if (partitionIDs!=null && partitionIDs.size()>0) // these are the partitions the step can "reach"
            {
                return partitionIDs.size();
            }
        }

		return copies;
	}

	public void drawStep()
	{
		setDraw(true);
		setChanged();
	}

	public void hideStep()
	{
		setDraw(false);
		setChanged();
	}
	
	/**
	 * Two steps are equal if their names are equal.
	 * @return true if the two steps are equal.
	 */
	public boolean equals(Object obj)
	{
		if (obj==null) return false;
		StepMeta stepMeta = (StepMeta)obj;
		return getName().equalsIgnoreCase(stepMeta.getName());
	}
	
	public int hashCode()
	{
		return stepname.hashCode();
	}
	
	public int compareTo(StepMeta o)
	{
		return toString().compareTo(o.toString());
	}
	
	public boolean hasChanged()
	{
		BaseStepMeta bsi = (BaseStepMeta)this.getStepMetaInterface();
		return bsi!=null?bsi.hasChanged():false;
	}
	
	public void setChanged(boolean ch)
	{
		BaseStepMeta bsi = (BaseStepMeta)this.getStepMetaInterface();
		if (bsi!=null) bsi.setChanged(ch);
	}

	public void setChanged()
	{
		BaseStepMeta bsi = (BaseStepMeta)this.getStepMetaInterface();
		if (bsi!=null) bsi.setChanged();
	}

	public boolean chosesTargetSteps()
	{
	    if (getStepMetaInterface()!=null)
	    {
	        return getStepMetaInterface().getTargetSteps()!=null;
	    }
	    return false;
	}
	
	
	public Object clone()
	{
        StepMeta stepMeta = new StepMeta();
        stepMeta.replaceMeta(this);
        stepMeta.setID(-1L);
        return stepMeta;
	}
    

    public void replaceMeta(StepMeta stepMeta)
    {
        this.stepid = stepMeta.stepid;   // --> StepPlugin.id
        this.stepname = stepMeta.stepname;
        if (stepMeta.stepMetaInterface!=null)
        {
            this.stepMetaInterface = (StepMetaInterface) stepMeta.stepMetaInterface.clone();
        }
        else
        {
            this.stepMetaInterface = null;
        }
        this.selected = stepMeta.selected;
        this.distributes = stepMeta.distributes;
        this.copies = stepMeta.copies;
        if (stepMeta.location!=null)
        {
            this.location = new Point(stepMeta.location.x, stepMeta.location.y);
        }
        else
        {
            this.location = null;
        }
        this.drawstep = stepMeta.drawstep;
        this.description = stepMeta.description;
        this.terminator = stepMeta.terminator;
        
        if (stepMeta.stepPartitioningMeta!=null)
        {
            this.stepPartitioningMeta = (StepPartitioningMeta) stepMeta.stepPartitioningMeta.clone();
        }
        else
        {
            this.stepPartitioningMeta = null;
        }
        if (stepMeta.clusterSchema!=null)
        {
            this.clusterSchema = (ClusterSchema) stepMeta.clusterSchema.clone();
        }
        else
        {
            this.clusterSchema = null;
        }
        this.clusterSchemaName = stepMeta.clusterSchemaName; // temporary to resolve later.

        // Also replace the remote steps with cloned versions...
        //
        this.remoteInputSteps = new ArrayList<RemoteStep>();
        for (RemoteStep remoteStep : stepMeta.remoteInputSteps) this.remoteInputSteps.add((RemoteStep)remoteStep.clone());
        this.remoteOutputSteps = new ArrayList<RemoteStep>();
        for (RemoteStep remoteStep : stepMeta.remoteOutputSteps) this.remoteOutputSteps.add((RemoteStep)remoteStep.clone());
        
        // The error handling needs to be done too...
        //
        if (stepMeta.stepErrorMeta!=null) {
        	this.stepErrorMeta = stepMeta.stepErrorMeta.clone();
        }
        
        // this.setShared(stepMeta.isShared());
        this.id = stepMeta.getID();
        this.setChanged(true);
    }
	
	public StepMetaInterface getStepMetaInterface()
	{
		return stepMetaInterface;
	}
	
	public String getStepID()
	{
		return stepid;
	}
	
	public String getName()
	{
		return stepname;
	}

	public void setName(String sname)
	{
		stepname=sname;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description=description;
	}
	
	public void setSelected(boolean sel)
	{
		selected=sel;
	}

	public void flipSelected()
	{
		selected=!selected;
	}

	public boolean isSelected()
	{
		return selected;
	}
	
	public void setTerminator()
	{
		setTerminator(true);
	}
	
	public void setTerminator(boolean t)
	{
		terminator = t;
	}
	
	public boolean hasTerminator()
	{
		return terminator;
	}
	    
    public StepMeta(long id_step)
    {
        this((String)null, (String)null, (StepMetaInterface)null);
        setID(id_step);
    }

    /**
     * Create a new step by loading the metadata from the specified repository.  
     * @param rep
     * @param id_step
     * @param databases
     * @param counters
     * @param partitionSchemas
     * @throws KettleException
     */
	public StepMeta(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters, List<PartitionSchema> partitionSchemas) throws KettleException
	{
        this();
        StepLoader steploader = StepLoader.getInstance();

		try
		{
			RowMetaAndData r = rep.getStep(id_step);
			if (r!=null)
			{
				setID(id_step);
				
				stepname = r.getString("NAME", null); //$NON-NLS-1$
				//System.out.println("stepname = "+stepname);
				description = r.getString("DESCRIPTION", null); //$NON-NLS-1$
				//System.out.println("description = "+description);
				
				long id_step_type = r.getInteger("ID_STEP_TYPE", -1L); //$NON-NLS-1$
				//System.out.println("id_step_type = "+id_step_type);
				RowMetaAndData steptyperow = rep.getStepType(id_step_type);
				
				stepid     = steptyperow.getString("CODE", null); //$NON-NLS-1$
				distributes = r.getBoolean("DISTRIBUTE", true); //$NON-NLS-1$
				copies     = (int)r.getInteger("COPIES", 1); //$NON-NLS-1$
				int x = (int)r.getInteger("GUI_LOCATION_X", 0); //$NON-NLS-1$
				int y = (int)r.getInteger("GUI_LOCATION_Y", 0); //$NON-NLS-1$
				location = new Point(x,y);
				drawstep = r.getBoolean("GUI_DRAW", false); //$NON-NLS-1$
				
				// Generate the appropriate class...
				StepPlugin sp = steploader.findStepPluginWithID(stepid);
                if (sp!=null)
                {
                    stepMetaInterface = BaseStep.getStepInfo(sp, steploader);
                }
                else
                {
                    throw new KettleStepLoaderException(Messages.getString("StepMeta.Exception.UnableToLoadClass",stepid+Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }

				stepMetaInterface = BaseStep.getStepInfo(sp, steploader);
				if (stepMetaInterface!=null)
				{
					// Read the step info from the repository!
					stepMetaInterface.readRep(rep, getID(), databases, counters);
				}
                
                // Get the partitioning as well...
                stepPartitioningMeta = new StepPartitioningMeta(rep, getID());
                
                // Get the cluster schema name
                clusterSchemaName = rep.getStepAttributeString(id_step, "cluster_schema");
			}
			else
			{
				throw new KettleException(Messages.getString("StepMeta.Exception.StepInfoCouldNotBeFound",String.valueOf(id_step))); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("StepMeta.Exception.StepCouldNotBeLoaded",String.valueOf(getID())), dbe); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	public void saveRep(Repository rep, long id_transformation)
		throws KettleException
	{
        LogWriter log = LogWriter.getInstance();
        
		try
		{
			log.logDebug(toString(), Messages.getString("StepMeta.Log.SaveNewStep")); //$NON-NLS-1$
			// Insert new Step in repository
			setID(rep.insertStep(	id_transformation,
									getName(), 
									getDescription(),
									getStepID(),
									distributes,
									copies,
									getLocation()==null?-1:getLocation().x,
									getLocation()==null?-1:getLocation().y,
									isDrawn()
								)
					);
            
            // Save partitioning selection for the step
            stepPartitioningMeta.saveRep(rep, id_transformation, getID());
	
			// The id_step is known, as well as the id_transformation
			// This means we can now save the attributes of the step...
			log.logDebug(toString(), Messages.getString("StepMeta.Log.SaveStepDetails")); //$NON-NLS-1$
			stepMetaInterface.saveRep(rep, id_transformation, getID());
            
            // Save the clustering schema that was chosen.
            rep.saveStepAttribute(id_transformation, getID(), "cluster_schema", clusterSchema==null?"":clusterSchema.getName());
		}
		catch(KettleException e)
		{
			throw new KettleException(Messages.getString("StepMeta.Exception.UnableToSaveStepInfo",String.valueOf(id_transformation)), e); //$NON-NLS-1$
		}
	}

	public void setLocation(int x, int y)
	{
		int nx = (x>=0?x:0);
		int ny = (y>=0?y:0);
		
		Point loc = new Point(nx,ny);
		if (!loc.equals(location)) setChanged();
		location=loc;
	}
	
	public void setLocation(Point loc)
	{
		if (loc!=null && !loc.equals(location)) setChanged();
		location = loc;
	}
	
	public Point getLocation()
	{
		return location;
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		stepMetaInterface.check(remarks, transMeta, this, prev, input, output, info);
	}
	
	public String toString()
	{
		if (getName()==null) return getClass().getName();
		return getName();
	}
    

    /**
     * @return true is the step is partitioned
     */
    public boolean isPartitioned()
    {
        return stepPartitioningMeta.isPartitioned();
    }
    
    /**
     * @return true is the step is partitioned
     */
    public boolean isTargetPartitioned()
    {
        return targetStepPartitioningMeta.isPartitioned();
    }
    
    /**
     * @return the stepPartitioningMeta
     */
    public StepPartitioningMeta getStepPartitioningMeta()
    {
        return stepPartitioningMeta;
    }

    /**
     * @param stepPartitioningMeta the stepPartitioningMeta to set
     */
    public void setStepPartitioningMeta(StepPartitioningMeta stepPartitioningMeta)
    {
        this.stepPartitioningMeta = stepPartitioningMeta;
    }

    /**
     * @return the clusterSchema
     */
    public ClusterSchema getClusterSchema()
    {
        return clusterSchema;
    }

    /**
     * @param clusterSchema the clusterSchema to set
     */
    public void setClusterSchema(ClusterSchema clusterSchema)
    {
        this.clusterSchema = clusterSchema;
    }

    /**
     * @return the distributes
     */
    public boolean isDistributes()
    {
        return distributes;
    }

    /**
     * @param distributes the distributes to set
     */
    public void setDistributes(boolean distributes)
    {
    	if (this.distributes != distributes){
    		this.distributes = distributes;
    		setChanged();
    	}

    }

    /**
     * @return the StepErrorMeta error handling metadata for this step  
     */
    public StepErrorMeta getStepErrorMeta()
    {
        return stepErrorMeta;
    }

    /**
     * @param stepErrorMeta the error handling metadata for this step
     */
    public void setStepErrorMeta(StepErrorMeta stepErrorMeta)
    {
        this.stepErrorMeta = stepErrorMeta;
    }
    
    /**
     * Find a step with the ID in a given ArrayList of steps
     *
     * @param steps The List of steps to search
     * @param id The ID of the step
     * @return The step if it was found, null if nothing was found
     */
    public static final StepMeta findStep(List<StepMeta> steps, long id)
    {
        if (steps == null) return null;

        for (StepMeta stepMeta : steps)
        {
            if (stepMeta.getID() == id) { 
            	return stepMeta;
            }
        }
        return null;
    }

    /**
     * Find a step with its name in a given ArrayList of steps
     *
     * @param steps The List of steps to search
     * @param stepname The name of the step
     * @return The step if it was found, null if nothing was found
     */
    public static final StepMeta findStep(List<StepMeta> steps, String stepname)
    {
        if (steps == null) return null;

        for (StepMeta stepMeta : steps)
        {
            if (stepMeta.getName().equalsIgnoreCase(stepname)) return stepMeta;
        }
        return null;
    }
    
    public boolean supportsErrorHandling()
    {
        return stepMetaInterface.supportsErrorHandling();
    }
    /**
     * @return if error handling is supported for this step, if error handling is defined and a target step is set
     */
    public boolean isDoingErrorHandling()
    {
        return stepMetaInterface.supportsErrorHandling() && 
            stepErrorMeta!=null && 
            stepErrorMeta.getTargetStep()!=null &&
            stepErrorMeta.isEnabled()
            ;
    }
    
    public boolean isSendingErrorRowsToStep(StepMeta targetStep)
    {
        return (isDoingErrorHandling() && stepErrorMeta.getTargetStep().equals(targetStep));
    }

  /**
   * Support for CheckResultSourceInterface
   */
    public String getTypeId() {
      return this.getStepID();
    }
    
    public boolean isMapping() {
    	return STRING_ID_MAPPING.equals(stepid);
    }

    public boolean isMappingInput() {
    	return STRING_ID_MAPPING_INPUT.equals(stepid);
    }
    public boolean isMappingOutput() {
    	return STRING_ID_MAPPING_OUTPUT.equals(stepid);
    }

    /**
     * Get a list of all the resource dependencies that the step is depending on.
     * 
     * @return a list of all the resource dependencies that the step is depending on
     */
    public List<ResourceReference> getResourceDependencies(TransMeta transMeta) {
    	return stepMetaInterface.getResourceDependencies(transMeta, this);
    }

	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {

		// The step calls out to the StepMetaInterface...
		// These can in turn add anything to the map in terms of resources, etc.
		// Even reference files, etc.  For now it's just XML probably...
		//
		return stepMetaInterface.exportResources(space, definitions, resourceNamingInterface, repository);
	}

	/**
	 * @return the remoteInputSteps
	 */
	public List<RemoteStep> getRemoteInputSteps() {
		return remoteInputSteps;
	}

	/**
	 * @param remoteInputSteps the remoteInputSteps to set
	 */
	public void setRemoteInputSteps(List<RemoteStep> remoteInputSteps) {
		this.remoteInputSteps = remoteInputSteps;
	}

	/**
	 * @return the remoteOutputSteps
	 */
	public List<RemoteStep> getRemoteOutputSteps() {
		return remoteOutputSteps;
	}

	/**
	 * @param remoteOutputSteps the remoteOutputSteps to set
	 */
	public void setRemoteOutputSteps(List<RemoteStep> remoteOutputSteps) {
		this.remoteOutputSteps = remoteOutputSteps;
	}

	/**
	 * @return the targetStepPartitioningMeta
	 */
	public StepPartitioningMeta getTargetStepPartitioningMeta() {
		return targetStepPartitioningMeta;
	}

	/**
	 * @param targetStepPartitioningMeta the targetStepPartitioningMeta to set
	 */
	public void setTargetStepPartitioningMeta(StepPartitioningMeta targetStepPartitioningMeta) {
		this.targetStepPartitioningMeta = targetStepPartitioningMeta;
	}

	public boolean isRepartitioning() {
		if (!isPartitioned() && isTargetPartitioned()) return true;
		if (isPartitioned() && isTargetPartitioned() && !stepPartitioningMeta.equals(targetStepPartitioningMeta)) return true;
		return false;
	}

  public String getHolderType() {
    return "STEP"; //$NON-NLS-1$
  }
  
  public boolean isClustered() {
	  return clusterSchema!=null;
  }
}
