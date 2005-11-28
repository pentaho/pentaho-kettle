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

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Point;
import be.ibridge.kettle.core.Row;
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
public class StepMeta implements Cloneable, Comparable
{
	private String        stepid;   // --> StepPlugin.id
	private String        stepname;
	private StepMetaInterface stepMetaInterface;
	private boolean       selected;
	public  boolean       distributes;
	private int           copies;
	private Point         location;
	private boolean       drawstep;
	private String        description;
	private boolean       terminator;
	
	private LogWriter log;
		
	private long id;

	public StepMeta(LogWriter l, String stepid, String name, StepMetaInterface info)
	{
		log=l;
		this.stepid = stepid;
		stepname    = name;
		stepMetaInterface    = info;
		selected    = false;
		distributes  = true;
		copies      = 1;
		location    = new Point(0,0);
		drawstep    = false;
		description = null;
	}
		
	public StepMeta(LogWriter log)
	{
		this(log, (String)null, (String)null, (StepMetaInterface)null);
	}

	public String getXML()
	{
		String retval="";
		
		retval+="  <step>"+Const.CR;
		retval+="    "+XMLHandler.addTagValue("name",        getName());
		retval+="    "+XMLHandler.addTagValue("type",        getStepID());
		retval+="    "+XMLHandler.addTagValue("description", description);
		retval+="    "+XMLHandler.addTagValue("distribute",  distributes);
		retval+="    "+XMLHandler.addTagValue("copies",      copies);

		retval+=stepMetaInterface.getXML();
			
		retval+="    <GUI>"+Const.CR;
		retval+="      <xloc>"+location.x+"</xloc>"+Const.CR;
		retval+="      <yloc>"+location.y+"</yloc>"+Const.CR;
		retval+="      <draw>"+(drawstep?"Y":"N")+"</draw>"+Const.CR;
		retval+="      </GUI>"+Const.CR;
		retval+="    </step>"+Const.CR+Const.CR;
		
		return retval;
	}

	/**
	 * Read the step data from XML
	 * 
	 * @param stepnode The XML step node.
	 * @param databases A list of databases
	 * @param counters A hashtable with all defined counters.
	 * 
	 */
	public StepMeta(LogWriter log, Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		this.log = log;
		StepLoader steploader = StepLoader.getInstance();

		try
		{
			stepname = XMLHandler.getTagValue(stepnode, "name");
			stepid   = XMLHandler.getTagValue(stepnode, "type");
	
			log.logDebug("StepMeta()", "looking for the right step node ("+stepname+")");
	
			// Create a new StepMetaInterface object...
			StepPlugin sp = steploader.findStepPluginWithID(stepid);
            if (sp!=null)
            {
                stepMetaInterface = BaseStep.getStepInfo(sp, steploader);
            }
            else
            {
                throw new KettleStepLoaderException("Unable to load class for step/plugin with id ["+stepid+"]."+Const.CR+"Check if the plugin is available in the plugins subdirectory of the Kettle distribution.");
            }
			
			// Load the specifics from XML...
			if (stepMetaInterface!=null)
			{
				stepMetaInterface.loadXML(stepnode, databases, counters);
			}
			log.logDebug("StepMeta()", "specifics loaded for "+stepname);
			
			/* Handle info general to all step types...*/
			description    = XMLHandler.getTagValue(stepnode, "description");
			copies         = Const.toInt(XMLHandler.getTagValue(stepnode, "copies"), 1);
			String sdistri = XMLHandler.getTagValue(stepnode, "distribute");
			distributes     = "Y".equalsIgnoreCase(sdistri);
			if (sdistri==null) distributes=true; // default=distribute
	
			// Handle GUI information: location & drawstep?
			String xloc, yloc;
			int x,y;
			xloc=XMLHandler.getTagValue(stepnode, "GUI", "xloc");
			yloc=XMLHandler.getTagValue(stepnode, "GUI", "yloc");
			try{ x=Integer.parseInt(xloc); } catch(Exception e) { x=0; }
			try{ y=Integer.parseInt(yloc); } catch(Exception e) { y=0; }
			location=new Point(x,y);
			
			drawstep = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "GUI", "draw"));
	
			log.logDebug("StepMeta()", "end of readXML()");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML step node", e);
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
		try
		{
 			StepMeta retval   = (StepMeta)super.clone();
			boolean changed = hasChanged();
            
			retval.setLocation(getLocation().x, getLocation().y);
			if (stepMetaInterface!=null) retval.stepMetaInterface = (StepMetaInterface)stepMetaInterface.clone();
			else retval.stepMetaInterface=null;

            retval.setChanged( changed );
            setChanged(changed );

			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
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
	
	public StepMeta(LogWriter log, long id_step)
	{
		this(log, (String)null, (String)null, (StepMetaInterface)null);
		setID(id_step);
	}


	// Load from repository
	public StepMeta(LogWriter log, Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		this.log = log;
		
		try
		{
			StepLoader steploader = StepLoader.getInstance();

			Row r = rep.getStep(id_step);
			if (r!=null)
			{
				setID(id_step);
				
				stepname = r.searchValue("NAME").getString();
				//System.out.println("stepname = "+stepname);
				description = r.searchValue("DESCRIPTION").getString();
				//System.out.println("description = "+description);
				
				long id_step_type = r.searchValue("ID_STEP_TYPE").getInteger();
				//System.out.println("id_step_type = "+id_step_type);
				Row steptyperow = rep.getStepType(id_step_type);
				
				stepid     = steptyperow.searchValue("CODE").getString();
				distributes = r.searchValue("DISTRIBUTE").getBoolean();
				copies     = (int)r.searchValue("COPIES").getInteger();
				int x = (int)r.searchValue("GUI_LOCATION_X").getInteger();
				int y = (int)r.searchValue("GUI_LOCATION_Y").getInteger();
				location = new Point(x,y);
				drawstep = r.searchValue("GUI_DRAW").getBoolean();
				
				// Generate the appropriate class...
				StepPlugin sp = steploader.findStepPluginWithID(stepid);
                if (sp!=null)
                {
                    stepMetaInterface = BaseStep.getStepInfo(sp, steploader);
                }
                else
                {
                    throw new KettleStepLoaderException("Unable to load class for step/plugin with id ["+stepid+"]."+Const.CR+"Check if the plugin is available in the plugins subdirectory of the Kettle distribution.");
                }

				stepMetaInterface = BaseStep.getStepInfo(sp, steploader);
				if (stepMetaInterface!=null)
				{
					// Read the step info from the repository!
					stepMetaInterface.readRep(rep, getID(), databases, counters);
				}
			}
			else
			{
				throw new KettleException("Step information for id_step="+id_step+" could not be found!");
			}
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Step with id "+getID()+" could not be loaded from the repository!", dbe);
		}
	}


	public void saveRep(Repository rep, long id_transformation)
		throws KettleException
	{
		try
		{
			log.logDebug(toString(), "STEP SAVE insert general...");
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
	
			// The id_step is known, as well as the id_transformation
			// This means we can now save the attributes of the step...
			log.logDebug(toString(), "STEP SAVE details...");
			stepMetaInterface.saveRep(rep, id_transformation, getID());
		}
		catch(KettleException e)
		{
			throw new KettleException("Unable to save step info to the repository for id_transformation="+id_transformation, e);
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

}
