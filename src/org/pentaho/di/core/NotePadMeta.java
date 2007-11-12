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

package org.pentaho.di.core;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.GUISizeInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.repository.Repository;
import org.w3c.dom.Node;


/**
 * Describes a note displayed on a Transformation, Job, Schema, or Report.
 * 
 * @author Matt 
 * @since 28-11-2003
 *
 */
public class NotePadMeta implements Cloneable, XMLInterface, GUIPositionInterface, GUISizeInterface
{
	public static final String XML_TAG = "notepad";
    
    private String note;
	private Point location;
	public int width, height;
	private boolean selected;
	
	private boolean changed;
	
	private long id;

	public NotePadMeta()
	{
		note = null;
		location = new Point(-1, -1);
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
	private void readRep(LogWriter log, Repository rep, long id_note) throws KettleException
	{
		try
		{
			setID(id_note);
	
			RowMetaAndData r = rep.getNote(id_note);
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
        StringBuffer retval = new StringBuffer(100);
		
		retval.append("    <notepad>").append(Const.CR);
		retval.append("      ").append(XMLHandler.addTagValue("note",   note));
		retval.append("      ").append(XMLHandler.addTagValue("xloc",   location.x));
		retval.append("      ").append(XMLHandler.addTagValue("yloc",   location.y));
		retval.append("      ").append(XMLHandler.addTagValue("width",  width));
		retval.append("      ").append(XMLHandler.addTagValue("heigth", height));
		retval.append("    </notepad>").append(Const.CR);
		
		return retval.toString();
	}

    /**
     * @return the height
     */
    public int getHeight()
    {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height)
    {
        this.height = height;
    }

    /**
     * @return the width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width)
    {
        this.width = width;
    }
}