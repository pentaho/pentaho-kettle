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

 
package be.ibridge.kettle.core;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;


/**
 * Describes a note displayed on a Transformation, Job, Schema or Report
 * 
 * @author Matt 
 * @since 28-11-2003
 *
 */

public class NotePadMeta implements Cloneable, XMLInterface
{
	private String note;
	private Point location;
	public int width, height;
	private boolean selected;
	
	private boolean changed;
	
	private long id;

	public NotePadMeta()
	{
		note = null;
		location = null;
		width = -1;
		height = -1;
		selected = false;
	}
	
	public NotePadMeta(String n, int xl, int yl, int w, int h)
	{
		note  = n;
		location = new Point(xl, yl);
		width = w;
		height= h;
		selected = false;
	}

	public NotePadMeta(Node notepadnode)
		throws KettleXMLException
	{
		try
		{
			note           = XMLHandler.getTagValue(notepadnode, "note");
			String sxloc   = XMLHandler.getTagValue(notepadnode, "xloc");
			String syloc   = XMLHandler.getTagValue(notepadnode, "yloc");
			String swidth  = XMLHandler.getTagValue(notepadnode, "width");
			String sheight = XMLHandler.getTagValue(notepadnode, "heigth");
			int x   = Const.toInt(sxloc, 0);
			int y   = Const.toInt(syloc, 0);
			location = new Point(x,y);
			width  = Const.toInt(swidth, 0);
			height = Const.toInt(sheight, 0);
			selected = false;
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read Notepad info from XML", e);
		}
	}

	public NotePadMeta(LogWriter log, Repository rep, long id_note)
		throws KettleException
	{
		readRep(log, rep, id_note);
	}
	
	// Load from repository
	private void readRep(LogWriter log, Repository rep, long id_note)
		throws KettleException
	{
		try
		{
			setID(id_note);
	
			Row r = rep.getNote(id_note);
			if (r!=null)
			{
				note     =      r.getString("VALUE_STR", "");
				int x    = (int)r.getInteger("GUI_LOCATION_X", 0L);
				int y    = (int)r.getInteger("GUI_LOCATION_Y", 0L);
				location = new Point(x, y);
				width    = (int)r.getInteger("GUI_LOCATION_WIDTH", 0L);
				height   = (int)r.getInteger("GUI_LOCATION_HEIGHT", 0L);
				selected = false;
			}
			else
			{
			    setID(-1L);
			    throw new KettleException("I couldn't find Notepad with id_note="+id_note+" in the repository.");
			}
		}
		catch(KettleDatabaseException dbe)
		{
			setID(-1L);
			throw new KettleException("Unable to load Notepad from repository (id_note="+id_note+")", dbe);
		}
	}
	

	
	public void saveRep(Repository rep, long id_transformation)
		throws KettleException
	{
		try
		{
			int x = location==null?-1:location.x;
			int y = location==null?-1:location.y;
				
			// Insert new Note in repository
			setID( rep.insertNote(note, x, y, width, height) );
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save notepad in repository (id_transformation="+id_transformation+")", dbe);
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

	public void setLocation(int x, int y)
	{
		if (x!=location.x|| y!=location.y) setChanged();
		location.x = x;
		location.y = y;
	}

	public void setLocation(Point p)
	{
		setLocation(p.x, p.y);
	}
	
	public Point getLocation()
	{
		return location;
	}
	
	/**
     * @return Returns the note.
     */
    public String getNote()
    {
        return note;
    }
    
    /**
     * @param note The note to set.
     */
    public void setNote(String note)
    {
        this.note = note;
    }
    
    /**
     * @return Returns the selected.
     */
    public boolean isSelected()
    {
        return selected;
    }
    
    /**
     * @param selected The selected to set.
     */
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    /**
     * Change a selected state to not-selected and vice-versa. 
     */
    public void flipSelected()
    {
        this.selected = !this.selected;
    }
	
	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
	}
	
	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed=ch;
	}

	public boolean hasChanged()
	{
		return changed;
	}

	public String toString()
	{
		return note;
	}
	
	public String getXML()
	{
		String retval="";
		
		retval+="    <notepad>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("note",   note);
		retval+="      "+XMLHandler.addTagValue("xloc",   location.x);
		retval+="      "+XMLHandler.addTagValue("yloc",   location.y);
		retval+="      "+XMLHandler.addTagValue("width",  width);
		retval+="      "+XMLHandler.addTagValue("heigth", height);
		retval+="      </notepad>"+Const.CR;
		
		return retval;
	}
}
