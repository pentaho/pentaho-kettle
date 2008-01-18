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
 
package be.ibridge.kettle.trans.step;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.w3c.dom.Node;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.GUIPositionInterface;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SharedObjectBase;
import be.ibridge.kettle.core.SharedObjectInterface;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.StepPlugin;


/**
 * This class contains everything that is needed to define a step.
 * 
 * @since 27-mei-2003
 * @author Matt
 *
 */
public class StepMeta extends SharedObjectBase implements Cloneable, Comparable, GUIPositionInterface, SharedObjectInterface
{
	public static final String XML_TAG = "step";
    
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
    private ClusterSchema        clusterSchema;
    private String               clusterSchemaName; // temporary to resolve later.
    
    private StepErrorMeta stepErrorMeta;
    
	// private LogWriter log;
		
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
        clusterSchema = null; // non selected by default.
	}

    /**
     * @deprecated The logging is now a singlton, use the constructor without it.
     * 
     * @param log
     * @param stepid
     * @param stepname
     * @param stepMetaInterface
     */
    public StepMeta(LogWriter log, String stepid, String stepname, StepMetaInterface stepMetaInterface)
    {
        this(stepid, stepname, stepMetaInterface);
    }
        
	public StepMeta()
	{
		this((String)null, (String)null, (StepMetaInterface)null);
	}
    
    /**
     * @deprecated The logging is now a singlton, use the constructor without it.
     * @param log
     */
    public StepMeta(LogWriter log)
    {
        this();
    }

	public String getXML()
	{
		StringBuffer retval=new StringBuffer(200); //$NON-NLS-1$
		
		retval.append("  <").append(XML_TAG).append('>').append(Const.CR); //$NON-NLS-1$
		retval.append("    ").append(XMLHandler.addTagValue("name",        getName()) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("type",        getStepID()) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("description", description) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("distribute",  distributes) ); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("copies",      copies) ); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append( stepPartitioningMeta.getXML() );
		retval.append( stepMetaInterface.getXML() );
        retval.append("     ").append(XMLHandler.addTagValue("cluster_schema", clusterSchema==null?"":clusterSchema.getName()));
        
		retval.append("    <GUI>").append(Const.CR); //$NON-NLS-1$
		retval.append("      <xloc>").append(location.x).append("</xloc>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      <yloc>").append(location.y).append("</yloc>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      <draw>").append((drawstep?"Y":"N")).append("</draw>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		retval.append("      </GUI>").append(Const.CR); //$NON-NLS-1$
		retval.append("    </"+XML_TAG+">").append(Const.CR).append(Const.CR); //$NON-NLS-1$
		
		return retval.toString();
	}

    /**
     * @deprecated The logging is now a singlton, use the constructor without it.
     * 
     * Read the step data from XML
     * 
     * @param stepnode The XML step node.
     * @param databases A list of databases
     * @param counters A hashtable with all defined counters.
     * 
     */
    public StepMeta(LogWriter log, Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
    {
        this(stepnode, databases, counters);
    }
    
	/**
	 * Read the step data from XML
	 * 
	 * @param stepnode The XML step node.
	 * @param databases A list of databases
	 * @param counters A hashtable with all defined counters.
	 * 
	 */
	public StepMeta(Node stepnode, ArrayList databases, Hashtable counters) throws KettleXMLException
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
            Node partNode = XMLHandler.getSubNode(stepnode, "partitioning");
            stepPartitioningMeta = new StepPartitioningMeta(partNode);
            
            clusterSchemaName = XMLHandler.getTagValue(stepnode, "cluster_schema"); // resolve to clusterSchema later

			log.logDebug("StepMeta()", Messages.getString("StepMeta.Log.EndOfReadXML")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("StepMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
    
    /**
     * Resolves the name of the cluster loaded from XML/Repository to the correct clusterSchema object
     * @param clusterSchemas The list of clusterSchemas to reference.
     */
    public void setClusterSchemaAfterLoading(List clusterSchemas)
    {
        if (clusterSchemaName==null) return;
        for (int i=0;i<clusterSchemas.size();i++)
        {
            ClusterSchema look = (ClusterSchema) clusterSchemas.get(i);
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
	 * be.ibridge.kettle.trans.step.StepMeta.drawStep() instead of isDrawn().
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
	
	public int getCopies()
	{
        // If the step is partitioned, that's going to determine the number of copies, nothing else...
        if (isPartitioned() && getStepPartitioningMeta().getPartitionSchema()!=null)
        {
            String[] partitionIDs = getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
            if (partitionIDs!=null && partitionIDs.length>0) // these are the partitions the step can "reach"
            {
                return partitionIDs.length;
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
	
	public int compareTo(Object o)
	{
		return toString().compareTo(((StepMeta)o).toString());
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
        
        // The error handling needs to be done too...
        //
        if (stepMeta.stepErrorMeta!=null) {
        	this.stepErrorMeta = (StepErrorMeta)stepMeta.stepErrorMeta.clone();
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

	/*
	public String getStepTypeDesc()
	{
		return BaseStep.type_desc[steptype];
	}
	*/
	
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
	
    /**
     * @deprecated The logging is now a singlton, use the constructor without it.
     * @param log
     * @param id_step
     */
	public StepMeta(LogWriter log, long id_step)
	{
		this((String)null, (String)null, (StepMetaInterface)null);
		setID(id_step);
	}
    
    public StepMeta(long id_step)
    {
        this((String)null, (String)null, (StepMetaInterface)null);
        setID(id_step);
    }

    /**
     * @deprecated The logging is now a singlton, use the constructor without it.
     * 
     * @param log
     * @param rep
     * @param id_step
     * @param databases
     * @param counters
     * @param partitionSchemas
     * @throws KettleException
     */
    public StepMeta(LogWriter log, Repository rep, long id_step, ArrayList databases, Hashtable counters, List partitionSchemas) throws KettleException
    {
        this(rep, id_step, databases, counters, partitionSchemas);
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
	public StepMeta(Repository rep, long id_step, ArrayList databases, Hashtable counters, List partitionSchemas) throws KettleException
	{
        this();
        StepLoader steploader = StepLoader.getInstance();

		try
		{
			Row r = rep.getStep(id_step);
			if (r!=null)
			{
				setID(id_step);
				
				stepname = r.searchValue("NAME").getString(); //$NON-NLS-1$
				//System.out.println("stepname = "+stepname);
				description = r.searchValue("DESCRIPTION").getString(); //$NON-NLS-1$
				//System.out.println("description = "+description);
				
				long id_step_type = r.searchValue("ID_STEP_TYPE").getInteger(); //$NON-NLS-1$
				//System.out.println("id_step_type = "+id_step_type);
				Row steptyperow = rep.getStepType(id_step_type);
				
				stepid     = steptyperow.searchValue("CODE").getString(); //$NON-NLS-1$
				distributes = r.searchValue("DISTRIBUTE").getBoolean(); //$NON-NLS-1$
				copies     = (int)r.searchValue("COPIES").getInteger(); //$NON-NLS-1$
				int x = (int)r.searchValue("GUI_LOCATION_X").getInteger(); //$NON-NLS-1$
				int y = (int)r.searchValue("GUI_LOCATION_Y").getInteger(); //$NON-NLS-1$
				location = new Point(x,y);
				drawstep = r.searchValue("GUI_DRAW").getBoolean(); //$NON-NLS-1$
				
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

	public void check(ArrayList remarks, Row prev, String input[], String output[], Row info)
	{
		stepMetaInterface.check(remarks, this, prev, input, output, info);
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
        this.distributes = distributes;
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
    public static final StepMeta findStep(List steps, long id)
    {
        if (steps == null) return null;

        for (int i = 0; i < steps.size(); i++)
        {
            StepMeta stepMeta = (StepMeta) steps.get(i);
            if (stepMeta.getID() == id) return stepMeta;
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
    public static final StepMeta findStep(List steps, String stepname)
    {
        if (steps == null) return null;

        for (int i = 0; i < steps.size(); i++)
        {
            StepMeta stepMeta = (StepMeta) steps.get(i);
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

}
