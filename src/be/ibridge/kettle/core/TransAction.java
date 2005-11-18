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

import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.job.JobHopMeta;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.trans.TransHopMeta;
import be.ibridge.kettle.trans.step.StepMeta;


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

	public static final String desc_action[] = new String[]
		{
			"",
			"change step", "change connection", "change hop", "change note",
			"new step", "new connection", "new hop", "new note",
			"delete step", "delete connection", "delete hop", "delete note",
			"position step", "position note",
			
			"change job-entry", "change job-hop",
			"new job-entry", "new job-hop",
			"delete job-entry", "delete job-hop",
			"position job-entry",
			
			"change table row",
			"new table row",
			"delete table row",
			"position table row",
			
			"change table", "change relationship",
			"new table", "new relationship",
			"delete table", "delete relationship",
			"position table"
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
