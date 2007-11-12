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

 

package org.pentaho.di.core.undo;

import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * This class store undo and redo information...

	Possible changes to a transformation:<p>
	* step<p>
	* hop<p>
	* note<p>
	* connection<p>
	
	Build an Undo/Redo class containing:<p>
	Type of change<p>
	Description of action<p>
	Link to previous infomation<p>
 * 
 * @author Matt
 * @since 19-12-2003
 *
 */
public class TransAction 
{
	public static final int  TYPE_ACTION_NONE                   = 0;
	public static final int  TYPE_ACTION_CHANGE_STEP            = 1;
	public static final int  TYPE_ACTION_CHANGE_CONNECTION      = 2;
	public static final int  TYPE_ACTION_CHANGE_HOP             = 3;
	public static final int  TYPE_ACTION_CHANGE_NOTE            = 4;
	public static final int  TYPE_ACTION_NEW_STEP               = 5;
	public static final int  TYPE_ACTION_NEW_CONNECTION         = 6;
	public static final int  TYPE_ACTION_NEW_HOP                = 7;
	public static final int  TYPE_ACTION_NEW_NOTE               = 8;
	public static final int  TYPE_ACTION_DELETE_STEP            = 9;
	public static final int  TYPE_ACTION_DELETE_CONNECTION      = 10;
	public static final int  TYPE_ACTION_DELETE_HOP             = 11;
	public static final int  TYPE_ACTION_DELETE_NOTE            = 12;
	public static final int  TYPE_ACTION_POSITION_STEP          = 13;
	public static final int  TYPE_ACTION_POSITION_NOTE          = 14;

	public static final int  TYPE_ACTION_CHANGE_JOB_ENTRY       = 15;
	public static final int  TYPE_ACTION_CHANGE_JOB_HOP         = 16;
	public static final int  TYPE_ACTION_NEW_JOB_ENTRY          = 17;
	public static final int  TYPE_ACTION_NEW_JOB_HOP            = 18;
	public static final int  TYPE_ACTION_DELETE_JOB_ENTRY       = 19;
	public static final int  TYPE_ACTION_DELETE_JOB_HOP         = 20;
	public static final int  TYPE_ACTION_POSITION_JOB_ENTRY     = 21;

	public static final int  TYPE_ACTION_CHANGE_TABLEITEM       = 22;
	public static final int  TYPE_ACTION_NEW_TABLEITEM          = 23;
	public static final int  TYPE_ACTION_DELETE_TABLEITEM       = 24;
	public static final int  TYPE_ACTION_POSITION_TABLEITEM     = 25;

	public static final int  TYPE_ACTION_CHANGE_TABLE           = 26;
	public static final int  TYPE_ACTION_CHANGE_RELATIONSHIP    = 27;
	public static final int  TYPE_ACTION_NEW_TABLE              = 28;
	public static final int  TYPE_ACTION_NEW_RELATIONSHIP       = 29;
	public static final int  TYPE_ACTION_DELETE_TABLE           = 30;
	public static final int  TYPE_ACTION_DELETE_RELATIONSHIP    = 31;
	public static final int  TYPE_ACTION_POSITION_TABLE         = 32;
    
    public static final int  TYPE_ACTION_NEW_SLAVE              = 33;
    public static final int  TYPE_ACTION_CHANGE_SLAVE           = 34;
    public static final int  TYPE_ACTION_DELETE_SLAVE           = 35;
    
    public static final int  TYPE_ACTION_NEW_CLUSTER            = 36;
    public static final int  TYPE_ACTION_CHANGE_CLUSTER         = 37;
    public static final int  TYPE_ACTION_DELETE_CLUSTER         = 38;
    
    public static final int  TYPE_ACTION_NEW_PARTITION          = 39;
    public static final int  TYPE_ACTION_CHANGE_PARTITION       = 40;
    public static final int  TYPE_ACTION_DELETE_PARTITION       = 41;

	public static final String desc_action[] = new String[]
		{
			"",
      Messages.getString("TransAction.label.ChangeStep"), Messages.getString("TransAction.label.ChangeConnection"), Messages.getString("TransAction.label.ChangeHop"), Messages.getString("TransAction.label.ChangeNote"),
      Messages.getString("TransAction.label.NewStep"), Messages.getString("TransAction.label.NewConnection"), Messages.getString("TransAction.label.NewHop"), Messages.getString("TransAction.label.NewNote"),
      Messages.getString("TransAction.label.DeleteStep"), Messages.getString("TransAction.label.DeleteConnection"), Messages.getString("TransAction.label.DeleteHop"), Messages.getString("TransAction.label.DeleteNote"),
      Messages.getString("TransAction.label.PositionStep"), Messages.getString("TransAction.label.PositionNote"),
      Messages.getString("TransAction.label.ChangeJobEntry"), Messages.getString("TransAction.label.ChangeJobHop"),
      Messages.getString("TransAction.label.NewJobEntry"), Messages.getString("TransAction.label.NewJobHop"),
      Messages.getString("TransAction.label.DeleteJobEntry"), Messages.getString("TransAction.label.DeleteJobHop"),
      Messages.getString("TransAction.label.PositionJobEntry"),
      Messages.getString("TransAction.label.ChangeTableRow"),
      Messages.getString("TransAction.label.NewTableRow"),
      Messages.getString("TransAction.label.DeleteTableRow"),
      Messages.getString("TransAction.label.PositionTableRow"),
      Messages.getString("TransAction.label.ChangeTable"), Messages.getString("TransAction.label.ChangeRelationship"),
      Messages.getString("TransAction.label.NewTable"), Messages.getString("TransAction.label.NewRelationship"),
			Messages.getString("TransAction.label.DeleteTable"), Messages.getString("TransAction.label.DeleteRelationship"),
      Messages.getString("TransAction.label.PositionTable")
		};
		
	private int type;
	private Object previous[];
	private Point  previous_location[];
	private int    previous_index[];
	
	private Object current[];
	private Point  current_location[];
	private int    current_index[];
	
	private boolean nextAlso;
	
	public TransAction()
	{
		type=TYPE_ACTION_NONE;
	}

	public void setDelete(Object prev [], int idx[])
	{
		current=prev;
		current_index=idx;

		if ( prev[0] instanceof StepMeta)       type=TYPE_ACTION_DELETE_STEP;
		if ( prev[0] instanceof DatabaseMeta)   type=TYPE_ACTION_DELETE_CONNECTION;
		if ( prev[0] instanceof TransHopMeta)   type=TYPE_ACTION_DELETE_HOP;
		if ( prev[0] instanceof NotePadMeta)    type=TYPE_ACTION_DELETE_NOTE;
		if ( prev[0] instanceof JobEntryCopy)   type=TYPE_ACTION_DELETE_JOB_ENTRY;
		if ( prev[0] instanceof JobHopMeta)     type=TYPE_ACTION_DELETE_JOB_HOP;
		if ( prev[0] instanceof String[])       type=TYPE_ACTION_DELETE_TABLEITEM;
	}
	
	public void setChanged(Object prev[], Object curr[], int idx[])
	{
		previous=prev;
		current=curr;
		current_index=idx;
		previous_index=idx;

		if ( prev[0] instanceof StepMeta)     type=TYPE_ACTION_CHANGE_STEP;
		if ( prev[0] instanceof DatabaseMeta) type=TYPE_ACTION_CHANGE_CONNECTION;
		if ( prev[0] instanceof TransHopMeta) type=TYPE_ACTION_CHANGE_HOP;
		if ( prev[0] instanceof NotePadMeta)  type=TYPE_ACTION_CHANGE_NOTE;
		if ( prev[0] instanceof JobEntryCopy) type=TYPE_ACTION_CHANGE_JOB_ENTRY;
		if ( prev[0] instanceof JobHopMeta)   type=TYPE_ACTION_CHANGE_JOB_HOP;
		if ( prev[0] instanceof String[])     type=TYPE_ACTION_CHANGE_TABLEITEM;
	}

	public void setNew(Object prev[], int position[])
	{
	    if (prev.length==0) return;
	    
		current=prev;
		current_index=position;
		previous = null;

		if ( prev[0] instanceof StepMeta)     type=TYPE_ACTION_NEW_STEP;
		if ( prev[0] instanceof DatabaseMeta) type=TYPE_ACTION_NEW_CONNECTION;
		if ( prev[0] instanceof TransHopMeta) type=TYPE_ACTION_NEW_HOP;
		if ( prev[0] instanceof NotePadMeta)  type=TYPE_ACTION_NEW_NOTE;
		if ( prev[0] instanceof JobEntryCopy) type=TYPE_ACTION_NEW_JOB_ENTRY;
		if ( prev[0] instanceof JobHopMeta)   type=TYPE_ACTION_NEW_JOB_HOP;
		if ( prev[0] instanceof String[])     type=TYPE_ACTION_NEW_TABLEITEM;
	}
	
	public void setPosition(Object obj[], int idx[], Point prev[], Point curr[])
	{
		if (prev.length!=curr.length) return;
		
		previous_location=new Point[prev.length];
		current_location=new Point[curr.length];
		current = obj;
		current_index=idx;

		for (int i = 0; i < prev.length; i++) 
		{
			previous_location[i] = new Point(prev[i].x, prev[i].y);
			current_location[i] = new Point(curr[i].x, curr[i].y);	
		}
		
		Object fobj = obj[0];
		if ( fobj instanceof StepMeta)     type=TYPE_ACTION_POSITION_STEP; 
		if ( fobj instanceof NotePadMeta)  type=TYPE_ACTION_POSITION_NOTE;
		if ( fobj instanceof JobEntryCopy) type=TYPE_ACTION_POSITION_JOB_ENTRY;
	}
	
	public void setItemMove(int prev[], int curr[])
	{
		previous_location = null;
		current_location  = null;
		current           = null;
		current_index     = curr;
		previous          = null;
		previous_index    = prev;

		type=TYPE_ACTION_POSITION_TABLEITEM;
	}

	

	public int getType()
	{
		return type;
	}
	
	public Object[] getPrevious()
	{
		return previous;
	}
	
	public Object[] getCurrent()
	{
		return current;
	}
	
	public Point[] getPreviousLocation()
	{
		return previous_location;
	}

	public Point[] getCurrentLocation()
	{
		return current_location;
	}

	public int[] getPreviousIndex()
	{
		return previous_index;
	}

	public int[] getCurrentIndex()
	{
		return current_index;
	}
	
	/**
	 * Indicate that the next operations needs to be undone too.
	 * @param nextAlso The nextAlso to set.
	 */
	public void setNextAlso(boolean nextAlso)
	{
		this.nextAlso = nextAlso;
	}
	
	/**
	 * Get the status of the nextAlso flag.
	 * @return true if the next operation needs to be done too.
	 */
	public boolean getNextAlso()
	{
		return nextAlso;
	}
	
	public String toString()
	{
		String retval = "";
		if (type<0 || type>=desc_action.length) return TransAction.class.getName();
		
		retval = desc_action[type];
		
		if (current!=null && current.length>1) retval+=" (x"+current.length+")";
		
		return retval;
	}
}
