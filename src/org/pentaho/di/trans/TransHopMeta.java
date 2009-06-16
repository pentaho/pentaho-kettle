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

package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.step.StepMeta;
import org.w3c.dom.Node;

/*
 * Created on 19-jun-2003
 * 
 */

/**
 * Defines a link between 2 steps in a transformation
 */
public class TransHopMeta implements Cloneable, XMLInterface, Comparable<TransHopMeta>
{
	private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String XML_TAG = "hop";

	private StepMeta from_step;

	private StepMeta to_step;

	private boolean enabled;

	public boolean split = false;

	private boolean changed;

	private ObjectId id;

	public TransHopMeta(StepMeta from, StepMeta to, boolean en)
	{
		from_step = from;
		to_step = to;
		enabled = en;
	}

	public TransHopMeta(StepMeta from, StepMeta to)
	{
		from_step = from;
		to_step = to;
		enabled = true;
	}

	public TransHopMeta()
	{
		this(null, null, false);
	}

	public TransHopMeta(Node hopnode, List<StepMeta> steps) throws KettleXMLException
	{
		try
		{
			from_step = searchStep(steps, XMLHandler.getTagValue(hopnode, "from")); //$NON-NLS-1$
			to_step = searchStep(steps, XMLHandler.getTagValue(hopnode, "to")); //$NON-NLS-1$
			String en = XMLHandler.getTagValue(hopnode, "enabled"); //$NON-NLS-1$

			if (en == null)
				enabled = true;
			else
				enabled = en.equalsIgnoreCase("Y"); //$NON-NLS-1$
		} catch (Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "TransHopMeta.Exception.UnableToLoadHopInfo"), e); //$NON-NLS-1$
		}
	}

	public void setFromStep(StepMeta from)
	{
		from_step = from;
	}

	public void setToStep(StepMeta to)
	{
		to_step = to;
	}

	public StepMeta getFromStep()
	{
		return from_step;
	}

	public StepMeta getToStep()
	{
		return to_step;
	}

	private StepMeta searchStep(List<StepMeta> steps, String name)
	{
		for (StepMeta stepMeta : steps)
			if (stepMeta.getName().equalsIgnoreCase(name))
				return stepMeta;

		return null;
	}

	public Object clone()
	{
		try
		{
			Object retval = super.clone();
			return retval;
		} catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

	public boolean equals(Object obj)
	{
		TransHopMeta other = (TransHopMeta) obj;
		if (from_step == null || to_step == null)
			return false;
		return from_step.equals(other.getFromStep()) && to_step.equals(other.getToStep());
	}

	/**
	 * Compare 2 hops.
	 */
	public int compareTo(TransHopMeta obj)
	{
		return toString().compareTo(obj.toString());
	}

	public ObjectId getObjectId()
	{
		return id;
	}

	public void setObjectId(ObjectId id)
	{
		this.id = id;
	}

	public void setChanged()
	{
		setChanged(true);
	}

	public void setChanged(boolean ch)
	{
		changed = ch;
	}

	public boolean hasChanged()
	{
		return changed;
	}

	public void setEnabled()
	{
		setEnabled(true);
	}

	public void setEnabled(boolean en)
	{
		enabled = en;
		setChanged();
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void flip()
	{
		StepMeta dummy = from_step;
		from_step = to_step;
		to_step = dummy;
	}

	public String toString()
	{
		String str_fr = (from_step == null) ? "(empty)" : from_step.getName(); //$NON-NLS-1$
		String str_to = (to_step == null) ? "(empty)" : to_step.getName(); //$NON-NLS-1$
		return str_fr + " --> " + str_to + " (" + (enabled ? "enabled" : "disabled") + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public String getXML()
	{
		StringBuilder retval = new StringBuilder(200);

		if (from_step != null && to_step != null)
		{
			retval.append("  <hop> "); //$NON-NLS-1$
			retval.append(XMLHandler.addTagValue("from", from_step.getName(), false)); //$NON-NLS-1$
			retval.append(XMLHandler.addTagValue("to", to_step.getName(), false)); //$NON-NLS-1$
			retval.append(XMLHandler.addTagValue("enabled", enabled, false)); //$NON-NLS-1$
			retval.append(" </hop>"); //$NON-NLS-1$
		}

		return retval.toString();
	}
}
